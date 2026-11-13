package ar.jornadas.data

import androidx.room.*
import retrofit2.http.GET
import kotlinx.coroutines.flow.Flow

data class FichaRemota(val id: String, val title: String, val track: String, val day: Int,
    val time: String, val room: String, val speaker: String, val summary: String)

interface ProgramaApi {
    @GET("programa") suspend fun consultar(): List<FichaRemota>
}

@Entity(tableName = "charlas")
data class Charla(@PrimaryKey val id: String, val titulo: String, val tema: String, val dia: Int,
    val hora: String, val sala: String, val ponente: String, val resumen: String,
    val favorita: Boolean = false)

@Dao
interface CarteleraDao {
    @Query("SELECT * FROM charlas ORDER BY dia, hora, sala")
    fun observar(): Flow<List<Charla>>

    @Query("SELECT * FROM charlas")
    suspend fun obtenerTodas(): List<Charla>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardar(fichas: List<Charla>)

    @Query("DELETE FROM charlas WHERE id NOT IN (:ids)")
    suspend fun eliminarAusentes(ids: List<String>)

    @Query("DELETE FROM charlas")
    suspend fun vaciar()

    @Query("UPDATE charlas SET favorita = NOT favorita WHERE id = :id")
    suspend fun alternar(id: String)

    @Transaction
    suspend fun sincronizarCatalogo(remotas: List<FichaRemota>) {
        val locales = obtenerTodas().associateBy { it.id }
        val idsRemotos = remotas.map { it.id }

        if (idsRemotos.isEmpty()) {
            vaciar()
        } else {
            eliminarAusentes(idsRemotos)
        }

        val actualizadas = remotas.map { remota ->
            val estabaMarcada = locales[remota.id]?.favorita ?: false
            Charla(
                id = remota.id,
                titulo = remota.title,
                tema = remota.track,
                dia = remota.day,
                hora = remota.time,
                sala = remota.room,
                ponente = remota.speaker,
                resumen = remota.summary,
                favorita = estabaMarcada
            )
        }
        guardar(actualizadas)
    }
}

@Database(entities = [Charla::class], version = 1, exportSchema = false)
abstract class Archivo : RoomDatabase() {
    abstract fun cartelera(): CarteleraDao
}
