package com.example.pixuredlinux3.simplecamera2demo;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.util.Log;

import com.google.common.base.Optional;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class PixuredBitmapDecoder {

    public static PixuredBitmapDecoder getDecoder() {
        return new PixuredBitmapDecoder();
    }

    private int mLongerEdgeMax = 2048;

    public PixuredBitmapDecoder setLongerEdgeMax(int max) {
        mLongerEdgeMax = max;
        return this;
    }

    public Optional<Bitmap> decode(File file) {

        Log.v("PixuredBitmapDecoder", "[decode(File)]: " + file.getPath());

        Optional<Bitmap> op = Optional.absent();
        try {
            ExifInterface exif = new ExifInterface(file.getPath());
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            InputStream in = new FileInputStream(file);
            op = decode(in, orientation);
            closeInputStream(in);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return op;
    }

    public Optional<Bitmap> decode(InputStream in, int exifRotation) {

        if (!in.markSupported()) {
            Log.v("PixuredBitmapDecoder",
                    "[decode(InputStream)] - not mark supported, make BufferedInputStream");
            in = new BufferedInputStream(in);
        } else {
            Log.v("PixuredBitmapDecoder", "[decode(InputStream)] - mark supported");
        }

        Bitmap bm = bitmapFrom(in, mLongerEdgeMax);
        if (bm != null) {
            int rotation;
            switch(exifRotation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotation = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotation = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotation = 270;
                    break;
                default:
                    rotation = 0;
                    break;
            }
            Log.v("PixuredBitmapDecoder", "[decode(InputStream)] bitmap rotation " + rotation);
            Matrix matrix = new Matrix();
            matrix.postRotate(rotation);
            Bitmap finalBitmap = Bitmap.createBitmap(bm , 0, 0, bm.getWidth(), bm.getHeight(), matrix, true);
            return Optional.of(finalBitmap);
        } else {
            return Optional.absent();
        }
    }

    private static Bitmap bitmapFrom(InputStream in, int longerEdgeMax) {

        Log.v("PixuredBitmapDecoder", "[bitmapFrom(InputStream, int)]");

        if (longerEdgeMax <= 0) {
            Log.e("PixuredBitmapDecoder",
                    "[bitmapFrom(InputStream, int)] - longerEdgeMax <= 0, return null");
            return null;
        }

        int[] bmSize = new int[]{-1, -1};
        try {
            getBitmapSize(in, bmSize);
        } catch (IOException e) {
            Log.e("PixuredBitmapDecoder",
                    "[bitmapFrom(InputStream, int)] - getBitmapSize() --> IOException, return null");
            e.printStackTrace();
            return null;
        }
        if (bmSize[0] <= 0 && bmSize[1] <= 0) {
            Log.e("PixuredBitmapDecoder",
                    "[bitmapFrom(InputStream, int)] - getBitmapSize() --> size <= 0, return null");
            return null;
        }

        int scale = 1;
        int loadLimit = longerEdgeMax << 1;

        while (true) {
            if (bmSize[0] <= loadLimit && bmSize[1] <= loadLimit) {
                break;
            }
            bmSize[0] = bmSize[0] >> 1;
            bmSize[1] = bmSize[1] >> 1;
            scale = scale << 1;
        }
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inSampleSize = scale;

        Bitmap bm = BitmapFactory.decodeStream(in, null, opts);
        if (bm == null) {
            Log.e("PixuredBitmapDecoder",
                    "[bitmapFrom(InputStream, int)] - decodeStream() --> bm = null, return null");
            return null;
        } else {
            return getDownScaledBitmap(bm, longerEdgeMax);
        }
    }

    private static Bitmap getDownScaledBitmap(Bitmap bm, float longerEdgeMax) {
        if (longerEdgeMax <= Integer.MAX_VALUE) {

            int bmW = bm.getWidth();
            int bmH = bm.getHeight();

            if (bmW > bmH) {
                if (bmW <= longerEdgeMax) {
                    return bm;
                } else {
                    bmH = (int) (bmH * (longerEdgeMax / bmW));
                    bmW = (int) longerEdgeMax;
                }
            } else {
                if (bmH <= longerEdgeMax) {
                    return bm;
                } else {
                    bmW = (int) (bmW * (longerEdgeMax / bmH));
                    bmH = (int) longerEdgeMax;
                }
            }

            return Bitmap.createScaledBitmap(bm, bmW, bmH, false);
        } else {
            return bm;
        }
    }

    private static void getBitmapSize(InputStream in, int[] out) throws IOException {
        in.mark(Integer.MAX_VALUE);
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(in, null, opts);
        out[0] = opts.outWidth;
        out[1] = opts.outHeight;
        in.reset();
    }

    private static void closeInputStream(InputStream is) {
        try {
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
