package com.akrog.tolomet;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;

public class Station {		
	public List<Number> ListDirection, ListHumidity, ListSpeedMed, ListSpeedMax;
	public String Name, Code;
	public WindProviderType Provider;
		
	public Station() {
		this("none","none",WindProviderType.Euskalmet);
	}
	
	public Station( String name, String code, WindProviderType provider ) {
		Name = name;
		Code = code;
		Provider = provider;
		ListDirection = new ArrayList<Number>();
		ListHumidity = new ArrayList<Number>();
		ListSpeedMed = new ArrayList<Number>();
		ListSpeedMax = new ArrayList<Number>();
	}
	
	public Station( Station station ) {
		this( station.Name, station.Code, station.Provider );
		replace(station);
	}
	
	public Station( Bundle bundle, String code ) {
		this();
		loadState(bundle, code);
	}
	
	public void clear() {
		ListDirection.clear();
		ListHumidity.clear();
		ListSpeedMed.clear();
		ListSpeedMax.clear();
	}
	
	public void add( Station station ) {
		if( station == null )
			return;
		ListDirection.addAll(station.ListDirection);
		ListHumidity.addAll(station.ListHumidity);
		ListSpeedMed.addAll(station.ListSpeedMed);
		ListSpeedMax.addAll(station.ListSpeedMax);
	}
	
	public void replace( Station station ) {
		clear();
		add(station);
		/*Name = station.Name;
		Code = station.Code;
		Provider = station.Provider;*/
	}
	
	public boolean isEmpty() {
		return ListDirection == null || ListDirection.size() < 2;
	}
	
	public void saveState( Bundle outState ) {
		outState.putString(Code+"-"+"name", Name);
		outState.putInt(Code+"-"+"type", Provider.getValue());
		saveLongArray(outState, "dirx", ListDirection, 0);
		saveIntArray(outState, "diry", ListDirection, 1);
		saveLongArray(outState, "humx", ListHumidity, 0);
		saveFloatArray(outState, "humy", ListHumidity, 1);
		saveLongArray(outState, "medx", ListSpeedMed, 0);
		saveFloatArray(outState, "medy", ListSpeedMed, 1);
		saveLongArray(outState, "maxx", ListSpeedMax, 0);
		saveFloatArray(outState, "maxy", ListSpeedMax, 1);
	}
	
	public void loadState( Bundle bundle, String code ) {
		if( bundle == null )
			return;
		Code = code;
		Name = bundle.getString(Code+"-"+"name");
		if( Name == null )
			return;
		Provider = WindProviderType.values()[bundle.getInt(Code+"-"+"type")];
		loadLongInt( bundle, "dirx", "diry", ListDirection );
		loadLongFloat( bundle, "humx", "humy", ListHumidity );
		loadLongFloat( bundle, "medx", "medy", ListSpeedMed );
		loadLongFloat( bundle, "maxx", "maxy", ListSpeedMax );
	}
	
	private void loadLongInt( Bundle bundle, String name1, String name2, List<Number> list ) {
		long[] x = bundle.getLongArray(Code+"-"+name1);
		int[] y = bundle.getIntArray(Code+"-"+name2);
		list.clear();
		if( x == null || y == null )
			return;
		for( int i = 0; i < x.length; i++ ) {
			list.add(x[i]);
			list.add(y[i]);
		}
	}

	private void loadLongFloat( Bundle bundle, String name1, String name2, List<Number> list ) {
		long[] x = bundle.getLongArray(Code+"-"+name1);
		float[] y = bundle.getFloatArray(Code+"-"+name2);
		list.clear();
		if( x == null || y == null )
			return;
		for( int i = 0; i < x.length; i++ ) {
			list.add(x[i]);
			list.add(y[i]);
		}
	}

	private void saveLongArray( Bundle outState, String name, List<Number> list, int off ) {
		int len = list.size()/2;
		long[] data = new long[len];
		int j = 0;
				
		for( int i = off; i < list.size(); i+= 2 )
			data[j++] = (Long)list.get(i);
		
		outState.putLongArray(Code+"-"+name, data);
	}
	
	private void saveFloatArray( Bundle outState, String name, List<Number> list, int off ) {
		int len = list.size()/2;
		float[] data = new float[len];
		int j = 0;
				
		for( int i = off; i < list.size(); i+= 2 )
			data[j++] = (Float)list.get(i);
		
		outState.putFloatArray(Code+"-"+name, data);
	}
	
	private void saveIntArray( Bundle outState, String name, List<Number> list, int off ) {
		int len = list.size()/2;
		int[] data = new int[len];
		int j = 0;
				
		for( int i = off; i < list.size(); i+= 2 )
			data[j++] = (Integer)list.get(i);
		
		outState.putIntArray(Code+"-"+name, data);
	}
}