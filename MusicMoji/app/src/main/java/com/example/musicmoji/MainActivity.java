package com.example.musicmoji;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.spotify.android.appremote.api.SpotifyAppRemote;
import com.spotify.sdk.android.auth.AuthorizationClient;
import com.spotify.sdk.android.auth.AuthorizationRequest;
import com.spotify.sdk.android.auth.AuthorizationResponse;





// Connect with Spotify here for authentication
public class MainActivity extends AppCompatActivity {

    // Set some parameters required for connection with Spotify
    private SharedPreferences.Editor editor;
    private static final String CLIENT_ID = "76d0e59cae5b4af9bfc445a3ceaf3cfe";
    private static final String REDIRECT_URI = "http://musicmoji.com/callback/";
    // Request code will be used to verify if result comes from the login activity. Can be set to any integer.
    private static final int REQUEST_CODE = 1337;


    private Button btn_connect;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn_connect = (Button) findViewById(R.id.btn_connect);

        btn_connect.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Connect with Spotify
                authenticateSpotify();
            }
        });
    }

    // Connects to Spotify and starts authentication process
    // This is similar to an app request where it requests
    // user permission to login and redirect (implicit)
    private void authenticateSpotify() {
        AuthorizationRequest.Builder builder =
                new AuthorizationRequest.Builder(CLIENT_ID, AuthorizationResponse.Type.TOKEN, REDIRECT_URI);

        builder.setScopes(new String[]{"streaming", "user-read-private", " user-read-email", "user-read-recently-played"});
        AuthorizationRequest request = builder.build();

        AuthorizationClient.openLoginActivity(this, REQUEST_CODE, request);
    }

    // Checks to see if the authentication is successful. If successful
    // then the next activity can be started, otherwise don't do anything
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        // Check if result comes from the correct activity
        if (requestCode == REQUEST_CODE) {
            AuthorizationResponse response = AuthorizationClient.getResponse(resultCode, intent);

            switch (response.getType()) {
                // Response was successful and contains auth token
                case TOKEN:
                    // Handle successful response
                    editor = getSharedPreferences("SPOTIFY", 0).edit();
                    editor.putString("token", response.getAccessToken());
                    editor.apply();
                    Log.d("Spotify Info", "Login Success!");
                    // for now just moves to next activity
                    startActivity(new Intent(MainActivity.this, Main_Activity_Page.class));
                    break;

                // Auth flow returned an error
                case ERROR:
                    // Handle error response
                    Toast.makeText(getBaseContext(), "An Error Has Occurred: Authentication Not Recognized", Toast.LENGTH_SHORT).show();
                    break;

                // Most likely auth flow was cancelled
                default:
                    // Handle other cases
                    Toast.makeText(getBaseContext(), "Authentication Process Stopped. Please Authenticate with Spotify", Toast.LENGTH_SHORT).show();
            }
        }
    }
}