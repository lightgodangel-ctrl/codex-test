package com.arttrainer.toolbox;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public final class MacroDroidBridge {
    public static final String ACTION_GRAY = "com.arttrainer.toolbox.FILTER_GRAY";
    public static final String ACTION_INVERT = "com.arttrainer.toolbox.FILTER_INVERT";
    public static final String ACTION_RESTORE = "com.arttrainer.toolbox.FILTER_RESTORE";
    public static final String ACTION_TOGGLE = "com.arttrainer.toolbox.FILTER_TOGGLE";

    private MacroDroidBridge() {}

    public static void gray(Context c) { send(c, ACTION_GRAY); }
    public static void invert(Context c) { send(c, ACTION_INVERT); }
    public static void restore(Context c) { send(c, ACTION_RESTORE); }
    public static void toggle(Context c) { send(c, ACTION_TOGGLE); }

    public static void send(Context c, String action) {
        try {
            Intent i = new Intent(action);
            i.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
            c.sendBroadcast(i);
        } catch (Throwable e) {
            Toast.makeText(c, "MacroDroid 브리지 전송 실패", Toast.LENGTH_SHORT).show();
        }
    }
}
