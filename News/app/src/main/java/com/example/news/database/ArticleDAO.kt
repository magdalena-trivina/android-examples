package com.example.news.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import com.example.news.model.Article

@Dao
interface ArticleDao {
    @Upsert
    suspend fun upsertArticle(note: Article)

    @Delete
    suspend fun deleteArticle(note: Article)

    @Query("SELECT * FROM ARTICLE ORDER BY ID DESC")
    fun getAllArticles(): LiveData<List<Article>>
}