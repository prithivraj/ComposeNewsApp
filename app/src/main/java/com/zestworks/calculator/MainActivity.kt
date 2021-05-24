package com.zestworks.calculator

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.flowWithLifecycle
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.zestworks.calculator.model.NewsResponse
import com.zestworks.calculator.ui.theme.CalculatorTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import okhttp3.MediaType
import retrofit2.Retrofit


@ExperimentalAnimationApi
class MainActivity : ComponentActivity() {
    private val stateFlow = MutableStateFlow(
        NewsResponse(
            emptyList(),
            "",
            0
        )
    )

    private val newsService by lazy {
        val mediaType: MediaType = MediaType.get("application/json; charset=utf-8")
        val build = Retrofit
            .Builder()
            .baseUrl("https://newsapi.org/")
            .addConverterFactory(Json.asConverterFactory(mediaType))
            .build()
        build.create(NewsService::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        GlobalScope.launch(Dispatchers.IO) {
            val topTechNews = newsService.getTopTechNews()
            stateFlow.value = topTechNews
        }
        setContent {
            CalculatorTheme {
                MainScreen(stateFlow)
            }
        }
    }
}

@ExperimentalAnimationApi
@Composable
fun MainScreen(stateFlow: StateFlow<NewsResponse>) {
    val state by stateFlow.collectAsState()
    Surface(
        modifier = Modifier
            .fillMaxHeight()
            .fillMaxWidth()
    ) {
        LazyColumn {
            items(state.articles.size) {
                state.articles.forEach {
                    var expanded by remember {
                        mutableStateOf(false)
                    }
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .padding(16.dp)
                            .clickable {
                                expanded = !expanded
                            },
                        elevation = 16.dp,
                        shape = MaterialTheme.shapes.small
                    ) {
                        Column {
                            Text(
                                text = it.title,
                                modifier = Modifier.padding(16.dp)
                            )
                            AnimatedVisibility(visible = expanded) {
                                Text(
                                    text = it.content,
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@ExperimentalAnimationApi
@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    CalculatorTheme {
        MainScreen(
            MutableStateFlow(
                NewsResponse(
                    emptyList(),
                    "",
                    0
                )
            )
        )
    }
}