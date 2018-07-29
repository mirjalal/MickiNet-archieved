package com.talmir.mickinet.helpers.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.talmir.mickinet.R;
import com.talmir.mickinet.helpers.room.sent.SentFilesEntity;

import java.util.List;

/**
 * @author miri
 * @since 7/29/2018
 */
public class SentFilesListAdapter extends RecyclerView.Adapter<SentFilesListAdapter.SentFilesViewHolder> {

    class SentFilesViewHolder extends RecyclerView.ViewHolder {
        private final TextView file_name, operation_status, date_time;

        SentFilesViewHolder(View itemView) {
            super(itemView);
            file_name = itemView.findViewById(R.id.file_name);
            operation_status = itemView.findViewById(R.id.operation_status);
            date_time = itemView.findViewById(R.id.date_time);
        }
    }

    private final LayoutInflater mInflater;
    private List<SentFilesEntity> mListOfFiles; // Cached copy of sent files

    public SentFilesListAdapter(Context context) { mInflater = LayoutInflater.from(context); }

    @Override
    public SentFilesListAdapter.SentFilesViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = mInflater.inflate(R.layout.list_item_received, parent, false);
        return new SentFilesListAdapter.SentFilesViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(SentFilesListAdapter.SentFilesViewHolder holder, int position) {
        SentFilesEntity rfe = mListOfFiles.get(position);
        holder.file_name.setText(rfe.f_name);
        holder.operation_status.setText(rfe.f_operation_status.equals("1") ? "Operation status: Succeeded" : "Operation status: Failed");
        holder.date_time.setText(rfe.f_time);
    }

    // getItemCount() is called many times, and when it is first called,
    // mListOfFiles has not been updated (means initially, it's null, and we can't return null).
    @Override
    public int getItemCount() {
        return (mListOfFiles != null) ? mListOfFiles.size() : 0;
    }

    public void setSentFiles(List<SentFilesEntity> sentFiles){
        mListOfFiles = sentFiles;
        notifyDataSetChanged();
    }
}
