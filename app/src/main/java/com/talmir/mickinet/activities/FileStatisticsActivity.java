package com.talmir.mickinet.activities;

import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
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
import android.webkit.MimeTypeMap;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.talmir.mickinet.R;
import com.talmir.mickinet.helpers.ui.DividerItemDecoration;
import com.talmir.mickinet.helpers.ui.IRecyclerItemClickListener;
import com.talmir.mickinet.helpers.ui.RecyclerItemTouchListener;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FileStatisticsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_statistics);

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
        if (item.getItemId() == R.id.action_reset_statistics) {
            final AlertDialog alert = new AlertDialog.Builder(FileStatisticsActivity.this).create();
            alert.setTitle(getString(R.string.are_you_sure));
            alert.setMessage(getString(R.string.reset_all_stats));
            alert.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.reset), (dialog, which) -> {
                if (HomeActivity.getSentFilesViewModel().deleteAllRecords() && HomeActivity.getReceivedFilesViewModel().deleteAllRecords())
                    Toast.makeText(getApplicationContext(), R.string.done, Toast.LENGTH_LONG).show();
            });
            alert.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.cancel), (dialog, which) -> alert.dismiss());
            alert.show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        private static final String ARG_SECTION_NUMBER = "section_number";

        public static int sentPhotoFilesCount = 0;
        public static int sentVideoFilesCount = 0;
        public static int sentAPKFilesCount   = 0;
        public static int sentMediaFilesCount = 0;
        public static int sentOtherFilesCount = 0;
        public static int receivedPhotoFilesCount = 0;
        public static int receivedVideoFilesCount = 0;
        public static int receivedAPKFilesCount   = 0;
        public static int receivedMediaFilesCount = 0;
        public static int receivedOtherFilesCount = 0;

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
            int section = Objects.requireNonNull(getArguments()).getInt(ARG_SECTION_NUMBER);

            final Context context = inflater.getContext();
            int fragment = R.layout.fragment_sent;
            int pie = R.id.sent_pie;
            int recycler = R.id.sent_recycler_view;
            int adapterListItemCount = HomeActivity.getSentFilesListAdapter().getItemCount();
            int emptyView = R.id.sent_empty_view;
            int photoFilesCount = sentPhotoFilesCount;
            int videoFilesCount = sentVideoFilesCount;
            int mediaFilesCount = sentMediaFilesCount;
            int APKFilesCount   = sentAPKFilesCount;
            int otherFilesCount = sentOtherFilesCount;

            if (section == 1) {
                fragment = R.layout.fragment_received;
                pie = R.id.received_pie;
                recycler = R.id.received_recycler_view;
                adapterListItemCount = HomeActivity.getReceivedFilesListAdapter().getItemCount();
                emptyView = R.id.received_empty_view;
                photoFilesCount = receivedPhotoFilesCount;
                videoFilesCount = receivedVideoFilesCount;
                mediaFilesCount = receivedMediaFilesCount;
                APKFilesCount   = receivedAPKFilesCount;
                otherFilesCount = receivedOtherFilesCount;
            }

            View rootView = inflater.inflate(fragment, container, false);

            final PieChart pieChart = rootView.findViewById(pie);
            pieChart.setUsePercentValues(true);

            // enable rotation of the pie by gesture
            pieChart.setRotationAngle(0);
            pieChart.setRotationEnabled(true);

            final RecyclerView recyclerView = rootView.findViewById(recycler);
            final TextView emptyTextView = rootView.findViewById(emptyView);

            recyclerView.setAdapter(section == 0 ? HomeActivity.getSentFilesListAdapter() : HomeActivity.getReceivedFilesListAdapter());
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
            recyclerView.addItemDecoration(new DividerItemDecoration(context, LinearLayoutManager.VERTICAL));
            recyclerView.addOnItemTouchListener(new RecyclerItemTouchListener(context, recyclerView, new IRecyclerItemClickListener() {
                @Override
                public void onClick(View view, int position) {
                    final TextView _name = (TextView) ((ViewGroup) view).getChildAt(0);
                    final String fileName = _name.getText().toString();
                    String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileName.substring(fileName.lastIndexOf('.') + 1));
                    if (new File(getFileDirectory(section, fileName)).exists()) {
                        final Intent openReceivedFile = new Intent(Intent.ACTION_VIEW);
                        openReceivedFile.setDataAndType(
                                Uri.fromFile(new File(getFileDirectory(section, fileName))),
                                mimeType
                        );
                        PendingIntent openFilePendingIntent = PendingIntent.getActivity(context, 0, openReceivedFile, PendingIntent.FLAG_ONE_SHOT);
                        try {
                            openFilePendingIntent.send();
                        } catch (PendingIntent.CanceledException ignore) { }
                    } else
                        Toast.makeText(context, R.string.file_not_exists, Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onLongClick(View view, int position) {
                    Toast.makeText(context, R.string.click_to_open, Toast.LENGTH_SHORT).show();
                }
            }));

            // set data
            List<PieEntry> entries = new ArrayList<>();
            entries.add(new PieEntry(photoFilesCount, context.getString(R.string.photos)));
            entries.add(new PieEntry(videoFilesCount, context.getString(R.string.videos)));
            entries.add(new PieEntry(mediaFilesCount, context.getString(R.string.media)));
            entries.add(new PieEntry(APKFilesCount,   context.getString(R.string.apks)));
            entries.add(new PieEntry(otherFilesCount, context.getString(R.string.others)));

            // create pie dataset
            PieDataSet pieDataSet = new PieDataSet(entries, context.getString(R.string.file_types));
            pieDataSet.setSliceSpace(5);
            // pieDataSet.setSelectionShift(10);

            // add colors
            ArrayList<Integer> colors = new ArrayList<>();
            for (int color : ColorTemplate.LIBERTY_COLORS)
                colors.add(color);
            colors.add(ColorTemplate.getHoloBlue());
            pieDataSet.setColors(colors);

            // instantiate pie data object
            PieData pieData = new PieData(pieDataSet);
            pieData.setValueFormatter(new PercentFormatter());
            pieData.setValueTextSize(12f);
            pieData.setValueTextColor(Color.GRAY);
            pieChart.setData(pieData);

            // undo all highlights
            pieChart.highlightValue(null);

            // update pie
            pieChart.invalidate();

            // customize legends
            Legend legend = pieChart.getLegend();
            legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
            legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
            legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
            legend.setDrawInside(false);
            legend.setXEntrySpace(7);
            legend.setYEntrySpace(5);
            // legend.setPosition(Legend.LegendPosition.BELOW_CHART_CENTER);

            if (adapterListItemCount > 0) {
                pieChart.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.VISIBLE);
                emptyTextView.setVisibility(View.GONE);
            } else {
                pieChart.setVisibility(View.GONE);
                recyclerView.setVisibility(View.GONE);
                emptyTextView.setVisibility(View.VISIBLE);
            }

            return rootView;
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
            return PlaceholderFragment.newInstance(position);
        }

        @Override
        public int getCount() {
            return 2;
        }
    }

    /**
     * Gets file's full directory.
     *
     * @param section tab number: 0 - Sent fragment, 1 - Received fragment
     * @param fileName the file which info will be given about
     * @return full dir path to the selected fileName
     */
    @NotNull
    private static String getFileDirectory(int section, @NonNull @NotNull String fileName) {
        final String path = Environment.getExternalStorageDirectory() + "/MickiNet/";
        // inner folder depends on section number
        String inner;
        String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileName.substring(fileName.lastIndexOf('.') + 1));
        assert mimeType != null;
        if (mimeType.startsWith("image"))
            inner = "Photos/";
        else if (mimeType.startsWith("videos"))
            inner = "Videos/";
        else if (mimeType.startsWith("music") || mimeType.startsWith("audio"))
            inner = "Media/";
        else if (mimeType.equals("application/vnd.android.package-archive"))
            inner = "APKs/";
        else
            inner = "Others/";

        inner += (section == 0) ? "Sent/" : "Received/";

        return path + inner + fileName;
    }
}
