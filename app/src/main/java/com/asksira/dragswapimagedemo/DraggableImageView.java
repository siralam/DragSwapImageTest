package com.asksira.dragswapimagedemo;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;

public class DraggableImageView extends android.support.v7.widget.AppCompatImageView{


    public DraggableImageView(Context context) {
        super(context);
    }

    public DraggableImageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public DraggableImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
}
