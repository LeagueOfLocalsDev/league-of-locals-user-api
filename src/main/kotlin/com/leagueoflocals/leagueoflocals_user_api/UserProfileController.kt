package com.leagueoflocals.leagueoflocals_user_api

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/profiles")
class UserProfileController {

    /**
     * A dummy endpoint that returns a hardcoded user profile.
     * This is perfect for testing the CI/CD pipeline without needing a database.
     */
    @GetMapping("/me")
    fun getMyProfile(): UserProfile {
        return UserProfile(
            userId = "yfifhuf70hfwj9a8b7c",
            username = "andrewb23",
            homeCity = "Chicago",
            lifetimeRaces = 42
        )
    }
}