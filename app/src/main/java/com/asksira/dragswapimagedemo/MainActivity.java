package com.asksira.dragswapimagedemo;

import android.Manifest;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    ConstraintLayout clContainer;
    ImageView ivGrid1, ivGrid2, ivGrid3, ivGrid4;
    Button button;

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
                    //TODO: Start save file
                } else {
                    PermissionUtils.checkPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            1234);
                }
            }
        });


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
                Log.d("MiniGame", "Tried to close FileOutputStream");
            }
        }
        return file.getAbsolutePath();
    }
}
