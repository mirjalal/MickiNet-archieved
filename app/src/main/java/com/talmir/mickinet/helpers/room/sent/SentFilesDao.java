package com.talmir.mickinet.helpers.room.sent;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import java.util.List;

/**
 * @author miri
 * @since 7/29/2018
 */
@Dao
public interface SentFilesDao {
    // LiveData is a data holder class that can be observed within a given lifecycle.
    // Always holds/caches latest version of data. Notifies its active observers when the
    // data has changed. Since we are getting all the contents of the database,
    // we are notified whenever any of the database contents have changed.
    @Query("SELECT id, name, type, status, dateTime FROM sent")
    LiveData<List<SentFilesEntity>> getAllSentFiles();

    // We do not need a conflict strategy, because the s_f_id is our primary key, and you cannot
    // add two items with the same primary key to the database.
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(SentFilesEntity file);

    @Query("DELETE FROM sent")
    void deleteAllRecords();
}
