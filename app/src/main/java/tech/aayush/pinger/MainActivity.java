package tech.aayush.pinger;

import android.Manifest;
import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.content.pm.PackageManager;
import android.os.Bundle;

import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;
import java.util.TimeZone;

public class MainActivity extends AppCompatActivity implements
        FetchAddressTask.OnTaskCompleted {

    String[] beaconList = {"www.andi.dz", "waib.gouv.bj", "www.univ-ouaga.bf", "www.assemblee.bi", "www.anor.cm"};
    TextView tv;

    private RegexFormat regexFormat;
    private DatabaseReference pingDatabase;

    // Constants
    private static final int REQUEST_LOCATION_PERMISSION = 1;
    private static final String TRACKING_LOCATION_KEY = "tracking_location";

    // Views
    private Button mLocationButton;
    private TextView mLocationTextView;
    private ImageView mAndroidImageView;

    // Location classes
    private boolean mTrackingLocation;
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationCallback mLocationCallback;
    FirebaseDatabase database;
    DatabaseReference myRef;
    String userId;
    // Animation
    private AnimatorSet mRotateAnim;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tv = (TextView) findViewById(R.id.ping_output);

        // Initialize Firebase
        FirebaseApp.initializeApp(this);

        regexFormat = new RegexFormat();
        pingDatabase = FirebaseDatabase.getInstance().getReference("Ping");

        mLocationButton = (Button) findViewById(R.id.location_button);
        mLocationTextView = (TextView) findViewById(R.id.longitude);
        mAndroidImageView = (ImageView) findViewById(R.id.image);

        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("message");
        userId = FirebaseAuth.getInstance().getUid();

        // Initialize the FusedLocationClient.
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(
                this);

        // Set up the animation.
        mRotateAnim = (AnimatorSet) AnimatorInflater.loadAnimator
                (this, R.animator.rotate);
        mRotateAnim.setTarget(mAndroidImageView);

        // Restore the state if the activity is recreated.
        if (savedInstanceState != null) {
            mTrackingLocation = savedInstanceState.getBoolean(
                    TRACKING_LOCATION_KEY);
        }

        // Set the listener for the location button.
        mLocationButton.setOnClickListener(new View.OnClickListener() {
            /**
             * Toggle the tracking state.
             * @param v The track location button.
             */
            @Override
            public void onClick(View v) {
                if (!mTrackingLocation) {
                    startTrackingLocation();
                } else {
                    stopTrackingLocation();
                }
            }
        });

        // Initialize the location callbacks.
        mLocationCallback = new LocationCallback() {
            /**
             * This is the callback that is triggered when the
             * FusedLocationClient updates your location.
             * @param locationResult The result containing the device location.
             */
            @Override
            public void onLocationResult(LocationResult locationResult) {
                // If tracking is turned on, reverse geocode into an address
                if (mTrackingLocation) {
                    new FetchAddressTask(MainActivity.this, MainActivity.this)
                            .execute(locationResult.getLastLocation());
                }
            }
        };
    }

    /**
     * Starts tracking the device. Checks for
     * permissions, and requests them if they aren't present. If they are,
     * requests periodic location updates, sets a loading text and starts the
     * animation.
     */
    private void startTrackingLocation() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]
                            {Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION_PERMISSION);
        } else {
            mTrackingLocation = true;
            mFusedLocationClient.requestLocationUpdates
                    (getLocationRequest(),
                            mLocationCallback,
                            null /* Looper */);

            // Set a loading text while you wait for the address to be
            // returned

            mLocationTextView.setText(getString(R.string.address_text,
                    getString(R.string.loading),
                    System.currentTimeMillis()));
            mLocationButton.setText(R.string.stop_tracking_location);
            mRotateAnim.start();
            myRef.child(userId).setValue("Hello, World!");


        }
    }


    /**
     * Stops tracking the device. Removes the location
     * updates, stops the animation, and resets the UI.
     */
    private void stopTrackingLocation() {
        if (mTrackingLocation) {
            mTrackingLocation = false;
            mLocationButton.setText(R.string.start_tracking_location);
            mLocationTextView.setText(R.string.textview_hint);
            mRotateAnim.end();

        }
    }


    /**
     * Sets up the location request.
     *
     * @return The LocationRequest object containing the desired parameters.
     */
    private LocationRequest getLocationRequest() {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return locationRequest;
    }


    /**
     * Saves the last location on configuration change
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(TRACKING_LOCATION_KEY, mTrackingLocation);
        super.onSaveInstanceState(outState);
    }

    /**
     * Callback that is invoked when the user responds to the permissions
     * dialog.
     *
     * @param requestCode  Request code representing the permission request
     *                     issued by the app.
     * @param permissions  An array that contains the permissions that were
     *                     requested.
     * @param grantResults An array with the results of the request for each
     *                     permission requested.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_LOCATION_PERMISSION:

                // If the permission is granted, get the location, otherwise,
                // show a Toast
                if (grantResults.length > 0
                        && grantResults[0]
                        == PackageManager.PERMISSION_GRANTED) {
                    startTrackingLocation();
                } else {
                    Toast.makeText(this,
                            R.string.location_permission_denied,
                            Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    @Override
    public void onTaskCompleted(String result) {
        if (mTrackingLocation) {
            // Update the UI
            mLocationTextView.setText(getString(R.string.address_text,
                    result, System.currentTimeMillis()));

            Long tsLong = System.currentTimeMillis()/1000;
            String ts = tsLong.toString();

            myRef.child(userId).child(ts).setValue(result);
        }
    }

    @Override
    protected void onPause() {
        if (mTrackingLocation) {
            stopTrackingLocation();
            mTrackingLocation = true;
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        if (mTrackingLocation) {
            startTrackingLocation();
        }
        super.onResume();
    }

    public void bPing(View view){
        Ping();
    }

    public void Ping(){
        new Thread(new Runnable() {
            public void run() {
                Random random = new Random();
                for(int i=0;i<beaconList.length;i++){//
                    //int i = random.nextInt(beaconList.length);
                    Editable host = new SpannableStringBuilder(beaconList[i]);
                    int count=0;
                    Process p = null;
                    try {
                        String pingCmd = "ping -n -c 10 -w 30 -i 1 -s 100 " + host;//-D doesnt work on android  ping -n -w %deadline -c %count -i %interval -s %packetsize %destination
                        String pingResult = "";

                        Runtime r = Runtime.getRuntime();
                        p = r.exec(pingCmd);
                        BufferedReader in = new BufferedReader(new
                                InputStreamReader(p.getInputStream()));
                        String inputLine;

                        while ((inputLine = in.readLine()) != null) {
                            //System.out.println("Tester "+inputLine+" space "+inputLine.toLowerCase().contains("rtt"));

                            //Starting point check when ping issued
                            if((!inputLine.toLowerCase().contains("ping"))){
                            }
                            inputLine = "[" + System.currentTimeMillis() / 1000 + "]" + inputLine;

                            System.out.println(inputLine);

                            //keep adding to block
                            pingResult += inputLine+"\n";
                            final String ping_result=pingResult;


                            //Show progress in UI
                            tv.post(new Runnable() {
                                public void run() {
                                    tv.setText(ping_result);
                                }
                            });

                        }

                        syncToFirebase(pingResult);

                        System.out.print("Error Stream: " + p.getErrorStream());
                        in.close();
                    }//try
                    catch (IOException e) {
                        System.out.println(e);
                        System.out.print("Error Stream: " + p.getErrorStream());
                    }
                }
            }
        }).start();
    }

    public void syncToFirebase(String pingResult){

        //int HostIP = regexFormat.getHostIP(pingResult, this);

        String Remote_Name = regexFormat.getRemoteName(pingResult);
        String Remote_Addr = regexFormat.getRemoteAddr(pingResult);

        String round_trip_time = regexFormat.getRoundTripTime(pingResult);

        String packets_received = regexFormat.getNumberOfPacketsReceived(pingResult);
        packets_received = packets_received.substring(0, packets_received.indexOf("r"));

        String packets_transmitted = regexFormat.getNumberOfPacketsSent(pingResult);
        packets_transmitted = packets_transmitted.substring(0, packets_transmitted.indexOf("p"));

        String values[] = regexFormat.getMinMaxAvg(pingResult);
        String min  = values[0];
        String avg = values[1];
        String max = values[2];
        String mdev = values[3];

        ArrayList<String> seq = regexFormat.countICMP(pingResult);
        ArrayList<String> rtt = regexFormat.timeOFEachIcm(pingResult);

        /*for (int i=0; i< seq.size(); i++){
            Log.d("Seq number", "" + seq.get(i));
        }

        for (int i=0; i< seq.size(); i++){
            Log.d("rtt number", "" + rtt.get(i));
        }*/

        Log.d("Ping output",Remote_Name + " " + Remote_Addr + " " + round_trip_time + " " + packets_received + " " + packets_transmitted + " " + min + " " + max + " " + avg + " " + mdev);


        String id = pingDatabase.push().getKey();
        PingOutputParameters pingOutputParameters = new PingOutputParameters(Remote_Name, Remote_Addr, round_trip_time, packets_received, packets_transmitted, min, avg, max, mdev, seq, rtt);
        pingDatabase.child(id).setValue(pingOutputParameters);

    }

}