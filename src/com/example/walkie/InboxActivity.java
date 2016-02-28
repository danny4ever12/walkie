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
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Contacts;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

@SuppressWarnings("deprecation")
public class InboxActivity extends Activity implements
		TextToSpeech.OnInitListener,IpAddress {
	/**
	 * Text to speech variables
	 */
	private TextToSpeech tts;
	boolean speechflag = false;
	protected static final int RESULT_SPEECH = 1;// Speech code for activity

	
	private Timer timer;
	
	/**
	 * Inbox message variables
	 */
	ContentResolver contentResolver;
	Uri uri = Uri.parse("content://sms");
	String content, number, type, SmsMessageId;
	String percentage = "";// battery percentage
	Calendar c;
	
	
	JSONArray ja;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		contentResolver = getContentResolver();
		tts = new TextToSpeech(this, this);
		tts.setSpeechRate(0.6f);
		c = Calendar.getInstance();
		
		/**
		 * GPS location Updating code
		 */
		LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		LocationListener ll = new GpsLocation(InboxActivity.this);
		lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 0, ll);
		lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 10000, 0,
				ll);
		batteryLevel();
	}

	/**
	 * Speak button click Check if already speaking then close tts
	 * 
	 * @param v
	 */
	public void speakButtonClicked(View v) {

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
				speechtext = speechtext + " "+text.get(0) + " ";
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
				 * Inbox-go to Inbox Activity
				 * 
				 */

				else if ((speechtext.trim().equals("my inbox"))
						|| (speechtext.trim().equals("inbox"))) {
					Intent myint = new Intent(this, InboxActivity.class);
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
				}else if(speechtext.trim().equals("bus route"))
				{
					
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
							tts.setSpeechRate(0.6f);
							
							speakOut("Bus name is"+name+"going to "+destination+" from "+source);
							
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
				
				
				
				
				
				
				else if (speechtext.trim().equals("date")) {

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
					
					Intent callint = new Intent(InboxActivity.this,
							MakeCall.class);
					startActivity(callint);

				} else if (speechtext.trim().equals("help")) {

					speakOut("the keywords used are my battery make a call mylocation  my inbox  my contacts  find bus and  bus route");

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
					speakOut("You are using walkie torquay App developed by team Adhoc An userfriendly application for blind people");
				} else if (speechtext.trim().equalsIgnoreCase("next")) {

					getUnreadInboxMessages();

				} else {

					speakOut("Didn't Catch that Try again");
				}

			}

			break;
		}

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
		Toast.makeText(this, text, Toast.LENGTH_LONG).show();
	}

	/**
	 * get Unread messages
	 */

	public void getUnreadInboxMessages() {
		Log.i("inside", "msg");
		// Cursor cursor = contentResolver
		// .query(uri, null, "read = 0", null, null);

		Uri uri = Uri.parse("content://sms/inbox");
		Cursor cursor = (InboxActivity.this).getContentResolver().query(uri,
				null, "read=0", null, null);
		int totlamessage = cursor.getCount();

		if (totlamessage > 0) {
			if (cursor.moveToNext()) {

				content = cursor.getString(cursor.getColumnIndex("body"));
				number = cursor.getString(cursor.getColumnIndex("address"));
				type = cursor.getString(cursor.getColumnIndex("type"));
				SmsMessageId = cursor.getString(cursor.getColumnIndex("_id"));
				ContentValues values = new ContentValues();
				values.put("read", true);

				/**
				 * Inbox
				 */
				if (type.equals("1")) {

					String name = getContactName(number);
					if (name.equals("")) {
						name = number;
					}

					if (isNumber(name)) {
						name.replace("", " ").trim();
					}
					String speaktext = "Total " + totlamessage
							+ " unread messages. And the first message is from"
							+ name +".  "+ " Content is" + content;
					speakOut(speaktext);
					contentResolver.update(Uri.parse("content://sms/inbox"),
							values, "_id=" + SmsMessageId, null);
				}
				/**
				 * Outbox
				 */

				else if (type.equals("2")) {

				}
			}
		}

		else {

			speakOut("No unread Messages Now");
		}

		cursor.close();
	}

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

			getUnreadInboxMessages();
		} else {
			Log.e("TTS", "Initialization Failed!");
		}
	}

	private boolean isNumber(String word) {
		boolean isNumber = false;
		try {
			Integer.parseInt(word);
			isNumber = true;

		} catch (NumberFormatException e) {
			isNumber = false;
		}
		return isNumber;
	}

	/**
	 * Get Contact name
	 * 
	 * @param phoneNumber
	 * @return
	 */
	
	public String getContactName(final String phoneNumber) {
		Uri uri;
		String[] projection;
		Uri mBaseUri = Contacts.Phones.CONTENT_FILTER_URL;
		projection = new String[] { android.provider.Contacts.People.NAME };
		try {
			Class<?> c = Class
					.forName("android.provider.ContactsContract$PhoneLookup");
			mBaseUri = (Uri) c.getField("CONTENT_FILTER_URI").get(mBaseUri);
			projection = new String[] { "display_name" };
		} catch (Exception e) {
		}

		uri = Uri.withAppendedPath(mBaseUri, Uri.encode(phoneNumber));
		Cursor cursor = this.getContentResolver().query(uri, projection, null,
				null, null);

		String contactName = "";

		if (cursor.moveToFirst()) {
			contactName = cursor.getString(0);
		}

		cursor.close();
		cursor = null;

		return contactName;
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
}
