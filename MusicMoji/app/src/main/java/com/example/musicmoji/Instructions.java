package com.example.musicmoji;

import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class Instructions extends Fragment {
    TextView instructions;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_instructions, container, false);

        // Find the textview and add scrollable movement to it
        instructions = (TextView) view.findViewById(R.id.instructions2);
        instructions.setMovementMethod(new ScrollingMovementMethod());
        return view;
    }
}
