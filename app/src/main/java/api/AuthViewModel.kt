package api

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class AuthViewModel: ViewModel() {
    private val _loginResult = MutableLiveData<Result<AuthResponse>>()
    val loginResult: LiveData<Result<AuthResponse>> = _loginResult

    fun login(login: String, password: String) {
        viewModelScope.launch {
            try {
                val response = ApiClient.authService.login(
                    AuthRequest(login, password)
                )
                _loginResult.value = Result.success(response)
            } catch (e: Exception) {
                _loginResult.value = Result.failure(e)
            }
        }

    }
}