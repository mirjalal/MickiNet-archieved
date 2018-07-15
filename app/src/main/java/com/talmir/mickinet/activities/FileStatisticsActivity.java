package com.talmir.mickinet.activities;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.Toast;

import com.talmir.mickinet.R;
import com.talmir.mickinet.helpers.adapter.ExpandableListAdapter;
import com.talmir.mickinet.helpers.room.received.ReceivedViewModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FileStatisticsActivity extends AppCompatActivity {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    private ReceivedViewModel mReceiveddViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_statistics);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        /*
      The {@link ViewPager} that will host the section contents.
     */
        ViewPager mViewPager = findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        final TabLayout tabLayout = findViewById(R.id.tabs);
        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));

//        RecyclerView recyclerView = findViewById(R.id.recyclerview);
//        final ReceivedListAdapter adapter = new ReceivedListAdapter(this);
//        recyclerView.setAdapter(adapter);
//        recyclerView.setLayoutManager(new LinearLayoutManager(this));

//        mReceiveddViewModel = ViewModelProviders.of(this).get(ReceivedViewModel.class);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_file_statistics, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        // noinspection SimplifiableIfStatement
        if (id == R.id.action_reset_statistics) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";
        static ExpandableListAdapter listAdapter;
        static ExpandableListView expListView;
        static List<String> listDataHeader;
        static HashMap<String, List<String>> listDataChild;

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        /*
         * Preparing the list data
         */
        private static void prepareListData() {
            listDataHeader = new ArrayList<>();
            listDataChild = new HashMap<>();

            // Adding child data
            listDataHeader.add("Top 250");
            listDataHeader.add("Now Showing");
            listDataHeader.add("Coming Soon..");

            // Adding child data
            List<String> top250 = new ArrayList<>();
            top250.add("The Shawshank Redemption");
            top250.add("The Godfather");
            top250.add("The Godfather: Part II");
            top250.add("Pulp Fiction");
            top250.add("The Good, the Bad and the Ugly");
            top250.add("The Dark Knight");
            top250.add("12 Angry Men");

            List<String> nowShowing = new ArrayList<>();
            nowShowing.add("The Conjuring");
            nowShowing.add("Despicable Me 2");
            nowShowing.add("Turbo");
            nowShowing.add("Grown Ups 2");
            nowShowing.add("Red 2");
            nowShowing.add("The Wolverine");

            List<String> comingSoon = new ArrayList<>();
            comingSoon.add("2 Guns");
            comingSoon.add("The Smurfs 2");
            comingSoon.add("The Spectacular Now");
            comingSoon.add("The Canyons");
            comingSoon.add("Europa Report");

            listDataChild.put(listDataHeader.get(0), top250); // Header, Child data
            listDataChild.put(listDataHeader.get(1), nowShowing);
            listDataChild.put(listDataHeader.get(2), comingSoon);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            if (getArguments().getInt(ARG_SECTION_NUMBER) == 0) {
                View rootView = inflater.inflate(R.layout.fragment_sent, container, false);

//                TextView textView = rootView.findViewById(R.id.section_label);
//                textView.setText(getString(R.string.section_format, getArguments().getInt(ARG_SECTION_NUMBER)));
                return rootView;
            } else {
                View rootView = inflater.inflate(R.layout.fragment_received, container, false);
                expListView = rootView.findViewById(R.id.expandedlistview_received);

                // preparing list data
                prepareListData();

                listAdapter = new ExpandableListAdapter(getContext(), listDataHeader, listDataChild);

                // setting list adapter
                expListView.setAdapter(listAdapter);

                // Listview Group click listener
                expListView.setOnGroupClickListener((parent, v, groupPosition, id) -> {
                    // Toast.makeText(getApplicationContext(),
                    // "Group Clicked " + listDataHeader.get(groupPosition),
                    // Toast.LENGTH_SHORT).show();
                    return false;
                });

                // Listview Group expanded listener
                expListView.setOnGroupExpandListener(groupPosition -> Toast.makeText(getContext(),
                        listDataHeader.get(groupPosition) + " Expanded",
                        Toast.LENGTH_SHORT).show());

                // Listview Group collasped listener
                expListView.setOnGroupCollapseListener(groupPosition -> Toast.makeText(getContext(),
                        listDataHeader.get(groupPosition) + " Collapsed",
                        Toast.LENGTH_SHORT).show());

                // Listview on child click listener
                expListView.setOnChildClickListener((parent, v, groupPosition, childPosition, id) -> {
                    // TODO Auto-generated method stub
                    Toast.makeText(
                            getContext(),
                            listDataHeader.get(groupPosition)
                                    + " : "
                                    + listDataChild.get(
                                    listDataHeader.get(groupPosition)).get(
                                    childPosition), Toast.LENGTH_SHORT)
                            .show();
                    return false;
                });
                return rootView;
            }
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
//             getItem is called to instantiate the fragment for the given page.
//             Return a PlaceholderFragment (defined as a static inner class below).
            return PlaceholderFragment.newInstance(position);
        }

        @Override
        public int getCount() {
            return 2;
        }
    }
}
