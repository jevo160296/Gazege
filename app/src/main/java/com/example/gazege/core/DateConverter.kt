package com.example.gazege.core

import androidx.room.TypeConverter
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset

class DateConverter {
    @TypeConverter
    fun toDate(dateLong: Long?): LocalDate? {
        return if (dateLong == null) null else LocalDateTime
            .ofEpochSecond(dateLong, 0, ZoneOffset.ofHours(0))
            .toLocalDate()
    }

    @TypeConverter
    fun fromDate(date: LocalDate?): Long? {
        return date?.atStartOfDay()?.toEpochSecond(ZoneOffset.ofHours(0))
    }
}