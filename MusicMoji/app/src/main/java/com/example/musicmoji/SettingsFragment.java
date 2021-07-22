package com.example.musicmoji;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
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
import androidx.fragment.app.Fragment;

import java.util.ArrayList;


/* Current idea for settings:
   When MusixMatch API is called, we can pull the API's list of languages,
   parse it and display it in a listview. For details of how to display
   go to ServerSongFragment (just follow setup don't follow Database part).
   Be sure to include a NONE option.
   Once set, reference setOnItemClickListener to deal with item clicks and
   extracting the language value/string.
   My idea is if an item (language) is selected set it in a SharedPreference
   so it is saved and then move to next activity.

   I set up the listview for the languages, just add it to the arraylist from
   API call where I placed the example. If necessary use adapter.notifyDataSetChanged();
   refer to ServerSongFragment for how I used it there with a database.
*/
public class SettingsFragment extends Fragment {

    ListView list;
    public ArrayList<String> languages = new ArrayList<String>();


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);


        // Add some decided languages
        languages.add("English");
        languages.add("Spanish");
        languages.add("Portuguese");
        languages.add("French");
        languages.add("Chinese");
        languages.add("None");

        // get the listview we want to customize
        list = (ListView) view.findViewById(R.id.language_listview);

        // create instance of class CustomServerAdapter and pass in the
        // activity, the titles, and artists that our custom view wants to show
        CustomLanguageAdapter adapter = new CustomLanguageAdapter(getActivity(), languages);
        // set adapter to list
        list.setAdapter(adapter);


        // handle item clicks moves to next Activity
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TextView getLanguage = (TextView) view.findViewById(R.id.tv_language);

                String strLanguage = getLanguage.getText().toString().trim();

                // Log to see if title and artist are obtained
                // will use to pass to next activity
//                Log.d("Passed Title", strLanguage);
//                Toast.makeText(getActivity(), strLanguage + " Clicked", Toast.LENGTH_SHORT).show();

                // Save the language
                // Shared Preference Object
                saveSharedPreferenceInfo(strLanguage);
                // Toast to indcate which language is saved.
                Toast.makeText(getContext(),"Language is saved: " + strLanguage, Toast.LENGTH_LONG).show();
            }
        });

        return view;
    }



    // Custom List View Adapter to show custom list view
    // extends the ArrayAdapter
    class CustomLanguageAdapter extends ArrayAdapter {

        // The constructor for the adapter
        CustomLanguageAdapter(Context c, ArrayList<String> languages) {
            super(c, R.layout.list_item_language, R.id.tv_language, languages);

        }

        // gets the view and fills it in with custom information
        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            View v = ((Activity) getContext()).getLayoutInflater().inflate(R.layout.list_item_language, null);

            // finds the text view by id using View v as reference
            TextView language = (TextView) v.findViewById(R.id.tv_language);

            // sets the text with custom information passed in during instantiation
            language.setText(languages.get(position));

            // returns the view
            return v;
        }
    }

    void saveSharedPreferenceInfo(String language){
        SharedPreferences simpleAppInfo = getContext().getSharedPreferences("LanguageSelection", Context.MODE_PRIVATE);  //Private means no other Apps can access this.

        SharedPreferences.Editor editor = simpleAppInfo.edit();
        switch (language) {
            case "English": editor.putString("language", "en"); break;
            case "Spanish": editor.putString("language", "es"); break;
            case "Portuguese": editor.putString("language", "pt"); break;
            case "French": editor.putString("language", "fr"); break;
            case "Chinese": editor.putString("language", "zh"); break;
            case "None": editor.putString("language", ""); break;

        }
        editor.commit();
//        Toast.makeText(getContext(), getContext().toString(), Toast.LENGTH_LONG).show();

        Toast.makeText(getContext(), "Language Preference Updated", Toast.LENGTH_LONG).show();
    }
}
