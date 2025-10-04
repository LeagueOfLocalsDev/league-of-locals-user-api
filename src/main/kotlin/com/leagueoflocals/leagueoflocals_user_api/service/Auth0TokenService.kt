package com.leagueoflocals.leagueoflocals_user_api.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.cache.annotation.Cacheable
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono

@Service
class Auth0TokenService(
    @Value("\${auth0.domain}") private val domain: String,
    @Value("\${auth0.client-id}") private val clientId: String,
    @Value("\${auth0.client-secret}") private val clientSecret: String
) {
    private val webClient = WebClient.builder().build()

    @Cacheable(cacheNames = ["auth0-token"], unless = "#result.isBlank()")
    fun getManagementApiToken(): String {
        println("==> FETCHING NEW AUTH0 TOKEN... <==") // We'll add this log to prove it's working

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
}

private data class Auth0TokenRequest(val client_id: String, val client_secret: String, val audience: String, val grant_type: String)
private data class Auth0TokenResponse(val access_token: String)