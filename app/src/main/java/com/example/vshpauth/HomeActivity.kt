package com.example.vshpauth

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.drawerlayout.widget.DrawerLayout
import com.bumptech.glide.Glide
import com.google.android.material.navigation.NavigationView
import com.google.gson.Gson

class HomeActivity : Activity() {

    private lateinit var sharedPref: SharedPreferences
    private lateinit var userPrefs: SharedPreferences
    private lateinit var drawerLayout: DrawerLayout

    // Ключи для SharedPreferences
    private val TOKEN_KEY = "auth_token"
    private val USER_DATA_KEY = "full_user_data"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // Инициализация SharedPreferences
        sharedPref = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        userPrefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        drawerLayout = findViewById(R.id.drawer_layout)

        // Получаем данные пользователя
        val user = getCurrentUser()

        if (user == null) {
            showToast("Ошибка загрузки данных пользователя")
            logout()
            return
        }

        // Настройка кнопки меню
        findViewById<ImageButton>(R.id.menuButton).setOnClickListener {
            drawerLayout.openDrawer(Gravity.START)
        }

        // Инициализация элементов в NavigationView
        val navView = findViewById<NavigationView>(R.id.nav_view)
        val navHeader = navView.getHeaderView(0)

        // Элементы из шапки NavigationView
        val navAvatar = navHeader.findViewById<ImageView>(R.id.nav_avatarImage)
        val navUserName = navHeader.findViewById<TextView>(R.id.nav_userNameText)
        val navUserEmail = navHeader.findViewById<TextView>(R.id.nav_userEmailText)
        val navUnreadMessages = navHeader.findViewById<TextView>(R.id.nav_unreadMessagesText)
        val navLogoutButton = navHeader.findViewById<Button>(R.id.nav_logoutButton)

        // Установка данных пользователя
        navUserName.text = user.fullName
        navUserEmail.text = user.email
        navUnreadMessages.text = "Непрочитанных сообщений: ${user.unreadMessages}"

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