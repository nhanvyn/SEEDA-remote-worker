package com.sereem.remoteworker.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.Dialog;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.internal.NavigationMenuItemView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.sereem.remoteworker.R;
import com.sereem.remoteworker.databinding.ActivityMainBinding;
import com.sereem.remoteworker.model.User;
import com.sereem.remoteworker.ui.ui_for_main.PopupSignOutFragment;
import com.sereem.remoteworker.ui.ui_for_main.service.LocationService;

import java.io.File;

/**
 * MainActivity class, used for displaying all features and manipulating with them.
 */
public class MainActivity extends AppCompatActivity {

    public static Uri iconUri;

    private AppBarConfiguration mAppBarConfiguration;
    private static View headerView;
    private ColorPalette colorPalette;
    private ActivityMainBinding binding;
    private SensorManager sensorManager;
    private Sensor sensor;
    private NavigationView navigationView;
    private DocumentReference documentReference;
    private StorageReference storageReference;
    private User user;
    private FusedLocationProviderClient mFusedLocationClient;
    private static final String TAG = "MainActivity";
    private File iconFile;
    private boolean isFirstStart = true;
    public static Intent serviceIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LocationService.SHUT_DOWN = false;
        binding = DataBindingUtil.setContentView
                (this, R.layout.activity_main);
        colorPalette = new ColorPalette(this, binding, ColorPalette.TYPE.MAIN);
        binding.setColorPalette(colorPalette);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_worksites, R.id.nav_profile, R.id.nav_settings)
                .setDrawerLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);

        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        sensorManager = (SensorManager) getSystemService(Service.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);

        initializeDocumentReference();
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);


        // get broadcast latLng from service
        headerView = navigationView.getHeaderView(0);
        TextView latLngText = headerView.findViewById(R.id.latTextView);

        LocalBroadcastManager.getInstance(this).registerReceiver(new BroadcastReceiver() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onReceive(Context context, Intent intent) {
                double lat = intent.getDoubleExtra(LocationService.EXTRA_LATITUDE, 0);
                double lng = intent.getDoubleExtra(LocationService.EXTRA_LONGITUDE, 0);
                latLngText.setText("[" + lat + " °N, " + lng + " °W]");
            }
        }, new IntentFilter(LocationService.ACTION_LOCATION_BROADCAST));
    }

    private void downloadIconFromUri() {
        if(!isFirstStart) {
            iconUri = Uri.fromFile(iconFile);
            return;
        }
        isFirstStart = false;
        iconUri = Uri.fromFile(iconFile);
        storageReference.getFile(iconUri).addOnCompleteListener(task -> {
            if(task.isSuccessful()) {
                showUserInfo();
            }
        });
    }

    private void initializeStorageReference() {
        storageReference = FirebaseStorage.getInstance().getReference("profileIcons/" +
                user.getUID() + ".jpeg");
    }

    private void initializeDocumentReference() {
        SharedPreferences prefs = getSharedPreferences("user", MODE_PRIVATE);
        String UID = prefs.getString("UID", "");
        documentReference = FirebaseFirestore.getInstance().document(
                "/users/" + UID + "/");
        documentReference.addSnapshotListener((value, error) -> {
            if(error != null && error.getCode() !=
                    FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                ErrorDialog.show(this);
            }
            else if(value != null && value.exists()) {
                user = User.createNewInstance(value.toObject(User.class));
                iconFile = new File(getCacheDir() + "/" + user.getUID() + ".jpeg");
                initializeStorageReference();
                downloadIconFromUri();
                showUserInfo();
            }
        });
    }

    private void setUpSignOutOption() {
        NavigationMenuItemView signOut = navigationView.findViewById(R.id.nav_sign_out);
        signOut.setOnClickListener(v -> {
            PopupSignOutFragment dialog = new PopupSignOutFragment(MainActivity.this);
            dialog.showDialog(getSupportFragmentManager());
        });
    }

    @SuppressLint("SetTextI18n")
    private void showUserInfo() {
        headerView = navigationView.getHeaderView(0);
        ImageView icon = headerView.findViewById(R.id.profileIconMain);
        if(user.getIconUri() != null && !user.getIconUri().equals("")) {
            icon.setImageURI(iconUri);
        } else {
            icon.setImageResource(R.drawable.profile_icon);
        }
        TextView emailText = headerView.findViewById(R.id.emailMainTextView);
        emailText.setText(user.getEmail());
        TextView nameText = headerView.findViewById(R.id.nameMainTextView);
        nameText.setText(user.getFirstName() + " " + user.getLastName());
    }

    public static View getHeaderView() {
        return headerView;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        setUpSignOutOption();
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    public static Intent makeLaunchIntent(Context context, String email, String password) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra("email", email);
        intent.putExtra("password", password);
        return intent;
    }

    @Override
    protected void onPause() {
        super.onPause();
        colorPalette.unregisterListener();
    }


    @Override
    protected void onResume() {
        super.onResume();
        colorPalette.registerListener();
        System.out.println("Resume Location service from mainActivtity");
        checkLocationService();
    }

    public void checkLocationService() {
        if(checkMapServices()){
            System.out.println("CheckMapService is true ");
            getLocationPermission();
            getLastKnownLocation();
        }
    }


    private void getLastKnownLocation() {



        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Log.d(TAG, "getLastKnownLocation: called WW");
        mFusedLocationClient.getLastLocation().addOnCompleteListener(task -> {
            Log.d(TAG, "onComplete: called.");
            if (task.isSuccessful()) {
                Log.d(TAG, "startLocationService: called.");
                startLocationService();
            }
        });

    }


    private void startLocationService(){
        if(!isLocationServiceRunning()){
            serviceIntent = new Intent(this, LocationService.class);
            System.out.println("Service intent is created");
            System.out.println("MainActivity: visibility " + getVisibilityPreference());
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O){
               if (getVisibilityPreference()){
                   System.out.println("startForegroundService is called");
                   MainActivity.this.startForegroundService(serviceIntent);
               }
            }else{
                if (getVisibilityPreference()) {
                    System.out.println("MainActivity: startService is called");
                    startService(serviceIntent);
                }
            }
        }
    }

    private boolean getVisibilityPreference(){
        SharedPreferences prefs = this.getSharedPreferences("user", Context.MODE_PRIVATE);
        boolean value = prefs.getBoolean("visibility", true);
        System.out.println("visibility: " + value);
        return prefs.getBoolean("visibility", true);
    }

    public boolean isServicesOK(){
        Log.d(TAG, "isServicesOK: checking google services version");

        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(MainActivity.this);

        if(available == ConnectionResult.SUCCESS){
            //everything is fine and the user can make map requests
            Log.d(TAG, "isServicesOK: Google Play Services is working");
            return true;
        }
        else if(GoogleApiAvailability.getInstance().isUserResolvableError(available)){
            //an error occured but we can resolve it
            Log.d(TAG, "isServicesOK: an error occured but we can fix it");
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(MainActivity.this, available, 1000);
            dialog.show();
        }else{
            CustomSnackbar.create(getWindow().getDecorView()).setText("You can't make map requests")
                    .show();
        }
        return false;
    }

    private boolean checkMapServices(){
        return isServicesOK();
    }

    private boolean isLocationServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)){
            if("com.sereem.remoteworker.ui.ui_for_main.service.LocationService.java".equals(service.service.getClassName())) {
                Log.d(TAG, "isLocationServiceRunning: location service is already running.");
                return true;
            }
        }
        Log.d(TAG, "isLocationServiceRunning: location service is not running.");
        return false;
    }


    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            getLastKnownLocation();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    100);
        }
    }
}