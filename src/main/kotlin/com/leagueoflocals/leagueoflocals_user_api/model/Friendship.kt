package com.leagueoflocals.leagueoflocals_user_api.model

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "friendships")
data class Friendship(

    @Id
    val friendshipId: UUID = UUID.randomUUID(),

    val userOneId: UUID,
    val userTwoId: UUID,

    var status: String,
    val requestedAt: Instant = Instant.now()
)