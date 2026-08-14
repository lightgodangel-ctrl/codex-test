package com.arttrainer.toolbox;

import android.content.Context;
import android.content.SharedPreferences;

public final class OverlayPrefs {
    private static final String PREF = "arttrainer_overlay_ux";

    public static final String K_SIZE_DP = "size_dp";
    public static final String K_OPACITY = "opacity";
    public static final String K_AUTO_DIM = "auto_dim";
    public static final String K_DIM_OPACITY = "dim_opacity";
    public static final String K_DIM_DELAY = "dim_delay_sec";
    public static final String K_EDGE_SNAP = "edge_snap";
    public static final String K_BOOT_START = "boot_start";

    private OverlayPrefs() {}

    public static SharedPreferences prefs(Context c) {
        return c.getSharedPreferences(PREF, Context.MODE_PRIVATE);
    }

    public static int sizeDp(Context c) { return prefs(c).getInt(K_SIZE_DP, 54); }
    public static int opacity(Context c) { return prefs(c).getInt(K_OPACITY, 92); }
    public static boolean autoDim(Context c) { return prefs(c).getBoolean(K_AUTO_DIM, true); }
    public static int dimOpacity(Context c) { return prefs(c).getInt(K_DIM_OPACITY, 28); }
    public static int dimDelaySec(Context c) { return prefs(c).getInt(K_DIM_DELAY, 6); }
    public static boolean edgeSnap(Context c) { return prefs(c).getBoolean(K_EDGE_SNAP, true); }
    public static boolean bootStart(Context c) { return prefs(c).getBoolean(K_BOOT_START, false); }
}
