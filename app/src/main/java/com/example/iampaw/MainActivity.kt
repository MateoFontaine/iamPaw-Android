package com.example.iampaw

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.iampaw.ui.theme.IamPawTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Esto hace que la app ocupe toda la pantalla (hasta la barra de estado arriba)
        enableEdgeToEdge()

        setContent {
            IamPawTheme {
                // Acá adentro vamos a inyectar el NavigationStack en el próximo paso
                // tal cual como lo hizo el profe Gladkoff.
            }
        }
    }
}