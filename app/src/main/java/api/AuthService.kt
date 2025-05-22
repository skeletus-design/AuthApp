package api

import retrofit2.http.Body
import retrofit2.http.POST

interface AuthService {
    @POST("api/v1/user/session")
    suspend fun login(@Body request: AuthRequest): AuthResponse
}