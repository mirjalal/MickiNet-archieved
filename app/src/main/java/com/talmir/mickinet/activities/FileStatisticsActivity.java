package com.talmir.mickinet.activities;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.talmir.mickinet.R;
import com.talmir.mickinet.helpers.adapters.ReceivedFilesListAdapter;
import com.talmir.mickinet.helpers.adapters.SentFilesListAdapter;

import java.util.Objects;

public class FileStatisticsActivity extends AppCompatActivity {

    private static SentFilesListAdapter mSentFilesListAdapter;
    public static float sentPhotoFilesCount;
    public static float sentVideoFilesCount;
    public static float sentAPKFilesCount;
    public static float sentOtherFilesCount;

    private static ReceivedFilesListAdapter mReceivedFilesListAdapter;
    public static float receivedPhotoFilesCount;
    public static float receivedVideoFilesCount;
    public static float receivedAPKFilesCount;
    public static float receivedOtherFilesCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_statistics);

        mSentFilesListAdapter = HomeActivity.getSentFilesListAdapter();
        sentPhotoFilesCount = 0.0f;
        sentVideoFilesCount = 0.0f;
        sentAPKFilesCount   = 0.0f;
        sentOtherFilesCount = 0.0f;

        mReceivedFilesListAdapter = HomeActivity.getReceivedFilesListAdapter();
        receivedPhotoFilesCount = 0.0f;
        receivedVideoFilesCount = 0.0f;
        receivedAPKFilesCount   = 0.0f;
        receivedOtherFilesCount = 0.0f;

//        mSentFilesListAdapter.getSentFilesCountByTypes();
//        mReceivedFilesListAdapter.getReceivedFilesCountByTypes();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        // Create the adapters that will return a fragment for each of the three
        // primary sections of the activity.
        SectionsPagerAdapter mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapters.
        ViewPager mViewPager = findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        final TabLayout tabLayout = findViewById(R.id.tabs);
        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_file_statistics, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
//        if (item.getItemId() == R.s_f_id.action_reset_statistics) {
            // TODO: resetting statistical data (deleting ALL data from both databases)
