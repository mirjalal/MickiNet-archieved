package com.talmir.mickinet.activities;

import android.arch.lifecycle.ViewModelProviders;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
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

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.talmir.mickinet.R;
import com.talmir.mickinet.helpers.adapter.ReceivedFilesListAdapter;
import com.talmir.mickinet.helpers.adapter.SentFilesListAdapter;
import com.talmir.mickinet.helpers.room.received.ReceivedFilesViewModel;
import com.talmir.mickinet.helpers.room.sent.SentFilesViewModel;

import java.util.ArrayList;

public class FileStatisticsActivity extends AppCompatActivity {

    private static final String[] xData = { "Photos", "Videos", "APKs", "Others" };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_statistics);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        SectionsPagerAdapter mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
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
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            if (getArguments().getInt(ARG_SECTION_NUMBER) == 0) {
                View rootView = inflater.inflate(R.layout.fragment_sent, container, false);

                RecyclerView recyclerView = rootView.findViewById(R.id.recycler_view);
                TextView emptyView = rootView.findViewById(R.id.empty_view);

                final SentFilesListAdapter adapter = new SentFilesListAdapter(getContext());
                recyclerView.setAdapter(adapter);
                recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

                // Get a new or existing ViewModel from the ViewModelProvider.
                SentFilesViewModel mSentFilesViewModel = ViewModelProviders.of(this).get(SentFilesViewModel.class);

                // Add an observer on the LiveData returned by getAlphabetizedWords.
                // The onChanged() method fires when the observed data changes and the activity is
                // in the foreground.
                // Update the cached copy of the words in the adapter.
                mSentFilesViewModel.getAllSentFiles().observe(this, adapter::setSentFiles);

                if (adapter.getItemCount() > 0) {
                    recyclerView.setVisibility(View.VISIBLE);
                    emptyView.setVisibility(View.GONE);
                } else {
                    recyclerView.setVisibility(View.GONE);
                    emptyView.setVisibility(View.VISIBLE);
                }

                return rootView;
            } else {
                View rootView = inflater.inflate(R.layout.fragment_received, container, false);

                final PieChart pie = rootView.findViewById(R.id.received_pie);
                // configure pie chart
                final Description d = new Description();
                d.setEnabled(true);
                d.setTypeface(Typeface.DEFAULT_BOLD);
                d.setText("Description");
                pie.setDescription(d);
                pie.setUsePercentValues(true);

                // enable hole and configure
                pie.setDrawHoleEnabled(true);
                pie.setDrawSlicesUnderHole(true); // extra
                pie.setDrawEntryLabels(true);     // extra
                pie.setHoleRadius(7);
                pie.setTransparentCircleRadius(10);

                // enable rotation of the pie by gesture
                pie.setRotationAngle(0);
                pie.setRotationEnabled(true);

                final RecyclerView recyclerView = rootView.findViewById(R.id.recycler_view);
                final TextView emptyView = rootView.findViewById(R.id.empty_view);

                final ReceivedFilesListAdapter adapter = new ReceivedFilesListAdapter(getContext());
                recyclerView.setAdapter(adapter);
                recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

                // Get a new or existing ViewModel from the ViewModelProvider.
                ReceivedFilesViewModel mReceivedFilesViewModel = ViewModelProviders.of(this).get(ReceivedFilesViewModel.class);

                // Add an observer on the LiveData returned by getAlphabetizedWords.
                // The onChanged() method fires when the observed data changes and the activity is
                // in the foreground.
                // Update the cached copy of the words in the adapter.
                mReceivedFilesViewModel.getAllReceivedFiles().observe(this, adapter::setReceivedFiles);

                if (adapter.getItemCount() > 0) {
                    recyclerView.setVisibility(View.VISIBLE);
                    emptyView.setVisibility(View.GONE);
                } else {
                    recyclerView.setVisibility(View.GONE);
                    emptyView.setVisibility(View.VISIBLE);
                }

                // We must set data right here. Because just before our adapter initialized
                // we couldn't do anything with pie.
                adapter.getReceivedFilesCountByTypes();
                float[] yData = {
                        adapter.getPhotoFilesCount(), adapter.getVideoFilesCount(),
                        adapter.getAPKFilesCount(),   adapter.getOtherFilesCount()
                };

                // set data
                ArrayList<PieEntry> yVals = new ArrayList<>(4);
                for (int i = 0; i < 4; i++)
                    yVals.add(new PieEntry(yData[i], i));

                ArrayList<String> xVals = new ArrayList<>(4);
                for (int i = 0; i < 4; i++)
                    xVals.add(xVals.get(i));

                // create pie dataset
                PieDataSet pieDataSet = new PieDataSet(yVals, "Dataset String");
                pieDataSet.setSliceSpace(3);
                pieDataSet.setSelectionShift(10);

                // add colors
                ArrayList<Integer> colors = new ArrayList<>();

                for (int color : ColorTemplate.COLORFUL_COLORS)
                    colors.add(color);

                for (int color : ColorTemplate.JOYFUL_COLORS)
                    colors.add(color);

                for (int color : ColorTemplate.LIBERTY_COLORS)
                    colors.add(color);

                for (int color : ColorTemplate.MATERIAL_COLORS)
                    colors.add(color);

                for (int color : ColorTemplate.PASTEL_COLORS)
                    colors.add(color);

                for (int color : ColorTemplate.VORDIPLOM_COLORS)
                    colors.add(color);

                colors.add(ColorTemplate.getHoloBlue());
                pieDataSet.setColors(colors);

                // instantiate pie data object
                PieData pieData = new PieData();
                pieData.setValueFormatter(new PercentFormatter());
                pieData.setValueTextSize(12f);
                pieData.setValueTextColor(Color.GRAY);

                pie.setData(pieData);

                // undo all highlights
                pie.highlightValue(null);

                // update pie
                pie.invalidate();

                // customize legends
                Legend legend = pie.getLegend();
                legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
                legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
                legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
                legend.setDrawInside(false);
                legend.setXEntrySpace(7);
                legend.setYEntrySpace(5);

//                legend.setPosition(Legend.LegendPosition.BELOW_CHART_CENTER);


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
