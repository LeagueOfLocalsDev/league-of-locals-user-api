package com.leagueoflocals.leagueoflocals_user_api.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "user_profiles")
data class UserProfile(

    @Id
    val userId: UUID = UUID.randomUUID(),

    @Column(unique = true, nullable = false)
    val auth0UserId: String,

    @Column(unique = true, nullable = false)
    var username: String,

    var homeCity: String,
    var sex: String,
    var lifetimeRaces: Int = 0
)