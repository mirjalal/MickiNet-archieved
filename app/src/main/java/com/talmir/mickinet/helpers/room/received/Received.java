package com.talmir.mickinet.helpers.room.received;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

/**
 * @author miri
 * @since 7/15/2018
 */
@Entity(tableName = "received")
public class Received {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "name")
    public String fileName;

    @ColumnInfo(name = "type")
    public byte fileType;

    @ColumnInfo(name = "status")
    public byte operationStatus;

    @ColumnInfo(name = "date")
    public String dateTime;
}
