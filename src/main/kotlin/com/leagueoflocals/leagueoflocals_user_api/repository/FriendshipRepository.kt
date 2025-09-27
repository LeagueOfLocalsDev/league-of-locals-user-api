package com.leagueoflocals.leagueoflocals_user_api.repository

import com.leagueoflocals.leagueoflocals_user_api.model.Friendship
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface FriendshipRepository : JpaRepository<Friendship, UUID> {
    // You can add custom queries for finding friends, pending requests, etc.
}