package com.talmir.mickinet.activities;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;

import com.talmir.mickinet.R;
import com.talmir.mickinet.helpers.adapters.ApkListAdapter;
import com.talmir.mickinet.helpers.ui.DividerItemDecoration;
import com.talmir.mickinet.helpers.ui.IRecyclerItemClickListener;
import com.talmir.mickinet.helpers.ui.RecyclerViewFastScroller;

import java.util.ArrayList;
import java.util.List;

/**
 * @author miri
 * @since 4/30/17
 */
public class ApkShareActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {

    private PackageManager pm;
    private List<ApplicationInfo> packages;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apk_share);

        pm = getPackageManager();

        packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);

        // http://www.android--tutorials.com/2016/03/android-get-installed-apps-list.html
        final ApkListAdapter apkListAdapter = new ApkListAdapter(pm, packages);
        apkListAdapter.notifyDataSetChanged();

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setAdapter(apkListAdapter);
        final RecyclerViewFastScroller fastScroller = this.findViewById(R.id.fastscroller);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false) {
            @Override
            public void onLayoutChildren(final RecyclerView.Recycler recycler, final RecyclerView.State state) {
                super.onLayoutChildren(recycler, state);

                final int firstVisibleItemPosition = findFirstVisibleItemPosition();
                if (firstVisibleItemPosition != 0) {
                    // this avoids trying to handle un-needed calls
                    if (firstVisibleItemPosition == -1) {
                        // not initialized, or no items shown, so hide fast-scroller
                        fastScroller.setVisibility(View.GONE);
                    }
                    return;
                }
                final int lastVisibleItemPosition = findLastVisibleItemPosition();
                int itemsShown = lastVisibleItemPosition - firstVisibleItemPosition + 1;
                // if all items are shown, hide the fast-scroller
                fastScroller.setVisibility(apkListAdapter.getItemCount() > itemsShown ? View.VISIBLE : View.GONE);
            }
        });
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new DividerItemDecoration(getApplicationContext(), LinearLayoutManager.VERTICAL));
        recyclerView.addOnItemTouchListener(new RecyclerItemTouchListener(getApplicationContext(), recyclerView, new IRecyclerItemClickListener() {
            @Override
            public void onClick(View view, int position) {
                ApplicationInfo currentItem = ApkListAdapter.getApplicationInfoSortedList().get(position);
//                Toast.makeText(ApkShareActivity.this, currentItem.publicSourceDir/*loadLabel(pm)*/, Toast.LENGTH_LONG).show();
                Intent i = new Intent();
                i.putExtra("share_apk", true);
                i.putExtra("apk_dir", currentItem.publicSourceDir);
                i.putExtra("apk_name", currentItem.loadLabel(pm));
                setResult(Activity.RESULT_OK, i);
                finish();
            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));
        fastScroller.setRecyclerView(recyclerView);
        fastScroller.setViewsToUse(R.layout.recycler_view_fast_scroller__fast_scroller, R.id.fast_scroller_bubble, R.id.fast_scroller_handle);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.action_apk_search, menu);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setOnQueryTextListener(this);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setQueryRefinementEnabled(true);

        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        final String lowerCaseQuery = newText.toLowerCase();

        final List<ApplicationInfo> filteredModelList = new ArrayList<>();
        for (ApplicationInfo model : packages) {
            final String package_name_text = model.packageName.toLowerCase();
            final String app_name_text = model.loadLabel(pm).toString().toLowerCase();
            if (package_name_text.contains(lowerCaseQuery) || app_name_text.contains(lowerCaseQuery))
                filteredModelList.add(model);
        }

        ApkListAdapter mimeTypeAdapter = new ApkListAdapter(pm, filteredModelList);
        recyclerView.setAdapter(mimeTypeAdapter);
        mimeTypeAdapter.notifyDataSetChanged();

        return true;
    }

    private static class RecyclerItemTouchListener implements RecyclerView.OnItemTouchListener {
        private GestureDetector gestureDetector;
        private IRecyclerItemClickListener clickListener;

        RecyclerItemTouchListener(Context context, final RecyclerView recyclerView, final IRecyclerItemClickListener clickListener) {
            this.clickListener = clickListener;
            gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onSingleTapUp(MotionEvent e) {
                    return true;
                }

                @Override
                public void onLongPress(MotionEvent e) {
                    View child = recyclerView.findChildViewUnder(e.getX(), e.getY());
                    if (child != null && clickListener != null) {
                        clickListener.onLongClick(child, recyclerView.getChildAdapterPosition(child));
                    }
                }
            });
        }

        @Override
        public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {

            View child = rv.findChildViewUnder(e.getX(), e.getY());
            if (child != null && clickListener != null && gestureDetector.onTouchEvent(e)) {
                clickListener.onClick(child, rv.getChildAdapterPosition(child));
            }
            return false;
        }

        @Override
        public void onTouchEvent(RecyclerView rv, MotionEvent e) {
        }

        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

        }
    }
}
