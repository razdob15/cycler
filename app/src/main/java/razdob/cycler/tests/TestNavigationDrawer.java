package razdob.cycler.tests;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.DatePicker;
import android.widget.Toast;
import android.support.v7.widget.Toolbar;

import razdob.cycler.R;

/**
 * Created by Raz on 18/03/2018, for project: PlacePicker2
 */

public class TestNavigationDrawer extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mToggle;

    private Toolbar mToolbar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.navigation_drawer_test);

        mToolbar = findViewById(R.id.nav_action);
        setSupportActionBar(mToolbar);

        mDrawerLayout = findViewById(R.id.drawer_layout);
        mToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.open, R.string.close);

        mDrawerLayout.addDrawerListener(mToggle);
        mToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        NavigationView navigationView = findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (mToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

//        if (id == R.id.places){
//            Toast.makeText(this, "DashBoard Click", Toast.LENGTH_SHORT).createFragment();
//        }
        if (id == R.id.settings){
            Toast.makeText(this, "DashBoard Click", Toast.LENGTH_SHORT).show();
        }
        if (id == R.id.event){
            Toast.makeText(this, "Settings Click", Toast.LENGTH_SHORT).show();
        }
        if (id == R.id.search){
            Toast.makeText(this, "Search Click", Toast.LENGTH_SHORT).show();
        }
        if (id == R.id.favorites){
            Toast.makeText(this, "Activities Click", Toast.LENGTH_SHORT).show();
        }
        if (id == R.id.logout){
            Toast.makeText(this, "Logout Click", Toast.LENGTH_SHORT).show();
        }

        return false;
    }
}