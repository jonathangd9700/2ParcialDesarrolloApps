package ar.jornadas

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import ar.jornadas.ui.*

class MainActivity : ComponentActivity() {
    private val model: ProgramaModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null && intent.hasExtra("servicio_disponible")) {
            val disponible = intent.getBooleanExtra("servicio_disponible", true)
            getSharedPreferences("mesa", Context.MODE_PRIVATE)
                .edit().putBoolean("disponible", disponible).apply()
            intent.removeExtra("servicio_disponible")
        }
        setContent { Portada(model) }
    }
}
