package com.talmir.mickinet.helpers.room.utils;

import android.arch.persistence.room.TypeConverter;

import java.util.Date;

/**
 * @author miri
 * @since 8/20/2018
 */
public class DateConverter {
    @TypeConverter
    public static Long dateToLong(Date date) {
        return date == null ? null : date.getTime();
    }

    @TypeConverter
    public static Date longToDate(Long time) {
        return time == null ? null : new Date(time);
    }
}
