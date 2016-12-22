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

public class GarageInfoWindow implements GoogleMap.InfoWindowAdapter {
    private Context context;
    private final View markerview;
    private GarageManager garageManager;

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

    private void render(Marker marker) {
        TextView title = (TextView) markerview.findViewById(R.id.title);
        TextView snippet = (TextView) markerview.findViewById(R.id.snippet);

        Garage g = garageManager.GetGarageByName(marker.getTitle());

        title.setText(g.getName());
        snippet.setText(marker.getSnippet());
    }

}
