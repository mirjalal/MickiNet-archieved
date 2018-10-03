package com.talmir.mickinet.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.talmir.mickinet.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ReceivedFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ReceivedFragment extends Fragment {

    public ReceivedFragment() {
        // Required empty public constructor
    }

    public static ReceivedFragment newInstance() {
        return new ReceivedFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_received, container, false);
    }
}
