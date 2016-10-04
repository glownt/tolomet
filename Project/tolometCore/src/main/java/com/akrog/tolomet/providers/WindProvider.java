package com.akrog.tolomet.providers;

import com.akrog.tolomet.Station;

public interface WindProvider {
	void refresh( Station station );
	boolean getHistory( Station station, long date );
	void cancel();
	int getRefresh( String code );
	String getInfoUrl( String code );
	String getUserUrl( String code );
}