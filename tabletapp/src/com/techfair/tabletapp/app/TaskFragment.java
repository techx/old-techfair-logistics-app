
package com.techfair.tabletapp.app;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.app.SherlockFragment;
import com.techfair.tabletapp.R;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.util.Timer;
import java.util.TimerTask;

public class TaskFragment extends SherlockFragment {
    private WebView mWebView;
    private String mUrl = "http://portal.mittechfair.org/admin/portal/ticket/";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        LayoutInflater mInflater = (LayoutInflater) getActivity().getSystemService(
                SherlockActivity.LAYOUT_INFLATER_SERVICE);
        View view = (View) mInflater.inflate(R.layout.activity_tasks, null);
        mWebView = (WebView) view.findViewById(R.id.tasks_webview);
        mWebView.getSettings().setBuiltInZoomControls(true);
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return false;
            }
        });
        mWebView.loadUrl(mUrl);
        new ReloadWebView(this.getSherlockActivity(), 30, mWebView);
        return view;
    }

    public void loadUrl(String url) {
        mWebView.loadUrl(url);
    }

    public void setUrl(String url) {
        mUrl = url;
    }

    public String getUrl() {
        return mUrl;
    }

    protected class ReloadWebView extends TimerTask {
        Activity context;
        Timer timer;
        WebView wv;

        public ReloadWebView(Activity context, int seconds, WebView wv) {
            this.context = context;
            this.wv = wv;

            timer = new Timer();
            /* execute the first task after seconds */
            timer.schedule(this,
                    seconds * 1000, // initial delay
                    seconds * 1000); // subsequent rate

            /* if you want to execute the first task immediatly */
            /*
             * timer.schedule(this, 0, // initial delay null seconds * 1000); //
             * subsequent rate
             */
        }

        @Override
        public void run() {
            if (context == null || context.isFinishing()) {
                // Activity killed
                this.cancel();
                return;
            }

            context.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    wv.reload();
                }
            });
        }
    }

}
