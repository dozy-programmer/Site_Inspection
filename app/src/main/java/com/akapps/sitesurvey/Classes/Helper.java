package com.akapps.sitesurvey.Classes;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.akapps.sitesurvey.R;
import com.google.android.material.snackbar.Snackbar;
import java.io.File;
import java.util.Random;

public class Helper {

    /**
     * Determines current orientation of device
     *
     * @param context   The application context.
     * @return true if phone is in landscape
     */
    public static boolean getOrientation(Context context){
        int orientation = context.getResources().getConfiguration().orientation;
        return orientation == Configuration.ORIENTATION_LANDSCAPE;
    }

    /**
     * Sets orientation of device
     *
     * @param activity The current activity.
     */
    @SuppressLint("SourceLockedOrientationActivity")
    public static void setOrientation(Activity activity, String desiredOrientation){
        if(desiredOrientation.equals("Portrait"))
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        else  if(desiredOrientation.equals("Landscape"))
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        else
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
    }

    /**
     * Shows the user a message
     *
     * @param view   The current view
     */
    public static void showUserMessage(View view, String message, int duration){
        Snackbar.make(view, message, duration)
                .setAction("Action", null).show();
    }

    /**
     * Determines if recyclerview is empty and displays empty view
     *
     * @param size   The size of recyclerview
     * @param empty_Layout   This contains an image
     * @param app_background  This is the app background, consists of ImageView and TextView
     * @param empty_Text   This TextView is empty
     */
    public static void isListEmpty(int size, LinearLayout empty_Layout,
                                   LinearLayout app_background, TextView empty_Text){
        final int randomText = new Random().nextInt(6);
        if (size == 0) {
            empty_Layout.setVisibility(View.VISIBLE);
            if(app_background != null)
                app_background.setVisibility(View.INVISIBLE);
        }
        else {
            empty_Layout.setVisibility(View.INVISIBLE);
            if(app_background != null)
                app_background.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Deletes cache directory to free up space
     *
     * @param context   The application context.
     */
    public static void trimCache(Context context) {
        try {
            File dir = context.getCacheDir();
            if (dir != null && dir.isDirectory()) {
                deleteDir(dir);
            }
        } catch (Exception ignored) {}
    }

    public static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (String child : children) {
                boolean success = deleteDir(new File(dir, child));
                if (!success) {
                    return false;
                }
            }
        }

        return dir.delete();
    }

    // if address portion of dialog is empty or not filled correctly, return false
    public static boolean isDialogAddressEmpty(String address, String street, String city, String zipcode){
        try{
            if (address.length()>0 && street.length()>0 && city.length()>0 && zipcode.length()>0)
                return false;
        }catch (Exception e){
            return true;
        }
        return true;
    }

}
