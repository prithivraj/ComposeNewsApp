package com.zestworks.calculator

import android.os.Bundle
import android.util.Log
import android.view.KeyEvent.ACTION_DOWN
import android.view.KeyEvent.ACTION_UP
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.colorResource
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
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import okhttp3.MediaType
import retrofit2.Retrofit


@ExperimentalFoundationApi
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

@ExperimentalFoundationApi
@ExperimentalAnimationApi
@Composable
fun MainScreen(stateFlow: StateFlow<NewsResponse>) {
    val state by stateFlow.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    var focussedIndex = remember { 0 }
    Surface(
        modifier = Modifier
            .fillMaxHeight()
            .fillMaxWidth()
    ) {
        LazyColumn(
            modifier = Modifier
                .onPreviewKeyEvent { keyEvent ->
                if (keyEvent.nativeKeyEvent.action == ACTION_DOWN) {
                    if (keyEvent.key == Key(android.view.KeyEvent.KEYCODE_DPAD_DOWN)) {
                        focussedIndex++
                    } else if (keyEvent.key == Key(android.view.KeyEvent.KEYCODE_DPAD_UP)) {
                        focussedIndex--
                    }
                    state.articles.forEachIndexed { index, article ->
                        coroutineScope.launch {
                            if (index != focussedIndex) {
                                article.interactionSource.emit(FocusInteraction.Unfocus(article.focus))
                            } else {
                                article.interactionSource.emit(article.focus)
                            }
                        }
                    }
                }
                return@onPreviewKeyEvent false
            }
        ) {
            items(state.articles.size) {
                state.articles.forEachIndexed { index, article ->
                    var expanded by remember { mutableStateOf(false) }
                    val focussed = article.interactionSource.collectIsFocusedAsState().value
                    Card(
                        modifier = Modifier
                            .focusable(true, article.interactionSource)
                            .background(if (focussed) Color.Blue else Color.White)
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .padding(16.dp)

                            .combinedClickable(
                                enabled = true,
                                onLongClick = {
                                    expanded = !expanded
                                },
                                onClick = {

                                }
                            ),
                        elevation = 16.dp,
                        shape = MaterialTheme.shapes.small
                    ) {
                        Column {
                            Text(
                                text = article.title,
                                modifier = Modifier.padding(16.dp)
                            )
                            AnimatedVisibility(visible = expanded) {
                                Text(
                                    text = article.content,
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

@ExperimentalFoundationApi
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