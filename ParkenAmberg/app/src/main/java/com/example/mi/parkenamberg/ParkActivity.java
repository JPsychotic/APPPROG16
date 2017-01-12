package com.example.mi.parkenamberg;

import android.Manifest;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
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
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

/**
 * Main Activity for the application
 */
public class ParkActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
  GoogleApiClient.OnConnectionFailedListener, OnMapReadyCallback, ResultCallback<Status>
{
  /**
   * Variable to check if voice output is enabled
   */
  private boolean sprachausgabe = true;
  /**
   * Variable to check if the app is minimized or not
   */
  private boolean isBackground = false;

  /**
   * OnCreate of the Activity
   * @param savedInstanceState
     */
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

  /**
   * Creates the options menu
   * @param menu
   * @return
     */
  @Override
  public boolean onCreateOptionsMenu(Menu menu)
  {
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.menu, menu);

    return super.onCreateOptionsMenu(menu);
  }

  /**
   * OnClick Handler for the options menu
   * @param item
   * @return
     */
  @Override
  public boolean onOptionsItemSelected(MenuItem item)
  {
    switch (item.getItemId())
    {
      case R.id.maptype:
      {
        if (mMap.getMapType() == GoogleMap.MAP_TYPE_NORMAL)
        {
          mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        }
        else
        {
          mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        }
        break;
      }
      case R.id.favoriteSubMenu:
      {
        openFavoriteMenu();
        break;
      }
      case R.id.settings:
      {
        openSettingsMenu();
        break;
      }
      case R.id.homeButton:
      {
        if(mMap == null) break;
        mMap.moveCamera(CameraUpdateFactory.newLatLng(locationAmberg));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(14f));
        break;
      }
      default:
        break;
    }
    return super.onOptionsItemSelected(item);
  }

  /**
   * Opens the favoritesmenu-overlay
   */
  private void openFavoriteMenu()
  {
    final Dialog dialog = new Dialog(ParkActivity.this);
    dialog.setContentView(R.layout.favorite_dialog);
    dialog.setTitle(R.string.FavoritenMenuTitle);
    dialog.setCancelable(true);

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
        Toast.makeText(getApplicationContext(), "Parkhaus " + tag + " als Favorit " + (isChecked ? "hinzugefügt" : "entfernt"), Toast.LENGTH_SHORT).show();
        // do something, the isChecked will be
        // true if the switch is in the On position

        Garage g = garageManager.GetGarageById(Integer.parseInt(tag));
        g.setShow(isChecked);

        SharedPreferences settings = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(g.getName(), isChecked);
        editor.apply();

        if (!showOnlyFavos)
        {
          return;
        }

        //Marker entfernen bzw. hinzufügen
        if (isChecked)
        {
          markers.add(mMap.addMarker(new MarkerOptions().position(g.getLocation()).title(g.getName()).icon(GetIconForGarage(g)).snippet(GetSnippetForGarage(g))));
        }
        else
        {
          for (Marker m : markers)
          {
            if (m.getTitle().equals(g.getName()))
            {
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

  /**
   * Variable to check if the user wants to display only favorites
   */
  private boolean showOnlyFavos = false;
  /**
   * Variable to check if the user wants background voice output enabled
   */
  private boolean enableBackgroundSpeech = false;

  /**
   * Creates the settingsmenu-overlay
   */
  private void openSettingsMenu()
  {
    final Dialog dialog = new Dialog(ParkActivity.this);
    dialog.setContentView(R.layout.setting_dialog);
    dialog.setTitle(R.string.settingsTitle);
    dialog.setCancelable(true);

    Switch sw = (Switch) dialog.findViewById(R.id.sprachausgabeSwitch);
    sw.setChecked(sprachausgabe);

    Switch sw2 = (Switch) dialog.findViewById(R.id.onlyfavosSwitch);
    sw2.setChecked(showOnlyFavos);

    Switch sw3 = (Switch) dialog.findViewById(R.id.enableBackgroundSpeech);
    sw3.setChecked(enableBackgroundSpeech);

    sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
    {
      @Override
      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
      {
        Toast.makeText(getApplicationContext(), "Sprachausgabe " + (isChecked ? "aktiviert" : "deaktiviert"), Toast.LENGTH_SHORT).show();
        sprachausgabe = isChecked;
        SharedPreferences settings = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("sprachausgabe", sprachausgabe);
        editor.apply();
      }
    });


    sw2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
    {
      @Override
      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
      {
        Toast.makeText(getApplicationContext(), "Zeige nur Favoriten " + (isChecked ? "aktiviert" : "deaktiviert"), Toast.LENGTH_SHORT).show();
        showOnlyFavos = isChecked;
        setShowOnlyFavos();
        SharedPreferences settings = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("showOnlyFavos", showOnlyFavos);
        editor.apply();
      }
    });

    sw3.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
    {
      @Override
      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
      {
        Toast.makeText(getApplicationContext(), "Hintergrundprachausgabe " + (isChecked ? "aktiviert" : "deaktiviert"), Toast.LENGTH_SHORT).show();
        enableBackgroundSpeech = isChecked;
        SharedPreferences settings = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("enableBackgroundSpeech", enableBackgroundSpeech);
        editor.apply();
      }
    });

    dialog.show();
  }

  /**
   * Shows only the favorites
   */
  private void setShowOnlyFavos()
  {
    Iterator<Marker> iter = markers.iterator();
    while (iter.hasNext())
    {
      Marker str = iter.next();

      iter.remove();
      str.remove();
    }

    for (Garage g : garageManager.GetGarages())
    {
      if (!showOnlyFavos || g.getShow())
      {
        markers.add(mMap.addMarker(new MarkerOptions().position(g.getLocation()).title(g.getName()).icon(GetIconForGarage(g)).snippet(GetSnippetForGarage(g))));
      }
    }
  }

  /**
   * Loads the saved settings
   */
  private void loadSettings()
  {
    SharedPreferences settings = getPreferences(Context.MODE_PRIVATE);
    sprachausgabe = settings.getBoolean("sprachausgabe", false);
    showOnlyFavos = settings.getBoolean("showOnlyFavos", false);
    enableBackgroundSpeech = settings.getBoolean("enableBackgroundSpeech", false);
    setShowOnlyFavos();
  }

  /**
   * Called when the app is maximized
   */
  @Override
  protected void onStart()
  {
    super.onStart();
    isBackground = false;
    // Call GoogleApiClient connection when starting the Activity
    googleApiClient.connect();
  }

  /**
   * Called when the app is minimized
   */
  @Override
  protected void onStop()
  {
    super.onStop();
    isBackground = true;
    // Disconnect GoogleApiClient when stopping Activity
    gAPIConnected = false;
    googleApiClient.disconnect();
  }

  /**
   * Called when the app is destroyed
   */
  @Override
  public void onDestroy()
  {
    this.unregisterReceiver(receiver);
    super.onDestroy();
  }

  // Google Map methods and variables
  /**
   * The Google-Map
   */
  private GoogleMap mMap;
  /**
   * Variable to check if Google-API is connected
   */
  public Boolean gAPIConnected = false;
  /**
   * Variable to check if the user-positions should be tracked
   */
  private boolean trackPosition = false;
  /**
   * The instance of the Garage-Manager
   */
  private GarageManager garageManager;
  /**
   * The LocationManager, which provides the position
   */
  private LocationManager locationManager;
  /**
   * The provider used with the manager
   */
  private String provider;
  /**
   * Marker for the user position
   */
  private Marker userPos;
  /**
   * Markers for the Garages
   */
  private List<Marker> markers;
  /**
   * Interval-Time for the location update
   */
  private final int locationUpdateInterval = 5000;
  /**
   * Distance after which the update is triggered
   */
  private final int locationUpdateDistance = 10;
  /**
   * Position of Amberg
   */
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
    loadSettings();
  }

  /**
   * Sets Marker for garages on the map
   */
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
   * Location Listener for the locationManager
   */
  LocationListener locationListener = new LocationListener()
  {
    public void onLocationChanged(Location location)
    {
      if (mMap != null && trackPosition)
      {
        mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(location.getLatitude(), location.getLongitude())));

        if (userPos == null)
        {
          BitmapDescriptor icon;
          icon = BitmapDescriptorFactory.fromResource(R.drawable.pos);

          userPos = mMap.addMarker(new MarkerOptions().position(new LatLng(location.getLatitude(), location.getLongitude()))
            .title(getString(R.string.userposition)).icon(icon));
        }
        else
        {
          userPos.setPosition(new LatLng(location.getLatitude(), location.getLongitude()));
        }
      }
    }

    /**
     * Is triggered when there is no GPS-Signal
     * @param provider
       */
    public void onProviderDisabled(String provider)
    {
      Toast.makeText(getApplicationContext(), "GPS Signal verloren", Toast.LENGTH_LONG).show();
    }

    /**
     * Triggered when the GPS provider is enabled
     * @param provider
       */
    public void onProviderEnabled(String provider)
    {
    }

    /**
     * Triggered when the status changes
     * @param provider
     * @param status
     * @param extras
       */
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
      double besetzt = (double)g.getCurPlaetze() / (double)g.getMaxPlaetze();
      if (besetzt < 0.8f)
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
    if(g.closed)
      snippet += "Parkhaus ist geschlossen";
    else if (g.getMaxPlaetze() > 0)
    {
      snippet += (g.getMaxPlaetze() - g.getCurPlaetze());
    }
    else
    {
      snippet += "Keine Information";
    }

    snippet += "\nTrend: ";

    int trend = g.getTrend();
    if(trend == 0)
    {
      snippet += "\u2192";
    }
    else if(trend == 1)
    {
      snippet += "\u2197";
    }
    else if(trend == -1)
    {
      snippet += "\u2198";
    }
    else
      snippet += "Keine Information.";

    return snippet;
  }

  // Geofence methods & variables
  /**
   * List of geofences for the garages
   */
  private List<Geofence> geofences;
  /**
   * Geofence Intent
   */
  private PendingIntent geoFencePendingIntent;
  private final int GEOFENCE_REQ_CODE = 0;
  /**
   * The radius for the geofence of the garages
   */
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
      .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT)
      .setCircularRegion(position.latitude, position.longitude, GEOFENCE_RADIUS)
      .setExpirationDuration(Geofence.NEVER_EXPIRE)
      .build();
  }

  /**
   * Returns the Geofence for Amberg
   * @param position Position
   * @param id Id of this Geofence
   * @return the Geofence
     */
  private Geofence GetAmbergGeofence(LatLng position, int id)
  {
    String strid = "Geofence" + id;
    return new Geofence.Builder()
      .setRequestId(strid)
      .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT)
      .setCircularRegion(position.latitude, position.longitude, 2000f)
      .setExpirationDuration(Geofence.NEVER_EXPIRE)
      .build();
  }

  /**
   * Creates a GeofenceRequest
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
  /**
   * Variable for the Google API-client
   */
  private GoogleApiClient googleApiClient;

  /**
   * Triggered when the Google API client is connected
   * @param bundle
     */
  @Override
  public void onConnected(@Nullable Bundle bundle)
  {
    Log.d("GoogleAPI", "onConnected()");
    gAPIConnected = true;
    AddGeofences();
  }

  /**
   * Starts the Geofence Monitoring
   */
  void AddGeofences()
  {
    if (!geofences.isEmpty() && gAPIConnected)
    {
      GeofencingRequest geofenceRequest = createGeofenceRequest(geofences);
      addGeofence(geofenceRequest);
    }
  }

  /**
   * Triggered when the Google API connection is suspended
   * @param i
     */
  @Override
  public void onConnectionSuspended(int i)
  {
    Log.d("GoogleAPI", "onConnectionSuspended()");
  }

  /**
   * Triggered if the google API connection failed
   * @param connectionResult
     */
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
  /**
   * Text to speech instance
   */
  TextToSpeech resultSpeaker;

  /**
   * OnInitListener for the Text to speech instance
   */
  TextToSpeech.OnInitListener TTSOnInitListener = new TextToSpeech.OnInitListener()
  {
    @Override
    public void onInit(int status)
    {
      resultSpeaker.setLanguage(Locale.GERMANY);
    }
  };

  //Broadcast Receiver
  /**
   * Broadcast Receiver for the broadcats of the GeofenceTransitionService
   */
  private GeofenceResponseReceiver receiver;

  /**
   * Class to receive the Broadcast from the GeofenceTransitionService
   */
  public class GeofenceResponseReceiver extends BroadcastReceiver
  {
    /**
     * Geofence Response identifier string
     */
    public static final String GEOFENCE_RESPONSE = "Geofence_Response_Message";

    /**
     * Constructor
     */
    public GeofenceResponseReceiver()
    {
    }

    /**
     * Triggered when a Broadcast is received
     * @param context
     * @param intent
       */
    @Override
    public void onReceive(Context context, Intent intent)
    {
      String ids = intent.getStringExtra(GeofenceTransitionService.GEOFENCE_SERVICE_ID);

      if (ids == null)
      {
        return;
      }

      boolean enter = false;
      String[] triggeredGarages = ids.split(";");

      if(triggeredGarages.length > 0 && triggeredGarages[0].equals("enter"))
        enter = true;

      for (String i : triggeredGarages)
      {
        if(i.equals("enter") || i.equals("exit"))
          continue;

        if (i.equals("0"))
        {
          //Position tracking or not
          trackPosition = enter;
          continue;
        }

        Garage g = garageManager.GetGarageById(Integer.parseInt(i));
        if (g != null && enter && (!showOnlyFavos || g.getShow())) {
          String resultMessage = createResultMessage(g);
          Toast.makeText(context, resultMessage, Toast.LENGTH_SHORT).show();

          if (sprachausgabe) {
            if ((isBackground && enableBackgroundSpeech) || !isBackground) {
              //Deprecated after API 21 or sth like that, but LG Fino uses API 19
              resultSpeaker.speak(resultMessage, TextToSpeech.QUEUE_ADD, null);
            }
          }

          //Set Entered only true, if Garage is entered
          if(g != null)
            g.setEntered(enter);
        }

        //Set Entered to false for every garage
        if(g != null && !enter)
          g.setEntered(enter);
      }

      //Check if any geofence of a garage is entered
      boolean zoomed = false;
      for (Garage g: garageManager.GetGarages()) {
        if(g.getEntered())
        {
          //if yes zoom map in...
          mMap.animateCamera(CameraUpdateFactory.zoomTo(16f));
          zoomed = true;
          break;
        }
      }
      //if not zoom out
      if(!zoomed)
        mMap.animateCamera(CameraUpdateFactory.zoomTo(14f));
    }

    /**
     * Creates the message to show and speak.
     *
     * @param g the Garage which is near the user's position.
     * @return the message
     */
    private String createResultMessage(Garage g)
    {
      String msg = "Das Parkhaus " + g.getName() + " befindet sich in ihrer Nähe.";

      //Falls Geschlossen...
      if (g.closed)
      {
        msg += " Das Parkhaus ist leider geschlossen.";
        return msg;
      }

      //Freie Parkplätze ausgeben
      if(g.getMaxPlaetze() <= 0)
      {
        msg += " Es sind keine Informationen über die Parkplätze verfügbar.";
      }
      else if (g.getMaxPlaetze() - g.getCurPlaetze() > 0)
      {
        msg += " Es sind " + (g.getMaxPlaetze() - g.getCurPlaetze()) + " Parkplätze frei.";
      }
      else
      {
        msg += " Leider sind keine Parkplätze frei.";
      }

      return msg;
    }
  }
}
