package com.talmir.mickinet.helpers.adapter;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.support.v7.util.SortedList;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.talmir.mickinet.R;
import com.talmir.mickinet.helpers.ui.IBubbleTextGetter;

import java.util.List;

public class ApkListAdapter extends RecyclerView.Adapter<ApkListAdapter.ApkViewHolder> implements IBubbleTextGetter {

    private PackageManager packageManager;

    private SortedList<ApplicationInfo> applicationInfoList;

    @Override
    public String getTextToShowInBubble(int pos) {
        return Character.toString(applicationInfoList.get(pos).loadLabel(packageManager).charAt(0));
    }

    class ApkViewHolder extends RecyclerView.ViewHolder {
        private CardView cardView;
        private ImageView icon;
        private TextView app_name, package_name;

        ApkViewHolder(View itemView) {
            super(itemView);
            cardView = (CardView) itemView.findViewById(R.id.cardView);
            icon = (ImageView) itemView.findViewById(R.id.icon);
            app_name = (TextView) itemView.findViewById(R.id.app_name);
            package_name = (TextView) itemView.findViewById(R.id.package_name);
        }
    }

    public ApkListAdapter(final PackageManager packageManager, List<ApplicationInfo> applicationInfoList) {
        this.packageManager = packageManager;

        this.applicationInfoList = new SortedList<>(ApplicationInfo.class, new SortedList.Callback<ApplicationInfo>() {
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

        this.applicationInfoList.addAll(applicationInfoList);
    }

    public void add(ApplicationInfo model) {
        applicationInfoList.add(model);
    }

    public void remove(ApplicationInfo model) {
        applicationInfoList.remove(model);
    }

    public void add(List<ApplicationInfo> models) {
        applicationInfoList.addAll(models);
    }

    public void remove(List<ApplicationInfo> models) {
        applicationInfoList.beginBatchedUpdates();
        for (ApplicationInfo model : models) {
            applicationInfoList.remove(model);
        }
        applicationInfoList.endBatchedUpdates();
    }

    public void replaceAll(List<ApplicationInfo> models) {
        applicationInfoList.beginBatchedUpdates();
        for (int i = applicationInfoList.size() - 1; i >= 0; i--) {
            final ApplicationInfo model = applicationInfoList.get(i);
            if (!models.contains(model))
                applicationInfoList.remove(model);
        }
        applicationInfoList.addAll(models);
        applicationInfoList.endBatchedUpdates();
    }

    @Override
    public ApkViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        /* Big hack: http://stackoverflow.com/a/2605838 */
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView = inflater.inflate(R.layout.list_item, parent, false);
        return new ApkViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final ApkViewHolder holder, int position) {
        final ApplicationInfo applicationInfo = applicationInfoList.get(position);
        holder.icon.setImageDrawable(applicationInfo.loadIcon(packageManager));
        holder.app_name.setText(applicationInfo.loadLabel(packageManager).toString());
        holder.package_name.setText(applicationInfo.packageName);
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(v.getContext(), /*holder.app_name.getText().toString()*/applicationInfo.publicSourceDir, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return applicationInfoList.size();
    }
}
