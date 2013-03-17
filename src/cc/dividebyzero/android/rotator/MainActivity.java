package cc.dividebyzero.android.rotator;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings.SettingNotFoundException;
import android.util.Log;
import android.view.Surface;

public class MainActivity extends Activity {

    private static final String ACTION_ROTATE_LEFT        = "ROTATE_LEFT";
    private static final String ACTION_ROTATE_RIGHT       = "ROTATE_RIGHT";
    private static final String ACTION_EXIT               = "EXIT";
    private static final int    ID                        = 0xfCAFFE;
    private static final String LOG_TAG                   = "ROTATOR";
    NotificationManager         notificationManager;

    private static final String ORIGINAL_ROTATION_SETTING = "orig_setting";
    private static final String PREFERENCES               = "PREFERENCES";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        handleIntent(getIntent());

    }





    private void disableAutoRotate() {
        final SharedPreferences prefs = getSharedPreferences(PREFERENCES,Context.MODE_PRIVATE);
        final int deviceAutoRotate = readSystemSettings(android.provider.Settings.System.ACCELEROMETER_ROTATION,Integer.MIN_VALUE);
        prefs.edit().putInt(ORIGINAL_ROTATION_SETTING,deviceAutoRotate).commit();

        writeSystemSettings(android.provider.Settings.System.ACCELEROMETER_ROTATION,0);
    }


    private void restoreAutoRotate() {
        final SharedPreferences prefs = getSharedPreferences(PREFERENCES,Context.MODE_PRIVATE);
        final int deviceAutoRotate = prefs.getInt(ORIGINAL_ROTATION_SETTING,Integer.MIN_VALUE);
        if (deviceAutoRotate != Integer.MIN_VALUE)
        {
            writeSystemSettings(android.provider.Settings.System.ACCELEROMETER_ROTATION,deviceAutoRotate);
        }
    }


    private void handleIntent(final Intent intent) {
        final String action = intent.getAction();
        if (ACTION_ROTATE_LEFT.equals(action))
        {
            try
            {
                rotateLeft();
            } catch (SettingNotFoundException e)
            {
                Log.e(LOG_TAG,"rotateLeft failed",e);
            }
        } else if (ACTION_ROTATE_RIGHT.equals(action))
        {
            try
            {
                rotateRight();
            } catch (SettingNotFoundException e)
            {
                Log.e(LOG_TAG,"rotateRight failed",e);
            }

        } else if (ACTION_EXIT.equals(action))
        {
            restoreAutoRotate();
            removeNotification();
        } else
        {
            disableAutoRotate();
            showNotification();
        }

        finish();
    }



    private void rotateLeft() throws SettingNotFoundException {

        final int currentRotation = readSystemSettings(android.provider.Settings.System.USER_ROTATION,Integer.MIN_VALUE);
        int rotation = Surface.ROTATION_0;
        if (currentRotation == Surface.ROTATION_0)
        {
            rotation = Surface.ROTATION_270;
        } else if (currentRotation == Surface.ROTATION_90)
        {
            rotation = Surface.ROTATION_0;
        } else if (currentRotation == Surface.ROTATION_180)
        {
            rotation = Surface.ROTATION_90;
        } else if (currentRotation == Surface.ROTATION_270)
        {
            rotation = Surface.ROTATION_180;
        }

        writeSystemSettings(android.provider.Settings.System.USER_ROTATION,rotation);
    }

    private void rotateRight() throws SettingNotFoundException {

        final int currentRotation = readSystemSettings(android.provider.Settings.System.USER_ROTATION,Integer.MIN_VALUE);
        int rotation = Surface.ROTATION_0;

        if (currentRotation == Surface.ROTATION_0)
        {
            rotation = Surface.ROTATION_90;
        } else if (currentRotation == Surface.ROTATION_90)
        {
            rotation = Surface.ROTATION_180;
        } else if (currentRotation == Surface.ROTATION_180)
        {
            rotation = Surface.ROTATION_270;
        } else if (currentRotation == Surface.ROTATION_270)
        {
            rotation = Surface.ROTATION_0;
        }

        writeSystemSettings(android.provider.Settings.System.USER_ROTATION,rotation);
    }



    private void removeNotification() {
        notificationManager.cancel(ID);
    }


    private int readSystemSettings(final String which, final int defaultValue) {
        try
        {
            return android.provider.Settings.System.getInt(getContentResolver(),which);
        } catch (SettingNotFoundException e)
        {
            return defaultValue;
        }
    }


    private boolean writeSystemSettings(final String which, final int value) {
        try
        {
            return android.provider.Settings.System.putInt(getContentResolver(),which,value);
        } catch (Exception e)
        {
            return false;
        }
    }


    private void showNotification() {
        Intent rotateLeftIntent = new Intent(getApplicationContext(),MainActivity.class);
        rotateLeftIntent.setAction(ACTION_ROTATE_LEFT);

        Intent rotateRightIntent = new Intent(getApplicationContext(),MainActivity.class);
        rotateRightIntent.setAction(ACTION_ROTATE_RIGHT);

        Intent exitIntent = new Intent(getApplicationContext(),MainActivity.class);
        exitIntent.setAction(ACTION_EXIT);

        PendingIntent pi_rotateLeftIntent = PendingIntent.getActivity(this,0,rotateLeftIntent,0);
        PendingIntent pi_rotateRightIntent = PendingIntent.getActivity(this,0,rotateRightIntent,0);
        PendingIntent pi_exitIntent = PendingIntent.getActivity(this,0,exitIntent,0);

        Notification.Builder builder = new Notification.Builder(this);
        builder.setContentTitle(getString(R.string.rotator_active))
                        // Notification title
                        .setContentText("")
                        // you can put subject line.
                        .setSmallIcon(android.R.drawable.ic_dialog_info)
                        // Set your notification icon here.
                        .addAction(android.R.drawable.ic_menu_rotate,getString(R.string.rotate_left),pi_rotateLeftIntent)
                        .addAction(android.R.drawable.ic_menu_delete,getString(R.string.exit),pi_exitIntent)
                        .addAction(android.R.drawable.ic_menu_rotate,getString(R.string.rotate_right),pi_rotateRightIntent);



        // Now create the Big text notification.
        Notification notification = new Notification.BigTextStyle(builder)
                        .build();

        notification.flags |= Notification.FLAG_ONGOING_EVENT;

        notificationManager.notify(ID,notification);
    }


}
