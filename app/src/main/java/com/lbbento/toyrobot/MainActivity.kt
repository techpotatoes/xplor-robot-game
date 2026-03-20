package com.lbbento.toyrobot

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.lbbento.toyrobot.game.ui.GameScreen
import com.lbbento.toyrobot.ui.theme.ToyRobotTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ToyRobotTheme {
                GameScreen()
            }
        }
    }
}
