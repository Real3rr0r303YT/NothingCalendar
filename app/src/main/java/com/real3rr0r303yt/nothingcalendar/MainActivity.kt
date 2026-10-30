package com.real3rr0r303yt.nothingcalendar

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.real3rr0r303yt.nothingcalendar.ui.NothingCalendarApp
import com.real3rr0r303yt.nothingcalendar.ui.theme.NothingTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
        )
        super.onCreate(savedInstanceState)
        setContent {
            NothingTheme {
                NothingCalendarApp()
            }
        }
    }
}
