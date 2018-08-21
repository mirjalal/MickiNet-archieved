package com.talmir.mickinet.helpers.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.talmir.mickinet.R;
import com.talmir.mickinet.helpers.room.sent.SentFilesEntity;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import static com.talmir.mickinet.activities.FileStatisticsActivity.PlaceholderFragment.sentAPKFilesCount;
import static com.talmir.mickinet.activities.FileStatisticsActivity.PlaceholderFragment.sentMediaFilesCount;
import static com.talmir.mickinet.activities.FileStatisticsActivity.PlaceholderFragment.sentOtherFilesCount;
import static com.talmir.mickinet.activities.FileStatisticsActivity.PlaceholderFragment.sentPhotoFilesCount;
import static com.talmir.mickinet.activities.FileStatisticsActivity.PlaceholderFragment.sentVideoFilesCount;

/**
 * @author miri
 * @since 7/29/2018
 */
public class SentFilesListAdapter extends RecyclerView.Adapter<SentFilesListAdapter.SentFilesViewHolder> {

    class SentFilesViewHolder extends RecyclerView.ViewHolder {
        private final TextView file_name, operation_status, date_time;

        SentFilesViewHolder(View itemView) {
            super(itemView);
            file_name = itemView.findViewById(R.id.s_file_name);
            operation_status = itemView.findViewById(R.id.s_operation_status);
            date_time = itemView.findViewById(R.id.s_date_time);
        }
    }

    private final LayoutInflater mInflater;
    private List<SentFilesEntity> mListOfFiles; // Cached copy of sent files
    private int size = 0;
    private final SimpleDateFormat dateFormatter;

    public SentFilesListAdapter(Context context) {
        mInflater = LayoutInflater.from(context);
        dateFormatter = new SimpleDateFormat("dd MMM yyyy HH:mm:ss", new Locale(Locale.getDefault().getLanguage(), Locale.getDefault().getCountry()/*, Locale.getDefault().getDisplayVariant()*/));
    }

    @NonNull
    @Override
    public SentFilesListAdapter.SentFilesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = mInflater.inflate(R.layout.list_item_sent, parent, false);
        return new SentFilesListAdapter.SentFilesViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull SentFilesListAdapter.SentFilesViewHolder holder, int position) {
        SentFilesEntity sfe = mListOfFiles.get(position);
        holder.file_name.setText(sfe.s_f_name);
        holder.operation_status.setText(sfe.s_f_operation_status.equals("1") ? mInflater.getContext().getString(R.string.operation_succeeded) : mInflater.getContext().getString(R.string.operation_failed));
        holder.date_time.setText(dateFormatter.format(sfe.s_f_time));
    }

    // getItemCount() is called many times, and when it is first called,
    // mListOfFiles has not been updated (means initially, it's null, and we can't return null).
    @Override
    public int getItemCount() {
        return size;
    }

    public void setSentFiles(List<SentFilesEntity> sentFiles) {
        size = sentFiles.size();
        mListOfFiles = sentFiles;
        notifyDataSetChanged();
    }

    public void getSentFilesCountByTypes() {
        if (size > 0) {
            for (SentFilesEntity temp : mListOfFiles) {
                switch (temp.s_f_type) {
                    case "1":
                        sentPhotoFilesCount++;
                        break;
                    case "2":
                        sentVideoFilesCount++;
                        break;
                    case "3":
                        sentAPKFilesCount++;
                        break;
                    case "4":
                        sentMediaFilesCount++;
                        break;
                    default:
                        sentOtherFilesCount++;
                        break;
                }
            }
            getPhotoFilesCount();
            getVideoFilesCount();
            getMusicFilesCount();
            getAPKFilesCount();
            getOtherFilesCount();
        }
    }

    private void getPhotoFilesCount() {
        try {
            sentPhotoFilesCount = sentPhotoFilesCount * 100 / size;
        } catch (IllegalArgumentException | ArithmeticException ignored) {
            sentPhotoFilesCount = 0;
        }
    }

    private void getVideoFilesCount() {
        try {
            sentVideoFilesCount = sentVideoFilesCount * 100 / size;
        } catch (IllegalArgumentException | ArithmeticException ignored) {
            sentVideoFilesCount = 0;
        }
    }

    private void getMusicFilesCount() {
        try {
            sentMediaFilesCount = sentMediaFilesCount * 100 / size;
        } catch (IllegalArgumentException | ArithmeticException ignored) {
            sentMediaFilesCount = 0;
        }
    }

    private void getAPKFilesCount() {
        try {
            sentAPKFilesCount = sentAPKFilesCount * 100 / size;
        } catch (IllegalArgumentException | ArithmeticException ignored) {
            sentAPKFilesCount = 0;
        }
    }

    private void getOtherFilesCount() {
        try {
            sentOtherFilesCount = sentOtherFilesCount * 100 / size;
        } catch (IllegalArgumentException | ArithmeticException ignored) {
            sentOtherFilesCount = 0;
        }
    }
}
