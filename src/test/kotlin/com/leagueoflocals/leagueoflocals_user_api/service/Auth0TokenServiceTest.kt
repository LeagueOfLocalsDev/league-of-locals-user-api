package com.leagueoflocals.leagueoflocals_user_api.service

import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.web.reactive.function.client.WebClientResponseException

@ExtendWith(MockitoExtension::class)
class Auth0TokenServiceTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var auth0TokenService: Auth0TokenService

    @BeforeEach
    fun setUp() {
        mockWebServer = MockWebServer()
        mockWebServer.start()

        val mockServerUrl = mockWebServer.url("/").toString()
        val domain = mockServerUrl.removePrefix("http://").removeSuffix("/")

        auth0TokenService = Auth0TokenService(
            domain = domain,
            clientId = "test-client-id",
            clientSecret = "test-client-secret",
            scheme = "http",
        )
    }

    @AfterEach
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun `getManagementApiToken should return a valid token when auth0 call is successful`() {
        val mockJsonResponse = """
            {
              "access_token": "mock-jwt-token-string",
              "expires_in": 86400,
              "token_type": "Bearer"
            }
        """.trimIndent()

        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(mockJsonResponse)
        )

        val token = auth0TokenService.getManagementApiToken()

        assertThat(token).isEqualTo("mock-jwt-token-string")

        val recordedRequest = mockWebServer.takeRequest()

        assertThat(recordedRequest.path).isEqualTo("/oauth/token")
        assertThat(recordedRequest.method).isEqualTo("POST")
    }

    @Test
    fun `getManagementApiToken should throw an exception when auth0 call fails`() {
        mockWebServer.enqueue(
            MockResponse().setResponseCode(500)
        )

        assertThrows<WebClientResponseException> {
            auth0TokenService.getManagementApiToken()
        }
    }
}