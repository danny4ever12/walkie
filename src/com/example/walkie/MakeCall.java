package com.example.walkie;

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
import android.net.Uri;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

public class MakeCall extends Activity implements TextToSpeech.OnInitListener,IpAddress {

	protected static final int RESULT_SPEECH = 1;// Speech code for activity
	private TextToSpeech tts;
	boolean speechflag = false;

	String percentage = "",myph;// battery percentage
	Calendar c;

	private Timer timer;
	
	private ListView wordsList;
	String ph;
	
	long phonenum=0;
	
	JSONArray ja;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
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
				} else if ((speechtext.trim().equals("my inbox"))
						|| (speechtext.trim().equals("inbox"))) {
					Intent myint = new Intent(this, InboxActivity.class) ;
					startActivity(myint);
					finish();

				} else if (speechtext.trim().equals("time")) {

					int hour = c.get(Calendar.HOUR);
					int minute = c.get(Calendar.MINUTE);

					if(Calendar.AM_PM==Calendar.AM)
					{
						speakOut("The time is " + hour + " " + minute+"A M");
						
					}
					else
					{
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
					        }, 10000, 10000);
					
					Intent callint = new Intent(MakeCall.this, MakeCall.class);
					startActivity(callint);

				}
				else if(speechtext.trim().equals("bus route"))
				{
					//call webservice
					
					WebServer web=new WebServer();
					
					List<NameValuePair> mylist = new ArrayList<NameValuePair>(2);
					mylist.add(new BasicNameValuePair("latitude", "" + GpsLocation.lat));
					mylist.add(new BasicNameValuePair("longitude", "" + GpsLocation.log));

					ja=web.doPost(mylist, "http://"+ip+":8084/Walkie_Talkie/getbusdetails.jsp");
					if(ja!=null)
					{
						
						try {

							JSONObject jo=ja.getJSONObject(0);
							
							if(jo.getString("resp").equals("success"))
							{
							
							String name=jo.getString("vehicle_name");
							String source=jo.getString("source");
							String destination=jo.getString("destination");
							tts.setSpeechRate(0.8f);
							
							speakOut("Bus name is"+name+"going to "+destination);
							
							}
							else
							{
								speakOut("No nearby bus");	
							}
							
							
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
						
						
					}
					else
					{
						speakOut("No nearby bus");
					}
	
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
					speakOut("You are using walkie torquay App developed by team ipsr An userfriendly application for blind people");

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
						
						speakOut("The number is"+myph+"speak out okay to proceed or cancel to cancel the call");
						
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


