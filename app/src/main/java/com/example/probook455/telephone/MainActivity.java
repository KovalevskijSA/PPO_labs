package com.example.probook455.telephone;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import android.util.Log;
import android.view.MenuItem;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.view.Menu;
import android.net.Uri;

public class MainActivity extends AppCompatActivity
        implements ProfileFragment.OnFragmentInteractionListener{
    private UserRepository userRepository;
    private NavController navController = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        userRepository = new UserRepository();
        if (FirebaseAuth.getInstance().getCurrentUser() == null){
            startAuthActivity();
            return;
        }
        setContentView(R.layout.activity_main);
        navController = Navigation.findNavController(findViewById(R.id.fragment));
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        NavigationView nvDrawer = (NavigationView) findViewById(R.id.nav_view);
        setupDrawerContent(nvDrawer);
    }

    public void startAuthActivity(){
        startActivity(new Intent(this, AuthActivity.class));
        finish();
    }

    private  void setupDrawerContent(NavigationView navigationView){
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                        selectDrawerItem(menuItem);
                        return false;
                    }
                });
    }

    public void selectDrawerItem(MenuItem menuItem) {
        if (navController.getCurrentDestination().getId() == R.id.profileEditFragment) {
            askAndNavigateToFragment(menuItem.getItemId(), null);
            return;
        }
        navigate(menuItem.getItemId());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_settings:
                if (navController.getCurrentDestination().getId() == R.id.profileEditFragment) {
                    askAndNavigateToFragment(0, this);
                }
                else{
                    startActivity(new Intent(this, AboutActivity.class));
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }
        if (navController.getCurrentDestination().getId() == R.id.profileEditFragment) {
            askAndNavigateToFragment(R.id.nav_profile, null);
        }
        else {
            super.onBackPressed();
        }
    }
    private void navigate(int id){
        switch (id){
            case R.id.nav_profile:
                navController.navigate(R.id.profileFragment);
                break;
            case R.id.nav_news:
                navController.navigate(R.id.newsFragment);
                break;
            case R.id.nav_home:
                navController.navigate(R.id.homeFragment);
                break;
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
    }

    private void askAndNavigateToFragment(final int fragmentId, final Context cn) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.youre_about_to_loose_changes)
                .setPositiveButton(R.string.leave, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        navController.popBackStack();
                        if (cn == null){
                            navigate(fragmentId);
                        }
                        else startActivity(new Intent(cn, AboutActivity.class));
                    }
                })
                .setNegativeButton(R.string.stay, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
                        drawer.closeDrawer(GravityCompat.START);
                    }
                });
        builder.create().show();
    }

}
