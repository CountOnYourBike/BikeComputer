package pl.edu.pg.eti.bikecounter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

import static pl.edu.pg.eti.bikecounter.DeviceScanFragment.REQUEST_LOCATION_PERMISSION;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mActionBarDrawerToggle;
    private NavigationView mNavigationView;
    private double wheelCirc;
    private double distance = 0;
    private boolean mConnected = false;
    private boolean mPaused = false;
    private boolean mStarted = false;
    private static final String PREFERENCES = "pl.edu.pg.eti.bikecounter.preferences";
    protected SharedPreferences mSharedPreferences;
    protected SharedPreferences.Editor mEditor;

    // time of actual ride (since play pressed)
    private long mRideTime = 0;
    // time without actual ride
    private long mTime = 0;
    long startTime = 0;

    private static final String DEFAULT_WHEEL_CIRC = "2100 mm";


    // runs without a timer by re-posting this handler at the end of the runnable
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

            ((TextView) findViewById(R.id.total_time))
                    .setText(String.format(
                            Locale.ENGLISH,"%d:%02d:%02d", hours, minutes, seconds));

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
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container, MainFragment.newInstance())
                    .addToBackStack("home")
                    .commit();
            mNavigationView.getMenu().getItem(0).setChecked(true);
        }

    }

    private void initPreferences() {
        mSharedPreferences = getSharedPreferences(PREFERENCES,Context.MODE_PRIVATE);
        mEditor = mSharedPreferences.edit();

        String wheelSizeSystem =
                mSharedPreferences.getString("WheelSizeSystem", null);
        String wheelSizeString = (mSharedPreferences.getString("WheelSize", null));
        if(wheelSizeString == null || wheelSizeSystem == null) {
            wheelSizeSystem = getString(R.string.circ_system);
            wheelSizeString = DEFAULT_WHEEL_CIRC;
        }
        try {
            wheelCirc = Wheel.getCircValue(getApplicationContext(), wheelSizeSystem, wheelSizeString);
        } catch (IllegalArgumentException ex) {
            wheelCirc = Integer.parseInt(DEFAULT_WHEEL_CIRC);
            wheelSizeSystem = getString(R.string.circ_system);
            wheelSizeString = DEFAULT_WHEEL_CIRC;
        }

        mEditor.putString("WheelSizeSystem", wheelSizeSystem);
        mEditor.putString("WheelSize", wheelSizeString);
        mEditor.apply();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        return mActionBarDrawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        FragmentManager manager = getSupportFragmentManager();
        if(mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else if(manager.getBackStackEntryCount() >= 2){
            super.onBackPressed();
            Fragment currentFragment = manager.findFragmentById(R.id.container);
            if(currentFragment instanceof MainFragment){
                mNavigationView.getMenu().getItem(0).setChecked(true);
            }
            if(currentFragment instanceof SettingsFragment){
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
        mActionBarDrawerToggle = new ActionBarDrawerToggle(
                this, mDrawerLayout, R.string.open, R.string.close);

        mDrawerLayout.addDrawerListener(mActionBarDrawerToggle);
        mActionBarDrawerToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mNavigationView = findViewById(R.id.navigation_view);
        mNavigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                int id = menuItem.getItemId();

                if(id != R.id.exit)
                    menuItem.setChecked(true);
                // close drawer when item is tapped
                mDrawerLayout.closeDrawers();

                String lastOpenedFragmentName = getSupportFragmentManager().getBackStackEntryAt(
                        getSupportFragmentManager().getBackStackEntryCount()-1).getName();
                if(lastOpenedFragmentName == null) {
                    lastOpenedFragmentName = "";
                }
                switch (id) {
                    case R.id.home:
                        if(!lastOpenedFragmentName.equals("home")) {
                            getSupportFragmentManager()
                                    .beginTransaction()
                                    .replace(R.id.container, MainFragment.newInstance(),
                                            "home")
                                    .addToBackStack("home")
                                    .commit();
                        }
                        break;
                    case R.id.settings:
                        if(!lastOpenedFragmentName.equals("settings")) {
                            getSupportFragmentManager()
                                    .beginTransaction()
                                    .replace(R.id.container, SettingsFragment.newInstance(),
                                            "settings")
                                    .addToBackStack("settings")
                                    .commit();
                        }
                        break;
                    case R.id.configuration:
                        if(!lastOpenedFragmentName.equals("configuration")) {
                            getSupportFragmentManager()
                                    .beginTransaction()
                                    .replace(R.id.container, DeviceScanFragment.newInstance(),
                                            "configuration")
                                    .addToBackStack("configuration")
                                    .commit();
                        }
                        break;
                    case R.id.exit:
                        AlertDialog.Builder builder =
                                new AlertDialog.Builder(MainActivity.this);
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
        mEditor.putString("wheelCirc", Double.toString(wheelCirc));
        mEditor.apply();
    }

    public boolean isConnected() {
        return mConnected;
    }

    public void setConnected(boolean connected) {
        this.mConnected = connected;
    }

    public double getDistance() {
        return distance;
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
        } else {
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

    private long getTotalTime() {
        return mTime + mRideTime;
    }

    public double getTotalTimeInHours() {
        double totalTimeInHours = Double.longBitsToDouble(getTotalTime());
        // so far we get time in milliseconds, so we have to divide it by 1000*60*60
        totalTimeInHours /= 3600000.;
        return totalTimeInHours;
    }

    // results are handled in activity, not in fragment,
    // it will catch all fragments request permission results
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_LOCATION_PERMISSION:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted
                    Toast.makeText(getBaseContext(),
                            getString(R.string.permission_granted),
                            Toast.LENGTH_SHORT)
                            .show();
                } else {
                    // permission denied
                    Toast.makeText(getBaseContext(),
                            getString(R.string.location_permission_denied),
                            Toast.LENGTH_SHORT)
                            .show();
                }
                break;
            default:
                break;
        }
    }
}
