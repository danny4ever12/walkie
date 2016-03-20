package com.example.walkie;

import java.math.BigDecimal;
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
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MakeCall extends Activity implements TextToSpeech.OnInitListener{

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
	private static final String TAG_LATITUDE = "latitude";
	private static final String TAG_LONGITUDE = "longitude";
	
	
	String percentage = "",myph;// battery percentage
	Calendar c;

	private Timer timer;
	
	
	String ph;
	
	long phonenum=0;
	
	JSONArray ja;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		
		
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

		StrictMode.setThreadPolicy(policy); 
		
		
        myshare=PreferenceManager.getDefaultSharedPreferences(MakeCall.this);
		
		myedit=myshare.edit();
		
		
		
		 MyPhoneListener phoneListener = new MyPhoneListener();
	        
         TelephonyManager telephonyManager =
 
             (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
 
         // receive notifications of telephony state changes

         telephonyManager.listen(phoneListener,PhoneStateListener.LISTEN_CALL_STATE);

		
		c = Calendar.getInstance();

		/**
		 * Adding text to speech
		 */
		tts = new TextToSpeech(this, this);
		batteryLevel();
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

				String speechtext = " ";

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
					Intent myint = new Intent(this, InboxActivity.class) ;
					startActivity(myint);
					finish();

				} 
				
				/**
				 * time
				 */
				else if (speechtext.trim().equals("time")) {

					int hour = c.get(Calendar.HOUR);
					int minute = c.get(Calendar.MINUTE);

					if(Calendar.AM_PM == Calendar.AM)
					{
						speakOut("The time is " + hour + " " + minute+"A M");
						
					}
					else
					{
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

	                    	  Intent callint = new Intent(MakeCall.this, MakeCall.class);
	      					  startActivity(callint);

	                      }
	                  }, 3000);
					
					
				}
				
				/**
				 * bus route
				 */
				else if(speechtext.trim().equals("bus route"))
				{
					
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

				
				/**
				 * FIND PARTICULAR BUS
				 */
				
				else if (speechtext.trim().equals("find bus")
						|| speechtext.trim().equals("find bus ")) {

					speakOut("Speak out the bus number");	
					
					new Handler().postDelayed(new Runnable() {
	                      @Override
	                      public void run() {
	                    	  Intent findbusint=new Intent(MakeCall.this, FindBus.class);
	      					  startActivity(findbusint);
	                        
	                      }
	                  }, 3000);
					

				}
				
				
				/**
				 * help:app codes
				 */
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
					speakOut("You are using walkie torquay App developed by team CEC An userfriendly application for blind people");

				}

				else if ((speechtext.trim().equals("next"))) {
					speakOut("speak out inbox for reading inbox messages");

				}
				
				else if(speechtext.trim().equals("ok")||speechtext.trim().equals("ok "))
				{
					//Intent i = new Intent(android.content.Intent.ACTION_CALL,Uri.parse("tel:+" + phonenum)); // Make the
																// call
					Intent i = new Intent(android.content.Intent.ACTION_CALL,Uri.parse("tel:+" +myph)); // Make the
								startActivity(i);
				}

				else {
					
					String ph=(speechtext.trim());
					Log.i("string phonenum",ph);
					
					String str = ph.replaceAll("\\s+",""); 
					Log.i("string phonenum after replacing",str);
					myph=str;
					try {
						phonenum = Integer.parseInt(str);
						Log.i("integer phonenum",""+phonenum);
						
						String str1 = ph.replaceAll("\\s+","_"); 
						tts.setSpeechRate(2.0f);
						
						speakOut("The number is "+myph+" speak out okay to proceed or cancel to cancel the call");
						
						speak_function();
						
						


					} catch (Exception e) {
						// TODO: handle exception
						Log.e("hello", "" +e);
						speakOut("Didnt Catch that");
						
						//FOR DELAY
						timer = new Timer();
						timer.scheduleAtFixedRate(new TimerTask() {
						            @Override
						            public void run() {
						                //Generate number
						            }
						        }, 10000, 10000);
						
					}

					
					if((speechtext.trim()).equals("ok")||(speechtext.trim()).equals("ok "))
					{
						Intent i = new Intent(android.content.Intent.ACTION_CALL,Uri.parse("tel:+" +myph)); // Make the
															// call
							startActivity(i);
						
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

				Log.e("TTS", "This Language is not supported");
			} else {

			}

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
		calendar.set(Calendar.MONTH, month - 1);
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
	
	
	
	private class MyPhoneListener extends PhoneStateListener {
        
        
        
        private boolean onCall = false;

  

        @Override

        public void onCallStateChanged(int state, String incomingNumber) {

  

            switch (state) {

            case TelephonyManager.CALL_STATE_RINGING:

                // phone ringing...

                Toast.makeText(MakeCall.this, incomingNumber + " calls you",Toast.LENGTH_LONG).show();

                break;

             

            case TelephonyManager.CALL_STATE_OFFHOOK:

                // one call exists that is dialing, active, or on hold

                Toast.makeText(MakeCall.this, "on call...",

                        Toast.LENGTH_LONG).show();

                //because user answers the incoming call

                onCall = true;

                break;

 

            case TelephonyManager.CALL_STATE_IDLE:

                // in initialization of the class and at the end of phone call

                 

                // detect flag from CALL_STATE_OFFHOOK

                if (onCall == true) {

                    Toast.makeText(MakeCall.this, "restart app after call",Toast.LENGTH_LONG).show();

  

                    // restart our application

                    Intent restart = getBaseContext().getPackageManager().

                        getLaunchIntentForPackage(getBaseContext().getPackageName());

                    restart.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                    startActivity(restart);

  

                    onCall = false;

                }

                break;

            default:

                break;

            }

             

        }

}

}


