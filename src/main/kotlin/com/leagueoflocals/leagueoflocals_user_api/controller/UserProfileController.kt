package com.leagueoflocals.leagueoflocals_user_api.controller

import com.leagueoflocals.leagueoflocals_user_api.model.UserProfile
import com.leagueoflocals.leagueoflocals_user_api.repository.UserProfileRepository
import com.leagueoflocals.leagueoflocals_user_api.service.UserService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/user-profile-api")
class UserProfileController(
    private val userProfileRepository: UserProfileRepository,
    private val userService: UserService,
) {

    @PostMapping("/profiles")
    fun createUserProfile(@RequestBody profileRequest: CreateProfileRequest): ResponseEntity<Map<String, UUID>> {

        val savedProfile = userService.createUser(profileRequest)
        return ResponseEntity.ok(mapOf("id" to savedProfile.userId))
    }

    @GetMapping("/profiles")
    fun listUserProfiles(): ResponseEntity<List<UserProfile>> {
        val all = userProfileRepository.findAll()
        return ResponseEntity.ok(all)
    }

    @GetMapping("/profiles/{userId}")
    fun getUserProfile(@PathVariable userId: UUID): ResponseEntity<UserProfile> {
        return userProfileRepository.findById(userId)
            .map { ResponseEntity.ok(it) }
            .orElse(ResponseEntity.notFound().build())
    }

    @DeleteMapping("/profiles/{userId}")
    fun deleteUserProfile(@PathVariable userId: UUID): ResponseEntity<Void> {

        return if (userProfileRepository.existsById(userId)) {
            userService.deleteUser(userId)
            ResponseEntity.noContent().build()
        } else {
            ResponseEntity.notFound().build()
        }
    }
}

data class CreateProfileRequest(
    val username: String,
    val homeCity: String,
    val sex: String,
    val email: String,
    val password: String,
)