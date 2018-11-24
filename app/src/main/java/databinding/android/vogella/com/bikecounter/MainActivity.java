package databinding.android.vogella.com.bikecounter;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mActionBarDrawerToggle;
    private NavigationView mNavigationView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //placeHolder = findViewById(R.id.activity_main);

        initViewObject();

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, MainFragment.newInstance())
                    .commitNow();
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        return mActionBarDrawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.navigation_menu, menu);
        return true;
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

                menuItem.setChecked(true);
                // close drawer when item is tapped
                mDrawerLayout.closeDrawers();


                switch (id) {
                    case R.id.profile:
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.container, Profile.newInstance())
                                .commitNow();
                        Toast.makeText(MainActivity.this, R.string.profile, Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.settings:
                        Toast.makeText(MainActivity.this, R.string.settings, Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.configuration:
                        Intent intent = new Intent(MainActivity.this, DeviceScanActivity.class);
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.container, DeviceScanActivity.newInstance())
                                .commitNow();
                        //getLayoutInflater().inflate(R.layout.configuration_fragment, placeHolder);
                        //startActivity(intent);
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
}
