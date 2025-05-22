package com.example.vshpauth

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.vshpauth.ui.theme.VshpAuthTheme
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

class MainActivity : Activity() {

    // Интерфейс для API авторизации
    interface AuthApi {
        @POST("/api/v1/user/session/")
        suspend fun login(@Body request: AuthRequest): AuthResponse
    }

    // Модель запроса
    data class AuthRequest(
        val login: String,
        val password: String
    )

    data class User(
        @SerializedName("id") val id: Int,
        @SerializedName("first-name") val firstName: String?,
        @SerializedName("middle-name") val middleName: String?,
        @SerializedName("last-name") val lastName: String?,
        @SerializedName("email") val email: String,
        @SerializedName("phone") val phone: String?,
        @SerializedName("unread-messages-count") val unreadMessages: Int,
        @SerializedName("photo") val photo: Photo?,
        @SerializedName("consultant-name") val consultantName: String?,
        @SerializedName("consultant-avatar-url") val consultantAvatar: String?
    ) {
        val fullName: String
            get() = listOfNotNull(lastName, firstName, middleName).joinToString(" ")
    }

    data class Photo(
        @SerializedName("original") val original: String?,
        @SerializedName("avatar") val avatar: String?,
        @SerializedName("profile") val profile: String?
    )

    // Модель ответа
    data class AuthResponse(
        val token: String,
        val user: User
    )

    // Клиент Retrofit
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://my.vshp.online")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val authApi = retrofit.create(AuthApi::class.java)

    // SharedPreferences
    private lateinit var sharedPref: SharedPreferences
    private val TOKEN_KEY = "auth_token"
    private val HOME_ACTIVITY = HomeActivity::class.java

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Инициализация SharedPreferences
        sharedPref = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

        // Проверка сохраненного токена
        if (isUserLoggedIn()) {
            navigateToHome()
            return
        }

        setContentView(R.layout.login_page)

        val submitButton: Button = findViewById(R.id.button)
        submitButton.setOnClickListener {
            val loginEditText = findViewById<EditText>(R.id.editTextText)
            val passwordEditText = findViewById<EditText>(R.id.editTextText2)

            val login = loginEditText.text.toString()
            val password = passwordEditText.text.toString()

            if (login.isNotEmpty() && password.isNotEmpty()) {
                performLogin(login, password)
            } else {
                showToast("Введите логин и пароль")
            }
        }

        val registerTextView = findViewById<TextView>(R.id.textView)
        registerTextView.setOnClickListener {
            navigateToRegistration()
        }
    }

    private fun performLogin(login: String, password: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = authApi.login(AuthRequest(login, password))
                withContext(Dispatchers.Main) {
                    saveAuthData(response.token, response.user)
                    showToast("Добро пожаловать, ${response.user.fullName}!")
                    navigateToHome()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showToast("Ошибка авторизации: ${e.localizedMessage}")
                }
            }
        }
    }

    private fun saveAuthData(token: String, user: User) {
        // Сохраняем токен
        sharedPref.edit().putString(TOKEN_KEY, token).apply()

        // Сохраняем данные пользователя
        val userPrefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        with(userPrefs.edit()) {
            // Основные данные
            putInt("user_id", user.id)
            putString("email", user.email)
            putString("first_name", user.firstName)
            putString("last_name", user.lastName)
            putString("phone", user.phone)
            putInt("unread_messages", user.unreadMessages)

            // Данные фото
            user.photo?.let {
                putString("photo_avatar", it.avatar)
                putString("photo_original", it.original)
            }

            // Данные консультанта
            putString("consultant_name", user.consultantName)
            putString("consultant_avatar", user.consultantAvatar)

            // Полный объект как JSON
            putString("full_user_data", Gson().toJson(user))

            apply()
        }
    }

    private fun getCurrentUser(): User? {
        val userPrefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        return try {
            userPrefs.getString("full_user_data", null)?.let {
                Gson().fromJson(it, User::class.java)
            } ?: run {
                User(
                    id = userPrefs.getInt("user_id", 0),
                    firstName = userPrefs.getString("first_name", null),
                    middleName = null,
                    lastName = userPrefs.getString("last_name", null),
                    email = userPrefs.getString("email", "") ?: "",
                    phone = userPrefs.getString("phone", null),
                    unreadMessages = userPrefs.getInt("unread_messages", 0),
                    photo = Photo(
                        original = userPrefs.getString("photo_original", null),
                        avatar = userPrefs.getString("photo_avatar", null),
                        profile = null
                    ),
                    consultantName = userPrefs.getString("consultant_name", null),
                    consultantAvatar = userPrefs.getString("consultant_avatar", null)
                )
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun isUserLoggedIn(): Boolean {
        return sharedPref.getString(TOKEN_KEY, null)?.isNotEmpty() == true
    }

    private fun navigateToHome() {
        startActivity(Intent(this, HOME_ACTIVITY).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        finish()
    }

    private fun navigateToRegistration() {
        startActivity(Intent(this, RegistrationActivity::class.java))
        finish()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    companion object {
        fun getAuthToken(context: Context): String? {
            return context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                .getString("auth_token", null)
        }
    }

    fun onTextViewClick(view: View) {
        if (view is TextView) {
            val intent = Intent(this, RegistrationActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(text = "Hello $name!", modifier = modifier)
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    VshpAuthTheme {
        Greeting("Android")
    }
}