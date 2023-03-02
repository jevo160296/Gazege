package com.example.gazege.core

import androidx.room.TypeConverter
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

class DateConverter {
    @TypeConverter
    fun toDate(dateLong: Long?): LocalDate? {
        val instant = dateLong?.let { Instant.ofEpochMilli(it) }
        return instant?.atOffset(ZoneOffset.ofHours(-5))?.toLocalDate()
    }

    @TypeConverter
    fun fromDate(date: LocalDate?): Long? {
        return date
            ?.atStartOfDay()
            ?.toInstant(ZoneOffset.ofHours(-5))
            ?.toEpochMilli()
    }
}