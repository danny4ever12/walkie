package com.example.walkie;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.example.walkie.R;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

public class FindBus extends Activity implements TextToSpeech.OnInitListener,
		IpAddress {

	protected static final int RESULT_SPEECH = 1;// Speech code for activity
	private TextToSpeech tts;
	boolean speechflag = false;

	private Timer timer;
	
	String percentage = "";// battery percentage
	Calendar c;

	JSONArray ja;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

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
				} else if ((speechtext.trim().equals("my inbox"))
						|| (speechtext.trim().equals("inbox"))) {
					Intent myint = new Intent(this, InboxActivity.class);
					finish();
					startActivity(myint);

				} else if (speechtext.trim().equals("time")
						|| speechtext.trim().equals("time ")) {

					int hour = c.get(Calendar.HOUR);
					int minute = c.get(Calendar.MINUTE);

					if (Calendar.AM_PM == Calendar.AM) {
						speakOut("The time is " + hour + " " + minute + "A M");

					} else {
						speakOut("The time is " + hour + " " + minute + "P M");
					}

				} else if (speechtext.trim().equals("date")) {

					int day = c.get(Calendar.DAY_OF_MONTH);
					int month = c.get(Calendar.MONTH);
					int year = c.get(Calendar.YEAR);

					String monthstr = formatMonth(month);

					speakOut("The date is " + day + monthstr + year);

				} else if ((speechtext.trim().equals("make a call"))
						|| (speechtext.trim().equals("make call"))
						|| (speechtext.trim().equals("call"))) {
					speakOut("Tell the number or name");

					//FOR DELAY
					timer = new Timer();
					timer.scheduleAtFixedRate(new TimerTask() {
					            @Override
					            public void run() {
					                //Generate number
					            }
					        }, 5000, 5000);
					Intent callint = new Intent(FindBus.this,
							MakeCall.class);
					finish();
					startActivity(callint);

				} else if (speechtext.trim().equals("bus route")
						|| speechtext.trim().equals("bus route ")) {
					// call webservice

					WebServer web = new WebServer();

					List<NameValuePair> mylist = new ArrayList<NameValuePair>(2);
					mylist.add(new BasicNameValuePair("latitude", ""
							+ GpsLocation.lat));
					mylist.add(new BasicNameValuePair("longitude", ""
							+ GpsLocation.log));

					ja = web.doPost(
							mylist,
							"http://"
									+ ip
									+ ":8084/Walkie_Talkie/getbusdetails.jsp");
					if (ja != null) {

						try {
							JSONObject jo = ja.getJSONObject(0);

							if (jo.getString("resp").equals("success")) {

								String name = jo.getString("vehicle_name");
								String source = jo.getString("source");
								String destination = jo
										.getString("destination");
								tts.setSpeechRate(0.6f);

								speakOut("Bus name is" + name + "going to "
										+ destination + " from " + source);

							} else {
								speakOut("No nearby bus");
							}

						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

					} else {
						speakOut("No nearby bus");
					}

				}

				else if (speechtext.trim().equals("find bus")
						|| speechtext.trim().equals("find bus ")) {

					speakOut("Speak out the bus number");					
					Intent findbusint=new Intent(FindBus.this, FindBus.class);
					startActivity(findbusint);

				}

				else if (speechtext.trim().equals("help")) {

					speakOut("the keywords used are my battery make a call mylocation  my inbox my contacts find bus and  bus route");

				} else if ((speechtext.trim().equals("help my location"))
						|| (speechtext.trim().equals("help location"))) {
					speakOut("Speak out my location to get your current location details");

				} else if ((speechtext.trim().equals("help my battery"))
						|| (speechtext.trim().equals("help battery"))) {
					speakOut("Speak out my battery or battery to get your current battery level");

				} else if ((speechtext.trim().equals("help my inbox"))
						|| (speechtext.trim().equals("help inbox"))) {
					speakOut("Speak out my inbox or inbox to read your incoming messages");

				} else if ((speechtext.trim().equals("help my contacts"))
						|| (speechtext.trim().equals("help contacts"))) {
					speakOut("Speak out my contacts or contacts to access your contacts");

				} else if ((speechtext.trim().equals("help make a call"))
						|| (speechtext.trim().equals("help make call"))
						|| (speechtext.trim().equals("help call"))) {
					speakOut("Speak out make a call or make call or call to make a call");

				} else if ((speechtext.trim().equals("help find bus"))) {
					speakOut("Speak out find bus to find location of a bus");

				} else if (speechtext.trim().equals("help bus route")) {
					speakOut("Speak out bus route to read a bus route");

				} else if ((speechtext.trim().equalsIgnoreCase("walkie talkie"))
						|| (speechtext.trim()
								.equalsIgnoreCase("walkie torquay"))
						|| (speechtext.trim().equalsIgnoreCase("walkie"))) {
					speakOut("You are using walkie torquay App developed by team IPSR An userfriendly application for blind people");

				}

				else if ((speechtext.trim().equals("next"))) {
					speakOut("speak out inbox for reading inbox messages");

				}

				else {

					String ph=(speechtext.trim());
					Log.i("string num",ph);
					
					String str = ph.replaceAll("\\s+",""); 
					Log.i("string num after replacing",str);
					
					long phonenum = 0;
				
						phonenum = Integer.parseInt(str.trim());
						Log.i("integer num",""+phonenum);
						
						String str1 = ph.replaceAll("\\s+","_"); 
						//tts.setSpeechRate(0.6f);
		
						WebServer web=new WebServer();
						List<NameValuePair> mylist = new ArrayList<NameValuePair>(1);
						
						mylist.add(new BasicNameValuePair("busnum", ""
								+ phonenum));
					

						ja = web.doPost(
								mylist,
								"http://"
										+ ip
										+ ":8080/WalkieTalkie/webservice-user/findbus.jsp");
						
						if(ja!=null)
						{
							String location;
							
							try {
								JSONObject jo=ja.getJSONObject(0);
								
								if(jo.getString("resp").equals("success"))
								{
								
								
								String lat=jo.getString("latitude");
								String lon=jo.getString("longitude");
								
								double latitude=Double.parseDouble(lat);
								double longitude=Double.parseDouble(lon);
								
								try {
									Geocoder geocoder;
									List<Address> addresses;
									geocoder = new Geocoder(FindBus.this, Locale.getDefault());
									Log.i("LatLong",""+latitude+longitude);
									addresses = geocoder.getFromLocation(latitude, longitude, 1);
									if (addresses.size() > 0) {
										String address = addresses.get(0).getAddressLine(0);
										String city = addresses.get(0).getAddressLine(1);
										String country = addresses.get(0).getAddressLine(2);
										location = address + city;
										Toast.makeText(FindBus.this, ""+location, 5000).show();
										speakOut("Bus is now at"+location);
										
										
									} else {
										Log.i("msg", "soryy geocoder error");
										
										speakOut("Bus not Located.Please try again");
									}
									
									

								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
									speakOut("Bus not Located.Please try again");
								}
								}
								else
								{
									speakOut("Bus not Located.Please try again");
									
								}
								
							} catch (JSONException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
								speakOut("Bus not Located.Please try again");
							}
							
						}
						else
						{
							
							speakOut("Please try again");
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
