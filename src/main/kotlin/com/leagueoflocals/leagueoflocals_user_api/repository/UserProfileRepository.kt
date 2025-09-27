package com.leagueoflocals.leagueoflocals_user_api.repository

import com.leagueoflocals.leagueoflocals_user_api.model.UserProfile
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface UserProfileRepository : JpaRepository<UserProfile, UUID> {

}