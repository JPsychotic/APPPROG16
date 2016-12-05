package com.example.mi.parkenamberg;

import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;

public class ParkActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private List<Parkhaus> parkhaeuser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_park);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        CreateParkhausList();
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(49.443730, 11.847551);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(sydney, 12.0f)); //Test Zoom
    }

    private void CreateParkhausList() {
        parkhaeuser.add(new Parkhaus(0, 0, "Parkgarage am Ziegeltor"     , 257));
        parkhaeuser.add(new Parkhaus(0, 0, "Parkgarage am Kurfürstenbad" , 242));
        parkhaeuser.add(new Parkhaus(0, 0, "Tiefgarage am Bahnhof"       , 191));
        parkhaeuser.add(new Parkhaus(0, 0, "Theatergarage"               , 112));
        parkhaeuser.add(new Parkhaus(0, 0, "Parkdeck Kräuterwiese"       , 285));
        parkhaeuser.add(new Parkhaus(0, 0, "Parkdeck Marienstraße"       , 372));
        parkhaeuser.add(new Parkhaus(0, 0, "Außenparkplätze Marienstraße", 219));
        parkhaeuser.add(new Parkhaus(0, 0, "Parkplatz Schießstätteweg"   , 388));
    }
}
