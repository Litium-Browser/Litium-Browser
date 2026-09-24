package com.litium.browser;

import android.app.Activity;
import android.app.role.RoleManager;
import android.app.DownloadManager;
import android.content.Intent;
import android.provider.Settings;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.content.SharedPreferences;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.text.InputType;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebResourceError;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebSettings;
import android.webkit.URLUtil;
import android.widget.Toast;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Locale;

public final class MainActivity extends Activity {
    private static final String HOME_URL = "https://duckduckgo.com/";
    private static final String SETTINGS_URL = "https://litium.local/settings/";
    private static final String STATS_PREFS = "privacy_stats";
    private static final String[] BLOCKED_HOSTS = {
            "doubleclick.net", "googlesyndication.com", "google-analytics.com",
            "googletagmanager.com", "adservice.google.com", "connect.facebook.net",
            "facebook.net", "hotjar.com", "fullstory.com", "clarity.ms",
            "scorecardresearch.com", "taboola.com", "outbrain.com", "criteo.com",
            "adsrvr.org", "amazon-adsystem.com", "quantserve.com", "adnxs.com",
            "rubiconproject.com", "pubmatic.com", "googletagservices.com",
            "bat.bing.com", "mc.yandex.ru", "analytics.tiktok.com", "pixel.wp.com"
        };
    private static final String HOME_PAGE = "<!doctype html>"
            + "<html><head><meta name=\"viewport\" content=\"width=device-width,initial-scale=1\">"
            + "<style>"
            + "*{box-sizing:border-box}body{margin:0;min-height:100vh;background:#1f1f1f;color:#fff;"
            + "font-family:'Segoe UI',Arial,sans-serif}.page{max-width:760px;min-height:100vh;margin:0 auto;padding:18px}"
            + ".brand{background:#107c10;color:#fff;padding:22px 20px;font-size:clamp(34px,10vw,62px);"
            + "font-weight:300;letter-spacing:3px}.tag{background:#2d2d2d;color:#ddd;padding:12px 20px;"
            + "font-size:15px}.search{display:flex;margin-top:16px;background:#fff;padding:6px;"
            + "border-left:6px solid #ffb900}.search input{min-width:0;flex:1;border:0;padding:14px;"
            + "font-size:17px;outline:0;color:#202b35}.search button{border:0;background:#f0a400;color:#fff;"
            + "padding:0 20px;font-size:14px;font-weight:bold}.tiles{display:grid;grid-template-columns:repeat(3,1fr);"
            + "gap:8px;margin-top:16px}.tile{min-height:84px;padding:13px;background:#2d2d2d;color:#fff;"
            + "border-left:6px solid #00a4ef;font-size:13px}.tile strong{display:block;font-size:16px;margin-bottom:7px;"
            + "font-weight:400}.tile{animation:tileIn .28s ease both}.tile:nth-child(2){animation-delay:.06s}.tile:nth-child(3){animation-delay:.12s}.tile.green{border-color:#107c10}.tile.orange{border-color:#ffb900}.note{"
            + "font-size:12px;color:#aaa;margin-top:18px;line-height:1.5}@keyframes tileIn{from{opacity:0;transform:translateY(8px)}to{opacity:1;transform:none}}@media(prefers-reduced-motion:reduce){.tile{animation:none}}@media(max-width:520px){.page{padding:10px}.tiles{grid-template-columns:repeat(2,1fr)}.search{display:block}.search button{width:100%;height:44px;margin-top:4px}}"
            + "</style></head><body><main class=\"page\"><div class=\"brand\">LITIUM</div>"
            + "<div class=\"tag\">Простой поиск. Меньше слежки.</div>"
            + "<form class=\"search\" action=\"https://duckduckgo.com/\" method=\"get\">"
            + "<input name=\"q\" autofocus autocomplete=\"off\" placeholder=\"Введите запрос\"><button type=\"submit\">НАЙТИ</button></form>"
            + "<div class=\"tiles\"><div class=\"tile green\"><strong>Приватность</strong>Сторонние cookies заблокированы.</div>"
            + "<div class=\"tile orange\"><strong>Картинки</strong>Результаты в своей сетке.</div>"
            + "<div class=\"tile\"><strong>Защита</strong>Трекеры и реклама фильтруются.</div></div>"
            + "<div class=\"note\">Cache и история очищаются при запуске. Лишние разрешения не используются.</div>"
            + "</main></body></html>";

