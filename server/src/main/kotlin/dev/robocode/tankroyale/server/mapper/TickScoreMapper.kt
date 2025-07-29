package dev.robocode.tankroyale.server.mapper

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import dev.robocode.tankroyale.server.model.TickScore

object TickScoreMapper {
    fun mapToJson(tickScores: List<TickScore>): JsonArray {
        val jsonArray = JsonArray()
        tickScores.forEach { tickScore ->
            val jsonObject = JsonObject()
            jsonObject.addProperty("participantId", tickScore.participantId.botId.value)
            tickScore.participantId.teamId?.let { teamId ->
                jsonObject.addProperty("teamId", teamId.id)
            }
            jsonObject.addProperty("bulletDamageScore", tickScore.bulletDamageScore)
            jsonObject.addProperty("bulletKillBonus", tickScore.bulletKillBonus)
            jsonObject.addProperty("ramDamageScore", tickScore.ramDamageScore)
            jsonObject.addProperty("ramKillBonus", tickScore.ramKillBonus)
            jsonObject.addProperty("survivalScore", tickScore.survivalScore)
            jsonObject.addProperty("lastSurvivorBonus", tickScore.lastSurvivorBonus)
            jsonObject.addProperty("totalScore", tickScore.totalScore)
            jsonObject.addProperty("rank", tickScore.rank)
            jsonArray.add(jsonObject)
        }
        return jsonArray
    }
}
