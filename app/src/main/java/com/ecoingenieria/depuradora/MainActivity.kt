package com.ecoingenieria.depuradora

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
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
                Surface(modifier = Modifier.fillMaxSize()) {
                    EcoNavGraph(container = container)
                }
            }
        }
    }
}
