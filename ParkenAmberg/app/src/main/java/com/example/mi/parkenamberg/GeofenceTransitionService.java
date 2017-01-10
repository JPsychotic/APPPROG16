package com.example.mi.parkenamberg;

import android.app.IntentService;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofenceStatusCodes;
import com.google.android.gms.location.GeofencingEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jeremy on 07.12.2016.
 */

/**
 * Class which receives the geofence notifications
 */
public class GeofenceTransitionService extends IntentService {

    /**
     * Tag for Console Outputs
     */
    private static final String TAG = GeofenceTransitionService.class.getSimpleName();
    /**
     * String to identify the responses which this class sends
     */
    public static final String GEOFENCE_SERVICE_ID = "Geofence_Transition_Service";
    /**
     * Handler for the main activity
     */
    private Handler mainHandle;

    /**
     * Constructor
     */
    public GeofenceTransitionService() {
        super(TAG);
        mainHandle = new Handler(Looper.getMainLooper());
    }

    /**
     * Triggered when the geofence notification is received
     * @param intent
     */
    @Override
    protected void onHandleIntent(Intent intent) {
        // Retrieve the Geofencing intent
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);

        // Handling errors
        if ( geofencingEvent.hasError() ) {
            String errorMsg = getErrorString(geofencingEvent.getErrorCode() );
            Log.e( TAG, errorMsg );
            return;
        }

        // Retrieve GeofenceTrasition
        int geoFenceTransition = geofencingEvent.getGeofenceTransition();
        // Check if the transition type
        if (geoFenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
            // Get the geofence that were triggered
            List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();

            //Create String with ids
            String geofenceIds = "enter;";
            for (Geofence g: triggeringGeofences) {
                String strid = g.getRequestId();
                geofenceIds += strid.replace("Geofence", "") + ";";
            }
            //Delete the last ;
            geofenceIds = geofenceIds.substring(0, geofenceIds.length() - 1);

            //send the ids to the activity
            Intent broadcastIntent = new Intent();
            broadcastIntent.setAction(ParkActivity.GeofenceResponseReceiver.GEOFENCE_RESPONSE);
            broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
            broadcastIntent.putExtra(GEOFENCE_SERVICE_ID, geofenceIds);
            sendBroadcast(broadcastIntent);
        }
        else if(geoFenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {
            // Get the geofence that were triggered
            List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();

            //Create String with ids
            String geofenceIds = "exit;";
            for (Geofence g: triggeringGeofences) {
                String strid = g.getRequestId();
                geofenceIds += strid.replace("Geofence", "") + ";";
            }
            //Delete the last ;
            geofenceIds = geofenceIds.substring(0, geofenceIds.length() - 1);

            //send the ids to the activity
            Intent broadcastIntent = new Intent();
            broadcastIntent.setAction(ParkActivity.GeofenceResponseReceiver.GEOFENCE_RESPONSE);
            broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
            broadcastIntent.putExtra(GEOFENCE_SERVICE_ID, geofenceIds);
            sendBroadcast(broadcastIntent);
        }
    }

    /**
     * Handles the errors of this class
     * @param errorCode
     * @return the error message
     */
    private static String getErrorString(int errorCode) {
        switch (errorCode) {
            case GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE:
                return "GeoFence not available";
            case GeofenceStatusCodes.GEOFENCE_TOO_MANY_GEOFENCES:
                return "Too many GeoFences";
            case GeofenceStatusCodes.GEOFENCE_TOO_MANY_PENDING_INTENTS:
                return "Too many pending intents";
            default:
                return "Unknown error.";
        }
    }
}
