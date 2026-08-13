package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.ui.main.KeyboardAppScreen
import com.example.ui.theme.MyApplicationTheme

/**
 * Main Activity entry point for Teclado Blanco application.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                KeyboardAppScreen()
            }
        }
    }
}
