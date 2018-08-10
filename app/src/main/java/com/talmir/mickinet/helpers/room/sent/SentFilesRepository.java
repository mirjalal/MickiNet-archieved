package com.talmir.mickinet.helpers.room.sent;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.os.AsyncTask;
import android.util.Log;

import com.talmir.mickinet.helpers.room.db.AppDatabase;

import java.util.List;

/**
 * @author miri
 * @since 7/29/2018
 */
public class SentFilesRepository {

    private SentFilesDao mSentFilesDao;
    private LiveData<List<SentFilesEntity>> mAllSentFiles;

    public SentFilesRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        mSentFilesDao = db.mSentFilesDao();
        mAllSentFiles = mSentFilesDao.getAllSentFiles();
    }

    public LiveData<List<SentFilesEntity>> getAllSentFiles() { return mAllSentFiles; }

    public void insert(SentFilesEntity sentFilesEntity) {
        new SentFilesRepository.insertAsyncTask(mSentFilesDao).execute(sentFilesEntity);
    }

    private static class insertAsyncTask extends AsyncTask<SentFilesEntity, Void, Void> {
        private SentFilesDao mAsyncTaskDao;

        insertAsyncTask(SentFilesDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final SentFilesEntity... params) {
            mAsyncTaskDao.insert(params[0]);
            Log.e("doInBackground: ", "reached here!");
            return null;
        }
    }
}
