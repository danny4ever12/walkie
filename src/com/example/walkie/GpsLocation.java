package com.example.walkie;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;


/**
 * Location Listener for GPS Location update
 * 
 *
 */
public class GpsLocation implements LocationListener {

	static double lat, log;
	static String cityName;
	Context context;
	static StringBuilder strReturnedAddress = new StringBuilder("Address:\n");
	Geocoder geocoder;

	public GpsLocation(Context context) {
		super();
		this.context = context;
		geocoder = new Geocoder(context,Locale.ENGLISH);

	}

	/**
	 * Get updated latitude and longitude
	 */
	public void onLocationChanged(Location location) {
		
		
		
		if (location != null) {
			
			
			lat = location.getLatitude();
			log = location.getLongitude();
			
	        Log.d("LOCATION CHANGED", lat + "");
	        Log.d("LOCATION CHANGED", log + "");
//			      
	        
	     
	        /*----------to get City-Name from coordinates ------------- */
			cityName="test"; 
	        @SuppressWarnings("unused")
			String LocalityName=null;
	        @SuppressWarnings("unused")
			String subloc=null;
	                 
	          
	        try { 
	        	List<Address>  addresses;
	        	Log.e("inside try","Ddd");
	        addresses = geocoder.getFromLocation(lat, log, 1);  
	        if (addresses.size() > 0)  
	           System.out.println(addresses.get(0).getLocality());  
	           cityName=addresses.get(0).getLocality();  
	            LocalityName = addresses.get(0).getAddressLine(0);
            subloc=addresses.get(0).getSubLocality();

           
	          } catch (IOException e) {            
	          e.printStackTrace();  
	        } 
	            
	        

 
         }
		
		
		gpslocation();
	}

	/**
	 * Get Location address from latitude and longitude
	 */
	public void gpslocation() {
		strReturnedAddress = new StringBuilder("Address:\n");
		try {
			List<Address> addresses = geocoder.getFromLocation(lat, log, 1);
			if (addresses != null) {
				Address returnedAddress = addresses.get(0);
				for (int i = 0; i < returnedAddress.getMaxAddressLineIndex(); i++) {
					strReturnedAddress
							.append(returnedAddress.getAddressLine(i)).append(
									"\n");
				}
			} else {
				strReturnedAddress.append("not available");
			}
		} catch (Exception e) {
			e.printStackTrace();
			strReturnedAddress.append("Address not available");
		}

	}

	public void onProviderDisabled(String provider) {
	

	}

	public void onProviderEnabled(String provider) {
		

	}

	public void onStatusChanged(String provider, int status, Bundle extras) {
	

	}

}