package com.akrog.tolomet.ui.activities;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;
import android.widget.ListView;

import com.akrog.tolomet.R;
import com.akrog.tolomet.providers.WindProviderType;
import com.akrog.tolomet.ui.adapters.ProviderAdapter;
import com.akrog.tolomet.viewmodel.DbTolomet;
import com.akrog.tolomet.viewmodel.ProviderWrapper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class UpdateActivity extends ProgressActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        task = new AsyncTask<Void,Void,Map<String,DbTolomet.ProviderInfo>>() {
            @Override
            protected void onPreExecute() {
                beginProgress();
            }

            @Override
            protected Map<String, DbTolomet.ProviderInfo> doInBackground(Void... voids) {
                return DbTolomet.getInstance().getProviderCounts();
            }

            @Override
            protected void onPostExecute(Map<String, DbTolomet.ProviderInfo> map) {
                task = null;
                updateList(map);
                endProgress();
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        addCancelListenner(() -> {
            if( task != null ) {
                task.cancel(true);
                task = null;
            }
        });
        updateList(null);
    }

    private void updateList(Map<String, DbTolomet.ProviderInfo> map) {
        List<ProviderWrapper> providers = buildWrappers(map);
        sortProviders(providers);

        ListView list = findViewById(R.id.list_providers);
        ProviderAdapter adapter = new ProviderAdapter(this, providers.toArray(new ProviderWrapper[0]));
        list.setAdapter(adapter);
    }

    private List<ProviderWrapper> buildWrappers(Map<String,DbTolomet.ProviderInfo> map) {
        Date defaultDate;
        try {
            defaultDate = DATE_FORMAT.parse("19/11/2018 18:20:00");
        } catch (ParseException e) {
            e.printStackTrace();
            defaultDate = new Date();
        }
        List<ProviderWrapper> providers = new ArrayList<>(WindProviderType.values().length);
        for( WindProviderType type : WindProviderType.values() ) {
            ProviderWrapper wrapper = new ProviderWrapper(type);
            if( type == WindProviderType.Euskalmet )
                wrapper.setIconId(R.drawable.euskalmet);
            else if( type == WindProviderType.Aemet )
                wrapper.setIconId(R.drawable.aemet);
            wrapper.setDate(DATE_FORMAT.format(defaultDate));
            if( map != null ) {
                DbTolomet.ProviderInfo info = map.get(type.name());
                if (info != null) {
                    wrapper.setStations(info.getStationCount());
                }
            }
            providers.add(wrapper);
        }
        return providers;
    }

    private void sortProviders(List<ProviderWrapper> providers) {
        Collections.sort(providers, (p1, p2) -> {
            WindProviderType t1 = p1.getType();
            WindProviderType t2 = p2.getType();
            if( t1.isDynamic() && !t2.isDynamic() )
                return -1;
            if( t2.isDynamic() && !t1.isDynamic() )
                return 1;
            if( p1.getIconId() > 0 && p2.getIconId() <= 0 )
                return -1;
            if( p2.getIconId() > 0 && p1.getIconId() <= 0 )
                return 1;
            if( t1.getQuality() != t2.getQuality() )
                return t1.getQuality().ordinal() - t2.getQuality().ordinal();
            return t1.toString().compareTo(t2.toString());
        });
    }

    @Override
    public void onSettingsChanged() {
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                endProgress();
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
    private AsyncTask<Void,Void,Map<String,DbTolomet.ProviderInfo>> task;
}