//            return true;
//        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        private static final String ARG_SECTION_NUMBER = "section_number";

        public PlaceholderFragment() {  }

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

        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            if (Objects.requireNonNull(getArguments()).getInt(ARG_SECTION_NUMBER) == 0) {
                View rootView = inflater.inflate(R.layout.fragment_sent, container, false);

//                final PieChart pie = rootView.findViewById(R.s_f_id.sent_pie);
//                // configure pie chart
//                final Description d = new Description();
//                d.setEnabled(true);
//                d.setTypeface(Typeface.DEFAULT_BOLD);
//                d.setText("Comparison among received file types");
//                pie.setDescription(d);
//                pie.setUsePercentValues(true);
//
//                // enable rotation of the pie by gesture
//                pie.setRotationAngle(0);
//                pie.setRotationEnabled(true);

                final RecyclerView recyclerView = rootView.findViewById(R.id.sent_recycler_view);
                final TextView emptyView = rootView.findViewById(R.id.sent_empty_view);

                recyclerView.setAdapter(HomeActivity.getSentFilesListAdapter());
                recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

                // set data
//                List<PieEntry> entries = new ArrayList<>();
//                entries.add(new PieEntry(sentPhotoFilesCount, "Photos"));
//                entries.add(new PieEntry(sentVideoFilesCount, "Videos"));
//                entries.add(new PieEntry(sentAPKFilesCount, "APKs"));
//                entries.add(new PieEntry(sentOtherFilesCount, "Others"));
//
//                // create pie dataset
//                PieDataSet pieDataSet = new PieDataSet(entries, "File types");
//                pieDataSet.setSliceSpace(3);
//                pieDataSet.setSelectionShift(10);
//
//                // add colors
//                ArrayList<Integer> colors = new ArrayList<>();
//
//                for (int color : ColorTemplate.LIBERTY_COLORS)
//                    colors.add(color);
//                colors.add(ColorTemplate.getHoloBlue());
//                pieDataSet.setColors(colors);
//
//                // instantiate pie data object
//                PieData pieData = new PieData(pieDataSet);
//                pieData.setValueFormatter(new PercentFormatter());
//                pieData.setValueTextSize(12f);
//                pieData.setValueTextColor(Color.GRAY);
//                pie.setData(pieData);
//
//                // undo all highlights
//                pie.highlightValue(null);
//
//                // update pie
//                pie.invalidate();
//
//                // customize legends
//                Legend legend = pie.getLegend();
//                legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
//                legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
//                legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
//                legend.setDrawInside(false);
//                legend.setXEntrySpace(7);
//                legend.setYEntrySpace(5);
//                legend.setPosition(Legend.LegendPosition.BELOW_CHART_CENTER);

                if (HomeActivity.getSentFilesListAdapter().getItemCount() > 0) {
//                    pie.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.VISIBLE);
                    emptyView.setVisibility(View.GONE);
                } else {
//                    pie.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.GONE);
                    emptyView.setVisibility(View.VISIBLE);
                }

                return rootView;
            } else {
                View rootView = inflater.inflate(R.layout.fragment_received, container, false);

//                final PieChart pie = rootView.findViewById(R.s_f_id.received_pie);
//                // configure pie chart
//                final Description d = new Description();
//                d.setEnabled(true);
//                d.setTypeface(Typeface.DEFAULT_BOLD);
//                d.setText("Comparison among received file types");
//                pie.setDescription(d);
//                pie.setUsePercentValues(true);
//
//                // enable rotation of the pie by gesture
//                pie.setRotationAngle(0);
//                pie.setRotationEnabled(true);

                final RecyclerView recyclerView = rootView.findViewById(R.id.received_recycler_view);
                final TextView emptyView = rootView.findViewById(R.id.received_empty_view);

                recyclerView.setAdapter(HomeActivity.getReceivedFilesListAdapter());
                recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

                // set data
//                List<PieEntry> entries = new ArrayList<>();
//                entries.add(new PieEntry(receivedPhotoFilesCount, "Photos"));
//                entries.add(new PieEntry(receivedVideoFilesCount, "Videos"));
//                entries.add(new PieEntry(receivedAPKFilesCount, "APKs"));
//                entries.add(new PieEntry(receivedOtherFilesCount, "Others"));

                // create pie dataset
//                PieDataSet pieDataSet = new PieDataSet(entries, "File types");
//                pieDataSet.setSliceSpace(3);
//                pieDataSet.setSelectionShift(10);
//
//                // add colors
//                ArrayList<Integer> colors = new ArrayList<>();
//
//                for (int color : ColorTemplate.LIBERTY_COLORS)
//                    colors.add(color);
//                colors.add(ColorTemplate.getHoloBlue());
//                pieDataSet.setColors(colors);
//
//                // instantiate pie data object
//                PieData pieData = new PieData(pieDataSet);
//                pieData.setValueFormatter(new PercentFormatter());
//                pieData.setValueTextSize(12f);
//                pieData.setValueTextColor(Color.GRAY);
//                pie.setData(pieData);
//
//                // undo all highlights
//                pie.highlightValue(null);
//
//                // update pie
//                pie.invalidate();

                // customize legends
//                Legend legend = pie.getLegend();
//                legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
//                legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
//                legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
//                legend.setDrawInside(false);
//                legend.setXEntrySpace(7);
//                legend.setYEntrySpace(5);
//                legend.setPosition(Legend.LegendPosition.BELOW_CHART_CENTER);

                if (HomeActivity.getReceivedFilesListAdapter().getItemCount() > 0) {
//                    pie.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.VISIBLE);
                    emptyView.setVisibility(View.GONE);
                } else {
//                    pie.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.GONE);
                    emptyView.setVisibility(View.VISIBLE);
                }

                return rootView;
            }
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        SectionsPagerAdapter(FragmentManager fm) {
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
