package com.arttrainer.toolbox;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;

public final class BitmapFilters {
    private BitmapFilters() {}

    public static Bitmap apply(Bitmap src, String mode) {
        if (src == null) return null;
        switch (mode) {
            case "gray": return grayscale(src);
            case "lowSat": return lowSaturation(src, 0.25f);
            case "mirror": return mirror(src);
            case "blur": return squintBlur(src);
            case "two": return quantize(src, 2);
            case "three": return quantize(src, 3);
            case "thumb": return thumbnail(src, 0.30f);
            case "r": return channel(src, 0);
            case "g": return channel(src, 1);
            case "b": return channel(src, 2);
            default: return src.copy(Bitmap.Config.ARGB_8888, false);
        }
    }

    private static Bitmap grayscale(Bitmap src) {
        int w = src.getWidth(), h = src.getHeight();
        int[] p = new int[w * h];
        src.getPixels(p, 0, w, 0, 0, w, h);
        for (int i = 0; i < p.length; i++) {
            int c = p[i];
            int a = Color.alpha(c);
            int y = luminance(c);
            p[i] = Color.argb(a, y, y, y);
        }
        Bitmap out = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        out.setPixels(p, 0, w, 0, 0, w, h);
        return out;
    }

    private static Bitmap lowSaturation(Bitmap src, float keepColor) {
        int w = src.getWidth(), h = src.getHeight();
        int[] p = new int[w * h];
        src.getPixels(p, 0, w, 0, 0, w, h);
        float grayWeight = 1f - keepColor;
        for (int i = 0; i < p.length; i++) {
            int c = p[i];
            int a = Color.alpha(c);
            int y = luminance(c);
            int r = clamp(Math.round(Color.red(c) * keepColor + y * grayWeight));
            int g = clamp(Math.round(Color.green(c) * keepColor + y * grayWeight));
            int b = clamp(Math.round(Color.blue(c) * keepColor + y * grayWeight));
            p[i] = Color.argb(a, r, g, b);
        }
        Bitmap out = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        out.setPixels(p, 0, w, 0, 0, w, h);
        return out;
    }

    private static Bitmap mirror(Bitmap src) {
        Matrix m = new Matrix();
        m.preScale(-1f, 1f);
        return Bitmap.createBitmap(src, 0, 0, src.getWidth(), src.getHeight(), m, true);
    }

    private static Bitmap squintBlur(Bitmap src) {
        int sw = Math.max(24, src.getWidth() / 22);
        int sh = Math.max(24, src.getHeight() / 22);
        Bitmap tiny = Bitmap.createScaledBitmap(src, sw, sh, true);
        Bitmap out = Bitmap.createScaledBitmap(tiny, src.getWidth(), src.getHeight(), true);
        if (tiny != out && !tiny.isRecycled()) tiny.recycle();
        return out;
    }

    private static Bitmap quantize(Bitmap src, int levels) {
        int w = src.getWidth(), h = src.getHeight();
        int[] p = new int[w * h];
        src.getPixels(p, 0, w, 0, 0, w, h);
        for (int i = 0; i < p.length; i++) {
            int c = p[i];
            int a = Color.alpha(c);
            int y = luminance(c);
            int q;
            if (levels == 2) q = y < 128 ? 0 : 255;
            else q = y < 85 ? 0 : (y < 170 ? 128 : 255);
            p[i] = Color.argb(a, q, q, q);
        }
        Bitmap out = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        out.setPixels(p, 0, w, 0, 0, w, h);
        return out;
    }

    private static Bitmap thumbnail(Bitmap src, float scale) {
        int w = src.getWidth(), h = src.getHeight();
        Bitmap out = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(out);
        canvas.drawColor(Color.BLACK);
        float dw = w * scale, dh = h * scale;
        float left = (w - dw) / 2f, top = (h - dh) / 2f;
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
        canvas.drawBitmap(src, null, new RectF(left, top, left + dw, top + dh), p);
        return out;
    }

    private static Bitmap channel(Bitmap src, int channel) {
        int w = src.getWidth(), h = src.getHeight();
        int[] p = new int[w * h];
        src.getPixels(p, 0, w, 0, 0, w, h);
        for (int i = 0; i < p.length; i++) {
            int c = p[i];
            int a = Color.alpha(c);
            int v = channel == 0 ? Color.red(c) : (channel == 1 ? Color.green(c) : Color.blue(c));
            p[i] = Color.argb(a, v, v, v);
        }
        Bitmap out = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        out.setPixels(p, 0, w, 0, 0, w, h);
        return out;
    }

    private static int luminance(int c) {
        return clamp(Math.round(Color.red(c) * 0.2126f + Color.green(c) * 0.7152f + Color.blue(c) * 0.0722f));
    }

    private static int clamp(int v) { return Math.max(0, Math.min(255, v)); }
}
