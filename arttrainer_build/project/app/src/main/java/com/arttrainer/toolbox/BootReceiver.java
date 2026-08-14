package com.arttrainer.toolbox;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (!OverlayPrefs.bootStart(context) || !Settings.canDrawOverlays(context)) return;
        try {
            Intent svc = new Intent(context, OverlayService.class);
            svc.setAction(OverlayService.ACTION_START);
            if (Build.VERSION.SDK_INT >= 26) context.startForegroundService(svc);
            else context.startService(svc);
        } catch (Throwable ignored) {}
    }
}
