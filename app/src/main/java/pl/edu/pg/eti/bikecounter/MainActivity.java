package pl.edu.pg.eti.bikecounter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
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
import android.widget.Toast;

import static pl.edu.pg.eti.bikecounter.DeviceScanFragment.REQUEST_ENABLE_BT;
import static pl.edu.pg.eti.bikecounter.DeviceScanFragment.REQUEST_LOCATION_PERMISSION;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mActionBarDrawerToggle;
    private NavigationView mNavigationView;
    public Double circuit = 2100.;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
            //getSupportFragmentManager().popBackStack();
            Fragment currentFragment = manager.findFragmentById(R.id.container);
            if(currentFragment instanceof MainFragment){
                mNavigationView.getMenu().getItem(0).setChecked(true);
            }
            if(currentFragment instanceof Profile){
                mNavigationView.getMenu().getItem(1).setChecked(true);
            }
//            else if(currentFragment instanceof Settings){
//                mNavigationView.getMenu().getItem(2).setChecked(true);
//            }
            else if(currentFragment instanceof DeviceScanFragment){
                mNavigationView.getMenu().getItem(3).setChecked(true);
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
//        mActionBarDrawerToggle.setDrawerIndicatorEnabled(true);
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
                                    .replace(R.id.container, MainFragment.newInstance())
                                    .addToBackStack("home")
                                    .commit();
                        }
                        break;
                    case R.id.profile:
                        if(!getSupportFragmentManager().getBackStackEntryAt(getSupportFragmentManager().getBackStackEntryCount()-1).getName().equals("profile")) {
                            getSupportFragmentManager().beginTransaction()
                                    .replace(R.id.container, Profile.newInstance())
                                    .addToBackStack("profile")
                                    .commit();
                        }
                        break;
                    case R.id.settings:
                        Toast.makeText(MainActivity.this, R.string.settings, Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.configuration:
                        if(!getSupportFragmentManager().getBackStackEntryAt(getSupportFragmentManager().getBackStackEntryCount()-1).getName().equals("configuration")) {
                            getSupportFragmentManager().beginTransaction()
                                    .replace(R.id.container, DeviceScanFragment.newInstance())
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

    public Double getCircuit() {
        return circuit;
    }

    public void setCircuit(Double circuit) {
        this.circuit = circuit;
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
                } else {
                    // permission denied
                    Toast.makeText(getBaseContext(), getString(R.string.permission_denied), Toast.LENGTH_SHORT).show();
                    onBackPressed();
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
                    .replace(R.id.container, MainFragment.newInstance())
                    .commitNow();
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
