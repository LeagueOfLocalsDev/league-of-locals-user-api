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
    fun createUserProfile(@RequestBody profileRequest: CreateProfileRequest): ResponseEntity<Map<String, UUID>> {
        val newUserProfile = UserProfile(
            username = profileRequest.username,
            homeCity = profileRequest.homeCity,
            sex = profileRequest.sex
        )
        val savedProfile = userProfileRepository.save(newUserProfile)
        return ResponseEntity.ok(mapOf("id" to savedProfile.userId))
    }

    @GetMapping("/{userId}")
    fun getUserProfile(@PathVariable userId: UUID): ResponseEntity<UserProfile> {
        return userProfileRepository.findById(userId)
            .map { ResponseEntity.ok(it) }
            .orElse(ResponseEntity.notFound().build())
    }

    @DeleteMapping("/{userId}")
    fun deleteUserProfile(@PathVariable userId: UUID): ResponseEntity<Void> {

        return if (userProfileRepository.existsById(userId)) {
            userProfileRepository.deleteById(userId)
            ResponseEntity.noContent().build()
        } else {
            ResponseEntity.notFound().build()
        }
    }
}

data class CreateProfileRequest(val username: String, val homeCity: String, val sex: String)