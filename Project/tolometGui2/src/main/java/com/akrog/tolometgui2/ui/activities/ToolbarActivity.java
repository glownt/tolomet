package com.akrog.tolometgui2.ui.activities;

import android.content.Context;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProviders;

import com.akrog.tolomet.Station;
import com.akrog.tolometgui2.R;
import com.akrog.tolometgui2.model.Model;
import com.akrog.tolometgui2.ui.adapters.SpinnerAdapter;

public abstract class ToolbarActivity extends BaseActivity implements AdapterView.OnItemSelectedListener {
    private Model model;
    private Spinner spinner;
    private SpinnerAdapter spinnerAdapter;
    private boolean autoSelected;
    private Menu menu;

    protected Toolbar configureToolbar() {
        model = ViewModelProviders.of(this).get(Model.class);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        spinner = toolbar.findViewById(R.id.spinner);
        spinnerAdapter = new SpinnerAdapter(this, model.getSelStations());
        int pos = spinnerAdapter.getPosition(model.getCurrentStation());
        autoSelected = pos == 0;
        spinner.setAdapter(spinnerAdapter);
        spinner.setSelection(pos);
        spinner.setOnItemSelectedListener(this);

        return toolbar;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        this.menu = menu;
        getMenuInflater().inflate(R.menu.main, menu);
        model.liveCurrentStation().observe(this, station -> updateMenu(station));
        updateMenu(null);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        Station station = model.getCurrentStation();

        //noinspection SimplifiableIfStatement

        if( id == R.id.favorite_item ) {
            settings.setFavorite(station,!item.isChecked());
            model.selectStation(station);
        }
        else if (id == R.id.action_settings) {
            return true;
        }

        updateMenu(station);

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        Station station = (Station)adapterView.getSelectedItem();
        model.setCurrentStation(station);
        if( i == SpinnerAdapter.Command.FAV.ordinal() ) {
            model.selectFavorites();
            spinnerAdapter.notifyDataSetChanged(SpinnerAdapter.Command.FAV);
        } else if( i == SpinnerAdapter.Command.NEAR.ordinal() )
            selectNearest(() -> {}, () -> {
                spinnerAdapter.notifyDataSetChanged(SpinnerAdapter.Command.NEAR);
                spinner.performClick();
            });
        if( station == null ) {
            if( autoSelected )
                autoSelected = false;
            else
                spinner.performClick();
        }
    }

    private void updateMenu(Station station) {
        MenuItem favItem = menu.findItem(R.id.favorite_item);
        int favIcon;
        if( station == null || !station.isFavorite() )
            favIcon = R.drawable.ic_favorite_outline;
        else
            favIcon = R.drawable.ic_favorite;
        favItem.setIcon(favIcon);
        favItem.setChecked(station != null && station.isFavorite());
        setEnabled(favItem, station != null);
    }

    private void setEnabled( MenuItem item, boolean enabled ) {
        item.setEnabled(enabled);
        item.getIcon().setAlpha(enabled?0xFF:0x42);
    }

    private void selectNearest(Runnable onNothing, Runnable onFound) {
        final Context activity = this;
        askLocation(ll -> {
            if( ll == null ) {
                Toast.makeText(activity, R.string.error_gps, Toast.LENGTH_SHORT).show();
                onNothing.run();
            } else {
                model.selectNearest(ll.getLatitude(), ll.getLongitude());
                if (model.getSelStations().isEmpty()) {
                    Toast.makeText(activity, R.string.warn_near, Toast.LENGTH_SHORT).show();
                    onNothing.run();
                } else
                    onFound.run();
            }
        }, () -> {
            Toast.makeText(activity, R.string.warn_near,Toast.LENGTH_SHORT).show();
            onNothing.run();
        }, true);
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
    }
}
