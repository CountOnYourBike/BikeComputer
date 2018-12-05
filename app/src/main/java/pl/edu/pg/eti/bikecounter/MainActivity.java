package pl.edu.pg.eti.bikecounter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

import static pl.edu.pg.eti.bikecounter.DeviceScanFragment.REQUEST_ENABLE_BT;
import static pl.edu.pg.eti.bikecounter.DeviceScanFragment.REQUEST_LOCATION_PERMISSION;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mActionBarDrawerToggle;
    private NavigationView mNavigationView;
    private Double wheelCirc;
    private double distance = 0;
    private boolean mConnected = false;
    private boolean mPaused = false;
    private boolean mStarted = false;
    //TODO: Obsłużyć czas przejazdu
    // time of actual ride (since play pressed)
    private long mRideTime = 0;
    // time without actual ride
    private long mTime = 0;


    long startTime = 0;
    private double mTotalTime = 0.001;


    private static final String PREFERENCES = "pl.edu.pg.eti.bikecounter.preferences";
    protected SharedPreferences mSharedPreferences;
    protected SharedPreferences.Editor mEditor;
    //runs without a timer by reposting this handler at the end of the runnable
    Handler timerHandler = new Handler();
    Runnable timerRunnable = new Runnable() {

        @Override
        public void run() {
            mTime = System.currentTimeMillis() - startTime;
            long millis = System.currentTimeMillis() - startTime + mRideTime;
            int seconds = (int) (millis / 1000);
            int minutes = seconds / 60;
            int hours = minutes / 60;
            minutes = minutes % 60;
            seconds = seconds % 60;


            ((TextView)findViewById(R.id.total_time)).setText(String.format(Locale.ENGLISH,"%d:%02d:%02d", hours, minutes, seconds));

            timerHandler.postDelayed(this, 500);
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        initPreferences();
        initViewObject();

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, MainFragment.newInstance())
                    .addToBackStack("home")
                    .commit();
            mNavigationView.getMenu().getItem(0).setChecked(true);
        }

    }

    @Override
    protected void onStop(){
        mEditor.putBoolean("FirstUse",false);
        mEditor.commit();
        super.onStop();
    }

    private void initPreferences() {
        mSharedPreferences = getSharedPreferences(PREFERENCES,Context.MODE_PRIVATE);
        mEditor = mSharedPreferences.edit();


        wheelCirc = Double.valueOf(mSharedPreferences.getString("wheelCirc","2100"));
        mSharedPreferences.getString("WheelSizeScale",getString(R.string.circ_systems));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        return mActionBarDrawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.navigation_menu, menu);
        return true;
    }

    @Override
    public void onBackPressed() {
        FragmentManager manager = getSupportFragmentManager();
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else if(manager.getBackStackEntryCount() >= 2){
            super.onBackPressed();
            Fragment currentFragment = manager.findFragmentById(R.id.container);
            if(currentFragment instanceof MainFragment){
                mNavigationView.getMenu().getItem(0).setChecked(true);
            }
            if(currentFragment instanceof Settings){
                mNavigationView.getMenu().getItem(1).setChecked(true);
            }
            else if(currentFragment instanceof DeviceScanFragment){
                mNavigationView.getMenu().getItem(2).setChecked(true);
            }
        } else {
            finish();
        }
    }


    private void initViewObject(){
        mDrawerLayout = findViewById(R.id.activity_main);
        mActionBarDrawerToggle = new ActionBarDrawerToggle(this,mDrawerLayout,R.string.open,R.string.close);

        mDrawerLayout.addDrawerListener(mActionBarDrawerToggle);
        mActionBarDrawerToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mNavigationView = findViewById(R.id.navigation_view);
        mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                int id = menuItem.getItemId();

                if(id != R.id.exit)
                    menuItem.setChecked(true);
                // close drawer when item is tapped
                mDrawerLayout.closeDrawers();


                switch (id) {
                    case R.id.home:
                        if(!getSupportFragmentManager().getBackStackEntryAt(getSupportFragmentManager().getBackStackEntryCount()-1).getName().equals("home")) {
                            getSupportFragmentManager().beginTransaction()
                                    .replace(R.id.container, MainFragment.newInstance(), "home")
                                    .addToBackStack("home")
                                    .commit();
                        }
                        break;
                    case R.id.settings:
                        if(!getSupportFragmentManager().getBackStackEntryAt(getSupportFragmentManager().getBackStackEntryCount()-1).getName().equals("settings")) {
                            getSupportFragmentManager().beginTransaction()
                                    .replace(R.id.container, Settings.newInstance(), "settings")
                                    .addToBackStack("settings")
                                    .commit();
                        }
                        break;
                    case R.id.configuration:
                        if(!getSupportFragmentManager().getBackStackEntryAt(getSupportFragmentManager().getBackStackEntryCount()-1).getName().equals("configuration")) {
                            getSupportFragmentManager().beginTransaction()
                                    .replace(R.id.container, DeviceScanFragment.newInstance(), "configuration")
                                    .addToBackStack("configuration")
                                    .commit();
                        }
                        break;
                    case R.id.exit:
                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                        builder.setTitle(getString(R.string.exit))
                                .setMessage(getString(R.string.really_want_to_exit))
                                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        finishAndRemoveTask();
                                    }
                                })
                                .setNegativeButton(android.R.string.no, null);
                        builder.create().show();
                        break;
                }
                return true;
            }
        });
    }

    public Double getWheelCirc() {
        return wheelCirc;
    }

    public void setWheelCirc(Double wheelCirc) {
        this.wheelCirc = wheelCirc;
        mEditor.putString("wheelCirc",Double.toString(wheelCirc));
        mEditor.commit();
    }

    public boolean isConnected() {
        return mConnected;
    }

    public void setConnected(boolean connected) {
        this.mConnected = connected;
    }

    public double getDistance() {
        return Math.floor(distance*100)/100;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }

    public void addToDistance(double distanceToAdd) {
        this.distance += distanceToAdd;
    }

    public boolean isStarted() {
        return mStarted;
    }

    public void setStarted(boolean mStarted) {
        this.mStarted = mStarted;
        if(isStarted()) {
            startTime = System.currentTimeMillis();
            timerHandler.postDelayed(timerRunnable, 0);
        }
        else {
            removeTimeCallback();
            mRideTime = 0;
            mTime = 0;
            distance = 0;
        }
    }

    public void removeTimeCallback() {
        timerHandler.removeCallbacks(timerRunnable);
    }

    public boolean isPaused() {
        return mPaused;
    }

    public void setPaused(boolean mPaused) {
        this.mPaused = mPaused;
        if(isPaused()) {
            removeTimeCallback();
            mRideTime = getTotalTime();
        }
    }

    public long getTotalTime() {
        return mTime + mRideTime;
    }

    public double getTotalTimeInHours() {
        double totalTimeinHours = (double)getTotalTime();
        totalTimeinHours /= 3600000.;
        return totalTimeinHours;
    }

    //it works only in activity, not in fragment
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_LOCATION_PERMISSION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted
                    Toast.makeText(getBaseContext(), getString(R.string.permission_granted), Toast.LENGTH_SHORT).show();
                    getSupportFragmentManager().beginTransaction().replace(R.id.container, MainFragment.newInstance(), "home")
                            .commitNow();
                } else {
                    // permission denied
                    Toast.makeText(getBaseContext(), getString(R.string.location_permission_denied), Toast.LENGTH_SHORT).show();
                    //onBackPressed();
                }
                break;
            }
        }
    }

    //it works only in activity, not in fragment, it will catch all fragments activityResults
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // User chose not to enable Bluetooth.
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            Toast.makeText(getBaseContext(),getString(R.string.without_bluetooth_unable_to_search), Toast.LENGTH_SHORT).show();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, MainFragment.newInstance(), "home")
                    .commitNow();
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
