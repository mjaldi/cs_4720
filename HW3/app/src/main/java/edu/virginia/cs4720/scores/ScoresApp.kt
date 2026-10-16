package edu.virginia.cs4720.scores

import android.app.Application
import android.content.Context
import androidx.room.Room
import edu.virginia.cs4720.scores.data.GameRepository
import edu.virginia.cs4720.scores.data.local.AppDatabase
import edu.virginia.cs4720.scores.data.remote.NcaaApi
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

class AppContainer(context: Context) {
    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    private val api: NcaaApi = Retrofit.Builder()
        .baseUrl("https://ncaa-api.henrygd.me/")   // must end with "/"
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()
        .create(NcaaApi::class.java)

    private val db = Room.databaseBuilder(
        context.applicationContext,
        AppDatabase::class.java,
        "scores.db",
    ).build()

    val repository = GameRepository(api, db.gameDao())
}

class ScoresApp : Application() {
    val container by lazy { AppContainer(this) }
}