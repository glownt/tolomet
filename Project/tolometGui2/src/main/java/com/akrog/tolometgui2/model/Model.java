package com.akrog.tolometgui2.model;

import android.location.Location;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.akrog.tolomet.Manager;
import com.akrog.tolomet.Station;
import com.akrog.tolomet.providers.WindProviderType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by gorka on 6/10/16.
 */

public class Model extends ViewModel {
    private static Model instance;
    private final Manager manager;
    private final DbTolomet db = DbTolomet.getInstance();
    //private final DbMeteo cache = DbMeteo.getInstance();
    private final List<Station> selection = new ArrayList<Station>();
    private final MutableLiveData<Station> currentStation = new MutableLiveData<>();

    public Model() {
        manager = new Manager();
    }

    public Station findStation(String id) {
        return db.findStation(id);
    }

    public Station findStation(WindProviderType type, String code) {
        return findStation(Station.buildId(type,code));
    }

    private void setSelection(Collection<Station> stations) {
        selection.clear();
        selection.addAll(stations);
    }

    public void selectNone() {
        selection.clear();
    }

    public void selectStation(Station station) {
        selection.clear();
        if( station == null )
            return;
        if( station.isFavorite() )
            selectFavorites();
        else
            selection.add(station);
        currentStation.setValue(station);
    }

    public void selectFavorites() {
        selection.clear();
        AppSettings settings = AppSettings.getInstance();
        for( String stationId : settings.getFavorites() ) {
            try {
                Station station = db.findStation(stationId);
                selection.add(station);
            } catch (Exception e) {
                settings.removeFavorite(stationId);
            }
        }
        Collections.sort(selection, new Comparator<Station>() {
            @Override
            public int compare(Station s1, Station s2) {
                return s1.getName().compareTo(s2.getName());
            }
        });
    }

    public void selectNearest(double lat, double lon) {
        List<Station> stations = db.findCloseStations(lat, lon, 5.0);
        float[] dist = new float[1];
        for( Station station : stations ) {
            Location.distanceBetween(lat, lon, station.getLatitude(), station.getLongitude(), dist);
            station.setDistance(dist[0]);
        }
        List<Station> close = new ArrayList<>();
        for( Station station : stations ) {
            if( station.getDistance() < 50000.0F )
                close.add(station);
        }
        Collections.sort(close, new Comparator<Station>() {
            @Override
            public int compare(Station s1, Station s2) {
                return (int)Math.signum(s1.getDistance()-s2.getDistance());
            }
        });
        setSelection(close);
    }

    public void setCurrentStation(Station station) {
        currentStation.postValue(station);
    }

    public Station getCurrentStation() {
        return currentStation.getValue();
    }

    public LiveData<Station> liveCurrentStation() {
        return currentStation;
    }

    public List<Station> getSelStations() {
        return selection;
    }

    public int getRefresh() {
        return getRefresh(getCurrentStation());
    }

    public String getInforUrl() {
        return getInforUrl(getCurrentStation());
    }

    public String getUserUrl() {
        return getUserUrl(getCurrentStation());
    }

    public boolean refresh() {
        return refresh(getCurrentStation());
    }

    public Station safeRefresh() {
        Station station = getCurrentStation().clone();
        if( !refresh(station) && station.isEmpty() )
            return null;
        return station;
    }

    public boolean loadCache() {
        return loadCache(getCurrentStation());
    }

    public boolean travel(long date) {
        return travel(getCurrentStation(), date);
    }

    public void cancel() {
        cancel(getCurrentStation());
    }

    public String getSummary(boolean large, float factor, String unit) {
        return getSummary(getCurrentStation(), large, factor, unit);
    }

    public String getSummary(Long stamp, boolean large, float factor, String unit) {
        return getSummary(getCurrentStation(), stamp, large, factor, unit);
    }

    public String getStamp() {
        return getStamp(getCurrentStation());
    }

    public String getStamp(Long stamp) {
        return getStamp(getCurrentStation(), stamp);
    }

    public boolean isOutdated() {
        return isOutdated(getCurrentStation());
    }

    public String parseDirection(int degrees) {
        return manager.parseDirection(degrees);
    }

    public boolean checkStation() {
        return checkStation(getCurrentStation());
    }

    public int getRefresh(Station station) {
        return manager.getRefresh(station);
    }

    public String getInforUrl(Station station) {
        return manager.getInforUrl(station);
    }

    public String getUserUrl(Station station) {
        return manager.getUserUrl(station);
    }

    public boolean refresh(Station station) {
        if( !checkStation(station) )
            return false;
        //cache.refresh(station);
        if( !manager.refresh(station) )
            return false;
        //cache.save(station);
        return true;
    }

    private boolean loadCache(Station station) {
        return false;
        /*if( !checkStation(station) )
            return false;
        return cache.refresh(station) > 0;*/
    }

    public boolean travel(Station station, long date) {
        return false;
        /*if( cache.travel(station, date) > 0 )
            return true;
        if( !manager.travel(station, date) )
            return false;
        cache.travelled(station, date);
        return true;*/
    }

    public void cancel(Station station) {
        manager.cancel(station);
    }

    public String getSummary(Station station, boolean large, float factor, String unit) {
        return manager.getSummary(station, large, factor, unit);
    }

    public String getSummary(Station station, Long stamp, boolean large, float factor, String unit) {
        return manager.getSummary(station, stamp, large, factor, unit);
    }

    public String getStamp(Station station) {
        return manager.getStamp(station);
    }

    public String getStamp(Station station, Long stamp) {
        return manager.getStamp(station, stamp);
    }

    public boolean isOutdated(Station station) {
        return manager.isOutdated(station);
    }

    public boolean checkStation(Station station) {
        return manager.checkStation(station);
    }
}