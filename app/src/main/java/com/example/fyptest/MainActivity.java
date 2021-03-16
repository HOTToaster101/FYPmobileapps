package com.example.fyptest;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

import org.opencv.android.OpenCVLoader;

import java.util.ArrayList;
import java.util.Objects;

import io.mattcarroll.hover.overlay.OverlayPermission;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    boolean fabOpenedFlag = false;
    FloatingActionButton fabCamera;
    FloatingActionButton fabShare;
    ArrayList<String> mPermissions = new ArrayList();
    private static final int ALL_PERMISSIONS_RESULT = 1011;
    private static final int REQUEST_CODE_HOVER_PERMISSION = 1000;
    private boolean mPermissionsRequested = false;


    /*static {
        if (!OpenCVLoader.initDebug())
            Log.d("ERROR", "Unable to load OpenCV");
        else
            Log.d("SUCCESS", "OpenCV loaded");
    }*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //Floating Button Setting
        FloatingActionButton fab = findViewById(R.id.fab);
        fabCamera = findViewById(R.id.fab_camera);
        fabShare = findViewById(R.id.fab_share);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               if(!fabOpenedFlag){
                   OpenFab();
               }else{
                   CloseFab();
               }
            }
        });
        fabCamera.setOnClickListener(new View.OnClickListener() {
              @Override
              public void onClick(View view) {
                  Intent intent = new Intent(getApplication(), ActivityCamera.class);
                   startActivity(intent);
              }
              //public void onClick(View view) {
              //                               Intent intent = new Intent(getApplication(), Grabcut.class);
               //                              startActivity(intent);
                //                         }

        });
        fabShare.setOnClickListener(new View.OnClickListener() {
                                         @Override
                                         public void onClick(View view) {
                                             Intent intent = new Intent(getApplication(), ActivityVideoCamera.class);
                                             startActivity(intent);
                                         }
                                     });
        //Navigation View Setting
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow, R.id.fragment_sticker)
                .setDrawerLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
        //HoverMenu Initiation
        Button hoverLaunch = findViewById(R.id.b_launch);
        hoverLaunch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FYPHoverMenuService.showFloatingMenu(getApplicationContext());
            }
        });

        //Start requesting permission
        mPermissions.add(Manifest.permission.CAMERA);
        mPermissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        mPermissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        mPermissions.add(Manifest.permission.MANAGE_EXTERNAL_STORAGE);
        mPermissions.add(Manifest.permission.SYSTEM_ALERT_WINDOW);

        ArrayList<String> mPermissionsToRequest = permissionsToRequest(mPermissions);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (mPermissionsToRequest.size() > 0) {
                requestPermissions(mPermissionsToRequest.toArray(
                        new String[mPermissionsToRequest.size()]),
                        ALL_PERMISSIONS_RESULT);
            }
        }

        if (!mPermissionsRequested && !OverlayPermission.hasRuntimePermissionToDrawOverlay(this)) {
            @SuppressWarnings("NewApi")
            Intent myIntent = OverlayPermission.createIntentToRequestOverlayPermission(this);
            startActivityForResult(myIntent, REQUEST_CODE_HOVER_PERMISSION);
        }
        //End requesting permission
    }

    private ArrayList<String> permissionsToRequest(ArrayList<String> permissions) {
        ArrayList<String> result = new ArrayList<>();
        for (String permission : permissions)
            if (!hasPermission(permission))
                result.add(permission);
        return result;
    }

    private boolean hasPermission(String permission) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            return Objects.requireNonNull(this)
                    .checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
        return true;
    }

    //END requesting permission

    private void OpenFab() {
        fabCamera.animate().translationY(-getResources().getDimension(R.dimen.fab_camera_margin));
        fabShare.animate().translationY(-getResources().getDimension(R.dimen.fab_share_margin));
        fabOpenedFlag = true;
    }

    private void CloseFab(){
        fabCamera.animate().translationY(0);
        fabShare.animate().translationY(0);
        fabOpenedFlag = false;
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
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (REQUEST_CODE_HOVER_PERMISSION == requestCode) {
            mPermissionsRequested = true;
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }


    /*@Override
    public void onResume()
    {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d("OpenCV", "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            //OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_4_0, this, mLoaderCallback);
        } else {
            Log.d("OpenCV", "OpenCV library found inside package. Using it!");
            //mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
            Toast.makeText(this, "opencv load ", Toast.LENGTH_LONG).show();
        }
    }*/
}