package com.example.mi.parkenamberg;

import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

class GarageManager
{
  Boolean Initialized = false;
  private Geocoder coder;
  private Garage[] garages = new Garage[8];
  UpdateFinishedCallback UpdateCallback;
  private Handler internalUpdateHandler = new Handler();

  GarageManager(ParkActivity mainActivity)
  {
    coder = new Geocoder(mainActivity);

    garages[0] = (new Garage(GetLocationFromAddress(mainActivity.getString(R.string.address0)), mainActivity.getString(R.string.garage0), 1));
    garages[1] = (new Garage(GetLocationFromAddress(mainActivity.getString(R.string.address1)), mainActivity.getString(R.string.garage1), 2));
    garages[2] = (new Garage(GetLocationFromAddress(mainActivity.getString(R.string.address2)), mainActivity.getString(R.string.garage2), 3));
    garages[3] = (new Garage(GetLocationFromAddress(mainActivity.getString(R.string.address3)), mainActivity.getString(R.string.garage3), 4));
    garages[4] = (new Garage(GetLocationFromAddress(mainActivity.getString(R.string.address4)), mainActivity.getString(R.string.garage4), 5));
    garages[5] = (new Garage(GetLocationFromAddress(mainActivity.getString(R.string.address5)), mainActivity.getString(R.string.garage5), 6));
    garages[6] = (new Garage(GetLocationFromAddress(mainActivity.getString(R.string.address6)), mainActivity.getString(R.string.garage6), 7));
    garages[7] = (new Garage(GetLocationFromAddress(mainActivity.getString(R.string.address7)), mainActivity.getString(R.string.garage7), 8));

    Runnable runnable = new Runnable()
    {
      @Override
      public void run()
      {
        Log.d("GarageManager","Update running...");
        Update();
        internalUpdateHandler.postDelayed(this, 60000);
      }
    };
    internalUpdateHandler.postDelayed(runnable, 10000);
  }

  interface UpdateFinishedCallback
  {
    void onFinished(Boolean success);
  }

  ArrayList<Garage> GetGarages()
  {
    return new ArrayList<>(Arrays.asList(garages));
  }

  /**
   * Returns the location of an address
   *
   * @param strAddress the address
   * @return the location
   */
  private LatLng GetLocationFromAddress(String strAddress)
  {
    if (coder == null)
    {
      return null;
    }

    List<Address> address;
    LatLng l;

    try
    {
      address = coder.getFromLocationName(strAddress, 1);
      if (address == null)
      {
        return null;
      }
      Address location = address.get(0);

      l = new LatLng(location.getLatitude(), location.getLongitude());
    } catch (Exception e)
    {
      return null;
    }

    return l;
  }

  void Update()
  {
    HTTPRequest.TaskListener listener = new HTTPRequest.TaskListener()
    {
      @Override
      public void onFinished(Document result)
      {
        Boolean success = UpdatePlaetze(result);
        Initialized = success;
        if (UpdateCallback != null)
        {
          UpdateCallback.onFinished(success);
        }
      }
    };

    HTTPRequest task = new HTTPRequest(listener);

    task.execute();
  }

  /**
   * Updates parking lot numbers
   */
  private Boolean UpdatePlaetze(Document doc)
  {
    try
    {
      NodeList nodes = doc.getElementsByTagName("Parkhaus");
      for (int i = 0; i < nodes.getLength(); i++)
      {
        Element element = (Element) nodes.item(i);
        NodeList idNodes = element.getElementsByTagName("ID");
        if (idNodes.getLength() > 0)
        {
          int id = Integer.parseInt(idNodes.item(0).getFirstChild().getNodeValue());
          NodeList currentNodes, maxNodes;

          maxNodes = element.getElementsByTagName("Gesamt");
          currentNodes = element.getElementsByTagName("Aktuell");

          if (maxNodes.getLength() > 0)
          {
            Element max = (Element) maxNodes.item(0);
            garages[id - 1].setMaxPlaetze(Integer.parseInt(max.getFirstChild().getNodeValue()));
          }
          if (currentNodes.getLength() > 0)
          {
            Element current = (Element) currentNodes.item(0);
            garages[id - 1].setCurPlaetze(Integer.parseInt(current.getFirstChild().getNodeValue()));
          }
        }
      }
    } catch (Exception e)
    {
      //dam, son
      Log.d("", Log.getStackTraceString(e));

      return false;
    }
    return true;
  }

  /**
   * Returns the garage with the name
   *
   * @param name of garage
   * @return returns the garage or null
   */
  Garage GetGarageByName(String name)
  {
    for (Garage g : garages)
    {
      if (g.getName().equals(name))
      {
        return g;
      }
    }

    return null;
  }

  /**
   * Returns the garage with the id
   *
   * @param id of garage
   * @return returns the garage or null
   */
  Garage GetGarageById(int id)
  {
    for (Garage g : garages)
    {
      if (g.getId() == id)
      {
        return g;
      }
    }

    return null;
  }

  private static class HTTPRequest extends AsyncTask<String, Void, Document>
  {
    interface TaskListener
    {
      void onFinished(Document result);
    }

    // This is the reference to the associated listener
    private final TaskListener taskListener;

    HTTPRequest(TaskListener listener)
    {
      // The listener reference is passed in through the constructor
      this.taskListener = listener;
    }

    @Override
    protected void onPostExecute(Document result)
    {
      super.onPostExecute(result);

      // In onPostExecute we check if the listener is valid
      if (this.taskListener != null)
      {

        // And if it is we call the callback function on it.
        this.taskListener.onFinished(result);
      }
    }

    @Override
    protected Document doInBackground(String... foobar)
    {
      Document doc = null;
      try
      {
        URL url = new URL("http://parken.amberg.de/wp-content/uploads/pls/pls.xml");
        URLConnection conn = url.openConnection();

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        doc = builder.parse(conn.getInputStream());
      } catch (Exception e)
      {
        // well, shit
      }
      return doc;
    }
  }
}