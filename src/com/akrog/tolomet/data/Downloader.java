package com.akrog.tolomet.data;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.os.AsyncTask;
import android.widget.Toast;

import com.akrog.tolomet.R;
import com.akrog.tolomet.Tolomet;

public class Downloader extends AsyncTask<Void, Void, String> {
	private Tolomet tolomet;
	private ProgressDialog progress;
	private String url, method;
	private List<NameValuePair> params;

	public Downloader( Tolomet tolomet ) {
		this.tolomet = tolomet;
		this.progress = new ProgressDialog(this.tolomet);
        this.progress.setMessage( this.tolomet.getString(R.string.Downloading)+"..." );
        this.progress.setTitle( "" );//getString(R.string.Progress) );
        this.progress.setIndeterminate(true);
        this.progress.setCancelable(true);
        this.progress.setOnCancelListener(new OnCancelListener(){
        	public void onCancel(DialogInterface dialog) {
        		cancel(true);
        	}
        });
        this.params = new ArrayList<NameValuePair>();
	}
	
	@Override
    protected void onPreExecute() {
        super.onPreExecute();	        
        this.progress.show();
    }
	
	@Override
	protected void onCancelled() {
		super.onCancelled();
		//this.progress.dismiss();
		Toast.makeText(this.tolomet,R.string.DownloadCancelled,Toast.LENGTH_SHORT).show();
		this.tolomet.OnCancelled();
	}	
	
	@Override
	protected String doInBackground(Void... params) {
		StringBuilder builder = new StringBuilder();
    	try {    		
    		HttpURLConnection con;
    		if( this.method != null && this.method.equalsIgnoreCase("POST") ) {
    			URL url = new URL(this.url);
    			con = (HttpURLConnection)url.openConnection();
    			con.setDoOutput(true);
    			con.setDoInput(true);
    			OutputStream os = con.getOutputStream();
    			BufferedWriter wr = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
    			wr.write(getQuery());
    			wr.close();
    			os.close();
    		} else {
    			URL url = new URL(this.url+"?"+getQuery());
    			con = (HttpURLConnection)url.openConnection();
    		}
    		//con.setRequestProperty("User-Agent","Mozilla/5.0 (Linux)");
    		BufferedReader rd = new BufferedReader(new InputStreamReader(con.getInputStream()));
    		String line;
    		while( (line=rd.readLine()) != null && !isCancelled() )
    			builder.append(line);
    		rd.close();
    	} catch( Exception e ) {
    		System.out.println(e.getMessage());
    		onCancelled();
		}
    	return builder.toString();
	}
	
	private String getQuery() throws UnsupportedEncodingException {
		StringBuilder result = new StringBuilder();
		NameValuePair entry;
	    
	    for( int i = 0; i < this.params.size(); i++ ) {
	    	if( i != 0 )
	            result.append("&");
	    	entry = this.params.get(i);
	    	result.append(URLEncoder.encode(entry.getName(), "UTF-8"));
	        result.append("=");
	        result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
	    }
	    
	    return result.toString();
	}
	
	@Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);	        
        this.progress.dismiss();
        this.tolomet.onDownloaded(result);
    }

	public String getUrl() {
		return this.url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
	
	public void addParam( String name, Object value ) {
		this.params.add(new BasicNameValuePair(name, value.toString()));
	}
	
	public void addParam( String name, String format, Object... values ) {
		this.params.add(new BasicNameValuePair(name, String.format(format, values)));
	}

	public String getMethod() {
		return this.method;
	}

	public void setMethod(String method) {
		this.method = method;
	}
}
