package com.nem.icons

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.nem.icons.ui.MainViewModel
import com.nem.icons.ui.screens.MainNavigation
import com.nem.icons.ui.theme.NemIconsTheme

class MainActivity : ComponentActivity() {

    private val viewModel by lazy {
        ViewModelProvider(this, MainViewModelFactory(this))[MainViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NemIconsTheme {
                MainNavigation(viewModel = viewModel)
            }
        }
    }
}

class MainViewModelFactory(private val activity: ComponentActivity) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            return MainViewModel(activity.applicationContext) as T
        }
        throw IllegalArgumentException("Unknown ViewModel: $modelClass")
    }
}
