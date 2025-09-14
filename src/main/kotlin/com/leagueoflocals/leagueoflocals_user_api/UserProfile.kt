package com.leagueoflocals.leagueoflocals_user_api

/**
 * A simple data class to represent a user profile.
 * Spring Boot will automatically convert this to a JSON response.
 */
data class UserProfile(
    val userId: String,
    val username: String,
    val homeCity: String,
    val lifetimeRaces: Int
)