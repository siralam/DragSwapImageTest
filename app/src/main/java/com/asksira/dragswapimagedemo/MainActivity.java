package com.asksira.dragswapimagedemo;

import android.Manifest;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.constraint.ConstraintLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import static android.view.DragEvent.ACTION_DRAG_ENDED;
import static android.view.DragEvent.ACTION_DRAG_ENTERED;
import static android.view.DragEvent.ACTION_DRAG_EXITED;
import static android.view.DragEvent.ACTION_DRAG_LOCATION;
import static android.view.DragEvent.ACTION_DRAG_STARTED;
import static android.view.DragEvent.ACTION_DROP;

public class MainActivity extends AppCompatActivity {

    ConstraintLayout clContainer;
    ImageView ivGrid1, ivGrid2, ivGrid3, ivGrid4;
    Button button;

    //Drag and Drop
    View dragOriginView;
    Drawable whiteDrawable;
    Drawable tempDrawableStorage;
    private final Object lock = new Object();
    boolean isDragging = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        clContainer = findViewById(R.id.container);
        ivGrid1 = findViewById(R.id.grid_1);
        ivGrid2 = findViewById(R.id.grid_2);
        ivGrid3 = findViewById(R.id.grid_3);
        ivGrid4 = findViewById(R.id.grid_4);
        button = findViewById(R.id.button);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (PermissionUtils.isStorageGranted(MainActivity.this)) {
                    String filePath = bitmapToFile(convertViewToBitmap(clContainer));
                    notifyGallery(filePath);
                } else {
                    PermissionUtils.checkPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            1234);
                }
            }
        });

        setupTouchListeners();
        setupDragListeners();

        whiteDrawable = new ColorDrawable(ContextCompat.getColor(this, android.R.color.white));
    }

    private void setupTouchListeners () {
        View.OnTouchListener listener = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                    return true;
                }
                if (event.getActionMasked() == MotionEvent.ACTION_MOVE) {
                    boolean beyondLeft = event.getX() < 0;
                    boolean beyondRight = event.getX() > v.getWidth();
                    boolean beyondTop = event.getY() < 0;
                    boolean beyondBottom = event.getY() > v.getHeight();
                    if (beyondLeft || beyondRight || beyondTop || beyondBottom) {
                        startDragAndDrop(v);
                    } else {
                        return true;
                    }
                }
                return false;
            }
        };
        ivGrid1.setOnTouchListener(listener);
        ivGrid2.setOnTouchListener(listener);
        ivGrid3.setOnTouchListener(listener);
        ivGrid4.setOnTouchListener(listener);
    }

    private void startDragAndDrop (View view) {
        if (isDragging) return;
        isDragging = true;
        dragOriginView = view;
        view.setAlpha(0.5f);
        ClipData.Item item = new ClipData.Item((String)view.getTag());
        ClipData dragData = new ClipData((String)view.getTag(), new String[]{ClipDescription.MIMETYPE_TEXT_PLAIN},
                item);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            view.startDragAndDrop(dragData, new View.DragShadowBuilder(view), null, 0);
        } else {
            view.startDrag(dragData, new View.DragShadowBuilder(view), null, 0);
        }
        tempDrawableStorage = view.getBackground();
        view.setBackground(whiteDrawable);
    }

    private void setupDragListeners () {
        View.OnDragListener listener = new View.OnDragListener() {
            @Override
            public boolean onDrag(View v, DragEvent event) {
                switch (event.getAction()) {
                    case ACTION_DRAG_STARTED:
                        return event.getClipDescription().hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN);
                    case ACTION_DRAG_ENTERED:
                        v.setAlpha(0.5f);
                        v.invalidate();
                        return true;
                    case ACTION_DRAG_LOCATION:
                        return true;
                    case ACTION_DRAG_EXITED:
                        v.setAlpha(1f);
                        v.invalidate();
                        return true;
                    case ACTION_DROP:
                        v.setAlpha(1f);
                        swapColor(v);
                        return true;
                    case ACTION_DRAG_ENDED:
                        if (!event.getResult() && v == dragOriginView) {
                            dragOriginView.setBackground(tempDrawableStorage);
                            tempDrawableStorage = null;
                        }
                        v.setAlpha(1f);
                        v.invalidate();
                        isDragging = false;
                        Log.i("dragEvent", "drag ended");
                        return true;
                    default:
                        //Do nothing
                }
                return false;
            }
        };
        ivGrid1.setOnDragListener(listener);
        ivGrid2.setOnDragListener(listener);
        ivGrid3.setOnDragListener(listener);
        ivGrid4.setOnDragListener(listener);
    }

    private void swapColor (View dragTarget) {
        if (dragOriginView == dragTarget) {
            //Restore color
            dragOriginView.setBackground(tempDrawableStorage);
            tempDrawableStorage = null;
            return;
        }
        Drawable targetBackground = dragTarget.getBackground();
        dragOriginView.setBackground(targetBackground);
        dragTarget.setBackground(tempDrawableStorage);
        tempDrawableStorage = null;
    }

    private Bitmap convertViewToBitmap (View view){
        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bitmap);
        view.layout(view.getLeft(), view.getTop(), view.getRight(), view.getBottom());
        view.draw(c);
        return bitmap;
    }

    private String bitmapToFile(Bitmap bitmap) {
        //create a file to write bitmap data
        Date currentTime = Calendar.getInstance().getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        String fileName = getString(R.string.app_name) + sdf.format(currentTime) + ".jpg";
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), fileName);
        try {
            file.createNewFile();
        } catch (Exception e) {
            if (BuildConfig.DEBUG) e.printStackTrace();
            return "";
        }

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 95, bos);
        bitmap.recycle(); //Bitmap should be useless since here, recycle it
        byte[] bitmapData = bos.toByteArray();

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            fos.write(bitmapData);
            fos.flush();
            fos.close();
        } catch (Exception e) {
            if (BuildConfig.DEBUG) e.printStackTrace();
            return "";
        } finally {
            try {
                fos.close();
            } catch (Exception e) {
                Log.d("MainActivity", "Tried but failed to close FileOutputStream");
            }
        }
        return file.getAbsolutePath();
    }

    private void notifyGallery(String filePath) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File file = new File(filePath);
        Uri contentUri = Uri.fromFile(file);
        mediaScanIntent.setData(contentUri);
        sendBroadcast(mediaScanIntent);
    }
}
