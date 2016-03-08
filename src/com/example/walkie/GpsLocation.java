package com.example.walkie;

import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;


/**
 * Location Listener for GPS Location update
 * 
 *
 */
public class GpsLocation implements LocationListener {

	static double lat, log;
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
		lat = location.getLatitude();
		log = location.getLongitude();
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