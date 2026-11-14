package com.amigoesrpg.launcher;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

public class MainActivity extends Activity {
    private WebView web;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(android.graphics.Color.rgb(7, 20, 43));
        getWindow().setNavigationBarColor(android.graphics.Color.rgb(7, 20, 43));
        web = new WebView(this);
        web.setBackgroundColor(android.graphics.Color.rgb(7, 20, 43));
        web.getSettings().setJavaScriptEnabled(true);
        web.getSettings().setDomStorageEnabled(true);
        web.setWebChromeClient(new WebChromeClient());
        web.setWebViewClient(new WebViewClient());
        web.addJavascriptInterface(new LauncherBridge(this), "Android");
        setContentView(web);
        web.loadUrl("file:///android_asset/index.html");
    }

    public class LauncherBridge {
        private final Context ctx;
        LauncherBridge(Context c) { ctx = c; }

        @JavascriptInterface public void download(String url, String fileName) {
            runOnUiThread(() -> {
                try {
                    Uri uri = Uri.parse(url);
                    if (!"https".equalsIgnoreCase(uri.getScheme()) && !"http".equalsIgnoreCase(uri.getScheme())) {
                        Toast.makeText(ctx, "URL de download inválida.", Toast.LENGTH_LONG).show();
                        return;
                    }
                    DownloadManager.Request req = new DownloadManager.Request(uri);
                    req.setTitle("Amigoes RPG - " + fileName);
                    req.setDescription("Baixando pacote do servidor...");
                    req.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                    req.setDestinationInExternalFilesDir(ctx, Environment.DIRECTORY_DOWNLOADS, fileName);
                    DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                    dm.enqueue(req);
                    Toast.makeText(ctx, "Download iniciado. O Android mostrará o progresso.", Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                    Toast.makeText(ctx, "Não foi possível iniciar o download.", Toast.LENGTH_LONG).show();
                }
            });
        }

        @JavascriptInterface public void connect(String host, int port, String packageName) {
            runOnUiThread(() -> {
                if (host == null || host.trim().isEmpty() || port < 1 || port > 65535) {
                    Toast.makeText(ctx, "Configure um IP e uma porta válidos.", Toast.LENGTH_LONG).show();
                    return;
                }
                if ("localhost".equalsIgnoreCase(host) || "127.0.0.1".equals(host)) {
                    Toast.makeText(ctx, "Atenção: localhost aponta para o próprio celular. Configure o IP público do servidor.", Toast.LENGTH_LONG).show();
                    return;
                }
                try {
                    Intent launch = getPackageManager().getLaunchIntentForPackage(packageName);
                    if (launch == null) {
                        Toast.makeText(ctx, "Cliente SA-MP não encontrado. Instale/configure o cliente compatível.", Toast.LENGTH_LONG).show();
                        return;
                    }
                    launch.putExtra("server_ip", host);
                    launch.putExtra("server_port", port);
                    launch.putExtra("hostname", host + ":" + port);
                    startActivity(launch);
                } catch (Exception e) {
                    Toast.makeText(ctx, "Não foi possível abrir o cliente do jogo.", Toast.LENGTH_LONG).show();
                }
            });
        }

        @JavascriptInterface public void toast(String msg) {
            runOnUiThread(() -> Toast.makeText(ctx, msg, Toast.LENGTH_SHORT).show());
        }
    }

    @Override public void onBackPressed() {
        if (web != null && web.canGoBack()) web.goBack();
        else super.onBackPressed();
    }
          }
