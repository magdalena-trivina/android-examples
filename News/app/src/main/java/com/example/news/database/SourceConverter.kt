package com.example.news.database

import androidx.room.TypeConverters
import com.example.news.model.Source

class SourceConverter {

    @TypeConverters
    fun fromSource(source: Source): String {
        return source.name
    }

    @TypeConverters
    fun toSource(name: String): Source {
        return Source(name, name)
    }
}