package com.example.musicmoji;

import androidx.annotation.NonNull;
//import android.app.FragmentManager;
//import android.app.FragmentTransaction;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.widget.FrameLayout.LayoutParams;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;
import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.PlayerApi;
import com.spotify.android.appremote.api.SpotifyAppRemote;
import com.spotify.protocol.types.Track;
import com.spotify.sdk.android.auth.AuthorizationClient;

import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.SpotifyAppRemote;

import com.spotify.protocol.client.Subscription;
import com.spotify.protocol.types.PlayerState;
import com.spotify.protocol.types.Track;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;


public class Main_Activity_Page extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private DrawerLayout drawer;


    // Initializes the client information and other Spotify connection parameters
    // includes a callback uri so after login, the user returns back to the app
    private static final String CLIENT_ID = "76d0e59cae5b4af9bfc445a3ceaf3cfe";
    private static final String REDIRECT_URI = "http://musicmoji.com/callback/";
    public static SpotifyAppRemote mSpotifyAppRemote;
    public static Boolean isPlaying;
    // Set the connection parameters
    public static ConnectionParams connectionParams =
            new ConnectionParams.Builder(CLIENT_ID)
                    .setRedirectUri(REDIRECT_URI)
                    .showAuthView(true)
                    .build();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_page);

        // Initialize toolbar and set up the support action
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // set isPlaying (from Spotify) to false
        isPlaying = false;

        // find drawer layout
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        // implement by declaring an navigation item listener to listen for clicks
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Initialize the toggle bar for drawer (navigation menu)
        // syncs the toggle so it opens and closes on toggle clicks
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();


        // Load the instructions/welcome fragment whenever onCreate is called
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new Instructions()).commit();
    }


    // --------------------------------Setting up the Navigation Bar and Toolbar with Search---------------------------------------------------------


    // implements navigation item listener to listen for click/selection events
    // uses switch statement to see which nav item is selected
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_server:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new ServerSongFragment()).commit();
//                Toast.makeText(this, "Server Clicked", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_spotify:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new SpotifySongFragment()).commit();
//                Toast.makeText(this, "Spotify Clicked", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_settings:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new SettingsFragment()).commit();
//                Toast.makeText(this, "Settings Clicked", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_logout:
                // for now just make a toast
                Log.d("logout", "onNavigationItemSelected: logout successful!");
//                Toast.makeText(this, "Signed out", Toast.LENGTH_SHORT).show();
                AuthorizationClient.clearCookies(this.getBaseContext());

                this.finish();
                break;
        }
        // we want to close drawer/navigation bar after we load a fragment
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    // opens the drawer/navigation menu bar
    @Override
    public void onBackPressed() {
        // checks whether drawer is open
        // START indicates the drawer is on the left
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }


    // followed https://www.youtube.com/watch?v=rCmF2Ie1m0Y to create
    // and understand how the search action view was created and
    // setup. This method loads the menu item into the toolbar, so it
    // is viewable in the activity
    // Follow https://stackoverflow.com/questions/34603157/how-to-get-a-text-from-searchview
    // to create the listeners
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // inflates the search toolbar item into the menu
        getMenuInflater().inflate(R.menu.search_toolbar, menu);

        // find the menu item using id
        MenuItem searchItem = menu.findItem(R.id.search);

        // create a searchview that is initialized so that we can
        // set a listener to listen for the when user submits a query
        SearchView sv = (SearchView) searchItem.getActionView();

        // creates and implements the setOnQueryTextListener to listen
        // for text submissions
        sv.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                Toast.makeText(getBaseContext(), query, Toast.LENGTH_SHORT).show();
                return true;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        return true;
    }


    //-----------------------------------------------------------Spotify Connection Methods---------------------------------------------------

    // This method starts the Spotify Connection by opening up
    // the connection once user is authenticated
    @Override
    protected void onStart() {
        super.onStart();
        // We will start writing our code here.

        SpotifyAppRemote.disconnect(mSpotifyAppRemote);
        // Set the connection parameters
        ConnectionParams connectionParams =
                new ConnectionParams.Builder(CLIENT_ID)
                        .setRedirectUri(REDIRECT_URI)
                        .showAuthView(true)
                        .build();

        // Connect to Spotify using the connection parameters and
        // set a Connection listener and implement the ConnectionListener
        // methods. Handles connection failures and success
        SpotifyAppRemote.connect(this, connectionParams,
                new Connector.ConnectionListener() {

                    @Override
                    public void onConnected(SpotifyAppRemote spotifyAppRemote) {
                        mSpotifyAppRemote = spotifyAppRemote;
                        Log.d("MainActivity", "Connected! Yay!");

                        // Now you can start interacting with App Remote
                        //connected();
                    }

                    @Override
                    public void onFailure(Throwable throwable) {
                        Log.e("MainActivity", throwable.getMessage(), throwable);

                        // Something went wrong when attempting to connect! Handle errors here
                    }
                });
    }

    // Implements this when user is connected
    // It gets the user's playlst information
    private void connected(String trackID) {
        // Then we will write some more code here.
        // Play a playlist
        // https://api.spotify.com/v1/playlists/1g89RXrmPj5xE8F6QJ5vm7/tracks
        PlayerApi playerApi = mSpotifyAppRemote.getPlayerApi();
        mSpotifyAppRemote.getPlayerApi().play("spotify:track:"+ trackID);

        // Subscribe to PlayerState
        mSpotifyAppRemote.getPlayerApi()
                .subscribeToPlayerState()
                .setEventCallback(playerState -> {
                    final Track track = playerState.track;
                    if (track != null) {
                        Log.d("MainActivity", track.name + " by " + track.artist.name+ "is playing?" + playerState.isPaused);
                        isPlaying = !(playerState.isPaused);
                    }
                });
    }

//    public static void paused(){
//        mSpotifyAppRemote.getPlayerApi().pause();
//
//        // Subscribe to PlayerState
//        mSpotifyAppRemote.getPlayerApi()
//                .subscribeToPlayerState()
//                .setEventCallback(playerState -> {
//                    final Track track = playerState.track;
//                    if (track != null) {
//                        Log.d("MainActivity", track.name + " by " + track.artist.name+ "is playing?" + playerState.isPaused);
//                        isPlaying = !(playerState.isPaused);
//                    }
//                });
//    }

    // If the connection is stopped, then disconnect from Spotify
    @Override
    protected void onStop() {
        super.onStop();
        // Aaand we will finish off here.
        //mSpotifyAppRemote.getPlayerApi().pause();
        SpotifyAppRemote.disconnect(mSpotifyAppRemote);
    }


}


