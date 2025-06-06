package com.example.vshpauth

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.CountDownTimer
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.drawerlayout.widget.DrawerLayout
import com.bumptech.glide.Glide
import com.example.vshpauth.HomeActivity.SessionResponse
import com.example.vshpauth.MainActivity.AuthApi
import com.example.vshpauth.MainActivity.AuthRequest
import com.google.android.material.navigation.NavigationView
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

class HomeActivity : Activity() {

    // В начало класса добавляем интерфейс
    // Определение интерфейса API
    interface OtpApi {
        @FormUrlEncoded
        @POST("/api/v1/user/otp")
        suspend fun requestOtp(
            @Header("Authorization") authHeader: String,
            @Field("lifetime") lifetime: String
        ): SessionResponse
    }

    // Добавляем модель ответа
    data class SessionResponse(
        @SerializedName("code") val code: Int,
        @SerializedName("message") val message: String,
        @SerializedName("otp") val otp: String
    )

    private lateinit var sharedPref: SharedPreferences
    private lateinit var userPrefs: SharedPreferences
    private lateinit var drawerLayout: DrawerLayout

    // Ключи для SharedPreferences
    private val TOKEN_KEY = "auth_token"
    private val USER_DATA_KEY = "full_user_data"

    // Константы для OTP
    private val OTP_PREFS = "otp_prefs"
    private val OTP_KEY = "otp_code"
    private var countdownTimer: CountDownTimer? = null
    private var countdownDuration: Long = 300 // 300 секунд по умолчанию

    // Сервисы Retrofit для API
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://my.vshp.online")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val otpApi = retrofit.create(OtpApi::class.java)

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // Инициализация SharedPreferences
        sharedPref = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        userPrefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        drawerLayout = findViewById(R.id.drawer_layout)

        val token = sharedPref.getString("auth_token", "")

        // Формируем заголовки запроса
        val headers = mutableMapOf(
            "Authorization" to "Bearer $token",
            "Content-Type" to "multipart/form-data"
        ).toString()
        // Формируем body запроса
        val body = "lifetime"


        val sendButton = findViewById<Button>(R.id.button_send_otp)
        val tvOtp = findViewById<TextView>(R.id.tv_otp)
        val otpTimer = findViewById<TextView>(R.id.timer_textView)
        val otpCoolDown = findViewById<ProgressBar>(R.id.progressBar)
        sendButton.setOnClickListener {
            val lifetime = 300L

            if (token.isNullOrEmpty()) {
                showToast("Токен авторизации отсутствует!")
                return@setOnClickListener
            }

            CoroutineScope(Dispatchers.Main).launch {
                val otpResponse = requestOtpCode(token, lifetime)
                if (otpResponse != null) {
                    tvOtp.setText("${otpResponse.code}")

                    object : CountDownTimer(60000, 1000) { // 60 секунд, тикает каждые 1 сек
                        override fun onTick(millisUntilFinished: Long) {
                            val secondsLeft = millisUntilFinished / 1000
                            otpTimer.text = String.format("Осталось: %02d сек", secondsLeft)
                            sendButton.visibility = View.GONE
                            sendButton.isEnabled = false

                            otpCoolDown.visibility = View.VISIBLE
                        }

                        override fun onFinish() {
                            tvOtp.text = "Код истёк"
                            otpTimer.setText("Время до конца действия кода")
                            otpCoolDown.visibility = View.GONE

                            sendButton.visibility = View.VISIBLE
                            sendButton.isEnabled = true
                        }
                    }.start()
                } else {
                    showToast("Ошибка сети")
                }
            }
        }

        // Получаем данные пользователя
        val user = getCurrentUser()

        if (user == null) {
            showToast("Ошибка загрузки данных пользователя")
            logout()
            return
        }

        // Настройка кнопки меню
        findViewById<ImageButton>(R.id.menuButton).setOnClickListener {
            drawerLayout.openDrawer(Gravity.LEFT)
        }

        // Инициализация элементов в NavigationView
        val navView = findViewById<NavigationView>(R.id.nav_view)
        val navHeader = navView.getHeaderView(0)

        // Элементы из шапки NavigationView
        val navAvatar = navHeader.findViewById<ImageView>(R.id.nav_avatarImage)
        val navUserName = navHeader.findViewById<TextView>(R.id.nav_userNameText)
        val navUserEmail = navHeader.findViewById<TextView>(R.id.nav_userEmailText)
//        val navUnreadMessages = navHeader.findViewById<TextView>(R.id.nav_unreadMessagesText)
        val navLogoutButton = navHeader.findViewById<Button>(R.id.nav_logoutButton)

        if (navUserName == null || navUserEmail == null) {
            showToast("Ошибка: элементы шапки не найдены")
        } else {
            navUserName.text = user.fullName
            navUserEmail.text = user.email
//            navUnreadMessages.text = "Непрочитанных сообщений: ${user.unreadMessages}"
        }

        // Загрузка аватара
        user.photo?.avatar?.let { avatarUrl ->
            Glide.with(this)
                .load(avatarUrl)
                .placeholder(R.drawable.ic_profile_placeholder)
                .into(navAvatar)
        }

        // Обработчик выхода
        navLogoutButton.setOnClickListener {
            logout()
        }

        // Обработка выбора пунктов меню
        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_profile -> {
                    showToast("Профиль")
                    true
                }
                R.id.nav_settings -> {
                    showToast("Настройки")
                    true
                }
                else -> false
            }
        }
    }

    // Подход с suspend функциями
    suspend fun requestOtpCode(token: String, lifetime: Long): SessionResponse? {
        return withContext(Dispatchers.IO) {
            try {
                otpApi.requestOtp(
                    authHeader = "Bearer $token",
                    lifetime = lifetime.toString()
                )
            } catch (e: Exception) {
                null
            }
        }
    }

    private fun getCurrentUser(): MainActivity.User? {
        return try {
            userPrefs.getString(USER_DATA_KEY, null)?.let {
                Gson().fromJson(it, MainActivity.User::class.java)
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun logout() {
        clearUserData()
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }

    private fun clearUserData() {
        sharedPref.edit().clear().apply()
        userPrefs.edit().clear().apply()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}