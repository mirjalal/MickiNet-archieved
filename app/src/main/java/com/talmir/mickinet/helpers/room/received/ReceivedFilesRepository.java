package com.talmir.mickinet.helpers.room.received;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.os.AsyncTask;

import com.talmir.mickinet.helpers.room.utils.AppDatabase;

import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * @author miri
 * @since 7/26/2018
 */
public class ReceivedFilesRepository {
    private static IReceivedFilesDao mReceivedFilesDao;
    private LiveData<List<ReceivedFilesEntity>> mAllReceivedFiles;

    ReceivedFilesRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        mReceivedFilesDao = db.mReceivedFilesDao();
        mAllReceivedFiles = mReceivedFilesDao.getAllReceivedFiles();
    }

    LiveData<List<ReceivedFilesEntity>> getAllReceivedFiles() { return mAllReceivedFiles; }

    public void insert(ReceivedFilesEntity entity) {
        // AsyncTask.THREAD_POOL_EXECUTOR is important!
        new insertAsyncTask(mReceivedFilesDao).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, entity);
    }

    private static class insertAsyncTask extends AsyncTask<ReceivedFilesEntity, Void, Void> {
        private IReceivedFilesDao mAsyncTaskDao;

        insertAsyncTask(IReceivedFilesDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final ReceivedFilesEntity... params) {
            mAsyncTaskDao.insert(params[0]);
            return null;
        }
    }

    public boolean deleteAllRecords() {
        AsyncTask<Void, Void, Boolean> deleteTask = new deleteAllRecordsAsyncTask(mReceivedFilesDao).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        try {
            return deleteTask.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
            return false;
        } catch (ExecutionException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static class deleteAllRecordsAsyncTask extends AsyncTask<Void, Void, Boolean> {
        private IReceivedFilesDao dao;

        deleteAllRecordsAsyncTask(IReceivedFilesDao dao) {
            this.dao = dao;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                dao.deleteAllRecords();
                return true;
            } catch (Exception ignored) {
                return false;
            }
        }
    }
}
