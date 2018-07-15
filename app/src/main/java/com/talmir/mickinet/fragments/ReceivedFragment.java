package com.talmir.mickinet.fragments;


import android.os.Bundle;
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

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ReceivedFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ReceivedFragment newInstance(String param1, String param2) {
        return new ReceivedFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        // get the listview
        return inflater.inflate(R.layout.fragment_received, container, false);
    }


}
