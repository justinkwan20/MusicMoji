package com.example.musicmoji;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.PlayerApi;
import com.spotify.android.appremote.api.SpotifyAppRemote;
import com.spotify.protocol.client.CallResult;
import com.spotify.protocol.types.PlayerState;
import com.spotify.protocol.types.Track;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static com.example.musicmoji.Main_Activity_Page.mSpotifyAppRemote;

// Similar to ServerSongFragment but modified for
// Spotify songs from API call
// List View is set up just need to add the song titles and artists
// to the ArrayList before setAdapter, or if necessary add and then
// call adapter.notifyDataSetChanged();
// refer to ServerSongFragment for more detail
public class SpotifySongFragment extends Fragment {

    // Here to set up listview with Spotify Songs
    ListView list;
    public ArrayList<String> titles = new ArrayList<String>();
    public ArrayList<String> artists = new ArrayList<String>();
    //public ArrayList<String> trackID = new ArrayList<String>();

    private SharedPreferences mSharedPreferences;
    private JSONObject playlistJSON;
    private List<List<String>> songsInfo;

    private MyBroadcastReceiver mBroadcastReceiver;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_spotify_song, container, false);

        mSharedPreferences = getActivity().getSharedPreferences("SPOTIFY", 0);

        songsInfo = new ArrayList<>();

        getPlaylistFromSpotify();

        // EXAMPLE of adding to ArrayList
//        titles.add("Laputa, Castle in the Sky");
//        artists.add("Joe Hisashi");

        // get the listview we want to customize
        list = (ListView) view.findViewById(R.id.spotify_listview);

        // handle item clicks
        // currently show toast on click
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(getActivity(), "Item Clicked", Toast.LENGTH_SHORT).show();

                TextView getTitle = (TextView) view.findViewById(R.id.tv_title);
                TextView getArtist = (TextView) view.findViewById(R.id.tv_artist);

                String strTitle = getTitle.getText().toString().trim();
                String strArtist = getArtist.getText().toString().trim();
                String trackID = songsInfo.get(position).get(0);

                Log.d("listsetONCLICKLISnenter", strTitle + strArtist+ "   "+ trackID);
                System.out.println(songsInfo);



                // Log to see if title and artist are obtained
                // will use to pass to next activity
                Log.d("Passed Title", strTitle);
                Log.d("Passed Artist", strArtist);

                Intent intent = new Intent(getActivity(), PlaySongForSpotify.class);
                intent.putExtra("Title", strTitle);
                intent.putExtra("Artist", strArtist);
                startActivity(intent);
                //((Main_Activity_Page)getActivity()).connected(trackID);
                connected(trackID);
            }
        });

        return view;
    }

    private void getSongsInfo() {
        JSONArray items = playlistJSON.getJSONArray("items");
        for (int i = 0; i < items.size(); i++) {
            List<String> songInfo = new ArrayList<>();
            JSONObject songInfoJSON = items.getJSONObject(i);
            JSONObject trackJSON = songInfoJSON.getJSONObject("track");
            String name = trackJSON.getString("name");
            String id = trackJSON.getString("id");
            JSONArray artistsJSON = trackJSON.getJSONArray("artists");
            StringBuilder artistsName = new StringBuilder();
            for (int j = 0; j < artistsJSON.size() - 1; j++) {
                JSONObject artistInfo = artistsJSON.getJSONObject(j);
                String artistName = artistInfo.getString("name");
                artistsName.append(artistName);
                artistsName.append("; ");
            }
            JSONObject artistInfo = artistsJSON.getJSONObject(artistsJSON.size() - 1);
            String artistName = artistInfo.getString("name");
            artistsName.append(artistName);
            String artist = artistsName.toString();
            songInfo.add(id);
            songInfo.add(name);
            songInfo.add(artist);
            songsInfo.add(songInfo);
            System.out.println(name);
            titles.add(name);
            //trackID.add(id);
            artists.add(artist);
        }
        // here you check the value of getActivity() and break up if needed
        if(getActivity() == null)
            return;

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
            // create instance of class CustomServerAdapter and pass in the
            // activity, the titles, and artists that our custom view wants to show
            CustomSpotifyAdapter adapter = new CustomSpotifyAdapter(getActivity(), titles, artists);
            // set adapter to list
            list.setAdapter(adapter);
            }
        }
        );
    }


    // get playlist from spotify
    private void getPlaylistFromSpotify() {
        //need to get play list first, temporary
        String url = "https://api.spotify.com/v1/me/player/recently-played";
        String token = mSharedPreferences.getString("token", "");
        OkHttpClient okHttpClient = new OkHttpClient();
        final Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + token)
                .get()
                .build();
        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("Spotify Info", "onFailure: Get Playlist failed.");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String res = response.body().string();
                Log.d("Spotify Info", "onResponse: " + res);
                playlistJSON = JSONObject.parseObject(res);
                getSongsInfo();


            }
        });
    }

    // Custom List View Adapter to show custom list view
    // extends the ArrayAdapter
    class CustomSpotifyAdapter extends ArrayAdapter {

        // The constructor for the adapter
        CustomSpotifyAdapter(Context c, ArrayList<String> titles, ArrayList<String> artists) {
            super(c, R.layout.list_item, R.id.tv_title, titles);

        }

        // gets the view and fills it in with custom information
        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            View v = ((Activity) getContext()).getLayoutInflater().inflate(R.layout.list_item, null);

            // finds the text view by id using View v as reference
            TextView title = (TextView) v.findViewById(R.id.tv_title);
            TextView artist = (TextView) v.findViewById(R.id.tv_artist);


            // sets the text with custom information passed in during instantiation
            title.setText(titles.get(position));
            artist.setText(artists.get(position));


            // returns the view
            return v;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mBroadcastReceiver = new MyBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MyBroadcastReceiver.BroadcastTypes.METADATA_CHANGED);
        intentFilter.addAction(MyBroadcastReceiver.BroadcastTypes.PLAYBACK_STATE_CHANGED);
        intentFilter.addAction(MyBroadcastReceiver.BroadcastTypes.QUEUE_CHANGED);
        intentFilter.addAction(MyBroadcastReceiver.BroadcastTypes.SPOTIFY_PACKAGE);
        getActivity().getApplicationContext().registerReceiver(mBroadcastReceiver, intentFilter);
    }

//    @Override
//    public void onDestroy() {
//        super.onDestroy();
//        getActivity().unregisterReceiver(mBroadcastReceiver);
//    }

//    @Override
//    public void onPause() {
//        super.onPause();
//
//    }

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
                        Main_Activity_Page.isPlaying = !(playerState.isPaused);
                    }
                });
    }


}

