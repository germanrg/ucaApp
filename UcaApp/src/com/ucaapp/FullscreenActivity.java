package com.ucaapp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import com.ucaapp.util.SystemUiHider;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

/**
 * Full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 * 
 * @see SystemUiHider
 */
public class FullscreenActivity extends Activity {
	/**
	 * Whether or not the system UI should be auto-hidden after
	 * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
	 */
	private static final boolean AUTO_HIDE = true;

	/**
	 * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
	 * user interaction before hiding the system UI.
	 */
	private static final int AUTO_HIDE_DELAY_MILLIS = 2500;

	/**
	 * If set, will toggle the system UI visibility upon interaction. Otherwise,
	 * will show the system UI visibility upon interaction.
	 */
	private static final boolean TOGGLE_ON_CLICK = true;

	/**
	 * The flags to pass to {@link SystemUiHider#getInstance}.
	 */
	private static final int HIDER_FLAGS = SystemUiHider.FLAG_HIDE_NAVIGATION;

	/**
	 * The instance of the {@link SystemUiHider} for this activity.
	 */
	private SystemUiHider mSystemUiHider;
	
	private FullscreenActivity main;
	
	private ImageView departuresBlock, arrivalsBlock;
	private TextView departuresText, arrivalsText;
    private ImageView blueCar;
    private android.widget.FrameLayout.LayoutParams blueCarParams;
    private Handler blueCarHandler;
    
    private Button play;
    
    private ProgressBar mProgress;
    private int mProgressStatus = 0;
    private Handler progressBarHandler = new Handler();
    private Thread progressThread;
    
    private String selectedArrival = "", selectedDeparture = "";
    public ArrayList<String> places; // Global var now. See startActivityForResult()
    public HashMap<String, LatLng> coords;
    
    private View controlsView;
	private View contentView;

	public void setPlace(String name, LatLng coordinates){
		places.add(name);
		coords.put(name, new LatLng(coordinates.latitude, coordinates.longitude));
	}

