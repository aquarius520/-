package com.aquarius.pintu.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.ColorDrawable;

import com.aquarius.pintu.R;
import com.aquarius.pintu.entity.ImageItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by aquarius on 2017/6/5.
 */
public class ImageSplitUtil {

    /**
     * 分割图片 保存在list中
     * @param sourceBitmap 待分割的原始图片
     * @param group  分割为几行或者几列 行列数相同
     * @return
     */
    public static List<ImageItem> splitImageToPieces(Context context, Bitmap sourceBitmap, int group) {
        int total = group * group;
        int itemWidth = Math.min(sourceBitmap.getWidth(), sourceBitmap.getHeight()) / group;

        List<ImageItem> list = new ArrayList<ImageItem>(total);

        // 为了模拟真实的拼图 将最后一块用空白代替
        for(int i = 0; i < total - 1; i++) {
            Bitmap bitmap = Bitmap.createBitmap(sourceBitmap,
                    i % group * itemWidth,
                    i / group * itemWidth, itemWidth, itemWidth);
            ImageItem imageItem = new ImageItem(i, bitmap);
            list.add(imageItem);
        }
        // ColorDrawable drawable = new ColorDrawable(Color.TRANSPARENT);

        list.add(new ImageItem(total - 1,
                BitmapFactory.decodeResource(context.getResources(), R.mipmap.blank), true));

        return list;
    }


    public Bitmap resizeBitmap(float newWidth, float newHeight, Bitmap bitmap) {
        Matrix matrix = new Matrix();
        matrix.postScale(
                newWidth / bitmap.getWidth(),
                newHeight / bitmap.getHeight());
        Bitmap newBitmap = Bitmap.createBitmap(
                bitmap, 0, 0,
                bitmap.getWidth(),
                bitmap.getHeight(),
                matrix, true);
        return newBitmap;
    }
}
