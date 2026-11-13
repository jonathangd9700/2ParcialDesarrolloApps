package ar.jornadas.data

import android.content.SharedPreferences

class AgendaRepository(
    private val api: ProgramaApi,
    private val dao: CarteleraDao,
    private val prefs: SharedPreferences
) {
    val programa = dao.observar()

    var tieneEdicionPrevia: Boolean
        get() = prefs.getBoolean("edicion_previa", false)
        private set(value) = prefs.edit().putBoolean("edicion_previa", value).apply()

    suspend fun sincronizar() {
        val remotas = api.consultar()
        dao.sincronizarCatalogo(remotas)
        tieneEdicionPrevia = true
    }

    suspend fun cambiarFavorita(id: String) = dao.alternar(id)
}
