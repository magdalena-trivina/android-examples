package com.example.news.ui

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.news.common.Resource
import com.example.news.model.Article
import com.example.news.model.NewsResponse
import com.example.news.repository.NewsRepository
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.yield
import okio.IOException
import retrofit2.Response

class NewsViewModel(app: Application, val newsRepository: NewsRepository) : AndroidViewModel(app) {
    val headlines: MutableLiveData<Resource<NewsResponse?>> = MutableLiveData()
    var headlinesPage = 1
    var headlinesResponse: NewsResponse? = null

    val searchNews: MutableLiveData<Resource<NewsResponse?>> = MutableLiveData()
    var searchNewsPage = 1
    var searchNewsResponse: NewsResponse? = null
    var newSearchQuery: String? = null
    var oldSearchQuery: String? = null

    init {
        getHeadlines("us")
    }

    fun getHeadlines(countryCode: String) = viewModelScope.launch {
        headlinesInternet(countryCode)
    }

    fun searchNews(countryCode: String) = viewModelScope.launch {
        searchNewsInternet(countryCode)
    }

    private fun handleHeadlinesResponse(response: Response<NewsResponse>): Resource<NewsResponse?> {
        if (response.isSuccessful) {
            response.body().let { result ->
                headlinesPage++
                if (headlinesResponse == null) {
                    headlinesResponse = result
                } else {
                    val oldNews = headlinesResponse?.articles
                    val newNews = result?.articles
                    if (newNews != null) {
                        oldNews?.addAll(newNews)
                    }
                }
                return Resource.Success(headlinesResponse ?: result)
            }
        }
        return Resource.Error(null, response.message())
    }

    private fun handleSearchNewsResponse(response: Response<NewsResponse>): Resource<NewsResponse?> {
        if (response.isSuccessful) {
            response.body().let { result ->
                if (searchNewsResponse == null) {
                    searchNewsPage = 1
                    oldSearchQuery = newSearchQuery
                    searchNewsResponse = result
                } else {
                    searchNewsPage++
                    val oldNews = searchNewsResponse?.articles
                    val newNews = result?.articles
                    if (newNews != null) {
                        oldNews?.addAll(newNews)
                    }
                }
                return Resource.Success(searchNewsResponse ?: result)
            }
        }
        return Resource.Error(null, response.message())
    }

    fun addToFavorite(article: Article) = viewModelScope.launch {
        newsRepository.upsertArticle(article)
    }

    fun getFavoriteNews() = newsRepository.getAllArticles()

    fun deleteArticle(article: Article) = viewModelScope.launch {
        newsRepository.deleteArticle(article)
    }

    private fun internetConnection(context: Context): Boolean {
        (context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager).apply {
            return getNetworkCapabilities(activeNetwork)?.run {
                when {
                    hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                    hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                    hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                    else -> false
                }
            } ?: false
        }
    }

    private suspend fun headlinesInternet(countryCode: String) {
        headlines.postValue(Resource.Loading())
        try {
            if (internetConnection(this.getApplication())) {
                val response = newsRepository.getHeadlines(countryCode, headlinesPage)
                headlines.postValue(handleHeadlinesResponse(response))
            } else {
                headlines.postValue(Resource.Error(null, "No internet connection"))
            }
        } catch (t: Throwable) {
            when (t) {
                is IOException -> headlines.postValue(Resource.Error(null, "No signal"))
            }
        }
    }

    private suspend fun searchNewsInternet(countryCode: String) {
        searchNews.postValue(Resource.Loading())
        try {
            if (internetConnection(this.getApplication())) {
                val response = newsRepository.searchNews(countryCode, searchNewsPage)
                searchNews.postValue(handleSearchNewsResponse(response))
            } else {
                searchNews.postValue(Resource.Error(null, "No internet connection"))
            }
        } catch (t: Throwable) {
            when (t) {
                is IOException -> searchNews.postValue(Resource.Error(null, "No signal"))
            }
        }
    }
}
