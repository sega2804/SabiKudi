package com.crypticsamsara.sabikudi.di

import com.crypticsamsara.sabikudi.data.repository.AuthRepository
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.sabikudi.shared.dto.AuthResponse
import com.sabikudi.shared.dto.MeResponse
import com.sabikudi.shared.dto.OtpRequest
import com.sabikudi.shared.dto.OtpRequestResponse
import com.sabikudi.shared.dto.OtpVerifyRequest
import com.sabikudi.shared.dto.RecoveryEmailRequest
import com.sabikudi.shared.dto.RefreshRequest
import com.sabikudi.shared.dto.TokenPair
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import okhttp3.Authenticator
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import javax.inject.Named
import javax.inject.Singleton

interface SabikudiAuthApi {
    @POST("auth/otp/request")
    suspend fun requestOtp(@Body request: OtpRequest): OtpRequestResponse

    @POST("auth/otp/verify")
    suspend fun verifyOtp(@Body request: OtpVerifyRequest): AuthResponse

    @POST("auth/email/verify")
    suspend fun verifyEmailCode(@Body request: OtpVerifyRequest): AuthResponse

    @POST("auth/refresh")
    suspend fun refreshTokens(@Body request: RefreshRequest): TokenPair

    @POST("auth/logout")
    suspend fun logout(): retrofit2.Response<Unit>

    @GET("me")
    suspend fun getMe(): MeResponse

    @PUT("me/recovery-email")
    suspend fun setRecoveryEmail(@Body request: RecoveryEmailRequest): OtpRequestResponse
}

@Module
@InstallIn(SingletonComponent::class)
object AuthNetworkModule {

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    @Provides
    @Singleton
    @Named("AuthBaseUrl")
    fun provideBaseUrl(): String = "http://10.0.2.2:8080/"

    @Provides
    @Singleton
    fun provideOkHttpClient(
        authRepository: AuthRepository,
        @Named("AuthBaseUrl") baseUrl: String,
        json: Json,
    ): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val authInterceptor = Interceptor { chain ->
            val requestBuilder = chain.request().newBuilder()
            authRepository.getAccessToken()?.let { token ->
                requestBuilder.header("Authorization", "Bearer $token")
            }
            chain.proceed(requestBuilder.build())
        }

        val tokenAuthenticator = object : Authenticator {
            override fun authenticate(route: Route?, response: Response): Request? {
                if (response.code != 401) return null
                if (response.request.header("Authorization") == null) return null

                // Perform single-flight token refresh
                val refreshResult = runBlocking {
                    val rawRetrofit = Retrofit.Builder()
                        .baseUrl(baseUrl)
                        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
                        .build()
                    val authApi = rawRetrofit.create(SabikudiAuthApi::class.java)

                    authRepository.refreshTokenSingleFlight { req ->
                        authApi.refreshTokens(req)
                    }
                }

                return refreshResult.getOrNull()?.let { newPair ->
                    response.request.newBuilder()
                        .header("Authorization", "Bearer ${newPair.accessToken}")
                        .build()
                }
            }
        }

        return OkHttpClient.Builder()
            .addInterceptor(logging)
            .addInterceptor(authInterceptor)
            .authenticator(tokenAuthenticator)
            .build()
    }

    @Provides
    @Singleton
    fun provideSabikudiAuthApi(
        okHttpClient: OkHttpClient,
        @Named("AuthBaseUrl") baseUrl: String,
        json: Json,
    ): SabikudiAuthApi {
        val contentType = "application/json".toMediaType()
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
            .create(SabikudiAuthApi::class.java)
    }
}
