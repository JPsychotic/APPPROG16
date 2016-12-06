package com.example.mi.parkenamberg;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;


public class ParkActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private GoogleMap mMap;
    private List<Garage> garages;
    //Ca. Mittelpunkt vom Ring
    private final LatLng locationAmberg = new LatLng(49.445002, 11.857240);;
    private Geocoder coder;
    private LocationManager locationManager;
    private String provider;
    // Wird benötigt um sich zu speichern, für welchen Marker ein InfoWindow angezeigt wird
    private Marker marker = null;
    private List<Marker> markers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_park);

        //Check if GPS Permissions are set
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        garages = new ArrayList<>();
        markers = new ArrayList<>();
        coder = new Geocoder(this);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        provider = locationManager.GPS_PROVIDER;

        //minZeit in ms, minDistance
        locationManager.requestLocationUpdates(provider, 5000, 1, locationListener);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        CreateGarageList();
        UpdateMaxPlaetze();
        UpdateCurPlaetze();

        // Add a marker for every garage
        for (Garage g: garages) {
            if(g.getLocation() != null) {
                markers.add(mMap.addMarker(new MarkerOptions().position(g.getLocation()).title(g.getName()).icon(GetIconForGarage(g)).snippet(GetSnippetForGarage(g))));
            }
        }

        mMap.moveCamera(CameraUpdateFactory.newLatLng(locationAmberg));
        //Map auf Überblick über Ring zoomen
        mMap.animateCamera(CameraUpdateFactory.zoomTo(14f));

        mMap.setOnMarkerClickListener(this);
    }

    LocationListener locationListener = new LocationListener() {
        public void onLocationChanged(Location location) {

        }
        public void onProviderDisabled(String provider) {
        }
        public void onProviderEnabled(String provider) {
        }
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    };

    private void CreateGarageList() {
        garages.add(new Garage(GetLocationFromAddress(getString(R.string.address0)), getString(R.string.garage0) , 1));
        garages.add(new Garage(GetLocationFromAddress(getString(R.string.address1)), getString(R.string.garage1) , 2));
        garages.add(new Garage(GetLocationFromAddress(getString(R.string.address2)), getString(R.string.garage2) , 3));
        garages.add(new Garage(GetLocationFromAddress(getString(R.string.address3)), getString(R.string.garage3) , 4));
        garages.add(new Garage(GetLocationFromAddress(getString(R.string.address4)), getString(R.string.garage4) , 5));
        garages.add(new Garage(GetLocationFromAddress(getString(R.string.address5)), getString(R.string.garage5) , 6));
        garages.add(new Garage(GetLocationFromAddress(getString(R.string.address6)), getString(R.string.garage6) , 7));
        garages.add(new Garage(GetLocationFromAddress(getString(R.string.address7)), getString(R.string.garage7) , 8));
    }

    private LatLng GetLocationFromAddress(String strAddress) {
        if(coder == null) {
            return null;
        }

        List<Address> address;
        LatLng l;

        try {
            address = coder.getFromLocationName(strAddress, 1);
            if (address == null) {
                return null;
            }
            Address location = address.get(0);

            l = new LatLng(location.getLatitude(), location.getLongitude());
        }
        catch (Exception e) {
            return null;
        }

        return l;
    }

    private void UpdateMaxPlaetze() {
        UpdatePlaetze(true);
    }

    private void UpdateCurPlaetze() {
        UpdatePlaetze(false);
    }

    private void UpdatePlaetze(boolean gesamt) {
        try {
            URL url = new URL("http://parken.amberg.de/wp-content/uploads/pls/pls.xml");
            URLConnection conn = url.openConnection();

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(conn.getInputStream());

            NodeList nodes = doc.getElementsByTagName("Parkhaus");
            for (int i = 0; i < nodes.getLength(); i++) {
                Element element = (Element) nodes.item(i);
                NodeList idNodes = element.getElementsByTagName("ID");
                if(idNodes.getLength() > 0) {
                    int id = Integer.parseInt(idNodes.item(0).getNodeValue());
                    NodeList platzNodes;
                    if(gesamt) {
                        platzNodes = element.getElementsByTagName("Gesamt");
                    }
                    else {
                        platzNodes = element.getElementsByTagName("Aktuell");
                    }

                    if(platzNodes.getLength() > 0) {
                        Element e = (Element) platzNodes.item(0);
                        if(gesamt) {
                            GetGarageById(id).setCurPlaetze(Integer.parseInt(e.getNodeValue()));
                        }
                        else {
                            GetGarageById(id).setMaxPlaetze(Integer.parseInt(e.getNodeValue()));
                        }
                    }
                }


            }
        }
        catch (Exception e) {
            //evtl. hier, wenn kein Internet?
            e.printStackTrace();
        }
    }

    private Garage GetGarageById(int id) {
        for (Garage g: garages) {
            if(g.getId() == id) return g;
        }
        return null;
    }

    private BitmapDescriptor GetIconForGarage(Garage g) {
        BitmapDescriptor icon;

        if(g.getMaxPlaetze() <= 0) {
            icon = BitmapDescriptorFactory.fromResource(R.drawable.garage);
        }
        else {
            double besetzt = g.getCurPlaetze() / g.getMaxPlaetze();
            if(besetzt < 0.9f) icon = BitmapDescriptorFactory.fromResource(R.drawable.garage_free);
            else if(besetzt < 1.0f) icon = BitmapDescriptorFactory.fromResource(R.drawable.garage_fast_voll);
            else if(besetzt == 1.0) icon = BitmapDescriptorFactory.fromResource(R.drawable.garage_voll);
            else icon = BitmapDescriptorFactory.fromResource(R.drawable.garage);
        }

        return icon;
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        if(this.marker != null && this.marker.equals(marker)) {
            marker.hideInfoWindow();
            this.marker = null;
        }
        else {
            marker.showInfoWindow();
            this.marker = marker;
        }
        return true;
    }

    private void UpdateMarkers() {
        for (Marker m: markers) {
            Garage g = GetGarageByName(m.getTitle());
            if(g != null) {
                m.setIcon(GetIconForGarage(g));
                m.setSnippet(GetSnippetForGarage(g));
            }
        }
    }

    private Garage GetGarageByName(String name) {
        for (Garage g: garages) {
            if(g.getName().equals(name)) return g;
        }

        return null;
    }

    private String GetSnippetForGarage(Garage g) {
        String snippet = "Freie Plätze: ";
        if(g.getMaxPlaetze() > 0) snippet += (g.getMaxPlaetze() - g.getCurPlaetze());
        else snippet += "Keine Information";

        return snippet;
    }
}
