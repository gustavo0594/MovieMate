package com.gquesada.moviemate

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.gquesada.moviemate.navigation.AdaptiveAppScaffold
import com.gquesada.moviemate.ui.theme.MovieMateTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MovieMateTheme {
                AdaptiveAppScaffold()
            }
        }
    }
}
