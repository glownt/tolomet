package com.akrog.tolomet;

import android.os.Bundle;

public class HelpActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        createTitleView(savedInstanceState,R.layout.activity_help,
                R.id.share_item, R.id.whatsapp_item,
                R.id.about_item, R.id.report_item);
    }

    @Override
    public void onRefresh() {
    }

    @Override
    public void onBrowser() {
    }

    @Override
    public void onSettingsChanged() {
    }

    @Override
    public void onSelected(Station station) {
    }

    @Override
    public String getScreenShotSubject() {
        return getString(R.string.help_subject);
    }

    @Override
    public String getScreenShotText() {
        return getString(R.string.help_body);
    }
}