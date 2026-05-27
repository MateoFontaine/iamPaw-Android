package com.example.iampaw

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.example.iampaw.components.NavigationStack
import com.example.iampaw.ui.theme.IamPawTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            IamPawTheme {
                val navController = rememberNavController()

                NavigationStack(navController = navController)
            }
        }
    }
}