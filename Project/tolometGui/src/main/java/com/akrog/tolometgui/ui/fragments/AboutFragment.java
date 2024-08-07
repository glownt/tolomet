package com.akrog.tolometgui.ui.fragments;


import android.os.Bundle;
import android.text.Html;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;

import com.akrog.tolometgui.BuildConfig;
import com.akrog.tolometgui.R;
import com.akrog.tolometgui.model.db.DbTolomet;
import com.akrog.tolometgui.ui.activities.MainActivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Calendar;

import androidx.activity.OnBackPressedCallback;

public class AboutFragment extends ToolbarFragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        OnBackPressedCallback callback = new OnBackPressedCallback(true /* enabled by default */) {
            @Override
            public void handleOnBackPressed() {
                ((MainActivity) requireActivity()).navigate(R.id.nav_maps);
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(this, callback);

        return inflater.inflate(R.layout.fragment_about, container, false);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        TextView tv = getActivity().findViewById(R.id.legal_text);
        tv.setText(readRawTextFile(R.raw.legal));
        tv = getActivity().findViewById(R.id.info_text);
        String versionName = BuildConfig.VERSION_NAME;
        //Log.i("ChartsActivity",GoogleApiAvailability.getInstance().getOpenSourceSoftwareLicenseInfo(context));
        String info = readRawTextFile(R.raw.info)
            .replaceAll("\\$YEAR\\$", Calendar.getInstance().get(Calendar.YEAR)+"")
            .replaceAll("\\$VER\\$", versionName)
            .replaceAll("\\$DB\\$", String.valueOf(DbTolomet.VERSION));
        tv.setText(Html.fromHtml(info));
        //tv.setLinkTextColor(Color.WHITE);
        Linkify.addLinks(tv, Linkify.ALL);
    }

    private String readRawTextFile(int id) {
        InputStream inputStream = getActivity().getResources().openRawResource(id);
        InputStreamReader in = new InputStreamReader(inputStream);
        BufferedReader buf = new BufferedReader(in);
        String line;
        StringBuilder text = new StringBuilder();
        try {
            while (( line = buf.readLine()) != null) text.append(line);
            buf.close();
        } catch (IOException e) {
            return null;
        }
        return text.toString();
    }

    @Override
    protected int getMenuResource() {
        return R.menu.empty;
    }

    @Override
    protected int[] getLiveMenuItems() {
        return new int[0];
    }

    @Override
    public boolean needsScreenshotStation() {
        return false;
    }

    @Override
    public String getScreenshotSubject() {
        return getString(R.string.AboutSubject);
    }

    @Override
    public String getScreenshotText() {
        return getString(R.string.AboutText);
    }

    @Override
    public boolean useStation() {
        return false;
    }
}
