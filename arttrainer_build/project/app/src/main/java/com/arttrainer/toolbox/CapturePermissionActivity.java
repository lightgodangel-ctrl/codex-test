package com.arttrainer.toolbox;

import android.app.Activity;
import android.content.Intent;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

public class CapturePermissionActivity extends Activity {
    private static final int REQ_CAPTURE = 7001;
    private String previewMode = "original";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        previewMode = getIntent().getStringExtra(OverlayService.EXTRA_PREVIEW_MODE);
        if (previewMode == null) previewMode = "original";

        if (CaptureService.isRunning()) {
            Intent snap = new Intent(this, CaptureService.class);
            snap.setAction(CaptureService.ACTION_SNAPSHOT);
            snap.putExtra(CaptureService.EXTRA_PREVIEW_MODE, previewMode);
            startService(snap);
            finish();
            return;
        }

        MediaProjectionManager mgr = (MediaProjectionManager)
                getSystemService(MEDIA_PROJECTION_SERVICE);
        if (mgr == null) {
            restoreBubble();
            finish();
            return;
        }
        startActivityForResult(mgr.createScreenCaptureIntent(), REQ_CAPTURE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != REQ_CAPTURE) return;

        if (resultCode == RESULT_OK && data != null) {
            Intent svc = new Intent(this, CaptureService.class);
            svc.setAction(CaptureService.ACTION_START);
            svc.putExtra(CaptureService.EXTRA_RESULT_CODE, resultCode);
            svc.putExtra(CaptureService.EXTRA_RESULT_DATA, data);
            svc.putExtra(CaptureService.EXTRA_PREVIEW_MODE, previewMode);
            if (Build.VERSION.SDK_INT >= 26) startForegroundService(svc); else startService(svc);
        } else {
            restoreBubble();
        }
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (!CaptureService.isRunning()) restoreBubble();
        }, 1200);
    }

    private void restoreBubble() {
        Intent i = new Intent(this, OverlayService.class);
        i.setAction(OverlayService.ACTION_SHOW_BUBBLE);
        try { startService(i); } catch (Exception ignored) {}
    }
}
