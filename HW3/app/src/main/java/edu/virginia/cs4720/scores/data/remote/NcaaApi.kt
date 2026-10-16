package edu.virginia.cs4720.scores.data.remote

import retrofit2.http.GET
import retrofit2.http.Path

interface NcaaApi {
    @GET("scoreboard/football/{division}/{year}/{week}")
    suspend fun scoreboard(
        @Path("division") division: String,
        @Path("year") year: Int,
        @Path("week") week: String,
    ): ScoreboardResponse
}