package com.example.khel;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.Switch;
import android.widget.ToggleButton;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

import java.util.HashMap;
import java.util.Map;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    //Initialize variable
    private GoogleMap mMap;
    SupportMapFragment mapFragment;
    FusedLocationProviderClient client;
    Switch mSwitch;

    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    String userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //Firebase
        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setTimestampsInSnapshotsEnabled(true)
                .build();
        fStore.setFirestoreSettings(settings);



        //Save into firebase
        mSwitch = findViewById(R.id.invisibleSwitchID);

        if (mSwitch.isChecked()) {
           showOnMap();


        }




        //Initialize fused location
        client = LocationServices.getFusedLocationProviderClient(this);

        //Check Permission
        if (ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.
                ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            //When permission granted
            //Call method
            getCurrentLocation();
        }else{
            //when permission denied
            //request permission
            ActivityCompat.requestPermissions(MapsActivity.this, new String[]{Manifest.permission.
                    ACCESS_FINE_LOCATION},44);
        }

    }

    private void getCurrentLocation() {

        //Initialize task location
        Task<Location> task = client.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(final Location location) {
                //When successful
                if (location != null){
                    //Sync map
                    mapFragment.getMapAsync(new OnMapReadyCallback() {
                        @Override
                        public void onMapReady(GoogleMap googleMap) {

                            //Initialize lat lng
                            LatLng latLng = new LatLng(location.getLatitude(),location.getLongitude());

                            //Create marker options
                            Marker marker = mMap.addMarker(new MarkerOptions().position(latLng).title("me"));
                            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,10));
                            marker.setAlpha(1.0f);
                            marker.setVisible(true);
                            marker.setPosition(latLng);

                            //Show button to allow user to go to their current location
                            mMap.setMyLocationEnabled(true);
                            mMap.getUiSettings().setMyLocationButtonEnabled(true);
                            mMap.setPadding(0,2050,0,0);

                        }


                    });
                }
            }
        });

    }

    private void showOnMap (){
        getCurrentLocation();

        String userLocation = mMap.toString().trim();
        //Store into firebase
        userID = fAuth.getCurrentUser().getUid();
        DocumentReference documentReference = fStore.collection("userLocation").document(userID);

        Map<String, Object> allUser = new HashMap<>();
        allUser.put("userLctn",userLocation );
        documentReference.set(allUser).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d("TAG", "onSuccess: user location is saved for" + userID);
            }
        });



    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 44){
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                //When permission denied
                //call method
                getCurrentLocation();
            }
        }
    }



    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;
    }
}
