package com.painel.floating;

import android.app.*;
import android.content.*;
import android.net.Uri;
import android.os.*;
import android.provider.Settings;
import android.view.*;
import android.widget.*;

public class MainActivity extends Activity {
    private static final int OVERLAY = 1001;
    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(32,32,32,32);
        TextView title = new TextView(this);
        title.setText("Floating Panel");
        title.setTextSize(25);
        title.setTextColor(0xFF00E5FF);
        box.addView(title);
        TextView info = new TextView(this);
        info.setText("\nPermissão de sobreposição necessária para mostrar o painel flutuante.\n\nOs controles são apenas um simulador visual.");
        info.setTextSize(16);
        box.addView(info);
        Button start = new Button(this);
        start.setText("ABRIR PAINEL FLUTUANTE");
        box.addView(start);
        setContentView(box);
        start.setOnClickListener(v -> openPanel());
    }
    private void openPanel() {
        if (!Settings.canDrawOverlays(this)) {
            Intent i = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:"+getPackageName()));
            startActivityForResult(i, OVERLAY);
            return;
        }
        if (Build.VERSION.SDK_INT >= 26) startForegroundService(new Intent(this, OverlayService.class));
        else startService(new Intent(this, OverlayService.class));
    }
}