    private EditText addressBar;
    private WebView webView;
    private final Object statsLock = new Object();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.rgb(31, 31, 31));

        LinearLayout toolbar = new LinearLayout(this);
        toolbar.setBackgroundColor(Color.rgb(45, 45, 45));
        toolbar.setGravity(Gravity.CENTER_VERTICAL);
        toolbar.setPadding(8, 6, 8, 6);

        Button backButton = createButton("‹");
        backButton.setOnClickListener(view -> {
            if (webView.canGoBack()) {
                webView.goBack();
            }
        });

        Button forwardButton = createButton("›");
        forwardButton.setOnClickListener(view -> {
            if (webView.canGoForward()) {
                webView.goForward();
            }
        });

        addressBar = new EditText(this);
        addressBar.setSingleLine(true);
        addressBar.setHint("Адрес или поиск");
        addressBar.setTextColor(Color.rgb(25, 35, 31));
        addressBar.setHintTextColor(Color.rgb(105, 117, 106));
        addressBar.setBackground(addressBackground());
        addressBar.setMinWidth(0);
        addressBar.setMinimumWidth(0);
        addressBar.setPadding(12, 0, 12, 0);
        addressBar.setTextSize(16);
        addressBar.setImeOptions(EditorInfo.IME_ACTION_GO);
        addressBar.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_URI);
        addressBar.setOnEditorActionListener((view, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_GO) {
                openAddress(addressBar.getText().toString());
                return true;
            }
            return false;
        });

        Button reloadButton = createButton("↻");
        reloadButton.setOnClickListener(view -> webView.reload());

        Button settingsButton = createButton("☰");
        settingsButton.setOnClickListener(view -> showSettings());

        toolbar.addView(backButton, buttonParams());
        toolbar.addView(forwardButton, buttonParams());
        toolbar.addView(addressBar, new LinearLayout.LayoutParams(0, 48, 1));
        toolbar.addView(reloadButton, buttonParams());
        toolbar.addView(settingsButton, buttonParams());

        webView = new WebView(this);
        webView.setVisibility(View.INVISIBLE);
        configurePrivacy(webView);
        webView.setWebChromeClient(new WebChromeClient());
        webView.setDownloadListener((url, userAgent, contentDisposition, mimeType, contentLength) ->
            enqueueDownload(url, userAgent, contentDisposition, mimeType));
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                String target = request.getUrl().toString();
                if (target.startsWith("litium://")) {
                    if (target.equals("litium://clear-stats")) {
                        clearStats();
                    } else if (target.equals("litium://home")) {
                        showHome();
                    } else if (target.equals("litium://default-browser")) {
                        requestDefaultBrowser();
                    }
                    return true;
                }
                return false;
            }

            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                if (isBlockedHost(request.getUrl().getHost())) {
                    recordBlocked(request.getUrl());
                    return blockedResponse();
                }
                return super.shouldInterceptRequest(view, request);
            }

            @Override
            public void onPageStarted(WebView view, String url, android.graphics.Bitmap favicon) {
                view.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                String host = Uri.parse(url).getHost();
                if (host != null && !isSearchProvider(url) && !url.startsWith(SETTINGS_URL)) {
                    recordVisit(host);
                }
                addressBar.setText(url.equals(HOME_URL) ? "Litium" : url.startsWith(SETTINGS_URL) ? "Настройки" : url);
                if (!url.equals(HOME_URL) && isSearchProvider(url)) {
                    applyResultsShell(view);
                } else {
                    view.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                if (request.isForMainFrame()) {
                    view.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                handler.cancel();
            }
        });

        root.addView(toolbar, new LinearLayout.LayoutParams(-1, 64));
        root.addView(webView, new LinearLayout.LayoutParams(-1, 0, 1));
        setContentView(root);

        Uri launchUri = getIntent().getData();
        if (launchUri != null && ("http".equals(launchUri.getScheme()) || "https".equals(launchUri.getScheme()))) {
            webView.loadUrl(launchUri.toString());
        } else {
            showHome();
        }
    }

    private void showHome() {
        webView.loadDataWithBaseURL(HOME_URL, HOME_PAGE, "text/html", "UTF-8", null);
    }

    private void showSettings() {
        webView.loadDataWithBaseURL(SETTINGS_URL, buildSettingsPage(), "text/html", "UTF-8", null);
    }

    private void requestDefaultBrowser() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            RoleManager roleManager = getSystemService(RoleManager.class);
            if (roleManager != null && roleManager.isRoleAvailable(RoleManager.ROLE_BROWSER)
                    && !roleManager.isRoleHeld(RoleManager.ROLE_BROWSER)) {
                startActivityForResult(roleManager.createRequestRoleIntent(RoleManager.ROLE_BROWSER), 1001);
                return;
            }
        }
        startActivity(new Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS));
    }

    private void enqueueDownload(String url, String userAgent, String contentDisposition, String mimeType) {
        Uri downloadUri = Uri.parse(url);
        if (!"http".equals(downloadUri.getScheme()) && !"https".equals(downloadUri.getScheme())) {
            Toast.makeText(this, "Небезопасная загрузка заблокирована", Toast.LENGTH_SHORT).show();
            return;
        }
        DownloadManager.Request request = new DownloadManager.Request(downloadUri);
        request.setMimeType(mimeType);
        request.addRequestHeader("User-Agent", userAgent);
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        String fileName = URLUtil.guessFileName(url, contentDisposition, mimeType);
        request.setTitle(fileName);
        request.setDestinationInExternalFilesDir(this, Environment.DIRECTORY_DOWNLOADS, fileName);
        DownloadManager manager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        if (manager != null) {
            manager.enqueue(request);
            Toast.makeText(this, "Загрузка началась", Toast.LENGTH_SHORT).show();
        }
    }

    private void clearStats() {
        getSharedPreferences(STATS_PREFS, MODE_PRIVATE).edit().clear().apply();
        showSettings();
    }

    private void openAddress(String input) {
        String value = input.trim();
        if (value.isEmpty()) {
            return;
        }
        if (!value.startsWith("http://") && !value.startsWith("https://")) {
            value = HOME_URL + "?q=" + Uri.encode(value);
        }
        webView.loadUrl(value);
    }

    private void configurePrivacy(WebView view) {
        WebSettings settings = view.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        settings.setSaveFormData(false);
        settings.setAllowFileAccess(false);
        settings.setAllowContentAccess(false);
        settings.setGeolocationEnabled(false);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);
        settings.setMediaPlaybackRequiresUserGesture(true);
        settings.setSupportMultipleWindows(false);
        settings.setJavaScriptCanOpenWindowsAutomatically(false);
        settings.setAllowUniversalAccessFromFileURLs(false);
        settings.setAllowFileAccessFromFileURLs(false);
        settings.setBuiltInZoomControls(false);
        settings.setDisplayZoomControls(false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            settings.setSafeBrowsingEnabled(true);
        }
        CookieManager.getInstance().setAcceptThirdPartyCookies(view, false);
        view.clearHistory();
    }

    private boolean isSearchProvider(String url) {
        String host = Uri.parse(url).getHost();
        return host != null && (host.equals("duckduckgo.com") || host.endsWith(".duckduckgo.com"));
    }

    private boolean isBlockedHost(String host) {
        if (host == null) {
            return false;
        }
        String normalizedHost = host.toLowerCase(Locale.US);
        for (String blockedHost : BLOCKED_HOSTS) {
            if (normalizedHost.equals(blockedHost) || normalizedHost.endsWith("." + blockedHost)) {
                return true;
            }
        }
        return false;
    }

    private void recordBlocked(Uri uri) {
        String host = uri.getHost();
        if (host == null) {
            return;
        }
        String normalizedHost = host.toLowerCase(Locale.US);
        synchronized (statsLock) {
            SharedPreferences preferences = getSharedPreferences(STATS_PREFS, MODE_PRIVATE);
            String category = isTrackerHost(normalizedHost) ? "trackers" : "ads";
            String siteKey = "site.blocked." + normalizedHost;
            String lastKey = "site.last." + normalizedHost;
            preferences.edit()
                .putInt("total." + category, preferences.getInt("total." + category, 0) + 1)
                .putInt(siteKey, preferences.getInt(siteKey, 0) + 1)
                .putLong(lastKey, System.currentTimeMillis())
                .apply();
        }
    }

    private void recordVisit(String host) {
        String normalizedHost = host.toLowerCase(Locale.US);
        synchronized (statsLock) {
            SharedPreferences preferences = getSharedPreferences(STATS_PREFS, MODE_PRIVATE);
            preferences.edit()
                .putInt("site.visits." + normalizedHost,
                    preferences.getInt("site.visits." + normalizedHost, 0) + 1)
                .putLong("site.last." + normalizedHost, System.currentTimeMillis())
                .apply();
        }
    }

    private boolean isTrackerHost(String host) {
        return host.contains("analytics") || host.contains("tagmanager") || host.contains("hotjar")
                || host.contains("clarity") || host.contains("scorecard") || host.contains("pixel")
                || host.contains("bat.bing") || host.contains("mc.yandex");
    }

    private String buildSettingsPage() {
        SharedPreferences preferences = getSharedPreferences(STATS_PREFS, MODE_PRIVATE);
        int ads = preferences.getInt("total.ads", 0);
        int trackers = preferences.getInt("total.trackers", 0);
        List<SiteStat> sites = new ArrayList<>();
        for (Map.Entry<String, ?> entry : preferences.getAll().entrySet()) {
            if (!entry.getKey().startsWith("site.last.")) {
                continue;
            }
            String host = entry.getKey().substring("site.last.".length());
            sites.add(new SiteStat(host,
                    preferences.getInt("site.blocked." + host, 0),
                    preferences.getInt("site.visits." + host, 0),
                    preferences.getLong(entry.getKey(), 0)));
        }
        Collections.sort(sites, (left, right) -> Long.compare(right.lastVisit, left.lastVisit));
        StringBuilder rows = new StringBuilder();
        for (SiteStat site : sites.subList(0, Math.min(sites.size(), 8))) {
            rows.append("<div class=\"site\"><strong>")
                    .append(escapeHtml(site.host)).append("</strong><span>")
                    .append(site.blocked).append(" заблокировано · ")
                    .append(site.visits).append(" посещений</span></div>");
        }
        if (rows.length() == 0) {
            rows.append("<div class=\"empty\">Статистика появится после просмотра сайтов.</div>");
        }
        return "<!doctype html><html><head><meta name=\"viewport\" content=\"width=device-width,initial-scale=1\"><style>"
                + "*{box-sizing:border-box}body{margin:0;background:#1f1f1f;color:#fff;font-family:'Segoe UI',Arial,sans-serif}.page{max-width:780px;margin:auto;padding:12px}.head{background:#107c10;color:#fff;padding:20px}.head h1{font-size:32px;font-weight:300;margin:0 0 5px}.head p{margin:0;color:#d7f4d7}.stats{display:grid;grid-template-columns:repeat(3,1fr);gap:8px;margin:12px 0}.card{background:#2d2d2d;border-left:6px solid #00a4ef;padding:14px}.card.ads{border-color:#ffb900}.card.trackers{border-color:#107c10}.card b{display:block;font-size:25px;font-weight:400}.card span{font-size:12px;color:#bbb}.section{background:#2d2d2d;margin:10px 0;padding:15px}.section h2{font-size:18px;font-weight:400;margin:0 0 12px;color:#6dc2ff}.site{display:flex;justify-content:space-between;gap:10px;border-top:1px solid #4a4a4a;padding:11px 0}.site span{color:#bbb;font-size:12px;text-align:right}.empty{color:#aaa;font-size:13px}.actions{display:flex;gap:8px;flex-wrap:wrap}.action{display:inline-block;padding:11px 14px;text-decoration:none;color:#fff;background:#107c10}.action.warn{background:#d47d00}.action.default{background:#00a4ef}@media(max-width:520px){.stats{grid-template-columns:1fr 1fr}.stats .card:first-child{grid-column:1/-1}.site{display:block}.site span{display:block;text-align:left;margin-top:5px}}</style></head><body><main class=\"page\"><header class=\"head\"><h1>НАСТРОЙКИ</h1><p>Приватность и статистика Litium</p></header><section class=\"stats\"><div class=\"card\"><b>" + (ads + trackers) + "</b><span>всего заблокировано</span></div><div class=\"card ads\"><b>" + ads + "</b><span>рекламных запросов</span></div><div class=\"card trackers\"><b>" + trackers + "</b><span>трекеров</span></div></section><section class=\"section\"><h2>Недавние сайты</h2>" + rows + "</section><section class=\"section actions\"><a class=\"action warn\" href=\"litium://clear-stats\">Очистить статистику</a><a class=\"action default\" href=\"litium://default-browser\">Браузер по умолчанию</a><a class=\"action\" href=\"litium://home\">На главную</a></section></main></body></html>";
    }

    private String escapeHtml(String value) {
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&#39;");
    }

    private static final class SiteStat {
        private final String host;
        private final int blocked;
        private final int visits;
        private final long lastVisit;

        private SiteStat(String host, int blocked, int visits, long lastVisit) {
            this.host = host;
            this.blocked = blocked;
            this.visits = visits;
            this.lastVisit = lastVisit;
        }
    }

    private WebResourceResponse blockedResponse() {
        return new WebResourceResponse(
                "text/plain",
                "UTF-8",
                new ByteArrayInputStream(new byte[0]));
    }

    private void applyResultsShell(WebView view) {
        String script = "(function(){"
            + "if(document.getElementById('litium-results-shell'))return;"
            + "var tries=0;function render(){"
            + "var nodes=[].slice.call(document.querySelectorAll('.result,[data-testid=\\\"result\\\"]'));"
            + "var data=nodes.map(function(n){var a=n.querySelector('.result__a,h2 a,[data-testid=\\\"result-title-a\\\"]');"
            + "var s=n.querySelector('.result__snippet,[data-testid=\\\"result-snippet\\\"]');"
            + "return a?{title:a.textContent.trim(),href:a.href,snippet:s?s.textContent.trim():''}:null;}).filter(Boolean);"
            + "var imgs=[].slice.call(document.images).map(function(i){var src=i.currentSrc||i.src||i.dataset.src;"
            + "var a=i.closest('a');return src&&/^https?:/.test(src)&&(!i.naturalWidth||i.naturalWidth>=80)?{src:src,href:a?a.href:src,alt:i.alt||''}:null;}).filter(Boolean);"
            + "if(!data.length&&!imgs.length&&tries++<8){setTimeout(render,500);return;}"
            + "var q=new URL(location.href).searchParams.get('q')||'';var safeQ=q.replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;').replace(/\"/g,'&quot;');var imageMode=location.search.indexOf('ia=images')>-1;"
            + "var css=document.createElement('style');css.textContent=\"*{box-sizing:border-box}body{margin:0;min-height:100vh;"
            + "background:#1f1f1f!important;font-family:'Segoe UI',Arial,sans-serif!important;"
            + "color:#fff!important}.litium-top{position:sticky;top:0;z-index:5;padding:10px 12px;"
            + "border-bottom:3px solid #0b5b0b;background:#107c10;color:#fff;box-shadow:0 3px 0 #000}.litium-head{display:flex;align-items:center;"
            + "gap:12px;max-width:780px;margin:auto}.litium-logo{font-weight:300;font-size:20px;letter-spacing:2px}.litium-q{"
            + "flex:1;min-width:0;border:0;border-left:5px solid #ffb900;background:#2d2d2d;color:#fff;"
            + "padding:11px 10px;font-size:15px}.litium-tabs{display:flex;gap:8px;max-width:780px;margin:10px auto 0}.litium-tab{"
            + "border:0;padding:8px 13px;background:#00a4ef;color:#fff;font-weight:bold}.litium-tab.active{background:#ffb900}.litium-list{max-width:780px;margin:14px auto;padding:0 12px}."
            + "litium-card{display:block;margin:8px 0;padding:15px;border:0;border-left:6px solid #00a4ef;background:#2d2d2d}.litium-title{color:#6dc2ff;"
            + "font-size:18px;font-weight:400;text-decoration:none}.litium-url{display:block;color:#aaa;"
            + "font-size:12px;margin:6px 0;overflow:hidden;text-overflow:ellipsis;white-space:nowrap}.litium-snippet{"
            + "color:#ddd;font-size:14px;line-height:1.4}.litium-grid{display:grid;grid-template-columns:repeat(auto-fit,minmax(145px,1fr));"
            + "gap:8px;max-width:780px;margin:14px auto;padding:0 12px}.litium-image{display:block;overflow:hidden;"
            + "border:0;border-bottom:5px solid #107c10;background:#2d2d2d;animation:tileIn .28s ease both}.litium-image img{display:block;width:100%;height:155px;object-fit:cover}.litium-image-meta{padding:9px;color:#fff;font-size:13px;line-height:1.25}.litium-image-link{display:block;color:#aaa;font-size:11px;margin-top:5px;overflow:hidden;text-overflow:ellipsis;white-space:nowrap}.litium-empty{text-align:center;padding:40px 15px;color:#aaa}@keyframes tileIn{from{opacity:0;transform:translateY(8px)}to{opacity:1;transform:none}}@media(prefers-reduced-motion:reduce){.litium-image,.litium-card{animation:none}}"
            + "@media(max-width:480px){.litium-logo{font-size:16px}.litium-q{font-size:14px}.litium-title{font-size:16px}.litium-image img{height:125px}}\";"
            + "document.head.appendChild(css);document.body.innerHTML='';"
            + "var top=document.createElement('header');top.className='litium-top';"
            + "top.innerHTML='<div class=\\\"litium-head\\\"><span class=\\\"litium-logo\\\">LITIUM</span>"
            + "<input class=\\\"litium-q\\\" value=\\\"'+safeQ+'\\\" placeholder=\\\"Поиск\\\"></div>"
            + "<nav class=\\\"litium-tabs\\\"><button class=\\\"litium-tab '+(!imageMode?'active':'')+'\\\">Результаты</button>"
            + "<button class=\\\"litium-tab '+(imageMode?'active':'')+'\\\">Картинки</button></nav>';document.body.appendChild(top);"
            + "top.querySelector('.litium-q').onkeydown=function(e){if(e.key==='Enter')location.href='https://duckduckgo.com/?q='+encodeURIComponent(this.value)};"
            + "var tabs=top.querySelectorAll('.litium-tab');tabs[0].onclick=function(){location.href='https://duckduckgo.com/?q='+encodeURIComponent(q)};"
            + "tabs[1].onclick=function(){location.href='https://duckduckgo.com/?q='+encodeURIComponent(q)+'&iax=images&ia=images'};"
            + "var list=document.createElement('section');list.className=imageMode?'litium-grid':'litium-list';document.body.appendChild(list);"
            + "if(imageMode){imgs.slice(0,48).forEach(function(x){var a=document.createElement('a');a.className='litium-image';a.href=x.href;"
            + "a.target='_self';var i=document.createElement('img');i.loading='lazy';i.src=x.src;i.alt=x.alt;i.onerror=function(){a.remove();};a.appendChild(i);var meta=document.createElement('div');meta.className='litium-image-meta';meta.textContent=x.alt||'Изображение';var source=document.createElement('span');source.className='litium-image-link';source.textContent=x.href;meta.appendChild(source);a.appendChild(meta);list.appendChild(a);});}"
            + "else{data.forEach(function(x){var card=document.createElement('article');card.className='litium-card';"
            + "var link=document.createElement('a');link.className='litium-title';link.href=x.href;link.textContent=x.title;"
            + "var url=document.createElement('span');url.className='litium-url';url.textContent=x.href;"
            + "var snippet=document.createElement('div');snippet.className='litium-snippet';snippet.textContent=x.snippet;"
            + "card.appendChild(link);card.appendChild(url);card.appendChild(snippet);list.appendChild(card);});}"
            + "if(!list.children.length)list.innerHTML='<div class=\\\"litium-empty\\\">Ничего не найдено</div>';document.body.setAttribute('data-litium-ready','true');"
            + "}setTimeout(render,350);})();";
        view.evaluateJavascript(script, value -> revealWhenReady(view, 0));
    }

    private void revealWhenReady(WebView view, int attempt) {
        view.evaluateJavascript("document.body && document.body.getAttribute('data-litium-ready') === 'true'", value -> {
            if ("true".equals(value) || attempt >= 100) {
                view.setVisibility(View.VISIBLE);
                return;
            }
            new Handler(Looper.getMainLooper()).postDelayed(
                    () -> revealWhenReady(view, attempt + 1), 50);
        });
    }

    private GradientDrawable addressBackground() {
        GradientDrawable background = new GradientDrawable();
        background.setColor(Color.rgb(45, 45, 45));
        background.setStroke(2, Color.rgb(16, 124, 16));
        background.setCornerRadius(0);
        return background;
    }

    private Button createButton(String label) {
        Button button = new Button(this);
        button.setText(label);
        button.setContentDescription(label);
        button.setTextSize(22);
        button.setTextColor(Color.WHITE);
        button.setBackgroundColor(Color.rgb(16, 124, 16));
        button.setMinWidth(0);
        button.setMinimumWidth(0);
        button.setPadding(0, 0, 0, 0);
        return button;
    }

    private LinearLayout.LayoutParams buttonParams() {
        return new LinearLayout.LayoutParams(40, 48);
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        if (webView != null) {
            webView.stopLoading();
            webView.clearHistory();
            webView.destroy();
        }
        super.onDestroy();
    }
}