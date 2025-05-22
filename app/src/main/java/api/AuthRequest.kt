package api

data class AuthRequest(
    val login: String,
    val password: String
)
