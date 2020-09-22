package com.example.musicmoji;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class ServerSongFragment extends Fragment {

    DatabaseReference ref;

    ListView list;
    public ArrayList<String> titles = new ArrayList<String>();
    public ArrayList<String> artists = new ArrayList<String>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_server_song, container, false);

        Toast.makeText(getActivity(), "Lyric generation might take a few minutes after selection", Toast.LENGTH_LONG).show();

        // get the listview we want to customize
        list = (ListView) view.findViewById(R.id.server_listview);

        // create instance of class CustomServerAdapter and pass in the
        // activity, the titles, and artists that our custom view wants to show
        final CustomServerAdapter adapter = new CustomServerAdapter(getActivity(), titles, artists);
        // set adapter to list
        list.setAdapter(adapter);

        // Reference database to get the server songs saved on it
        ref = FirebaseDatabase.getInstance().getReference().child("ServerSongsData");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // gets the children and if there is more than 0
                // then access each child get the values and append to
                // titles and artists list, then notify adapter
                // doing this inside guarantees that titles and artists aren't
                // empty due to asynchronous access by data reference
                if (dataSnapshot.getChildrenCount() > 0) {
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        ServerSongsData s = ds.getValue(ServerSongsData.class);
                        titles.add(s.getTitle());
                        artists.add(s.getArtist());

                        // notify the adapter a change occurred and to update the listview
                        adapter.notifyDataSetChanged();
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        // handle item clicks moves to next Activity
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                TextView getTitle = (TextView) view.findViewById(R.id.tv_title);
                TextView getArtist = (TextView) view.findViewById(R.id.tv_artist);

                String strTitle = getTitle.getText().toString().trim();
                String strArtist = getArtist.getText().toString().trim();

                // Log to see if title and artist are obtained
                // will use to pass to next activity
//                Log.d("Passed Title", strTitle);
//                Log.d("Passed Artist", strArtist);

                // Move to PlaySong Activity and pass it the title and artist
                Intent intent = new Intent(getActivity(), PlaySong.class);
                intent.putExtra("Title", strTitle);
                intent.putExtra("Artist", strArtist);
                startActivity(intent);
            }
        });


        return view;
    }


    // Custom List View Adapter to show custom list view
    // extends the ArrayAdapter
    class CustomServerAdapter extends ArrayAdapter {

        // The constructor for the adapter
        CustomServerAdapter(Context c, ArrayList<String> titles, ArrayList<String> artists) {
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
}
