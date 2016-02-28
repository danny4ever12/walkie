package com.example.walkie;




import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Start our application when phone restarts
 * 
 *
 */
public class BootUpReceiver extends BroadcastReceiver{

	  @Override
      public void onReceive(Context context, Intent intent) {
              Intent i = new Intent(context, WalkieTalkie.class);  
              i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
              context.startActivity(i);  
      }

}
