package com.leagueoflocals.leagueoflocals_user_api.service

import com.leagueoflocals.leagueoflocals_user_api.client.Auth0ManagementClient
import com.leagueoflocals.leagueoflocals_user_api.client.Auth0User
import com.leagueoflocals.leagueoflocals_user_api.controller.CreateProfileRequest
import com.leagueoflocals.leagueoflocals_user_api.model.UserProfile
import com.leagueoflocals.leagueoflocals_user_api.repository.UserProfileRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import org.mockito.kotlin.verify
import java.util.*

@ExtendWith(MockitoExtension::class)
class UserServiceTest {

    @Mock
    private lateinit var userProfileRepository: UserProfileRepository

    @Mock
    private lateinit var auth0ManagementClient: Auth0ManagementClient

    @InjectMocks
    private lateinit var userService: UserService

    @Test
    fun `when createUser is called with valid data, it should register with auth0 and save to the database`() {

        val request = CreateProfileRequest(
            username = "testuser",
            homeCity = "Chicago",
            sex = "Male",
            email = "test@example.com",
            password = "password123"
        )

        val mockAuth0User = Auth0User(
            user_id = "auth0|12345",
            email = request.email,
            nickname = request.username
        )

        val savedUserProfile = UserProfile(
            userId = UUID.randomUUID(),
            auth0UserId = "auth0|12345",
            username = request.username,
            homeCity = request.homeCity,
            sex = request.sex
        )

        given(auth0ManagementClient.createUser(any(), any(), any())).willReturn(mockAuth0User)
        given(userProfileRepository.save(any<UserProfile>())).willReturn(savedUserProfile)

        val result = userService.createUser(request)

        verify(auth0ManagementClient).createUser(request.email, request.password, request.username)
        verify(userProfileRepository).save(any<UserProfile>())

        assertThat(result).isNotNull
        assertThat(result.username).isEqualTo(request.username)
        assertThat(result.auth0UserId).isEqualTo(mockAuth0User.user_id)
    }

    @Test
    fun `when createUser is called and auth0 fails, it should not save to the database`() {

        val request = CreateProfileRequest(
            username = "testuser",
            homeCity = "Chicago",
            sex = "Male",
            email = "test@example.com",
            password = "password123"
        )

        given(auth0ManagementClient.createUser(any(), any(), any())).willThrow(RuntimeException("Auth0 error"))

        try {
            userService.createUser(request)
        } catch (e: Exception) {
            // Expected exception
        }

        verify(auth0ManagementClient).createUser(request.email, request.password, request.username)
        verify(userProfileRepository, org.mockito.kotlin.never()).save(any<UserProfile>())
    }
}