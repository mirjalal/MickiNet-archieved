package com.talmir.mickinet.helpers.adapters;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.support.v7.util.SortedList;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.talmir.mickinet.R;
import com.talmir.mickinet.helpers.ui.IBubbleTextGetter;

import org.jetbrains.annotations.Contract;

import java.util.List;

/**
 * @author miri
 * @since 4/30/17
 */
public class ApkListAdapter extends RecyclerView.Adapter<ApkListAdapter.ApkViewHolder> implements IBubbleTextGetter {

    private static SortedList<ApplicationInfo> applicationInfoSortedList;
    private PackageManager packageManager;

    class ApkViewHolder extends RecyclerView.ViewHolder {
        private ImageView icon;
        private TextView app_name, package_name;

        ApkViewHolder(View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.icon);
            app_name = itemView.findViewById(R.id.app_name);
            package_name = itemView.findViewById(R.id.package_name);
        }
    }

    public ApkListAdapter(final PackageManager packageManager, List<ApplicationInfo> applicationInfoList) {
        this.packageManager = packageManager;

        applicationInfoSortedList = new SortedList<>(ApplicationInfo.class, new SortedList.Callback<ApplicationInfo>() {
            @Override
            public int compare(ApplicationInfo o1, ApplicationInfo o2) {
                return o1.loadLabel(packageManager).toString().compareTo(o2.loadLabel(packageManager).toString());
            }

            @Override
            public void onChanged(int position, int count) {
                notifyItemRangeChanged(position, count);
            }

            @Override
            public boolean areContentsTheSame(ApplicationInfo oldItem, ApplicationInfo newItem) {
                return oldItem.equals(newItem);
            }

            @Override
            public boolean areItemsTheSame(ApplicationInfo item1, ApplicationInfo item2) {
                return item1.packageName.equals(item2.packageName);
            }

            @Override
            public void onInserted(int position, int count) {
                notifyItemRangeInserted(position, count);
            }

            @Override
            public void onRemoved(int position, int count) {
                notifyItemRangeRemoved(position, count);
            }

            @Override
            public void onMoved(int fromPosition, int toPosition) {
                notifyItemMoved(fromPosition, toPosition);
            }
        });
        applicationInfoSortedList.addAll(applicationInfoList);
    }

    @Contract(pure = true)
    public static SortedList<ApplicationInfo> getApplicationInfoSortedList() {
        return applicationInfoSortedList;
    }

    @Override
    public String getTextToShowInBubble(int pos) {
        return Character.toString(applicationInfoSortedList.get(pos).loadLabel(packageManager).charAt(0));
    }

    @Override
    public ApkViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        /* Big hack: http://stackoverflow.com/a/2605838 */
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView = inflater.inflate(R.layout.list_item_apk_adapter, parent, false);
        return new ApkViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final ApkViewHolder holder, final int position) {
        final ApplicationInfo applicationInfo = applicationInfoSortedList.get(position);
        holder.icon.setImageDrawable(applicationInfo.loadIcon(packageManager));
        holder.app_name.setText(applicationInfo.loadLabel(packageManager).toString());
        holder.package_name.setText(applicationInfo.packageName);
    }

    @Override
    public int getItemCount() {
        return applicationInfoSortedList.size();
    }
}
