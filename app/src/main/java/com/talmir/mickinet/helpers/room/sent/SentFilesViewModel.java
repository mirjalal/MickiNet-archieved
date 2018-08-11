package com.talmir.mickinet.helpers.room.sent;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;

import java.util.List;

/**
 * @author miri
 * @since 7/29/2018
 */
public class SentFilesViewModel extends AndroidViewModel {
    private SentFilesRepository mRepository;
    private LiveData<List<SentFilesEntity>> mAllSentFiles;

    public SentFilesViewModel(Application application) {
        super(application);
        mRepository = new SentFilesRepository(application);
        mAllSentFiles = mRepository.getAllSentFiles();
    }

    public LiveData<List<SentFilesEntity>> getAllSentFiles() { return mAllSentFiles; }

    public void insert(SentFilesEntity sentFile) { mRepository.insert(sentFile); }
}
