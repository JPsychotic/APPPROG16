package com.example.mi.parkenamberg;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
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


public class ParkActivity extends FragmentActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, OnMapReadyCallback, GoogleMap.OnMarkerClickListener, ResultCallback<Status> {
    // Allgemeine Variablen
    private List<Garage> garages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_park);

        garages = new ArrayList<>();
        markers = new ArrayList<>();
        coder = new Geocoder(this);
        CreateGoogleApi();

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        provider = locationManager.GPS_PROVIDER;

        //minZeit in ms, minDistance
        if (!(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        else {
            locationManager.requestLocationUpdates(provider, locationUpdateInterval, locationUpdateDistance, locationListener);
        }

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Call GoogleApiClient connection when starting the Activity
        googleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();

        // Disconnect GoogleApiClient when stopping Activity
        googleApiClient.disconnect();
    }

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

    /**
     * Returns the garage with the id
     * @param id
     * @return
     */
    private Garage GetGarageById(int id) {
        for (Garage g: garages) {
            if(g.getId() == id) return g;
        }
        return null;
    }

    /**
     * Returns the garage with the name
     * @param name
     * @return
     */
    private Garage GetGarageByName(String name) {
        for (Garage g: garages) {
            if(g.getName().equals(name)) return g;
        }

        return null;
    }

    // Google Map methods and variables
    private GoogleMap mMap;
    private Geocoder coder;
    private LocationManager locationManager;
    private String provider;
    // Wird benötigt um sich zu speichern, für welchen Marker ein InfoWindow angezeigt wird
    private Marker marker = null;
    private List<Marker> markers;
    //Zeit in ms nach der ein Location-Update durchgeführt wird
    private final int locationUpdateInterval = 5000;
    //Distanz nach der ein location-Update durchgeführt wird
    private final int locationUpdateDistance = 1;
    //Ca. Mittelpunkt vom Ring
    private final LatLng locationAmberg = new LatLng(49.445002, 11.857240);

    /**
     * Is triggered when the Google Map is ready
     * @param googleMap
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

                geofences.add(GetGeofence(g.getLocation(), g.getId()));
            }
        }

        GeofencingRequest geofenceRequest = createGeofenceRequest(geofences);
        addGeofence(geofenceRequest);

        mMap.moveCamera(CameraUpdateFactory.newLatLng(locationAmberg));
        //Map auf Überblick über Ring zoomen
        mMap.animateCamera(CameraUpdateFactory.zoomTo(14f));

        mMap.setOnMarkerClickListener(this);
    }

    /**
     * Location Listener
     */
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

    /**
     * Returns the location of an address
     * @param strAddress the address
     * @return the location
     */
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

    /**
     * Gets the right marker-icon for the garage
     * @param g the Garage
     * @return the icon
     */
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

    /**
     * Marker Click Handler
     * @param marker
     * @return
     */
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

    /**
     * Updates marker icon and snippet
     */
    private void UpdateMarkers() {
        for (Marker m: markers) {
            Garage g = GetGarageByName(m.getTitle());
            if(g != null) {
                m.setIcon(GetIconForGarage(g));
                m.setSnippet(GetSnippetForGarage(g));
            }
        }
    }

    /**
     * Creates a snippet for a garage
     * @param g the garage
     * @return Snippet-string
     */
    private String GetSnippetForGarage(Garage g) {
        String snippet = "Freie Plätze: ";
        if(g.getMaxPlaetze() > 0) snippet += (g.getMaxPlaetze() - g.getCurPlaetze());
        else snippet += "Keine Information";

        return snippet;
    }

    // XML Reading methods and variables

    /**
     * Updates the max numbers of parking lots for every Garage
     */
    private void UpdateMaxPlaetze() {
        UpdatePlaetze(true);
    }

    /**
     * Updates the current number of parking lots for every parking lot
     */
    private void UpdateCurPlaetze() {
        UpdatePlaetze(false);
    }

    /**
     * Updates parking lot numbers
     * @param gesamt true if you want to update the max parking lots, false for the current
     */
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

    // Geofence methods & variables
    private List<Geofence> geofences;
    private PendingIntent geoFencePendingIntent;
    private final int GEOFENCE_REQ_CODE = 0;
    private final float GEOFENCE_RADIUS = 500.0f;

    /**
     * This method creates a Geofence
     * @param position the position of the Geofence
     * @return the Geofence
     */
    private Geofence GetGeofence(LatLng position, int id) {
        String strid = "Geofence" + id;
        return new Geofence.Builder()
                .setRequestId(strid)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                .setCircularRegion(position.latitude, position.longitude, GEOFENCE_RADIUS)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .build();
    }

    /**
     * Creates aGeofenceRequest
     * @param geofences
     * @return the GeofencingRequest
     */
    private GeofencingRequest createGeofenceRequest(List<Geofence> geofences) {
        Log.d("Geofence", "createGeofenceRequest()");
        return new GeofencingRequest.Builder()
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .addGeofences(geofences)
                .build();
    }

    /**
     * Creates a Geofence Pending Intent
     * @return the Pending Intent
     */
    private PendingIntent createGeofencePendingIntent() {
        Log.d("Geofence", "createGeofencePendingIntent()");
        if ( geoFencePendingIntent != null )
            return geoFencePendingIntent;

        Intent intent = new Intent( this, GeofenceTransitionService.class);
        return PendingIntent.getService(
                this, GEOFENCE_REQ_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT );
    }

    /**
     * Add the created GeofenceRequest to the device's monitoring list
     * @param request the GeofenceRequest
     */
    private void addGeofence(GeofencingRequest request) {
        Log.d("Geofence", "addGeofence");
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
            LocationServices.GeofencingApi.addGeofences(
                    googleApiClient,
                    request,
                    createGeofencePendingIntent()
            ).setResultCallback(this);
    }

    /**
     * Result of addGeofence
     * @param status
     */
    @Override
    public void onResult(@NonNull Status status) {
        Log.d("Geofence", "addGeofence" + status.toString());
    }

    // Google API methods & variables
    private GoogleApiClient googleApiClient;

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d("GoogleAPI", "onConnected()");
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d("GoogleAPI", "onConnectionSuspended()");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d("GoogleAPI", "onConnectionFailed()");
    }

    /**
     * creates the Google API-Client
     */
    private void CreateGoogleApi() {
        Log.d("GoogleAPI", "createGoogleApi()");
        if ( googleApiClient == null ) {
            googleApiClient = new GoogleApiClient.Builder( this )
                    .addConnectionCallbacks( this )
                    .addOnConnectionFailedListener( this )
                    .addApi( LocationServices.API )
                    .build();
        }
    }
}
