package com.example.mi.parkenamberg;

import android.Manifest;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.Toast;

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
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ParkActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
  GoogleApiClient.OnConnectionFailedListener, OnMapReadyCallback, ResultCallback<Status>
{
  private boolean sprachausgabe = true;

  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_park);

    garageManager = new GarageManager(this);

    markers = new ArrayList<>();
    geofences = new ArrayList<>();
    CreateGoogleApi();

    locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
    provider = LocationManager.GPS_PROVIDER;

    //minZeit in ms, minDistance
    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
      && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)
    {
      locationManager.requestLocationUpdates(provider, locationUpdateInterval, locationUpdateDistance, locationListener);
    }
    else
    {
      ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 0);
    }

    //Initialize the TextToSpeech API
    resultSpeaker = new TextToSpeech(this, this.TTSOnInitListener);

    //Start BroadcastReceiver
    IntentFilter filter = new IntentFilter(GeofenceResponseReceiver.GEOFENCE_RESPONSE);
    filter.addCategory(Intent.CATEGORY_DEFAULT);
    receiver = new GeofenceResponseReceiver();
    registerReceiver(receiver, filter);

    // Obtain the SupportMapFragment and get notified when the map is ready to be used.
    SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
    mapFragment.getMapAsync(this);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.menu, menu);

    return super.onCreateOptionsMenu(menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.maptype:
      {
        if(mMap.getMapType() == GoogleMap.MAP_TYPE_NORMAL)
          mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        else
          mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        break;
      }
      {
        openFavoriteMenu();
        break;
      }
      case R.id.settings:
      {
        openSettingsMenu();
        break;
      }
      default:
        break;
    }
    return super.onOptionsItemSelected(item);
  }

  private void openFavoriteMenu()
  {
    final Dialog dialog = new Dialog(ParkActivity.this);
    dialog.setContentView(R.layout.favorite_dialog);
    dialog.setTitle(R.string.FavoritenMenuTitle);
    dialog.setCancelable(true);

    //set up button
    Button button = (Button) dialog.findViewById(R.id.favosCloseButton);
    button.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        dialog.dismiss();
      }
    });

    Switch sw1 = (Switch) dialog.findViewById(R.id.parkhaus1Switch);
    sw1.setChecked(garageManager.GetGarageById(1).getShow());
    Switch sw2 = (Switch) dialog.findViewById(R.id.parkhaus2Switch);
    sw2.setChecked(garageManager.GetGarageById(2).getShow());
    Switch sw3 = (Switch) dialog.findViewById(R.id.parkhaus3Switch);
    sw3.setChecked(garageManager.GetGarageById(3).getShow());
    Switch sw4 = (Switch) dialog.findViewById(R.id.parkhaus4Switch);
    sw4.setChecked(garageManager.GetGarageById(4).getShow());
    Switch sw5 = (Switch) dialog.findViewById(R.id.parkhaus5Switch);
    sw5.setChecked(garageManager.GetGarageById(5).getShow());
    Switch sw6 = (Switch) dialog.findViewById(R.id.parkhaus6Switch);
    sw6.setChecked(garageManager.GetGarageById(6).getShow());
    Switch sw7 = (Switch) dialog.findViewById(R.id.parkhaus7Switch);
    sw7.setChecked(garageManager.GetGarageById(7).getShow());
    Switch sw8 = (Switch) dialog.findViewById(R.id.parkhaus8Switch);
    sw8.setChecked(garageManager.GetGarageById(8).getShow());

    CompoundButton.OnCheckedChangeListener handler = new CompoundButton.OnCheckedChangeListener()
    {
      @Override
      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
      {
        String tag = buttonView.getTag().toString();
        Toast.makeText(getApplicationContext(), "Parkhaus "+tag+" als Favorit " + (isChecked ? "hinzugefügt" : "entfernt"), Toast.LENGTH_SHORT).show();
        // do something, the isChecked will be
        // true if the switch is in the On position

        Garage g = garageManager.GetGarageById(Integer.parseInt(tag));
        g.setShow(isChecked);

        //Marker entfernen bzw. hinzufügen
        if(isChecked)
        {
          markers.add(mMap.addMarker(new MarkerOptions().position(g.getLocation()).title(g.getName()).icon(GetIconForGarage(g)).snippet(GetSnippetForGarage(g))));
        }
        else
        {
          for(Marker m: markers)
          {
            if(m.getTitle().equals(g.getName())) {
              markers.remove(m);
              m.remove();
              break;
            }
          }
        }
      }
    };

    sw1.setOnCheckedChangeListener(handler);
    sw2.setOnCheckedChangeListener(handler);
    sw3.setOnCheckedChangeListener(handler);
    sw4.setOnCheckedChangeListener(handler);
    sw5.setOnCheckedChangeListener(handler);
    sw6.setOnCheckedChangeListener(handler);
    sw7.setOnCheckedChangeListener(handler);
    sw8.setOnCheckedChangeListener(handler);

    dialog.show();
  }

  private void openSettingsMenu()
  {
    final Dialog dialog = new Dialog(ParkActivity.this);
    dialog.setContentView(R.layout.setting_dialog);
    dialog.setTitle(R.string.settingsTitle);
    dialog.setCancelable(true);

    //set up button
    Button button = (Button) dialog.findViewById(R.id.settingsCloseButton);
    button.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        dialog.dismiss();
      }
    });

    RadioGroup radioGroup = (RadioGroup) dialog.findViewById(R.id.radio_buttons1);
    radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
    {
      public void onCheckedChanged(RadioGroup group, int checkedId) {
        // checkedId is the RadioButton selected
        // do something useful
        View radioButton = group.findViewById(checkedId);
        int idx = group.indexOfChild(radioButton);
        Toast.makeText(getApplicationContext(), "option selected: " + idx, Toast.LENGTH_SHORT).show();
      }
    });

    Switch sw = (Switch) dialog.findViewById(R.id.sprachausgabeSwitch);
    sw.setChecked(sprachausgabe);

    sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
    {
      @Override
      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
      {
        Toast.makeText(getApplicationContext(), "Sprachausgabe " + ( isChecked ? "aktiviert" : "deaktiviert"), Toast.LENGTH_SHORT).show();
        // do something, the isChecked will be
        // true if the switch is in the On position

        sprachausgabe = isChecked;
      }
    });

    dialog.show();
  }

  @Override
  protected void onStart()
  {
    super.onStart();

    // Call GoogleApiClient connection when starting the Activity
    googleApiClient.connect();
  }

  @Override
  protected void onStop()
  {
    super.onStop();

    // Disconnect GoogleApiClient when stopping Activity
    gAPIConnected = false;
    googleApiClient.disconnect();
  }

  @Override
  public void onDestroy() {
    this.unregisterReceiver(receiver);
    super.onDestroy();
  }

  // Google Map methods and variables
  private GoogleMap mMap;
  public Boolean gAPIConnected = false;
  private boolean trackPosition = false;
  private GarageManager garageManager;
  private LocationManager locationManager;
  private String provider;
  private Marker userPos;
  private List<Marker> markers;
  //Zeit in ms nach der ein Location-Update durchgeführt wird
  private final int locationUpdateInterval = 5000;
  //Distanz nach der ein location-Update durchgeführt wird
  private final int locationUpdateDistance = 10;
  //Ca. Mittelpunkt vom Ring
  private final LatLng locationAmberg = new LatLng(49.445002, 11.857240);

  /**
   * Is triggered when the Google Map is ready
   *
   * @param googleMap
   */
  @Override
  public void onMapReady(GoogleMap googleMap)
  {
    mMap = googleMap;
    mMap.setInfoWindowAdapter(new GarageInfoWindow(this, garageManager));

    mMap.moveCamera(CameraUpdateFactory.newLatLng(locationAmberg));
    //Map auf Überblick über Ring zoomen
    mMap.animateCamera(CameraUpdateFactory.zoomTo(14f));

    SetMarkerOnMap();
    geofences.add(GetAmbergGeofence(locationAmberg, 0));
    AddGeofences();

    GarageManager.UpdateFinishedCallback callback = new GarageManager.UpdateFinishedCallback()
    {
      @Override
      public void onFinished(Boolean success)
      {
        if (success)
        {
          UpdateMarkers();
        }
        else
        {
          Log.d("XML", "Parsing failed");
        }
      }
    };
    garageManager.UpdateCallback = callback;
    garageManager.Update();
  }

  void SetMarkerOnMap()
  {
    // Add a marker for every garage
    for (Garage g : garageManager.GetGarages())
    {
      if (g.getLocation() != null)
      {
        markers.add(mMap.addMarker(new MarkerOptions().position(g.getLocation()).title(g.getName()).icon(GetIconForGarage(g)).snippet(GetSnippetForGarage(g))));

        geofences.add(GetGeofence(g.getLocation(), g.getId()));
      }
    }
  }

  /**
   * Location Listener
   */
  LocationListener locationListener = new LocationListener()
  {
    public void onLocationChanged(Location location)
    {
      if(mMap != null && trackPosition) {
        mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(location.getLatitude(), location.getLongitude())));

        if(userPos == null) {
          BitmapDescriptor icon;
          icon = BitmapDescriptorFactory.fromResource(R.drawable.pos);

          userPos = mMap.addMarker(new MarkerOptions().position(new LatLng(location.getLatitude(), location.getLongitude()))
                  .title(getString(R.string.userposition))
                  .icon(icon));
        }
        else {
          userPos.setPosition(new LatLng(location.getLatitude(), location.getLongitude()));
        }
      }
    }

    public void onProviderDisabled(String provider)
    {
    }

    public void onProviderEnabled(String provider)
    {
    }

    public void onStatusChanged(String provider, int status, Bundle extras)
    {
    }
  };

  /**
   * Gets the right marker-icon for the garage
   *
   * @param g the Garage
   * @return the icon
   */
  private BitmapDescriptor GetIconForGarage(Garage g)
  {
    BitmapDescriptor icon;

    if (g.getMaxPlaetze() <= 0)
    {
      icon = BitmapDescriptorFactory.fromResource(R.drawable.garage);
    }
    else
    {
      double besetzt = g.getCurPlaetze() / g.getMaxPlaetze();
      if (besetzt < 0.9f)
      {
        icon = BitmapDescriptorFactory.fromResource(R.drawable.garage_free);
      }
      else if (besetzt < 1.0f)
      {
        icon = BitmapDescriptorFactory.fromResource(R.drawable.garage_fast_voll);
      }
      else if (besetzt >= 1.0f)
      {
        icon = BitmapDescriptorFactory.fromResource(R.drawable.garage_voll);
      }
      else
      {
        icon = BitmapDescriptorFactory.fromResource(R.drawable.garage);
      }
    }

    return icon;
  }

  /**
   * Updates marker icon and snippet
   */
  private void UpdateMarkers()
  {
    for (Marker m : markers)
    {
      Garage g = garageManager.GetGarageByName(m.getTitle());
      if (g != null)
      {
        m.setIcon(GetIconForGarage(g));
        m.setSnippet(GetSnippetForGarage(g));
      }
    }
  }

  /**
   * Creates a snippet for a garage
   *
   * @param g the garage
   * @return Snippet-string
   */
  private String GetSnippetForGarage(Garage g)
  {
    String snippet = "Freie Plätze: ";
    if (g.getMaxPlaetze() > 0)
    {
      snippet += (g.getMaxPlaetze() - g.getCurPlaetze());
    }
    else
    {
      snippet += "Keine Information";
    }

    return snippet;
  }

  // Geofence methods & variables
  private List<Geofence> geofences;
  private PendingIntent geoFencePendingIntent;
  private final int GEOFENCE_REQ_CODE = 0;
  private final float GEOFENCE_RADIUS = 250.0f;

  /**
   * This method creates a Geofence
   *
   * @param position the position of the Geofence
   * @return the Geofence
   */
  private Geofence GetGeofence(LatLng position, int id)
  {
    String strid = "Geofence" + id;
    return new Geofence.Builder()
      .setRequestId(strid)
      .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
      .setCircularRegion(position.latitude, position.longitude, GEOFENCE_RADIUS)
      .setExpirationDuration(Geofence.NEVER_EXPIRE)
      .build();
  }

  private Geofence GetAmbergGeofence(LatLng position, int id)
  {
    String strid = "Geofence" + id;
    return new Geofence.Builder()
            .setRequestId(strid)
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
            .setCircularRegion(position.latitude, position.longitude, 2000f)
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .build();
  }

  /**
   * Creates aGeofenceRequest
   *
   * @param geofences
   * @return the GeofencingRequest
   */
  private GeofencingRequest createGeofenceRequest(List<Geofence> geofences)
  {
    Log.d("Geofence", "createGeofenceRequest()");
    return new GeofencingRequest.Builder()
      .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
      .addGeofences(geofences)
      .build();
  }

  /**
   * Creates a Geofence Pending Intent
   *
   * @return the Pending Intent
   */
  private PendingIntent createGeofencePendingIntent()
  {
    Log.d("Geofence", "createGeofencePendingIntent()");
    if (geoFencePendingIntent != null)
    {
      return geoFencePendingIntent;
    }
    Intent intent = new Intent(this, GeofenceTransitionService.class);
    return PendingIntent.getService(
      this, GEOFENCE_REQ_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);
  }

  /**
   * Add the created GeofenceRequest to the device's monitoring list
   *
   * @param request the GeofenceRequest
   */
  private void addGeofence(GeofencingRequest request)
  {
    Log.d("Geofence", "addGeofence");
    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
    {
      LocationServices.GeofencingApi.addGeofences(
        googleApiClient,
        request,
        createGeofencePendingIntent()
      ).setResultCallback(this);
    }
  }

  /**
   * Result of addGeofence
   *
   * @param status
   */
  @Override
  public void onResult(@NonNull Status status)
  {
    Log.d("Geofence", "addGeofence" + status.toString());
  }

  // Google API methods & variables
  private GoogleApiClient googleApiClient;

  @Override
  public void onConnected(@Nullable Bundle bundle)
  {
    Log.d("GoogleAPI", "onConnected()");
    gAPIConnected = true;
    AddGeofences();
  }
  void AddGeofences()
  {
    if(!geofences.isEmpty() && gAPIConnected)
    {
      GeofencingRequest geofenceRequest = createGeofenceRequest(geofences);
      addGeofence(geofenceRequest);
    }
  }

  @Override
  public void onConnectionSuspended(int i)
  {
    Log.d("GoogleAPI", "onConnectionSuspended()");
  }

  @Override
  public void onConnectionFailed(@NonNull ConnectionResult connectionResult)
  {
    Log.d("GoogleAPI", "onConnectionFailed()");
  }

  /**
   * creates the Google API-Client
   */
  private void CreateGoogleApi()
  {
    Log.d("GoogleAPI", "createGoogleApi()");
    if (googleApiClient == null)
    {
      googleApiClient = new GoogleApiClient.Builder(this)
        .addConnectionCallbacks(this)
        .addOnConnectionFailedListener(this)
        .addApi(LocationServices.API)
        .build();
    }
  }

  //TextToSpeech API
  TextToSpeech resultSpeaker;

  TextToSpeech.OnInitListener TTSOnInitListener = new TextToSpeech.OnInitListener() {
    @Override
    public void onInit(int status) {
      resultSpeaker.setLanguage(Locale.GERMANY);
    }
  };

  //Broadcast Receiver
  private GeofenceResponseReceiver receiver;

  /**
   * Class to receive the Broadcast from the GeofenceTransitionService
   */
  public class GeofenceResponseReceiver extends BroadcastReceiver {
    public static final String GEOFENCE_RESPONSE = "Geofence_Response_Message";

    public GeofenceResponseReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
      String ids = intent.getStringExtra(GeofenceTransitionService.GEOFENCE_SERVICE_ID);

      if(ids == null) return;

      String[] triggeredGarages = ids.split(";");

      for (String i: triggeredGarages) {
        if(i.equals("0")) {
          //Position tracken
          trackPosition = true;
          continue;
        }

        Garage g = garageManager.GetGarageById(Integer.parseInt(i));
        if(g != null && g.getShow()) {
          String resultMessage = createResultMessage(g);
          Toast.makeText(context, resultMessage, Toast.LENGTH_SHORT).show();
          //Deprecated after API 21 or sth like that, but LG Fino uses API 19

          if(sprachausgabe)
            resultSpeaker.speak(resultMessage, TextToSpeech.QUEUE_ADD, null);
        }
      }
    }

    /**
     * Creates the message to show and speak.
     *
     * @param g the Garage which is near the user's position.
     * @return the message
       */
    private String createResultMessage(Garage g) {
      String msg = "Das Parkhaus " + g.getName() + " befindet sich in ihrer Nähe.";

      //Falls Geschlossen...
      if(g.closed) {
        msg += " Das Parkhaus ist leider geschlossen.";
        return msg;
      }

      //Freie Parkplätze ausgeben
      if(g.getMaxPlaetze() - g.getCurPlaetze() > 0)
        msg += " Es sind " + g.getCurPlaetze() + " Parkplätze frei.";
      else
        msg += " Leider sind keine Parkplätze frei.";

      return msg;
    }
  }
}
