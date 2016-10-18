package com.akrog.tolomet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class Manager {
	private List<Country> countries;
	private String[] directions;
	private final String lang;
	
	public Manager() {
		this(Locale.getDefault().getLanguage());
	}
	
	public Manager( String lang) {
		this.lang = lang;
		loadDirections();
		loadCountries();
	}		
	
	private void loadDirections() {
		BufferedReader rd = new BufferedReader(new InputStreamReader(getLocalizedResource("directions.csv")));
		try {
			directions = rd.readLine().split(",");
			rd.close();
		} catch (IOException e) {
		}		
	}
	
	private void loadCountries() {
		countries = new ArrayList<>();
		BufferedReader rd = new BufferedReader(new InputStreamReader(getLocalizedResource("countries.csv")));
		String line, code;
		String[] fields;
		try {
			while( (line=rd.readLine()) != null ) {
				fields = line.split("\\t");
				code = fields[0];
				if(getClass().getResource(String.format("/res/stations_%s.dat", code)) == null )
					continue;
				Country country = new Country();
				country.setCode(code);
				country.setName(fields[1]);
				countries.add(country);
			}
			rd.close();
		} catch (IOException e) {}
		Collections.sort(countries, new Comparator<Country>() {
			@Override
			public int compare(Country o1, Country o2) {
				return o1.getName().compareTo(o2.getName());
			}
		});
	}
	
	private InputStream getLocalizedResource( String name ) {
		InputStream is = getClass().getResourceAsStream(String.format("/res/%s", name.replaceAll("\\.", String.format("_%s.", lang.toLowerCase()))));
		if( is == null )
			is = getClass().getResourceAsStream(String.format("/res/%s", name));
		return is;
	}
	
	private InputStream getCountryResource( String country, String name ) {
		return getClass().getResourceAsStream(String.format("/res/%s", name.replaceAll("\\.", String.format("_%s.", country.toUpperCase()))));
	}

	public List<Country> getCountries() {
		if( countries == null )
			loadCountries();
		return countries;
	}
	
	public int getRefresh( Station station ) {
		if( !checkStation(station) )
			return 10;
		return station.getProvider().getRefresh(station.getCode());
	}
	
	public String getInforUrl( Station station ) {
		if( !checkStation(station) )
			return null;
		return station.getProvider().getInfoUrl(station.getCode());
	}

	public String getUserUrl( Station station ) {
		if( !checkStation(station) )
			return null;
		return station.getProvider().getUserUrl(station.getCode());
	}
	
	public boolean refresh( Station station ) {
		if( !checkStation(station) )
			return false;
		if( !station.isEmpty() && ((System.currentTimeMillis()-station.getStamp())/60000L < getRefresh(station)) )
			return false;
		try {
			station.getProvider().refresh(station);
		} catch( Exception e ) {
			station.clear();
			return false;
		}
		return true;
	}

	public boolean travel( Station station, long date ) {
		if( !checkStation(station) )
			return false;
		return station.getProvider().travel(station, date);
	}
	
	public void cancel( Station station ) {
		if( !checkStation(station) )
			return;
		station.getProvider().cancel();
	}

	public String getSummary( Station station, boolean large ) {
		return getSummary(station, null, large);
	}
	
	public String getSummary( Station station, Long stamp, boolean large ) {
		if( !checkStation(station) )
			return null;

		stamp = station.getMeteo().getStamp(stamp);
		if( stamp == null )
			return "";
		
		String strDir = null;
		Number dir = station.getMeteo().getWindDirection().getAt(stamp);
		if( dir != null )
			strDir = parseDirection(dir.intValue());
		Number med = station.getMeteo().getWindSpeedMed().getAt(stamp);
		Number max = station.getMeteo().getWindSpeedMax().getAt(stamp);
		Number hum = station.getMeteo().getAirHumidity().getAt(stamp);
		Number temp = station.getMeteo().getAirTemperature().getAt(stamp);

		StringBuilder str = new StringBuilder(getStamp(station));
		if( temp != null )
			str.append(String.format(" | %.1f ºC", temp));
		if( hum != null )
			str.append(String.format(" | %.0f %%", hum));
		if( strDir != null )
			str.append(String.format(" | %dº (%s)", dir.intValue(), strDir));
		if( med != null )
			str.append(String.format(" | %.1f", med));
		if( max != null )
			str.append(String.format("~%.1f", max));
		if( med != null || max != null )
			str.append(" km/h");

		return large ? str.toString() : str.toString().replaceAll(" ", "");
	}

	private Number findClosest(Measurement meas, Long stamp) {
		Number closest = meas.getLast();
		if( stamp == null )
			return closest;
		long diff = Math.abs(stamp-meas.getStamp());
		long tmp;
		for( int i = 0; i < meas.size(); i++ ) {
			tmp = Math.abs(stamp - meas.getTimes()[i]);
			if( tmp < diff ) {
				diff = tmp;
				closest = meas.getValues()[i];
			}
		}
		return closest;
	}

	public String getStamp(Station station) {
		return getStamp(station, null);
	}
		
	public String getStamp(Station station, Long stamp) {
		if( !checkStation(station) )
			return null;
		if( stamp == null )
			stamp = station.getMeteo().getStamp();
		Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(stamp);
        return String.format("%02d:%02d", cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE));
	}
	
	public boolean isOutdated(Station station) {
		if( station.isSpecial() || station.isEmpty() )
			return true;
		long stamp = station.getStamp();
		if( Calendar.getInstance().getTimeInMillis()-stamp > getRefresh(station)*60*1000)
			return true;
		return false;
	}
	
	public String parseDirection( int degrees ) {
        double deg = degrees + 11.25;
		while( deg >= 360.0 )
			deg -= 360.0;
		int index = (int)(deg/22.5);
		if( index < 0 )
			index = 0;
		else if( index >= 16 )
			index = 15;
		return directions[index];
	}
	
	public boolean checkStation(Station station) {
		return station != null && !station.isSpecial();
	}
}