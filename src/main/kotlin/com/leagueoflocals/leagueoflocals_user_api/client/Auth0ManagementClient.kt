package com.leagueoflocals.leagueoflocals_user_api.client

import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono

@Component
class Auth0ManagementClient(
    @Value("\${auth0.domain}") private val domain: String,
    @Value("\${auth0.client-id}") private val clientId: String,
    @Value("\${auth0.client-secret}") private val clientSecret: String
) {
    private val webClient = WebClient.builder().build()

    private fun getManagementApiToken(): String {
        val tokenUrl = "https://$domain/oauth/token"
        val request = Auth0TokenRequest(
            client_id = clientId,
            client_secret = clientSecret,
            audience = "https://$domain/api/v2/",
            grant_type = "client_credentials"
        )

        val response = webClient.post()
            .uri(tokenUrl)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .retrieve()
            .bodyToMono<Auth0TokenResponse>()
            .block()

        return response?.access_token ?: throw IllegalStateException("Could not retrieve Auth0 token")
    }

    fun createUser(email: String, password: String, username: String): Auth0User {
        val token = getManagementApiToken()
        val usersUrl = "https://$domain/api/v2/users"

        val request = Auth0CreateUserRequest(
            email = email,
            password = password,
            connection = "Username-Password-Authentication",
            nickname = username
        )

        return webClient.post()
            .uri(usersUrl)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .retrieve()
            .bodyToMono<Auth0User>()
            .block() ?: throw IllegalStateException("Failed to create user in Auth0")
    }
}

private data class Auth0TokenRequest(val client_id: String, val client_secret: String, val audience: String, val grant_type: String)
private data class Auth0TokenResponse(val access_token: String)
private data class Auth0CreateUserRequest(val email: String, val password: String, val connection: String, val nickname: String)
data class Auth0User(val userId: String, val email: String, val nickname: String)