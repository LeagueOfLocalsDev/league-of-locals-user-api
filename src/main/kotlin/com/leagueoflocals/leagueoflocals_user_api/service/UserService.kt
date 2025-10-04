package com.leagueoflocals.leagueoflocals_user_api.service

import com.leagueoflocals.leagueoflocals_user_api.repository.UserProfileRepository
import com.leagueoflocals.leagueoflocals_user_api.client.Auth0ManagementClient
import com.leagueoflocals.leagueoflocals_user_api.controller.CreateProfileRequest
import com.leagueoflocals.leagueoflocals_user_api.model.UserProfile
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserService(
    private val userProfileRepository: UserProfileRepository,
    private val auth0ManagementClient: Auth0ManagementClient
) {
    @Transactional
    fun createUser(request: CreateProfileRequest): UserProfile {
        val auth0User = auth0ManagementClient.createUser(request.email, request.password, request.username)

        val newUserProfile = UserProfile(
            auth0UserId = auth0User.userId,
            username = request.username,
            homeCity = request.homeCity,
            sex = request.sex
        )

        return userProfileRepository.save(newUserProfile)
    }
}