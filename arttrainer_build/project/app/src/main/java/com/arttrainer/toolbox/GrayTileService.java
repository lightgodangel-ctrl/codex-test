package com.arttrainer.toolbox;

import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import android.widget.Toast;

public class GrayTileService extends TileService {
    @Override public void onStartListening() {
        super.onStartListening();
        Tile tile = getQsTile();
        if (tile != null) {
            tile.setState(Tile.STATE_INACTIVE);
            tile.setLabel("흑백 토글");
            tile.updateTile();
        }
    }

    @Override public void onClick() {
        super.onClick();
        MacroDroidBridge.toggle(this);
        Toast.makeText(this, "MacroDroid에 흑백 토글 신호 전송", Toast.LENGTH_SHORT).show();
    }
}
