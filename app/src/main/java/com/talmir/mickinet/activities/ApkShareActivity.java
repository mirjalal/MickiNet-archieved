package com.talmir.mickinet.activities;

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
import android.view.Menu;

import com.talmir.mickinet.R;
import com.talmir.mickinet.helpers.adapter.ApkListAdapter;
import com.talmir.mickinet.helpers.ui.DividerItemDecoration;

import java.util.ArrayList;
import java.util.List;

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
//        for (ApplicationInfo app : packages) {
//            checks for flags; if flagged, check if updated system app
//            if((app.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0) {
//                applicationInfoSortedList.add(app);
////                it's a system app, not interested
//            } if ((app.flags & ApplicationInfo.FLAG_SYSTEM) != 0) {
//                //Discard this one
//                //in this case, it should be a user-installed app
//                applicationInfoSortedList.add(app);
//            } else {
//                applicationInfoSortedList.add(app);
//            }
//        }
//        packages_.clear();

//        applicationInfoSortedList.addAll(pm.getInstalledApplications(PackageManager.GET_META_DATA));

        // http://www.android--tutorials.com/2016/03/android-get-installed-apps-list.html

        final ApkListAdapter apkListAdapter = new ApkListAdapter(pm, packages);
        apkListAdapter.notifyDataSetChanged();

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        recyclerView.setAdapter(apkListAdapter);
//        final RecyclerViewFastScroller fastScroller = (RecyclerViewFastScroller) this.findViewById(R.id.fastscroller);
//        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false) {
//            @Override
//            public void onLayoutChildren(final RecyclerView.Recycler recycler, final RecyclerView.State state) {
//                super.onLayoutChildren(recycler, state);
//
//                final int firstVisibleItemPosition = findFirstVisibleItemPosition();
//                if (firstVisibleItemPosition != 0) {
////                     this avoids trying to handle un-needed calls
//                    if (firstVisibleItemPosition == -1)
////                        not initialized, or no items shown, so hide fast-scroller
//                        fastScroller.setVisibility(View.GONE);
//                    return;
//                }
//                final int lastVisibleItemPosition = findLastVisibleItemPosition();
//                int itemsShown = lastVisibleItemPosition - firstVisibleItemPosition + 1;
////                if all items are shown, hide the fast-scroller
//                fastScroller.setVisibility(apkListAdapter.getItemCount() > itemsShown ? View.VISIBLE : View.GONE);
//            }
//        });
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new DividerItemDecoration(getApplicationContext(), LinearLayoutManager.VERTICAL));
//        fastScroller.setRecyclerView(recyclerView);
//        fastScroller.setViewsToUse(R.layout.recycler_view_fast_scroller__fast_scroller, R.id.fast_scroller_bubble, R.id.fast_scroller_handle);
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

//        SearchRecentSuggestions suggestions = new SearchRecentSuggestions(this, ApkSearchSuggestionProvider.AUTHORITY, ApkSearchSuggestionProvider.MODE);
//        suggestions.saveRecentQuery(newText, null);

        final List<ApplicationInfo> filteredModelList = new ArrayList<>();
        for (ApplicationInfo model : packages) {
            final String package_name_text = model.packageName.toLowerCase();
            final String app_name_text = model.loadLabel(pm).toString().toLowerCase();
            if (package_name_text.contains(lowerCaseQuery) || app_name_text.contains(lowerCaseQuery)) {
                filteredModelList.add(model);
            }
        }

        ApkListAdapter mimeTypeAdapter = new ApkListAdapter(pm, filteredModelList);
        recyclerView.setAdapter(mimeTypeAdapter);
        mimeTypeAdapter.notifyDataSetChanged();
        return false;
    }
}
