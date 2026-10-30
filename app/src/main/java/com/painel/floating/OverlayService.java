package com.painel.floating;

import android.app.*;
import android.content.*;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.*;
import android.provider.Settings;
import android.view.*;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class OverlayService extends Service {
    private WindowManager wm;
    private WebView panel;
    private WindowManager.LayoutParams layoutParams;

    @Override public void onCreate() {
        super.onCreate();
        createChannel();
        startForeground(7, notification());
        wm = (WindowManager)getSystemService(WINDOW_SERVICE);

        WebView web = new WebView(this);
        web.setBackgroundColor(Color.TRANSPARENT);
        web.getSettings().setJavaScriptEnabled(true);
        web.getSettings().setDomStorageEnabled(true);
        web.addJavascriptInterface(new PanelBridge(), "PanelBridge");
        web.setWebViewClient(new WebViewClient());
        web.loadUrl("file:///android_asset/index.html");
        panel = web;

        int type = Build.VERSION.SDK_INT >= 26
            ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            : WindowManager.LayoutParams.TYPE_PHONE;
        layoutParams = new WindowManager.LayoutParams(
            dp(350), dp(590), type,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        );
        layoutParams.gravity = Gravity.TOP | Gravity.START;
        layoutParams.x = dp(12);
        layoutParams.y = dp(80);
        try { wm.addView(panel, layoutParams); }
        catch(Exception e) { stopSelf(); }
    }

    private class PanelBridge {
        @JavascriptInterface public void setMinimized(boolean minimized) {
            new Handler(Looper.getMainLooper()).post(() -> {
                if (panel == null || layoutParams == null || !panel.isAttachedToWindow()) return;
                layoutParams.height = dp(minimized ? 58 : 590);
                try { wm.updateViewLayout(panel, layoutParams); } catch(Exception ignored) {}
            });
        }
        @JavascriptInterface public void closePanel() {
            new Handler(Looper.getMainLooper()).post(() -> stopSelf());
        }
    }

    private int dp(int n) {
        return (int)(n*getResources().getDisplayMetrics().density+.5f);
    }

    private void createChannel() {
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel c = new NotificationChannel(
                "floating", "Floating Panel", NotificationManager.IMPORTANCE_LOW
            );
            ((NotificationManager)getSystemService(NOTIFICATION_SERVICE)).createNotificationChannel(c);
        }
    }

    private Notification notification() {
        Notification.Builder b = Build.VERSION.SDK_INT >= 26
            ? new Notification.Builder(this, "floating")
            : new Notification.Builder(this);
        return b.setContentTitle("Floating Panel")
            .setContentText("Painel flutuante ativo")
            .setSmallIcon(android.R.drawable.ic_menu_manage).build();
    }

    @Override public int onStartCommand(Intent i, int f, int id) {
        return START_NOT_STICKY;
    }

    @Override public void onDestroy() {
        if (panel != null && wm != null) {
            try { wm.removeView(panel); } catch(Exception ignored) {}
            panel = null;
        }
        super.onDestroy();
    }

    @Override public IBinder onBind(Intent i) { return null; }
}