package com.leagueoflocals.leagueoflocals_user_api.controller

import com.leagueoflocals.leagueoflocals_user_api.model.UserProfile
import com.leagueoflocals.leagueoflocals_user_api.repository.UserProfileRepository
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/v1/profiles")
class UserProfileController(
    private val userProfileRepository: UserProfileRepository
) {

    @PostMapping
    fun createUserProfile(@RequestBody profileRequest: CreateProfileRequest): UserProfile {
        val newUserProfile = UserProfile(
            username = profileRequest.username,
            homeCity = profileRequest.homeCity,
            sex = profileRequest.sex
        )
        return userProfileRepository.save(newUserProfile)
    }

    @GetMapping("/{userId}")
    fun getUserProfile(@PathVariable userId: UUID): ResponseEntity<UserProfile> {
        return userProfileRepository.findById(userId)
            .map { ResponseEntity.ok(it) }
            .orElse(ResponseEntity.notFound().build())
    }
}

data class CreateProfileRequest(val username: String, val homeCity: String, val sex: String)