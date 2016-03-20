package com.example.walkie;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.example.walkie.R;

import android.app.Activity;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class FindBus extends Activity implements TextToSpeech.OnInitListener {

	protected static final int RESULT_SPEECH = 1;// Speech code for activity
	private TextToSpeech tts;
	boolean speechflag = false;
	
	
	SharedPreferences myshare;
	Editor myedit;
	//for json connect
    JSONParser jsonParser = new JSONParser();
	private static final String TAG_SUCCESS = "success";
	private static final String TAG_PRODUCT = "product";
	private static final String TAG_VEHICLE_ID = "vehicleId";
	private static final String SOURCE = "source";
	private static final String DESTINATION = "destination";
	private static final String TAG_LOCATION = "current_location";	
	private static final String TAG_LATITUDE = "latitude";
	private static final String TAG_LONGITUDE = "longitude";
	
	
	String percentage = "";// battery percentage
	Calendar c;

	JSONArray ja;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

		StrictMode.setThreadPolicy(policy); 
		
		
        myshare=PreferenceManager.getDefaultSharedPreferences(FindBus.this);
		
		myedit=myshare.edit();
		
		
		/**
		 * Adding text to speech
		 */
		tts = new TextToSpeech(this, this);

		/**
		 * GPS location Updating code
		 */
		LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		LocationListener ll = new GpsLocation(FindBus.this);
		lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 0, ll);
		lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 10000, 0,
				ll);
		batteryLevel();
		// unbindService();

	}

	/**
	 * Speak button click Check if already speaking then close tts
	 * 
	 * @param v
	 */
	public void speakButtonClicked(View v) {

		speak_function();
	}
	public void speak_function() {
		Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
		intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, "en-gb");
		try {
			startActivityForResult(intent, RESULT_SPEECH);
		} catch (ActivityNotFoundException a) {
			Toast t = Toast.makeText(this,
					"Oops! Your device doesn't support Speech to Text",
					Toast.LENGTH_SHORT);
			t.show();

		}
	}

	/**
	 * Activity result for speech to text
	 */
	@SuppressWarnings("unused")
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {

		case RESULT_SPEECH: {
			if (resultCode == Activity.RESULT_OK && null != data) {
				ArrayList<String> text = data
						.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

				Log.i("eachspeechtext", "" + text);

				String speechtext = "";

				speechtext = speechtext + text.get(0) + " ";
				Toast.makeText(this, "" + speechtext, Toast.LENGTH_LONG).show();

				/**
				 * Checking text from voice recognition
				 */

				/**
				 * my location
				 */
				if ((speechtext.trim().equals("my location"))
						|| (speechtext.trim().equals("location"))) {
					Toast.makeText(this,
							"" + GpsLocation.strReturnedAddress.toString(),
							Toast.LENGTH_LONG).show();
					speakOut(GpsLocation.strReturnedAddress.toString());
				}

				/**
				 * Battery level
				 */
				else if ((speechtext.trim().equals("my battery"))
						|| (speechtext.trim().equals("battery"))) {

					Toast.makeText(this, "" + percentage, Toast.LENGTH_LONG)
							.show();
					speakOut(percentage);
				} 

				/**
				 * inbox
				 */
				else if ((speechtext.trim().equals("my inbox"))
						|| (speechtext.trim().equals("inbox"))) {
					Intent myint = new Intent(this, InboxActivity.class);
					finish();
					startActivity(myint);

				} 
				/**
				 * time
				 */
				else if (speechtext.trim().equals("time")
						|| speechtext.trim().equals("time ")) {

					int hour = c.get(Calendar.HOUR);
					int minute = c.get(Calendar.MINUTE);

					if (Calendar.AM_PM == Calendar.AM) {
						speakOut("The time is " + hour + " " + minute + "A M");

					} else {
						speakOut("The time is " + hour + " " + minute + "P M");
					}

				} 
				
				/**
				 * date
				 */
				else if (speechtext.trim().equals("date")) {

					int day = c.get(Calendar.DAY_OF_MONTH);
					int month = c.get(Calendar.MONTH);
					int year = c.get(Calendar.YEAR);

					String monthstr = formatMonth(month);

					speakOut("The date is " + day + monthstr + year);

				} 
				
				/**
				 * voice call
				 */
				else if ((speechtext.trim().equals("make a call"))
						|| (speechtext.trim().equals("make call"))
						|| (speechtext.trim().equals("call"))) {
					speakOut("Tell the number or name");

					new Handler().postDelayed(new Runnable() {
	                      @Override
	                      public void run() {
	                    	  Intent callint = new Intent(FindBus.this,
	      							MakeCall.class);
	      					  finish();
	      					  startActivity(callint);
	                        
	                      }
	                  }, 3000);
					

				} 
				
				/**
				 * bus route
				 */
				else if (speechtext.trim().equals("bus route")
						|| speechtext.trim().equals("bus route ")) {
					
					
String loc=GpsLocation.cityName;
					
					
					Log.e("current_location",loc );
					
					List<NameValuePair> params = new ArrayList<NameValuePair>();
					
					params.add(new BasicNameValuePair("current_location",  loc));
					
				
					// getting JSON Object
					// Note that create product url accepts POST method
					String url_check="http://"+myshare.getString("ipaddress", "")+"/walkie/busesInLocation.php";
					JSONObject json = jsonParser.makeHttpRequest(url_check,
							"GET", params);
					
					// check log cat fro response
					Log.d("Create Response", json.toString());
					
					
					
					
					
					
					try {
						// check for success tag
						int success = json.getInt(TAG_SUCCESS);

						if (success == 1) {
							Log.d("ivide ethi","breakpoint");
							
							// successfully received product details
							JSONArray productObj = json
									.getJSONArray(TAG_PRODUCT); // JSON Array
							
							// get first product object from JSON Array
							JSONObject product = productObj.getJSONObject(0);
							String vehicle=product.getString(TAG_VEHICLE_ID);
							String source=product.getString(SOURCE);
							String destination=product.getString(DESTINATION);
							String latstr=product.getString(TAG_LATITUDE);
							String longstr=product.getString(TAG_LONGITUDE);
						
							
							double lat1=GpsLocation.lat;
							double log1=GpsLocation.log;
							
							//converting string to double
							double lat2 = Double.parseDouble(latstr);
							double log2 = Double.parseDouble(longstr);
							
							//calling calculate distance function
							double resultloc=distance(lat1, log1, lat2, log2, "K");
							
							//TRUNCATE RESULT DISTANCE
							Double truncresultloc = new BigDecimal(resultloc)
						    .setScale(5, BigDecimal.ROUND_HALF_UP)
						    .doubleValue();
							
							Double truncresultlocMetres=truncresultloc*1000;
							/**
							 * calculating time to reach
							 * bus speed-10 km/hr
							 * time=distance/speed
							 * in minutes... *60
							 */
							Double timeToReach=(truncresultloc/10)*60;
							
							//TRUNCATE RESULT TIME
							Double truncresulttime = new BigDecimal(timeToReach)
						    .setScale(4, BigDecimal.ROUND_HALF_UP)
						    .doubleValue();
							
							
							Log.e("vehicleid...", ""+vehicle+".....SOURCE"+source+".....destination"+destination+"...distance"+truncresultloc+"");
																
							
                            tts.setSpeechRate(0.6f);
							
							speakOut("Bus number is"+vehicle+"going to "+destination+" from "+source+"   Bus is  "+truncresultlocMetres+"  meters from your location    Bus will reach your location in   "+truncresulttime+"   minutes ");		        
							     
							
							
						} else {
							
							speakOut("No nearby bus");	
						}
					} catch (JSONException e) {
						e.printStackTrace();
						
					}
					

					
				}

				else if (speechtext.trim().equals("find bus")
						|| speechtext.trim().equals("find bus ")) {

					speakOut("Speak out the bus number");	
					
					new Handler().postDelayed(new Runnable() {
	                      @Override
	                      public void run() {
	                    	  Intent findbusint=new Intent(FindBus.this, FindBus.class);
	      					  startActivity(findbusint);
	                         
	                      }
	                  }, 3000);
					

				}

				/**
				 * help: app codes 
				 */
				else if (speechtext.trim().equals("help")) {

					speakOut("the keywords used are my battery make a call mylocation  my inbox my contacts find bus and  bus route");

				}
				/**
				 * location
				 */
				else if ((speechtext.trim().equals("help my location"))
						|| (speechtext.trim().equals("help location"))) {
					speakOut("Speak out my location to get your current location details");

				} 
				/**
				 * battery
				 */
				else if ((speechtext.trim().equals("help my battery"))
						|| (speechtext.trim().equals("help battery"))) {
					speakOut("Speak out my battery or battery to get your current battery level");

				} 
				/**
				 * inbox
				 */
				else if ((speechtext.trim().equals("help my inbox"))
						|| (speechtext.trim().equals("help inbox"))) {
					speakOut("Speak out my inbox or inbox to read your incoming messages");

				} 
				/**
				 * contacts
				 */
				else if ((speechtext.trim().equals("help my contacts"))
						|| (speechtext.trim().equals("help contacts"))) {
					speakOut("Speak out my contacts or contacts to access your contacts");

				} 
				/**
				 * voice call
				 */
				else if ((speechtext.trim().equals("help make a call"))
						|| (speechtext.trim().equals("help make call"))
						|| (speechtext.trim().equals("help call"))) {
					speakOut("Speak out make a call or make call or call to make a call");

				} 
				
				/**
				 *find bus 
				 */
				else if ((speechtext.trim().equals("help find bus"))) {
					speakOut("Speak out find bus to find location of a bus");

				} 
				
				/**
				 * bus route
				 */
				else if (speechtext.trim().equals("help bus route")) {
					speakOut("Speak out bus route to read a bus route");

				} 
				/**
				 * about app
				 */
				else if ((speechtext.trim().equalsIgnoreCase("walkie talkie"))
						|| (speechtext.trim()
								.equalsIgnoreCase("walkie torquay"))
						|| (speechtext.trim().equalsIgnoreCase("walkie"))) {
					speakOut("You are using walkie torquay App developed by team CEC. A userfriendly application for blind people");

				}

				else if ((speechtext.trim().equals("next"))) {
					speakOut("speak out inbox for reading inbox messages");

				}

				else {

					String ph=(speechtext.trim());
					Log.i("string num",ph);
					
					
					/**
					String str = ph.replaceAll("\\s+",""); 
					Log.i("string num after replacing",str);
					
					
					
					long phonenum = 0;
				
						phonenum = Integer.parseInt(str.trim());
						Log.i("integer num",""+phonenum);
						
						String str1 = ph.replaceAll("\\s+","_"); 
						//tts.setSpeechRate(0.6f);
		             **/
		            
					   
					 
				
					   
					  
					   
					   List<NameValuePair> params = new ArrayList<NameValuePair>();
						
					   params.add(new BasicNameValuePair("vehicleId", ph));
					  
					
					
						// getting JSON Object
						// Note that create product url accepts POST method
						String url_check="http://"+myshare.getString("ipaddress", "")+"/walkie/bus_finder.php";
						JSONObject json = jsonParser.makeHttpRequest(url_check,
								"GET", params);
						
						// check log cat fro response
						Log.d("Create Response", json.toString());
						
						try {
							// check for success tag
							int success = json.getInt(TAG_SUCCESS);

							if (success == 1) {
								Log.d("ivide ethi","breakpoint");
								
								// successfully received product details
								JSONArray productObj = json
										.getJSONArray(TAG_PRODUCT); // JSON Array
								
								// get first product object from JSON Array
								JSONObject product = productObj.getJSONObject(0);
								String curr_loc=product.getString(TAG_LOCATION);
								String getlat=product.getString(TAG_LATITUDE);
								String getlon=product.getString(TAG_LONGITUDE);
								
							
								
								Log.e("current location", ""+curr_loc+"...latitude"+getlat+"...longitude"+getlon);
																	
								
								
								  Double lat1=GpsLocation.lat;
								  Double lon1=GpsLocation.log;
								  Double lat2=Double.parseDouble(getlat);
								  Double lon2=Double.parseDouble(getlon);
								
								
								//calling calculate distance function
									double resultloc=distance(lat1, lon1, lat2, lon2, "K");
									
									//TRUNCATE RESULT DISTANCE
									Double truncresultloc = new BigDecimal(resultloc)
								    .setScale(5, BigDecimal.ROUND_HALF_UP)
								    .doubleValue();
									
									
									Double truncresultlocMetres=truncresultloc*1000;
									/**
									 * calculating time to reach
									 * bus speed-10 km/hr
									 * time=distance/speed
									 * in minutes... *60
									 */
									Double timeToReach=(truncresultloc/10)*60;
									
									//TRUNCATE RESULT TIME
									Double truncresulttime = new BigDecimal(timeToReach)
								    .setScale(4, BigDecimal.ROUND_HALF_UP)
								    .doubleValue();
								  
								
								
								
								
	                            tts.setSpeechRate(0.6f);
								
								speakOut("bus has reached "+curr_loc+"  You are "+truncresultlocMetres+" meters away from the bus. Bus will reach you in"+truncresulttime+" minutes");		        
								     
								
								
							} else {
								
								speakOut("Bus not Located.Please try again");	
							}
						} catch (JSONException e) {
							e.printStackTrace();
							
						}
						
						
						
						
					   
						
						
					
				}

			}

			break;
		}

		}
	}

	/**
	 * Text to speech intialization
	 */
	@Override
	public void onInit(int status) {
		// TODO Auto-generated method stub
		if (status == TextToSpeech.SUCCESS) {

			int result = tts.setLanguage(Locale.ENGLISH);

			if (result == TextToSpeech.LANG_MISSING_DATA
					|| result == TextToSpeech.LANG_NOT_SUPPORTED) {

				Intent installIntent = new Intent();
				installIntent
						.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
				startActivity(installIntent);
				Log.e("TTS", "This Language is not supported");
			} else {

			}

			c = Calendar.getInstance();

			speak_function();

		} else {
			Log.e("TTS", "Initialization Failed!");
		}
	}

	/**
	 * Speak the text
	 * 
	 * @param text
	 */
	private void speakOut(String text) {
		speechflag = true;
		tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);

	}

	/**
	 * Get Battery level
	 */

	private void batteryLevel() {

		BroadcastReceiver batteryLevelReceiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				// TODO Auto-generated method stub
				context.unregisterReceiver(this);
				int rawlevel = intent.getIntExtra("level", -1);
				int scale = intent.getIntExtra("scale", -1);
				int level = -1;
				if (rawlevel >= 0 && scale > 0) {
					level = (rawlevel * 100) / scale;
				}
				percentage = level + " percentage";

			}

		};
		IntentFilter batteryLevelFilter = new IntentFilter(
				Intent.ACTION_BATTERY_CHANGED);
		registerReceiver(batteryLevelReceiver, batteryLevelFilter);

	}

	/**
	 * Getting month name from integer month
	 * 
	 * @param month
	 * @return
	 */

	public String formatMonth(int month) {
		DateFormat formatter = new SimpleDateFormat("MMMM", Locale.US);
		GregorianCalendar calendar = new GregorianCalendar();
		calendar.set(Calendar.MONTH, month);
		return formatter.format(calendar.getTime());
	}

	
	/**
	 * for calculating distance between two locations 
	 * @author loco
	 *
	 */
	private static double distance(double lat1, double lon1, double lat2, double lon2, String unit) {
		double theta = lon1 - lon2;
		double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
		dist = Math.acos(dist);
		dist = rad2deg(dist);
		dist = dist * 60 * 1.1515;
		if (unit == "K") {
			dist = dist * 1.609344;
		} else if (unit == "N") {
			dist = dist * 0.8684;
		}

		return (dist);
	}

	/*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
	/*::	This function converts decimal degrees to radians						 :*/
	/*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
	private static double deg2rad(double deg) {
		return (deg * Math.PI / 180.0);
	}

	/*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
	/*::	This function converts radians to decimal degrees						 :*/
	/*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
	private static double rad2deg(double rad) {
		return (rad * 180 / Math.PI);
	}
	
	
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	// TODO Auto-generated method stub
    	menu.add("Set IP");
		return true;
    }
    @Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		String title=item.getTitle().toString();
		if(title.equals("Set IP"))
		{
			final Dialog dialog = new Dialog(this);
			dialog.setTitle("Specify IP");
			dialog.setContentView(R.layout.ipdialog);
			dialog.show();
			Button b1 = (Button) dialog.findViewById(R.id.ipsubmitButton);
			Button b2 = (Button) dialog.findViewById(R.id.ipcancelButton);
			final EditText ipEdit = (EditText) dialog.findViewById(R.id.ipaddressET);
			b1.setOnClickListener(new OnClickListener() {

				public void onClick(View v) {
					// TODO Auto-generated method stub
					String ipaddrress=ipEdit.getText().toString();
					//ipaddrress=ipaddrress;
					myedit.putString("ipaddress", ipaddrress);
					myedit.commit();
					dialog.dismiss();
				}
			});
			b2.setOnClickListener(new OnClickListener() {

				public void onClick(View v) {
					// TODO Auto-generated method stub
					dialog.dismiss();
				}
			});
		}
		return true;
	}
	
	
	
	
	@Override
	public void onDestroy() {
		// Don't forget to shutdown!
		if (tts != null) {
			tts.stop();
			tts.shutdown();
		}
		super.onDestroy();
	}
}
