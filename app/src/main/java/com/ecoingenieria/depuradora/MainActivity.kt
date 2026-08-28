package com.ecoingenieria.depuradora

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.ecoingenieria.depuradora.ui.navigation.EcoNavGraph
import com.ecoingenieria.depuradora.ui.theme.EcoDepuradoraTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val container = (application as EcoDepuradoraApp).container
        setContent {
            EcoDepuradoraTheme {
                // safeDrawingPadding() evita que cualquier contenido (botones,
                // texto, controles) quede pegado a la barra de estado o a la
                // zona de gestos/navegación del sistema, ahora que la app usa
                // enableEdgeToEdge().
                Surface(modifier = Modifier.fillMaxSize().safeDrawingPadding()) {
                    EcoNavGraph(container = container)
                }
            }
        }
    }
}
