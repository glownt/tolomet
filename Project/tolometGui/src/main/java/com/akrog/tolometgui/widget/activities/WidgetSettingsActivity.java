package com.akrog.tolometgui.widget.activities;

import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.os.Bundle;

import com.akrog.tolometgui.R;
import com.akrog.tolometgui.model.AppSettings;
import com.akrog.tolometgui.model.FlySpot;
import com.akrog.tolometgui.model.WidgetSettings;
import com.akrog.tolometgui.ui.activities.BaseActivity;
import com.akrog.tolometgui.widget.fragments.WidgetSettingsFragment;
import com.akrog.tolometgui.widget.providers.MediumWidgetProvider;
import com.akrog.tolometgui.widget.providers.SpotWidgetProvider;

public abstract class WidgetSettingsActivity extends BaseActivity {

    protected abstract int getWidgetSize();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_widget);

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if( extras != null )
            appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        if( appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID )
            finish();
        else {
            findViewById(R.id.ok_button).setOnClickListener(view -> onOK());
            findViewById(R.id.cancel_button).setOnClickListener(view -> cancel());
            getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.widget_content, new WidgetSettingsFragment())
                .commit();
        }
    }

    private void onOK() {
        if( appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID )
            cancel();
        FlySpot spot = appSettings.getSpot();
        if( spot.isValid() ) {
            WidgetSettings widgetSettings = new WidgetSettings(this, appWidgetId);
            widgetSettings.setSpot(spot);
            Intent result = new Intent();
            result.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            setResult(RESULT_OK, result);
            Intent intent = new Intent(this, MediumWidgetProvider.class);
            intent.setAction(SpotWidgetProvider.FORCE_WIDGET_UPDATE);
            intent.putExtra(SpotWidgetProvider.EXTRA_WIDGET_SIZE, getWidgetSize());
            sendBroadcast(intent);
            finish();
        } else
            cancel();
    }

    private void cancel() {
        setResult(RESULT_CANCELED);
        finish();
    }

    @Override
    public void onSettingsChanged(String key) {
        FlySpot spot = appSettings.getSpot();
        findViewById(R.id.ok_button).setEnabled(spot.isValid());
    }

    private int appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    private final AppSettings appSettings = AppSettings.getInstance();
}
