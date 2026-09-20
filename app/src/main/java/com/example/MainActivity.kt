package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.example.data.db.AppDatabase
import com.example.data.repository.TrainingRepository
import com.example.ui.MainAppScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.StealthBlack
import com.example.util.SoundHapticManager

class MainActivity : ComponentActivity() {
    private lateinit var soundHapticManager: SoundHapticManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = AppDatabase.getInstance(applicationContext)
        val repository = TrainingRepository(
            sessionDao = database.trainingSessionDao(),
            sensitivityDao = database.sensitivityPresetDao(),
            hudDao = database.hudLayoutDao()
        )
        soundHapticManager = SoundHapticManager(applicationContext)

        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = StealthBlack
                ) {
                    MainAppScreen(
                        repository = repository,
                        soundHapticManager = soundHapticManager
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::soundHapticManager.isInitialized) {
            soundHapticManager.release()
        }
    }
}

@androidx.compose.runtime.Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    androidx.compose.material3.Text(
        text = "ZERO SHOT $name",
        color = com.example.ui.theme.CyberCyan,
        modifier = modifier
    )
}

