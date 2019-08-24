package com.akrog.tolometgui2.ui.fragments;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.akrog.tolomet.Station;
import com.akrog.tolometgui2.R;
import com.akrog.tolometgui2.model.DbTolomet;
import com.akrog.tolometgui2.ui.services.LocationService;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import androidx.annotation.Nullable;

public class MapFragment extends ToolbarFragment implements OnMapReadyCallback, GoogleMap.OnCameraIdleListener, ClusterManager.OnClusterItemClickListener<MapFragment.StationItem> {
    private GoogleMap map;
    private final Map<Marker,Station> marker2station = new HashMap<>();
    private final Map<Station,Marker> station2marker = new HashMap<>();
    private ClusterManager<StationItem> cluster;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_map, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        MapView mapView = getView().findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);
        mapView.onResume();
        mapView.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap map) {
        this.map = map;
        setMapType();
        requestPermission(Manifest.permission.ACCESS_FINE_LOCATION, R.string.gps_rationale,
            () -> map.setMyLocationEnabled(true), null);
        cluster = new ClusterManager<StationItem>(getActivity(), map);
        map.setOnMarkerClickListener(cluster);
        map.setOnCameraIdleListener(this);
        cluster.setOnClusterItemClickListener(this);
        cluster.setRenderer(new StationRenderer(getActivity(), map, cluster));

        model.liveCurrentStation().observe(this, station -> {
            if( model.checkStation() )
                zoom(station);
        });

        if( !model.checkStation() ) {
            Location location = LocationService.getLocation(true);
            if( location != null )
                zoom(location.getLatitude(), location.getLongitude());
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.findItem(R.id.satellite_item).setChecked(settings.isSatellite());
    }

    @Override
    protected int getMenuResource() {
        return R.menu.map;
    }

    @Override
    protected int[] getLiveMenuItems() {
        return new int[0];
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if( id == R.id.satellite_item ) {
            item.setChecked(!item.isChecked());
            settings.setSatellite(item.isChecked());
            setMapType();
        } else if( id == R.id.browser_item )
            openBrowser();

        return super.onOptionsItemSelected(item);
    }

    private void openBrowser() {
        double lat, lon;
        if( model.getCurrentStation() != null && !model.getCurrentStation().isSpecial() ) {
            lat = model.getCurrentStation().getLatitude();
            lon = model.getCurrentStation().getLongitude();
        } else {
            lat = map.getCameraPosition().target.latitude;
            lon = map.getCameraPosition().target.longitude;
        }
        String url = getUrl(lat,lon);
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
    }

    public static String getUrl( double lat, double lon ) {
        return String.format(Locale.ENGLISH, "http://maps.google.com/maps?q=loc:%f,%f", lat, lon);
    }

    private void setMapType() {
        map.setMapType(settings.isSatellite() ? GoogleMap.MAP_TYPE_HYBRID : GoogleMap.MAP_TYPE_NORMAL);
    }

    @Override
    public String getScreenshotSubject() {
        return getString(R.string.ShareMapSubject);
    }

    @Override
    public String getScreenshotText() {
        return !model.checkStation() ? "" : String.format("%s %s%s",
            getString(R.string.ShareMapPre),
            model.getCurrentStation().getName(),
            getString(R.string.ShareMapPost));
    }

    @Override
    public void onSettingsChanged() {

    }

    @Override
    public void onCameraIdle() {
        float minZoom = 5.0f;
        CameraPosition cameraPosition = map.getCameraPosition();
        if( cameraPosition.zoom < minZoom ) {
            map.animateCamera(CameraUpdateFactory.zoomTo(minZoom));
            cluster.onCameraIdle();
            return;
        }
        LatLngBounds bounds = map.getProjection().getVisibleRegion().latLngBounds;
        List<Station> stations = DbTolomet.getInstance().findGeoStations(
                bounds.northeast.latitude, bounds.northeast.longitude,
                bounds.southwest.latitude, bounds.southwest.longitude);
        cluster.clearItems();
        for( Station station : stations )
            cluster.addItem(new StationItem(station));
        cluster.onCameraIdle();
    }

    @Override
    public boolean onClusterItemClick(StationItem stationItem) {
        model.selectStation(stationItem.getStation());
        return true;
    }

    private void zoom(Station station) {
        zoom(station.getLatitude(), station.getLongitude());
    }

    private void zoom(double lat, double lon) {
        LatLng cam = new LatLng(lat, lon);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(cam, 10));
    }

    private static class StationRenderer extends DefaultClusterRenderer<StationItem> {

        public StationRenderer(Context context, GoogleMap map, ClusterManager<StationItem> clusterManager) {
            super(context, map, clusterManager);
        }

        @Override
        protected void onBeforeClusterItemRendered(StationItem item, MarkerOptions markerOptions) {
            float hue;
            float hueHi = BitmapDescriptorFactory.HUE_GREEN;
            float hueMi = BitmapDescriptorFactory.HUE_YELLOW;
            float hueLo = BitmapDescriptorFactory.HUE_RED;
            switch( item.getStation().getProviderType().getQuality() ) {
                case Good: hue = hueHi; break;
                case Medium: hue = hueMi; break;
                default: hue = hueLo; break;
            }
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(hue));
            super.onBeforeClusterItemRendered(item, markerOptions);
        }
    }

    static class StationItem implements ClusterItem {
        private final Station station;

        public StationItem(Station station) {
            this.station = station;
        }

        @Override
        public LatLng getPosition() {
            return new LatLng(station.getLatitude(), station.getLongitude());
        }

        @Override
        public String getTitle() {
            return station.getName();
        }

        @Override
        public String getSnippet() {
            return station.getProviderType().name();
        }

        public Station getStation() {
            return station;
        }
    }
}