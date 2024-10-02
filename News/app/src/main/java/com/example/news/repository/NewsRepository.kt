package com.example.news.repository

import com.example.news.api.RetrofitInstance
import com.example.news.model.Article
import com.example.news.database.ArticleDatabase

class NewsRepository(private val db: ArticleDatabase) {
    suspend fun deleteArticle(article: Article) = db.getArticleDao().deleteArticle(article)
    suspend fun upsertArticle(article: Article) = db.getArticleDao().upsertArticle(article)

    fun getAllArticles() = db.getArticleDao().getAllArticles()

    suspend fun getHeadlines(countryCode: String, pageNumber: Int) =
        RetrofitInstance.api.getHeadlines(countryCode, pageNumber)

    suspend fun searchNews(countryCode: String, pageNumber: Int) =
        RetrofitInstance.api.searchForNews(countryCode, pageNumber)
}