package com.example.vshpauth

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.google.gson.Gson

class HomeActivity : Activity() {

    private lateinit var sharedPref: SharedPreferences
    private lateinit var userPrefs: SharedPreferences

    // Ключи для SharedPreferences
    private val TOKEN_KEY = "auth_token"
    private val USER_DATA_KEY = "full_user_data"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // Инициализация SharedPreferences
        sharedPref = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        userPrefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

        // Получаем данные пользователя
        val user = getCurrentUser()

        if (user == null) {
            showToast("Ошибка загрузки данных пользователя")
            logout()
            return
        }

        // Находим элементы интерфейса
        val userNameText = findViewById<TextView>(R.id.userNameText)
        val userEmailText = findViewById<TextView>(R.id.userEmailText)
        val unreadMessagesText = findViewById<TextView>(R.id.unreadMessagesText)
        val avatarImage = findViewById<ImageView>(R.id.avatarImage)
        val logoutButton = findViewById<Button>(R.id.logoutButton)

        // Устанавливаем данные пользователя
        userNameText.text = user.fullName
        userEmailText.text = user.email
        unreadMessagesText.text = "Непрочитанных сообщений: ${user.unreadMessages}"

        // Загружаем аватар
        user.photo?.avatar?.let { avatarUrl ->
            Glide.with(this)
                .load(avatarUrl)
                .placeholder(R.drawable.ic_profile_placeholder)
                .into(avatarImage)
        }

        // Обработчик выхода
        logoutButton.setOnClickListener {
            logout()
        }
    }

    private fun getCurrentUser(): MainActivity.User? {
        return try {
            // Пытаемся загрузить из JSON
            userPrefs.getString(USER_DATA_KEY, null)?.let {
                Gson().fromJson(it, MainActivity.User::class.java)
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun logout() {
        // Очищаем все данные
        clearUserData()

        // Переходим на экран входа
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }

    private fun clearUserData() {
        // Очищаем оба файла preferences
        sharedPref.edit().clear().apply()
        userPrefs.edit().clear().apply()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}