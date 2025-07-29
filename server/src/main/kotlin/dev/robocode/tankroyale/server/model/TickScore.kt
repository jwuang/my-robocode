package dev.robocode.tankroyale.server.model

/**
 * Tick score record for keeping track of scores at specific ticks for a specific participant.
 */
data class TickScore(
    /** Participant id */
    val participantId: ParticipantId,

    /** Bullet damage score at this tick */
    val bulletDamageScore: Double,

    /** Bullet kill bonus at this tick */
    val bulletKillBonus: Double,

    /** Ram damage score at this tick */
    val ramDamageScore: Double,

    /** Ram kill bonus at this tick */
    val ramKillBonus: Double,

    /** Survival score at this tick */
    val survivalScore: Double,

    /** Last survivor bonus at this tick */
    val lastSurvivorBonus: Double,

    /** The total score at this tick */
    val totalScore: Double,

    /** Rank at this tick */
    val rank: Int,
) {
    constructor(score: Score) : this(
        participantId = score.participantId,
        bulletDamageScore = score.bulletDamageScore,
        bulletKillBonus = score.bulletKillBonus,
        ramDamageScore = score.ramDamageScore,
        ramKillBonus = score.ramKillBonus,
        survivalScore = score.survivalScore,
        lastSurvivorBonus = score.lastSurvivorBonus,
        totalScore = score.totalScore,
        rank = score.rank
    )
}
