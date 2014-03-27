package com.ucaapp;

import java.util.HashMap;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class Map extends Activity {

	private GoogleMap googleMap;
	private String markerName;
	private HashMap<String, LatLng> markers;
	private Button go;
	private Intent resultIntent;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map);
        
        initilizeMap();
        
        // Markers container
        markers = (HashMap<String, LatLng>) getIntent().getSerializableExtra("coords");
        setMarker("ESI", markers.get("ESI"));
        setMarker("Catedral", markers.get("Catedral"));
        setMarker("Plaza de España", markers.get("Plaza de España"));
        
        //LatLng home = new LatLng(36.529055, -6.296238);
        //markerName = "Home";
        //markers.put(markerName, home);
        //setMarker("Home", markers.get("Home"));
        
        go = (Button) findViewById(R.id.button1);
        go.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				 CameraPosition cameraPosition = new CameraPosition.Builder().target(
		                new LatLng(36.529055, -6.296238)).zoom(17).build();
		 
		         googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
			}
		});
        
        
        googleMap.setMyLocationEnabled(true);
        googleMap.getUiSettings().setMyLocationButtonEnabled(true);
        
        googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        
        googleMap.setOnMapLongClickListener(new OnMapLongClickListener() {
			
			@Override
			public void onMapLongClick(LatLng latlng) {
				markers.put("new", latlng);
				createDialog(latlng);				
			}
		});
        
        
    }
    
    /**
     * function to load map. If map is not created it will create it for you
     * */
    private void initilizeMap() {
        if (googleMap == null) {
            googleMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
 
            // check if map is created successfully or not
            if (googleMap == null) {
                Toast.makeText(getApplicationContext(),
                        "Sorry! unable to create maps", Toast.LENGTH_SHORT)
                        .show();
            }
        }
    }
    
    private void createDialog(LatLng latlng){
    	//final CharSequence[] colors_radio={"Green","Black","White"};
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	builder.setTitle("Name for this place");

    	// Set up the input
    	final EditText input = new EditText(this);
    	// Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
    	input.setInputType(InputType.TYPE_CLASS_TEXT);
    	builder.setView(input);

    	// Set up the buttons
    	builder.setPositiveButton("OK", new DialogInterface.OnClickListener() { 
    	    @Override
    	    public void onClick(DialogInterface dialog, int which) {
    	        markerName = input.getText().toString();
    	        LatLng l = markers.get("new");
    	        markers.remove("new");
    	        markers.put(markerName, l);
    	        setMarker(markerName, l);

    	        // Pass new markers to the fullscreenactivity to store it.
    	        
    	        resultIntent = new Intent();
    	        resultIntent.putExtra("markers", markers);
    	        setResult(Activity.RESULT_OK, resultIntent);
    	        finish();
    	        

    	    }
    	});
    	builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
    	    @Override
    	    public void onClick(DialogInterface dialog, int which) {
    	    	markers.remove("new");
    	        dialog.cancel();
    	    }
    	});

    	builder.show();

    }
    
    private void setMarker(String name, LatLng latlng){
        // Create and add marker
        MarkerOptions marker = new MarkerOptions().position(latlng).title(name);
        googleMap.addMarker(marker);

    }

}
