package com.aquarius.pintu.entity;

import android.graphics.Bitmap;

/**
 * Created by aquarius on 2017/6/5.
 */
public class ImageItem {

    private int index;
    private Bitmap bitmap;
    private boolean isBlank; // 表示空白与否

    public ImageItem() {
    }

    public ImageItem(int index, Bitmap bitmap) {
        this(index, bitmap, false);
    }

    public ImageItem(int index, Bitmap bitmap, boolean isBlank) {
        this.index = index;
        this.bitmap = bitmap;
        this.isBlank = isBlank;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }


    public boolean isBlank() {
        return isBlank;
    }

    public void setBlank(boolean blank) {
        isBlank = blank;
    }

    @Override
    public String toString() {
        return "ImageItem{" +
                "index=" + index +
                ", bitmap=" + bitmap +
                ", isBlank=" + isBlank +
                '}';
    }
}
