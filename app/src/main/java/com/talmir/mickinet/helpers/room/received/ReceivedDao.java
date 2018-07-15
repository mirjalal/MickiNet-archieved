package com.talmir.mickinet.helpers.room.received;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import java.util.List;

/**
 * @author miri
 * @since 7/15/2018
 */
@Dao
public interface ReceivedDao {
    @Query("SELECT id, name, type, date, status FROM received;")
    List<Received> getAllReceiveds();

    @Insert(onConflict = OnConflictStrategy.ROLLBACK)
    void insertRecord(Received received);

    @Query("DELETE FROM received")
    void deleteAllReceiveds();
}
