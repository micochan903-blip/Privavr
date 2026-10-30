package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.vr.VrScreen
import com.example.viewmodel.VrViewModel

class MainActivity : ComponentActivity() {

    private val vrViewModel: VrViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                VrScreen(
                    viewModel = vrViewModel,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
