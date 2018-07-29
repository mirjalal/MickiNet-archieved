package com.talmir.mickinet.helpers.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.talmir.mickinet.R;
import com.talmir.mickinet.helpers.room.received.ReceivedFilesEntity;

import java.util.List;

/**
 * @author miri
 * @since 7/26/2018
 */
public class ReceivedFilesListAdapter extends RecyclerView.Adapter<ReceivedFilesListAdapter.ReceivedFilesViewHolder> {

    class ReceivedFilesViewHolder extends RecyclerView.ViewHolder {
        private final TextView file_name, operation_status, date_time;

        ReceivedFilesViewHolder(View itemView) {
            super(itemView);
            file_name = itemView.findViewById(R.id.file_name);
            operation_status = itemView.findViewById(R.id.operation_status);
            date_time = itemView.findViewById(R.id.date_time);
        }
    }

    private final LayoutInflater mInflater;
    private List<ReceivedFilesEntity> mListOfFiles; // Cached copy of received files
    private float photoFilesCount = 0;
    private float videoFilesCount = 0;
    private float APKFilesCount = 0;
    private float otherFilesCount = 0;

    public float getPhotoFilesCount() {
        try {
            return photoFilesCount * 100 / getItemCount();
        } catch (IllegalArgumentException | ArithmeticException ignored) {
            return 0;
        }
    }

    public float getVideoFilesCount() {
        try {
            return videoFilesCount * 100 / getItemCount();
        } catch (IllegalArgumentException | ArithmeticException ignored) {
            return 0;
        }
    }

    public float getAPKFilesCount() {
        try {
            return APKFilesCount * 100 / getItemCount();
        } catch (IllegalArgumentException | ArithmeticException ignored) {
            return 0;
        }
    }

    public float getOtherFilesCount() {
        try {
            return otherFilesCount * 100 / getItemCount();
        } catch (IllegalArgumentException | ArithmeticException ignored) {
            return 0;
        }
    }

    public ReceivedFilesListAdapter(Context context) { mInflater = LayoutInflater.from(context); }

    @Override
    public ReceivedFilesViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = mInflater.inflate(R.layout.list_item_received, parent, false);
        return new ReceivedFilesViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ReceivedFilesViewHolder holder, int position) {
        ReceivedFilesEntity rfe = mListOfFiles.get(position);
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

    public void setReceivedFiles(List<ReceivedFilesEntity> receivedFiles){
        mListOfFiles = receivedFiles;
        notifyDataSetChanged();
    }

    public void getReceivedFilesCountByTypes() {
        for (ReceivedFilesEntity temp : mListOfFiles) {
            if (temp.f_type.equals("1"))
                photoFilesCount++;
            else if (temp.f_type.equals("2"))
                videoFilesCount++;
            else if (temp.f_type.equals("3"))
                APKFilesCount++;
            else
                otherFilesCount++;
        }
    }
}
