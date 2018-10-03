package com.talmir.mickinet.activities;

import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.FileProvider;
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
import android.view.animation.DecelerateInterpolator;
import android.webkit.MimeTypeMap;
import android.widget.TextView;
import android.widget.Toast;

import com.razerdp.widget.animatedpieview.AnimatedPieView;
import com.razerdp.widget.animatedpieview.AnimatedPieViewConfig;
import com.razerdp.widget.animatedpieview.data.SimplePieInfo;
import com.talmir.mickinet.BuildConfig;
import com.talmir.mickinet.R;
import com.talmir.mickinet.helpers.ui.CustomDividerItemDecoration;
import com.talmir.mickinet.helpers.ui.IRecyclerItemClickListener;
import com.talmir.mickinet.helpers.ui.RecyclerItemTouchListener;

import org.jetbrains.annotations.NotNull;

import java.io.File;
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

        public static double sentPhotoFilesCount = 0.0f;
        public static double sentVideoFilesCount = 0.0f;
        public static double sentAPKFilesCount   = 0.0f;
        public static double sentMediaFilesCount = 0.0f;
        public static double sentOtherFilesCount = 0.0f;
        public static double receivedPhotoFilesCount = 0.0f;
        public static double receivedVideoFilesCount = 0.0f;
        public static double receivedAPKFilesCount   = 0.0f;
        public static double receivedMediaFilesCount = 0.0f;
        public static double receivedOtherFilesCount = 0.0f;

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
            // get section number at once, use multiple times
            int section = Objects.requireNonNull(getArguments()).getInt(ARG_SECTION_NUMBER);

            // init variables
            final Context context = inflater.getContext();
            int fragment = R.layout.fragment_sent;
            int pie = R.id.sent_pie;
            int recycler = R.id.sent_recycler_view;
            int adapterListItemCount = HomeActivity.getSentFilesListAdapter().getItemCount();
            int emptyView = R.id.sent_empty_view;
            double photoFilesCount = sentPhotoFilesCount;
            double videoFilesCount = sentVideoFilesCount;
            double mediaFilesCount = sentMediaFilesCount;
            double APKFilesCount   = sentAPKFilesCount;
            double otherFilesCount = sentOtherFilesCount;

            // change values of pre-defined variables depending on tab selection (tab number)
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

            AnimatedPieView mAnimatedPieView = rootView.findViewById(pie);
            AnimatedPieViewConfig conf = new AnimatedPieViewConfig();
            conf.startAngle(0.9f)
                .addData(new SimplePieInfo(photoFilesCount, Color.rgb(207, 248, 246), getString(R.string.photos)))
                .addData(new SimplePieInfo(videoFilesCount, Color.rgb(148, 212, 212), getString(R.string.videos)))
                .addData(new SimplePieInfo(mediaFilesCount, Color.rgb(136, 180, 187), getString(R.string.media)))
                .addData(new SimplePieInfo(APKFilesCount  , Color.rgb(118, 174, 175), getString(R.string.apks)))
                .addData(new SimplePieInfo(otherFilesCount, Color.rgb(42, 109, 130),  getString(R.string.others)))
                .duration(1200)
                .textSize(12)
                .autoSize(true)
                .focusAlphaType(AnimatedPieViewConfig.FOCUS_WITH_ALPHA)
                .textGravity(AnimatedPieViewConfig.BELOW)
                .interpolator(new DecelerateInterpolator())
                .canTouch(true);
            mAnimatedPieView.applyConfig(conf);
            mAnimatedPieView.start();

            /*final PieChart pieChart = rootView.findViewById(pie);
            pieChart.setUsePercentValues(true);

            // enable rotation of the pie by gesture
            pieChart.setRotationAngle(0);
            pieChart.setRotationEnabled(true);
            */

            final RecyclerView recyclerView = rootView.findViewById(recycler);
            final TextView emptyTextView = rootView.findViewById(emptyView);

            recyclerView.setAdapter(section == 0 ? HomeActivity.getSentFilesListAdapter() : HomeActivity.getReceivedFilesListAdapter());
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
            recyclerView.addItemDecoration(new CustomDividerItemDecoration(context, LinearLayoutManager.VERTICAL));
            recyclerView.addOnItemTouchListener(new RecyclerItemTouchListener(context, recyclerView, new IRecyclerItemClickListener() {
                @Override
                public void onClick(View view, int position) {
                    final TextView _name = (TextView) ((ViewGroup) view).getChildAt(0);
                    final String fileName = _name.getText().toString();
                    String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileName.substring(fileName.lastIndexOf('.') + 1));
                    final File clickedFile = new File(getFileDirectory(section, fileName, mimeType));
                    if (clickedFile.exists()) {
                        final Intent openReceivedFile = new Intent(Intent.ACTION_VIEW);
                        openReceivedFile.setDataAndType(
                            FileProvider.getUriForFile(
                                context,
                                BuildConfig.APPLICATION_ID + ".provider",
                                clickedFile
                            ),
                            mimeType
                        );
                        openReceivedFile.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
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
            /*
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
*/
            if (adapterListItemCount > 0) {
                mAnimatedPieView.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.VISIBLE);
                emptyTextView.setVisibility(View.GONE);
            } else {
                mAnimatedPieView.setVisibility(View.GONE);
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
    private static String getFileDirectory(int section, @NonNull @NotNull String fileName, String mimeType) {
        final String path = "/storage/emulated/0/MickiNet/";
        // inner folder depends on section number
        String inner;
        if (mimeType != null) {
            if (mimeType.startsWith("image"))
                inner = "Photos/";
            else if (mimeType.startsWith("video"))
                inner = "Videos/";
            else if (mimeType.startsWith("music") || mimeType.startsWith("audio"))
                inner = "Media/";
            else if (mimeType.equals("application/vnd.android.package-archive"))
                inner = "APKs/";
            else
                inner = "Others/";
        } else inner = "Others/";

        inner += (section == 0) ? "Sent/" : "Received/";

        return path + inner + fileName;
    }
}
