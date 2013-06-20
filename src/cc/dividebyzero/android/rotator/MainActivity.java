package cc.dividebyzero.android.rotator;

import android.app.Activity;


import android.app.Notification;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Builder;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings.SettingNotFoundException;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Surface;
import android.view.Window;
import android.widget.ImageView;

public class MainActivity extends Activity {

    private static final String ACTION_ROTATE_LEFT        = "ROTATE_LEFT";
    private static final String ACTION_ROTATE_RIGHT       = "ROTATE_RIGHT";
    private static final String ACTION_EXIT               = "EXIT";
    private static final int    ID                        = 0xfCAFFE;
    private static final String LOG_TAG                   = "ROTATOR";
    private static final String ACTION_SHOW               = "SHOW_ACTIVITY";
    NotificationManager notificationManager;

    private static final String ORIGINAL_ROTATION_SETTING = "orig_setting";
    private static final String PREFERENCES               = "PREFERENCES";
    private ImageView ivLeft;
    private ImageView ivRight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.main);

        ivLeft  = (ImageView)findViewById(R.id.iv_left);
        ivRight = (ImageView)findViewById(R.id.iv_right);
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
            finish();

        } else if (ACTION_ROTATE_RIGHT.equals(action))
        {
            try
            {
                rotateRight();
            } catch (SettingNotFoundException e)
            {
                Log.e(LOG_TAG,"rotateRight failed",e);
            }

            finish();

        } else if (ACTION_EXIT.equals(action))
        {
            restoreAutoRotate();
            removeNotification();
            finish();

        } else if (ACTION_SHOW.equals(action))
        {
            //essentially, do nothing.
        } else
        {
            disableAutoRotate();
            showNotification();
            finish();
        }


    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if(KeyEvent.KEYCODE_DPAD_LEFT==event.getKeyCode()){

            ivLeft.setImageResource(R.drawable.dpad_left_pressed);
            try
            {
                
                rotateLeft();
            } catch (SettingNotFoundException e)
            {
                Log.e(LOG_TAG,"rotateLeft failed",e);
            }

            return true;
        }else if(KeyEvent.KEYCODE_DPAD_RIGHT==event.getKeyCode()){
            ivRight.setImageResource(R.drawable.dpad_right_pressed);
            try
            {
                rotateRight();
            } catch (SettingNotFoundException e)
            {
                Log.e(LOG_TAG,"rotateRight failed",e);
            }

            return true;
        }

        return super.onKeyDown(keyCode, event);
    }


    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {

        if(KeyEvent.KEYCODE_DPAD_LEFT==event.getKeyCode()){
            ivLeft.setImageResource(R.drawable.dpad_left);
            return true;
        }else if(KeyEvent.KEYCODE_DPAD_RIGHT==event.getKeyCode()){
            ivRight.setImageResource(R.drawable.dpad_right);
            return true;
        }

        return super.onKeyDown(keyCode, event);
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

        Intent showIntent = new Intent(getApplicationContext(),MainActivity.class);
        showIntent.setAction(ACTION_SHOW);

        PendingIntent pi_rotateLeftIntent = PendingIntent.getActivity(this,0,rotateLeftIntent,0);
        PendingIntent pi_rotateRightIntent = PendingIntent.getActivity(this,0,rotateRightIntent,0);
        PendingIntent pi_exitIntent = PendingIntent.getActivity(this,0,exitIntent,0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setContentTitle(getString(R.string.rotator_active))
                        // Notification title
                        .setContentText("")
                        // you can put subject line.
                        .setSmallIcon(android.R.drawable.ic_dialog_info)
                        // Set your notification icon here.
                        .addAction(android.R.drawable.ic_menu_rotate,getString(R.string.rotate_left),pi_rotateLeftIntent)
                        .addAction(android.R.drawable.ic_menu_delete,getString(R.string.exit),pi_exitIntent)
                        .addAction(android.R.drawable.ic_menu_rotate,getString(R.string.rotate_right),pi_rotateRightIntent);




        builder.setContentIntent(PendingIntent.getActivity(this,0,showIntent,0));
        // Now create the Big text notification.
        Notification notification = new NotificationCompat.BigTextStyle(builder)
                        .build();

        notification.flags |= Notification.FLAG_ONGOING_EVENT;



        notificationManager.notify(ID,notification);
    }


}
