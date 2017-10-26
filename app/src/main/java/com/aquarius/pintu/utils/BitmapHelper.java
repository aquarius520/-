package com.aquarius.pintu.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

/**
 * Created by aquarius on 2017/10/26.
 */
public class BitmapHelper {

    private BitmapHelper(){}

    public static Bitmap acquireCompressedBitmapIfNeed(InputStream is, Context context) {
        if (is == null) {
            return null;
        }

        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        int len ;
        byte[] buffer = new byte[1024 * 8];
        try {
            while ((len = is.read(buffer)) != -1) {
                bos.write(buffer, 0, len);
            }
            bos.flush();
            bos.close();
            is.close();

            byte[] bytes = bos.toByteArray();

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;

            BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
            int outWidth = options.outWidth;
            int screenWidth = ScreenUtil.getScreenWidth(context);

            if (outWidth > screenWidth) {
                int result = outWidth / screenWidth;
                options.inSampleSize = result > 1 && result < 2 ? 2 : result;
            }

            options.inJustDecodeBounds = false;
            options.inPreferredConfig = Bitmap.Config.ARGB_4444;
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
