package edu.virginia.cs4720.scores.data

import edu.virginia.cs4720.scores.data.local.GameDao
import edu.virginia.cs4720.scores.data.local.GameEntity
import edu.virginia.cs4720.scores.data.remote.NcaaApi
import kotlinx.coroutines.flow.Flow
import kotlin.coroutines.cancellation.CancellationException

class GameRepository(
    private val api: NcaaApi,
    private val dao: GameDao,
) {
    fun observe(division: String, season: Int, week: Int): Flow<List<GameEntity>> =
        dao.observe(division, season, week)

    suspend fun refresh(division: String, season: Int, week: Int): Result<Unit> = try {
        val response = api.scoreboard(division, season, week.toString().padStart(2, '0'))
        dao.upsertAll(response.games.map { it.game.toEntity(division, season, week) })
        Result.success(Unit)
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Result.failure(e)
    }
}