    private void setupPlacesAndCoords(){
    	places = new ArrayList<String>();
    	coords = new HashMap<String, LatLng>();
    	// Names and Coords
    	places.add("ESI");
		coords.put("ESI", new LatLng(36.533504, -6.303289));
		places.add("Catedral");
		coords.put("Catedral", new LatLng(36.529029, -6.295146));
		places.add("Plaza de España");
		coords.put("Plaza de España", new LatLng(36.535055, -6.293376));
		
		// Set Markers
		
    }
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_fullscreen);
		
		// user places & coords
		setupPlacesAndCoords();
		
		// Force LandScape
		this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		
		departuresText = (TextView) findViewById(R.id.departuresBlockText);
		arrivalsText = (TextView) findViewById(R.id.arrivalsBlockText);

		controlsView = findViewById(R.id.fullscreen_content_controls);
		contentView = findViewById(R.id.fullscreen_content);

		// Set up an instance of SystemUiHider to control the system UI for
		// this activity.
		mSystemUiHider = SystemUiHider.getInstance(this, contentView,
				HIDER_FLAGS);
		mSystemUiHider.setup();
		mSystemUiHider
				.setOnVisibilityChangeListener(new SystemUiHider.OnVisibilityChangeListener() {
					// Cached values.
					int mControlsHeight;
					int mShortAnimTime;

					@Override
					@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
					public void onVisibilityChange(boolean visible) {
						if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
							// If the ViewPropertyAnimator API is available
							// (Honeycomb MR2 and later), use it to animate the
							// in-layout UI controls at the bottom of the
							// screen.
							if (mControlsHeight == 0) {
								mControlsHeight = controlsView.getHeight();
							}
							if (mShortAnimTime == 0) {
								mShortAnimTime = getResources().getInteger(
										android.R.integer.config_shortAnimTime);
							}
							controlsView
									.animate()
									.translationY(visible ? 0 : mControlsHeight)
									.setDuration(mShortAnimTime);
						} else {
							// If the ViewPropertyAnimator APIs aren't
							// available, simply show or hide the in-layout UI
							// controls.
							controlsView.setVisibility(visible ? View.VISIBLE
									: View.GONE);
						}

						if (visible && AUTO_HIDE) {
							// Schedule a hide().
							delayedHide(AUTO_HIDE_DELAY_MILLIS);
						}
					}
				});

		// Set up the user interaction to manually show or hide the system UI.
		contentView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (TOGGLE_ON_CLICK) {
					mSystemUiHider.toggle();
				} else {
					mSystemUiHider.show();
				}
			}
		});

		// Upon interacting with UI controls, delay any scheduled hide()
		// operations to prevent the jarring behavior of controls going away
		// while interacting with the UI.
		findViewById(R.id.dummy_button).setOnTouchListener(
				mDelayHideTouchListener);
		
		mSystemUiHider.toggle(); // Hide controls
		
		// Get instance
		main = this;
		
		// Get views
		departuresBlock = (ImageView) findViewById(R.id.departuresBlock);
		departuresBlock.setLongClickable(true);
		departuresBlock.setOnLongClickListener(new OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				departuresDialog();
				return false;
			}
	    });
		
		arrivalsBlock = (ImageView) findViewById(R.id.arrivalsBlock);
		arrivalsBlock.setLongClickable(true);
		arrivalsBlock.setOnLongClickListener(new OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				arrivalsDialog();
				return false;
			}
	    });
		
		play = (Button)findViewById(R.id.dummy_button);
		
		blueCar = (ImageView)findViewById(R.id.imageView1);
		blueCar.bringToFront();
		blueCarParams = (android.widget.FrameLayout.LayoutParams)blueCar.getLayoutParams();
		
		
		// Progress Bar
		
		mProgress = (ProgressBar) findViewById(R.id.progressBar1);
		mProgress.setMax(100);
		
		// Resource object to get Drawables
		Resources res = getResources(); 
		// Get the Drawable custom_progressbar                     
        Drawable draw = res.getDrawable(R.drawable.customprogressbar);
        // set the drawable as progress drawavle
        mProgress.setProgressDrawable(draw);
        
        play.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	if(departuresText.getText().equals("") || arrivalsText.getText().equals("") || departuresText.getText().equals(arrivalsText.getText()))
            		Toast.makeText(getApplicationContext(),
        	    			"Arrival and departure cannot be null\nArrival and departure cannot be the same place", Toast.LENGTH_LONG).show();
            	// Reset progress bar
            	else if(play.getText().equals("Reset")){
            		mProgress.setProgress(0);
            		play.setText("Play");
            		blueCarParams = (android.widget.FrameLayout.LayoutParams)blueCar.getLayoutParams();
            		blueCarParams.leftMargin = 295;
            		blueCar.setLayoutParams(blueCarParams);
            	}
            	// Init progress bar
            	else {
            		// Get distance from google maps //
            		GetDistanceFromGoogleMaps distance = 
            				new GetDistanceFromGoogleMaps(main, coords.get(departuresText.getText()), 
            						coords.get(arrivalsText.getText()),
            						"driving");
            		
            		distance.execute();
            		
                    play.setEnabled(false);
                    
                    // Start lengthy operation in a background thread
                    progressThread = new Thread(new Runnable() {
                        public void run() {
                            while (mProgressStatus <= 100) {
                                mProgressStatus = doWork(mProgressStatus);
                                
                                // Send 'OK' to draw car
                                String msg = "OK";
                                Message message = blueCarHandler.obtainMessage();
                                message.obj = msg;
                                blueCarHandler.sendMessage(message);

                                // Update the progress bar
                                progressBarHandler.post(new Runnable() {
                                    public void run() {
                                        mProgress.setProgress(mProgressStatus);
                                    }
                                });
                            }
                            // Send 'END' to enable RESET Button
                            if(mProgressStatus >= 100){
                            	String msg = "END";
                                Message message = blueCarHandler.obtainMessage();
                                message.obj = msg;
                                blueCarHandler.sendMessage(message);
                            }
                        }
                        // Progress delay
                        public int doWork(int n){
                        	for(int i = 0; i < 10000000; i++);
                        	return n+1;
                        }
                    });
                    
                    progressThread.start();            		
            	}
            }
        });
        
        blueCarHandler = new Handler(){
        	@Override
            public void handleMessage(Message msg) {
                String status = (String) msg.obj;
                // if 'OK' draw car at new position
                if(status.equals("OK")){
                	blueCarParams.leftMargin += 4;
            		blueCar.setLayoutParams(blueCarParams);
                }
                // if 'END' reset progress bar
                else if(status.equals("END")){
                	mProgress.setProgress(0);
                	play.setText("Reset");
                	play.setEnabled(true);
                	TextView t = (TextView)findViewById(R.id.fullscreen_content);
                	t.setText("You've arrived!!");
                }
            }
        };
        
        
	}
	
	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);

		// Trigger the initial hide() shortly after the activity has been
		// created, to briefly hint to the user that UI controls
		// are available.
		//delayedHide(100);

	}
	
	@Override 
	public void onActivityResult(int requestCode, int resultCode, Intent data) {     
		super.onActivityResult(requestCode, resultCode, data); 
		if (resultCode == Activity.RESULT_OK) { 
			HashMap<String, LatLng> markers = (HashMap<String, LatLng>) data.getSerializableExtra("markers");
			Iterator<Entry<String, LatLng>> it = markers.entrySet().iterator();
			while (it.hasNext()) {
				Entry<String, LatLng> e = it.next();
				if(!coords.containsKey(e.getKey())){
					coords.put(e.getKey(), e.getValue());
					places.add(e.getKey());
					selectedDeparture = e.getKey();
					departuresText.setText(e.getKey());
					departuresText.bringToFront();
				}	
			}
		} 	 
	}

	/**
	 * Touch listener to use for in-layout UI controls to delay hiding the
	 * system UI. This is to prevent the jarring behavior of controls going away
	 * while interacting with activity UI.
	 */
	View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
		@Override
		public boolean onTouch(View view, MotionEvent motionEvent) {
			if (AUTO_HIDE) {
				delayedHide(AUTO_HIDE_DELAY_MILLIS);
			}
			return false;
		}
	};

	Handler mHideHandler = new Handler();
	Runnable mHideRunnable = new Runnable() {
		@Override
		public void run() {
			mSystemUiHider.hide();
		}
	};

	/**
	 * Schedules a call to hide() in [delay] milliseconds, canceling any
	 * previously scheduled calls.
	 */
	private void delayedHide(int delayMillis) {
		mHideHandler.removeCallbacks(mHideRunnable);
		mHideHandler.postDelayed(mHideRunnable, delayMillis);
	}
	
	private void selectNewPlace(){		
		Intent i = new Intent(this, Map.class);
        i.putExtra("coords", coords);
		startActivityForResult(i, 1);
        
	}
	
	// Departures Dialog	
	private void departuresDialog(){
    	final CharSequence[] departuresDialogItems = {"My Places","New Place", "Remove Place"};
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	builder.setTitle("Options");
    	builder.setSingleChoiceItems(departuresDialogItems, -1, new DialogInterface.OnClickListener() {
    		@Override
    		public void onClick(DialogInterface dialog, int which) {
    			switch(which){
    			case 0:
    				dialog.dismiss();
    				selectDeparturePlaceDialog(places);
    				break;
    			case 1:
    				dialog.dismiss();
    				selectNewPlace();
    				break;
    			case 2:
    				dialog.dismiss();
    				removePlaceDialog(places);
    				break;
    			}
    		}
   		});

    	// Set up the buttons
    	builder.setPositiveButton("Done", new DialogInterface.OnClickListener() { 
    	    @Override
    	    public void onClick(DialogInterface dialog, int which) {
    	    	dialog.dismiss();
    	    }
    	});

    	builder.show();

    }
	
	// arrivals Dialog	
	private void arrivalsDialog(){
    	final CharSequence[] departuresDialogItems = {"My Places","New Place", "Remove Place"};
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	builder.setTitle("Options");
    	builder.setSingleChoiceItems(departuresDialogItems, -1, new DialogInterface.OnClickListener() {
    		@Override
    		public void onClick(DialogInterface dialog, int which) {
    			switch(which){
    			case 0:
    				dialog.dismiss();
    				selectArrivalPlaceDialog(places);
    				break;
    			case 1:
    				dialog.dismiss();
    				selectNewPlace();
    				break;
    			case 2:
    				dialog.dismiss();
    				removePlaceDialog(places);
    				break;
    			}
    		}
   		});

    	// Set up the buttons
    	builder.setPositiveButton("Done", new DialogInterface.OnClickListener() { 
    	    @Override
    	    public void onClick(DialogInterface dialog, int which) {
    	    	dialog.dismiss();
    	    }
    	});

    	builder.show();

    }
	
	// Select Departure Place Dialog	
	private void selectDeparturePlaceDialog(ArrayList<String> p){
    	final CharSequence[] csPlaces = p.toArray(new CharSequence[p.size()]);
    	int checkedItem = -1;
    	if(!selectedDeparture.equals(""))
    		checkedItem = places.indexOf(selectedDeparture);
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	builder.setTitle("Select one place");
    	builder.setSingleChoiceItems(csPlaces, checkedItem, new DialogInterface.OnClickListener() {
    		@Override
    		public void onClick(DialogInterface dialog, int which) {
    			Toast.makeText(getApplicationContext(),
    	    			"The selected place is " + csPlaces[which], Toast.LENGTH_LONG).show();
    			selectPlace(which, "departure");
    	    	dialog.dismiss();
    		}
   		});

    	builder.show();

    }
	
	// Select Arrival Place Dialog	
	private void selectArrivalPlaceDialog(ArrayList<String> p){
    	final CharSequence[] csPlaces = p.toArray(new CharSequence[p.size()]);
    	int checkedItem = -1;
    	if(!selectedArrival.equals(""))
    		checkedItem = places.indexOf(selectedArrival);
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	builder.setTitle("Select one place");
    	builder.setSingleChoiceItems(csPlaces, checkedItem, new DialogInterface.OnClickListener() {
    		@Override
    		public void onClick(DialogInterface dialog, int which) {
    			Toast.makeText(getApplicationContext(),
    	    			"The selected place is " + csPlaces[which], Toast.LENGTH_LONG).show();
    			selectPlace(which, "arrival");
    	    	dialog.dismiss();
    		}
   		});

    	builder.show();

    }
	
	private void selectPlace(int index, String type){
		TextView t;
		if(type.equals("arrival")){
			t = arrivalsText;
			selectedArrival = places.get(index);
		}
		else{
			t = departuresText;
			selectedDeparture = places.get(index);
		}
		t.setText(places.get(index));
		t.bringToFront();
		
	}
	
	// Remove Place Dialog	
	private void removePlaceDialog(ArrayList<String> p){
    	final CharSequence[] csPlaces = p.toArray(new CharSequence[p.size()]);

    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	builder.setTitle("Select one place");
    	builder.setSingleChoiceItems(csPlaces, -1, new DialogInterface.OnClickListener() {
    		@Override
    		public void onClick(DialogInterface dialog, int which) {
    			Toast.makeText(getApplicationContext(),
    	    			"The selected place is " + csPlaces[which], Toast.LENGTH_LONG).show();
    			removePlace(which);
    	    	dialog.dismiss();
    		}
   		});

    	builder.show();

    }
	
	private void removePlace(int index){
		places.remove(index);
	}
	
    public void response(String responseData){
        TextView t = (TextView)findViewById(R.id.fullscreen_content);
        t.setText(responseData);
    }
  
}
