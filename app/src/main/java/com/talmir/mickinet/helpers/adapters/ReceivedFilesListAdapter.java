package com.talmir.mickinet.helpers.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.talmir.mickinet.R;
import com.talmir.mickinet.activities.FileStatisticsActivity;
import com.talmir.mickinet.helpers.room.received.ReceivedFilesEntity;

import java.util.List;

/**
 * @author miri
 * @since 7/26/2018
 */
public class ReceivedFilesListAdapter extends RecyclerView.Adapter<ReceivedFilesListAdapter.ReceivedFilesViewHolder> {

    class ReceivedFilesViewHolder extends RecyclerView.ViewHolder {
        private final TextView file_name, operation_status, date_time;

        private ReceivedFilesViewHolder(View itemView) {
            super(itemView);
            file_name = itemView.findViewById(R.id.r_file_name);
            operation_status = itemView.findViewById(R.id.r_operation_status);
            date_time = itemView.findViewById(R.id.r_date_time);
        }
    }

    private final LayoutInflater mInflater;
    private static List<ReceivedFilesEntity> mListOfFiles; // Cached copy of received files

    public ReceivedFilesListAdapter(Context context) { mInflater = LayoutInflater.from(context); }

    @NonNull
    @Override
    public ReceivedFilesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = mInflater.inflate(R.layout.list_item_received, parent, false);
        return new ReceivedFilesViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ReceivedFilesViewHolder holder, int position) {
        ReceivedFilesEntity rfe = mListOfFiles.get(position);
        holder.file_name.setText(rfe.r_f_name);
        holder.operation_status.setText(rfe.r_f_operation_status.equals("1") ? "Operation status: Succeeded" : "Operation status: Failed");
        holder.date_time.setText(rfe.r_f_time);
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
        if (mListOfFiles != null) {
            for (ReceivedFilesEntity temp : mListOfFiles) {
                if (temp.r_f_type.equals("1"))
                    FileStatisticsActivity.receivedPhotoFilesCount = FileStatisticsActivity.receivedPhotoFilesCount + 1.0f;
                else if (temp.r_f_type.equals("2"))
                    FileStatisticsActivity.receivedVideoFilesCount = FileStatisticsActivity.receivedVideoFilesCount + 1.0f;
                else if (temp.r_f_type.equals("3"))
                    FileStatisticsActivity.receivedAPKFilesCount = FileStatisticsActivity.receivedAPKFilesCount + 1.0f;
                else
                    FileStatisticsActivity.receivedOtherFilesCount = FileStatisticsActivity.receivedOtherFilesCount + 1.0f;
            }
//            Log.e("saylar", "photo: " + receivedPhotoFilesCount + "\nvideo: " + receivedVideoFilesCount + "\napk: " + receivedAPKFilesCount + "\nother: " + receivedOtherFilesCount);
        }

        getPhotoFilesCount();
        getVideoFilesCount();
        getAPKFilesCount();
        getOtherFilesCount();
    }

    private void getPhotoFilesCount() {
        try {
            FileStatisticsActivity.receivedPhotoFilesCount = FileStatisticsActivity.receivedPhotoFilesCount * 100 / getItemCount();
//            Log.e("tag", "receivedPhotoFilesCount:" + FileStatisticsActivity.receivedPhotoFilesCount);
        } catch (IllegalArgumentException | ArithmeticException ignored) {
            FileStatisticsActivity.receivedPhotoFilesCount = 0.0f;
        }
    }

    private void getVideoFilesCount() {
        try {
            FileStatisticsActivity.receivedVideoFilesCount = FileStatisticsActivity.receivedVideoFilesCount * 100 / getItemCount();
//            Log.e("tag", "receivedVideoFilesCount :" + FileStatisticsActivity.receivedVideoFilesCount );
        } catch (IllegalArgumentException | ArithmeticException ignored) {
            FileStatisticsActivity.receivedVideoFilesCount = 0.0f;
        }
    }

    private void getAPKFilesCount() {
        try {
            FileStatisticsActivity.receivedAPKFilesCount = FileStatisticsActivity.receivedAPKFilesCount * 100 / getItemCount();
//            Log.e("tag", "receivedAPKFilesCount :" + FileStatisticsActivity.receivedAPKFilesCount );
        } catch (IllegalArgumentException | ArithmeticException ignored) {
            FileStatisticsActivity.receivedAPKFilesCount = 0.0f;
        }
    }

    private void getOtherFilesCount() {
        try {
            FileStatisticsActivity.receivedOtherFilesCount = FileStatisticsActivity.receivedOtherFilesCount * 100 / getItemCount();
//            Log.e("tag", "receivedOtherFilesCount :" + FileStatisticsActivity.receivedOtherFilesCount );
        } catch (IllegalArgumentException | ArithmeticException ignored) {
            FileStatisticsActivity.receivedOtherFilesCount = 0.0f;
        }
    }
}
