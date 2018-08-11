package com.talmir.mickinet.helpers.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.talmir.mickinet.R;
import com.talmir.mickinet.activities.FileStatisticsActivity;
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

    @NonNull
    @Override
    public SentFilesListAdapter.SentFilesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = mInflater.inflate(R.layout.list_item_sent, parent, false);
        return new SentFilesListAdapter.SentFilesViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull SentFilesListAdapter.SentFilesViewHolder holder, int position) {
        SentFilesEntity rfe = mListOfFiles.get(position);
        holder.file_name.setText(rfe.s_f_name);
        holder.operation_status.setText(rfe.s_f_operation_status.equals("1") ? "Operation status: Succeeded" : "Operation status: Failed");
        holder.date_time.setText(rfe.s_f_time);
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

    public void getSentFilesCountByTypes() {
        if (mListOfFiles != null) {
            for (SentFilesEntity temp : mListOfFiles) {
                if (temp.s_f_type.equals("1"))
                    FileStatisticsActivity.sentPhotoFilesCount = FileStatisticsActivity.sentPhotoFilesCount + 1.0f;
                else if (temp.s_f_type.equals("2"))
                    FileStatisticsActivity.sentVideoFilesCount = FileStatisticsActivity.sentVideoFilesCount + 1.0f;
                else if (temp.s_f_type.equals("3"))
                    FileStatisticsActivity.sentAPKFilesCount = FileStatisticsActivity.sentAPKFilesCount + 1.0f;
                else
                    FileStatisticsActivity.sentOtherFilesCount = FileStatisticsActivity.sentOtherFilesCount + 1.0f;
            }
//            Log.e("saylar", "photo: " + FileStatisticsActivity.sentPhotoFilesCount + "\nvideo: " + FileStatisticsActivity.sentVideoFilesCount + "\napk: " + FileStatisticsActivity.sentAPKFilesCount + "\nother: " + FileStatisticsActivity.sentOtherFilesCount);
        }

        getPhotoFilesCount();
        getVideoFilesCount();
        getAPKFilesCount();
        getOtherFilesCount();
    }

    private void getPhotoFilesCount() {
        try {
            FileStatisticsActivity.sentPhotoFilesCount = FileStatisticsActivity.sentPhotoFilesCount * 100 / getItemCount();
//            Log.e("tag", "sentPhotoFilesCount:" + FileStatisticsActivity.sentPhotoFilesCount);
        } catch (IllegalArgumentException | ArithmeticException ignored) {
            FileStatisticsActivity.sentPhotoFilesCount = 0.0f;
        }
    }

    private void getVideoFilesCount() {
        try {
            FileStatisticsActivity.sentVideoFilesCount = FileStatisticsActivity.sentVideoFilesCount * 100 / getItemCount();
//            Log.e("tag", "sentVideoFilesCount :" + FileStatisticsActivity.sentVideoFilesCount );
        } catch (IllegalArgumentException | ArithmeticException ignored) {
            FileStatisticsActivity.sentVideoFilesCount = 0.0f;
        }
    }

    private void getAPKFilesCount() {
        try {
            FileStatisticsActivity.sentAPKFilesCount = FileStatisticsActivity.sentAPKFilesCount * 100 / getItemCount();
//            Log.e("tag", "sentAPKFilesCount :" + FileStatisticsActivity.sentAPKFilesCount );
        } catch (IllegalArgumentException | ArithmeticException ignored) {
            FileStatisticsActivity.sentAPKFilesCount = 0.0f;
        }
    }

    private void getOtherFilesCount() {
        try {
            FileStatisticsActivity.sentOtherFilesCount = FileStatisticsActivity.sentOtherFilesCount * 100 / getItemCount();
//            Log.e("tag", "sentOtherFilesCount :" + FileStatisticsActivity.sentOtherFilesCount );
        } catch (IllegalArgumentException | ArithmeticException ignored) {
            FileStatisticsActivity.sentOtherFilesCount = 0.0f;
        }
    }
}
