package ar.jornadas.ui

import android.app.Application
import androidx.lifecycle.*
import ar.jornadas.Servicios
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.IOException

class ProgramaModel(
    app: Application,
    private val savedStateHandle: SavedStateHandle
) : AndroidViewModel(app) {
    private val servicios = Servicios(app)
    val mesa = servicios.mesa
    private val repo = servicios.agenda
    val charlas = repo.programa.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val dia: StateFlow<Int> = savedStateHandle.getStateFlow("dia", 0)
    val tema: StateFlow<String> = savedStateHandle.getStateFlow("tema", "Todos")
    val detalle: StateFlow<String?> = savedStateHandle.getStateFlow("detalle", null)
    val seccion: StateFlow<String> = savedStateHandle.getStateFlow("seccion", "Programa")

    val aviso = MutableStateFlow("")
    val cargando = MutableStateFlow(false)

    val tieneEdicionPrevia: Boolean
        get() = repo.tieneEdicionPrevia

    companion object {
        private var sesionIniciada = false
    }

    init {
        if (!sesionIniciada) {
            sesionIniciada = true
            recargar()
        }
    }

    fun setDia(nuevoDia: Int) {
        savedStateHandle["dia"] = nuevoDia
    }

    fun setTema(nuevoTema: String) {
        savedStateHandle["tema"] = nuevoTema
    }

    fun setDetalle(nuevoDetalle: String?) {
        savedStateHandle["detalle"] = nuevoDetalle
    }

    fun setSeccion(nuevaSeccion: String) {
        savedStateHandle["seccion"] = nuevaSeccion
    }

    fun recargar() {
        viewModelScope.launch {
            cargando.value = true
            aviso.value = ""
            try {
                repo.sincronizar()
            } catch (e: IOException) {
                if (repo.tieneEdicionPrevia) {
                    aviso.value = "Se están usando datos guardados"
                } else {
                    aviso.value = "No se pudo consultar el programa"
                }
            } finally {
                cargando.value = false
            }
        }
    }

    fun marcar(id: String) {
        viewModelScope.launch { repo.cambiarFavorita(id) }
    }
}
