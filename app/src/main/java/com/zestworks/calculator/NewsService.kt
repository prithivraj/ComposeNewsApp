package com.zestworks.calculator

import com.zestworks.calculator.model.NewsResponse
import retrofit2.http.GET
import retrofit2.http.Path

interface NewsService {
    @GET("v2/top-headlines?sources=techcrunch&apikey=75bf944bdf0b4cf7bfcbf4c7e00ff6b7")
    suspend fun getTopTechNews(): NewsResponse
}