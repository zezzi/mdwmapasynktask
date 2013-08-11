package com.maestros.mdwmapas;


import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.maestros.mdwmapas.api.APIConexionHelper;

import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Color;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.widget.Toast;

public class MainActivityMapas extends Activity {
	public static final String APPTAG = "MDWRestaurantes";
	private GoogleMap map;
	Location mCurrentLocation;
	private HashMap<Marker, Lugares> restauranteMarkerMap = new HashMap<Marker, Lugares>();
	private static APIConexionHelper api = new APIConexionHelper();
	Marker myLocation ;

	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
			setContentView(R.layout.activity_main_activity_mapas); 
			map =((MapFragment)getFragmentManager().findFragmentById(R.id.map)).getMap();
			if (map!=null){
				/* Hay Distintos tipos de Mapas: MAP_TYPE_NORMAL,
				MAP_TYPE_HYBRID, 
				MAP_TYPE_SATELLITE, 
				MAP_NONE, 
				MAP_TERRAIN*/
				new APIAsyncTask().execute();
				moveMapToMyLocation();
				map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
				map.setInfoWindowAdapter(new CustomInfoWindow(getLayoutInflater()));
			} 
    }

    
    
    
	private void moveMapToMyLocation() {
    	LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
    	Criteria crit = new Criteria();
    	Location loc = locationManager.getLastKnownLocation(locationManager.getBestProvider(crit, false));
    	map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(loc.getLatitude(),loc.getLongitude()), 35)); 
        map.animateCamera(CameraUpdateFactory.zoomTo(10), 2000, null);
        Marker myLocation = map.addMarker(new MarkerOptions().position(new                 
        		LatLng(loc.getLatitude(),loc.getLongitude())).title("Mi Posicion").icon(BitmapDescriptorFactory
                .fromResource(R.drawable.logomdw)).draggable(true));
        Lugares actual = new Lugares(new LatLng(loc.getLatitude(),loc.getLongitude()),"Yo","Mi posicion actual","LI","direccion Actual");
        restauranteMarkerMap.put(myLocation,actual);
    }
    
    

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; Esto agrega los elementos al action bar si estan presentes
        getMenuInflater().inflate(R.menu.main_activity_mapas, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.menu_licencia:
            	Log.d("menu"," menu-licencia");
            	Intent nextScreen2 = new Intent(MainActivityMapas.this, Contactenos.class);
                startActivityForResult(nextScreen2, 0);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    
    
    class APIAsyncTask extends AsyncTask<Void, Void, ArrayList<Lugares>> {
		private ProgressDialog dialog = new ProgressDialog(MainActivityMapas.this);

		@Override
		protected void onPreExecute() {
			dialog.setMessage("Actualizando...");
			dialog.show();
		}  

		@Override
		protected ArrayList<Lugares> doInBackground(Void... params) {
			Log.d("ApiRestaurantes",APIConexionHelper.URL);
			return api.parseJSONArray(api.readJSONArrayFromURL(APIConexionHelper.URL));			
		}

		@Override
		protected void onPostExecute(ArrayList<Lugares> result){
			dialog.dismiss();
			if(result.size()>0){
			  for (Lugares temp : result) {
	    		  Marker marker_res = map.addMarker(new MarkerOptions()
	              	.position(temp.getLatLng())
	              	.title(temp.getTitle())
	              	.snippet(temp.getDesc()+temp.getCode())
	              	.icon(BitmapDescriptorFactory
	                .fromResource(R.drawable.logomdw)));
	    		  
	    		  CircleOptions circleOptions = new CircleOptions()
	    		    	.center(temp.getLatLng())
	    		    	.radius(1000).fillColor(Color.BLUE);
	    		  Circle circle = map.addCircle(circleOptions);
	    		  restauranteMarkerMap.put(marker_res, temp);
	    	  }
			  map.setOnInfoWindowClickListener(new OnInfoWindowClickListener() {

	              @Override
	              public void onInfoWindowClick(Marker marker) {
	                  Log.d("markerdetail", marker.getTitle()); 
	                  Lugares lugar_presionado=restauranteMarkerMap.get(marker);
	                  Intent nextScreen = new Intent(MainActivityMapas.this, DetailActivity.class);
	                  nextScreen.putExtra("lugar_title",lugar_presionado.getTitle());
	                  nextScreen.putExtra("lugar_desc",lugar_presionado.getDesc());
	                  nextScreen.putExtra("lugar_address",lugar_presionado.getAddress());
	                  nextScreen.putExtra("lugar_code",lugar_presionado.getCode());
	                  startActivityForResult(nextScreen, 0);
	              }
	          });
			}	
		}
	}

}
