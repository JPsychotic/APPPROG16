package com.example.mi.parkenamberg;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

import java.util.List;

/**
 * Created by Jeremy on 22.12.2016.
 */

/**
 * Custom InfoWindow for the map
 */
public class GarageInfoWindow implements GoogleMap.InfoWindowAdapter {
    /**
     * Context
     */
    private Context context;
    /**
     * the used view
     */
    private final View markerview;
    /**
     * GarageManager
     */
    private GarageManager garageManager;

    /**
     * Constructor
     * @param c context
     * @param gm GarageManager
     */
    public GarageInfoWindow(Context c, GarageManager gm) {
        garageManager = gm;
        context = c;

        LayoutInflater li = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        markerview = li.inflate(R.layout.markerinfowindow, null);
    }

    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    @Override
    public View getInfoContents(Marker marker) {
        render(marker);
        return markerview;
    }

    /**
     * Called when the view gets rendered
     * @param marker the marker for this info window
     */
    private void render(Marker marker) {
        TextView title = (TextView) markerview.findViewById(R.id.title);
        TextView snippet = (TextView) markerview.findViewById(R.id.snippet);

        Garage g = garageManager.GetGarageByName(marker.getTitle());

        //Set Snippet and Title
        title.setText(g.getName());
        snippet.setText(marker.getSnippet());
    }

}
