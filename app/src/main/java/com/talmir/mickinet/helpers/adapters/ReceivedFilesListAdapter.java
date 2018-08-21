package com.talmir.mickinet.helpers.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.talmir.mickinet.R;
import com.talmir.mickinet.helpers.room.received.ReceivedFilesEntity;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import static com.talmir.mickinet.activities.FileStatisticsActivity.PlaceholderFragment.receivedAPKFilesCount;
import static com.talmir.mickinet.activities.FileStatisticsActivity.PlaceholderFragment.receivedMediaFilesCount;
import static com.talmir.mickinet.activities.FileStatisticsActivity.PlaceholderFragment.receivedOtherFilesCount;
import static com.talmir.mickinet.activities.FileStatisticsActivity.PlaceholderFragment.receivedPhotoFilesCount;
import static com.talmir.mickinet.activities.FileStatisticsActivity.PlaceholderFragment.receivedVideoFilesCount;

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
    private List<ReceivedFilesEntity> mListOfFiles; // Cached copy of received files
    private int size = 0;
    private final SimpleDateFormat dateFormatter;

    public ReceivedFilesListAdapter(Context context) {
        mInflater = LayoutInflater.from(context);
        dateFormatter = new SimpleDateFormat("dd MMM yyyy HH:mm:ss", new Locale(Locale.getDefault().getLanguage(), Locale.getDefault().getCountry()/*, Locale.getDefault().getDisplayVariant()*/));
    }

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
        holder.operation_status.setText(rfe.r_f_operation_status.equals("1") ?  mInflater.getContext().getString(R.string.operation_succeeded) : mInflater.getContext().getString(R.string.operation_failed));
        holder.date_time.setText(dateFormatter.format(rfe.r_f_time));
    }

    // getItemCount() is called many times, and when it is first called,
    // mListOfFiles has not been updated (means initially, it's null, and we can't return null).
    @Override
    public int getItemCount() {
        return size;
    }

    public void setReceivedFiles(List<ReceivedFilesEntity> receivedFiles) {
        size = receivedFiles.size();
        mListOfFiles = receivedFiles;
        notifyDataSetChanged();
    }

    public void getReceivedFilesCountByTypes() {
        if (size > 0) {
            for (ReceivedFilesEntity temp : mListOfFiles) {
                switch (temp.r_f_type) {
                    case "1":
                        receivedPhotoFilesCount++;
                        break;
                    case "2":
                        receivedVideoFilesCount++;
                        break;
                    case "3":
                        receivedAPKFilesCount++;
                        break;
                    case "4":
                        receivedMediaFilesCount++;
                        break;
                    default:
                        receivedOtherFilesCount++;
                        break;
                }
            }
        }
        getPhotoFilesCount();
        getVideoFilesCount();
        getMusicFilesCount();
        getAPKFilesCount();
        getOtherFilesCount();
    }

    private void getPhotoFilesCount() {
        try {
            receivedPhotoFilesCount = receivedPhotoFilesCount * 100 / size;
        } catch (IllegalArgumentException | ArithmeticException ignored) {
            receivedPhotoFilesCount = 0;
        }
    }

    private void getVideoFilesCount() {
        try {
            receivedVideoFilesCount = receivedVideoFilesCount * 100 / size;
        } catch (IllegalArgumentException | ArithmeticException ignored) {
            receivedVideoFilesCount = 0;
        }
    }

    private void getMusicFilesCount() {
        try {
            receivedMediaFilesCount = receivedMediaFilesCount * 100 / size;
        } catch (IllegalArgumentException | ArithmeticException ignored) {
            receivedMediaFilesCount = 0;
        }
    }

    private void getAPKFilesCount() {
        try {
            receivedAPKFilesCount = receivedAPKFilesCount * 100 / size;
        } catch (IllegalArgumentException | ArithmeticException ignored) {
            receivedAPKFilesCount = 0;
        }
    }

    private void getOtherFilesCount() {
        try {
            receivedOtherFilesCount = receivedOtherFilesCount * 100 / size;
        } catch (IllegalArgumentException | ArithmeticException ignored) {
            receivedOtherFilesCount = 0;
        }
    }
}
