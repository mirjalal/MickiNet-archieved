package com.talmir.mickinet.helpers.room.received;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;

import java.util.List;

/**
 * @author miri
 * @since 7/26/2018
 */
public class ReceivedFilesViewModel extends AndroidViewModel {
    private ReceivedFilesRepository mRepository;
    private LiveData<List<ReceivedFilesEntity>> mAllReceivedFiles;

    public ReceivedFilesViewModel(Application application) {
        super(application);
        mRepository = new ReceivedFilesRepository(application);
        mAllReceivedFiles = mRepository.getAllReceivedFiles();
    }

    public LiveData<List<ReceivedFilesEntity>> getAllReceivedFiles() { return mAllReceivedFiles; }

    public void insert(ReceivedFilesEntity receivedFile) { mRepository.insert(receivedFile); }

    public boolean deleteAllRecords() { return mRepository.deleteAllRecords(); }
}
