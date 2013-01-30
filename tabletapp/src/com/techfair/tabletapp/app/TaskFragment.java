
package com.techfair.tabletapp.app;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.app.SherlockFragment;
import com.techfair.tabletapp.R;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;

@SuppressLint("SetJavaScriptEnabled")
public class TaskFragment extends SherlockFragment {
    private WebView mWebView;
    private String mUrl = "http://portal.mittechfair.org/admin/portal/ticket/";

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        LayoutInflater mInflater = (LayoutInflater) getActivity().getSystemService(
                SherlockActivity.LAYOUT_INFLATER_SERVICE);
        View view = (View) mInflater.inflate(R.layout.activity_tasks, null);
        mWebView = (WebView) view.findViewById(R.id.tasks_webview);
        mWebView.getSettings().setBuiltInZoomControls(true);
        mWebView.getSettings().setJavaScriptEnabled(true);
        if(android.os.Build.VERSION.SDK_INT >= 11){
            mWebView.getSettings().setDisplayZoomControls(false);
        }
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return false;
            }
        });
        mWebView.loadUrl(mUrl);
        return view;
    }

    public void loadUrl(String url) {
        if(mWebView != null){
            mWebView.loadUrl(url);
        }
    }

    public void setUrl(String url) {
        mUrl = url;
        this.loadUrl(mUrl);
    }

    public String getUrl() {
        return mUrl;
    }

}
