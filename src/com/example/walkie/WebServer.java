package com.example.walkie;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;

import android.os.StrictMode;
import android.util.Log;

public class WebServer {
	public JSONArray doPost(List<NameValuePair> namevaluepair, String url) {
		JSONArray myjsonarray = null;

		try {

			StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
					.permitAll().build();

			StrictMode.setThreadPolicy(policy);
			HttpClient myclient = new DefaultHttpClient();
			HttpPost newhttppost = new HttpPost(url);
			newhttppost.setEntity(new UrlEncodedFormEntity(namevaluepair));
			HttpResponse myresponse = myclient.execute(newhttppost);
			Log.i("post response", "" + myresponse);
			String line = "";
			StringBuilder total = new StringBuilder();
			BufferedReader br = new BufferedReader(new InputStreamReader(
					myresponse.getEntity().getContent()));

			while ((line = br.readLine()) != null) {
				total.append(line);
			}

			myjsonarray = new JSONArray(total.toString());
			Log.i("array", "" + myjsonarray);
			// Log.i("msg", "hai");
		} catch (Exception e) {
			// Log.i("e",""+e);
			e.printStackTrace();
		}
		return myjsonarray;

	}

}
