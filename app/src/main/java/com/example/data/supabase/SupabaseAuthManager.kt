package com.example.data.supabase

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class SupabaseUser(
  val id: String,
  val email: String,
  val fullName: String = "",
  val accessToken: String = "",
  val refreshToken: String = ""
)

sealed class AuthResult {
  data class Success(val user: SupabaseUser, val message: String) : AuthResult()
  data class Error(val errorMessage: String) : AuthResult()
}

class SupabaseAuthManager private constructor(context: Context) {

  private val prefs: SharedPreferences =
    context.getSharedPreferences("posta_supabase_auth_prefs", Context.MODE_PRIVATE)

  private val client = OkHttpClient.Builder()
    .connectTimeout(15, TimeUnit.SECONDS)
    .readTimeout(20, TimeUnit.SECONDS)
    .build()

  val projectRef = "pkfbqoqfisxmjkhrnhpp"
  val supabaseUrl = "https://pkfbqoqfisxmjkhrnhpp.supabase.co"
  val anonKey =
    "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InBrZmJxb3FmaXN4bWpraHJuaHBwIiwicm9sZSI6ImFub24iLCJpYXQiOjE3ODk0MzAxNTksImV4cCI6MjEwNTAwNjE1OX0.D5HTr_vXEI6BXSYXU3se6Dn2dowcpvi7DIlkaMuz5wc"

  companion object {
    @Volatile
    private var INSTANCE: SupabaseAuthManager? = null

    fun getInstance(context: Context): SupabaseAuthManager {
      return INSTANCE ?: synchronized(this) {
        INSTANCE ?: SupabaseAuthManager(context.applicationContext).also { INSTANCE = it }
      }
    }
  }

  fun getCachedUser(): SupabaseUser? {
    val id = prefs.getString("user_id", null) ?: return null
    val email = prefs.getString("user_email", "") ?: ""
    val name = prefs.getString("user_name", "") ?: ""
    val token = prefs.getString("access_token", "") ?: ""
    val refresh = prefs.getString("refresh_token", "") ?: ""
    return SupabaseUser(id, email, name, token, refresh)
  }

  private fun saveUserSession(user: SupabaseUser) {
    prefs.edit()
      .putString("user_id", user.id)
      .putString("user_email", user.email)
      .putString("user_name", user.fullName)
      .putString("access_token", user.accessToken)
      .putString("refresh_token", user.refreshToken)
      .apply()
  }

  fun clearSession() {
    prefs.edit().clear().apply()
  }

  suspend fun signUpWithEmail(email: String, pass: String, fullName: String): AuthResult =
    withContext(Dispatchers.IO) {
      try {
        val endpoint = "$supabaseUrl/auth/v1/signup"
        val payload = JSONObject().apply {
          put("email", email.trim())
          put("password", pass)
          put("data", JSONObject().apply {
            put("full_name", fullName.trim())
          })
        }

        val request = Request.Builder()
          .url(endpoint)
          .addHeader("apikey", anonKey)
          .addHeader("Authorization", "Bearer $anonKey")
          .addHeader("Content-Type", "application/json")
          .post(payload.toString().toRequestBody("application/json; charset=utf-8".toMediaType()))
          .build()

        val response = client.newCall(request).execute()
        val responseBody = response.body?.string() ?: ""

        if (!response.isSuccessful) {
          val errorMsg = parseErrorMessage(responseBody) ?: "Sign up failed (${response.code})"
          return@withContext AuthResult.Error(errorMsg)
        }

        val json = JSONObject(responseBody)
        val userObj = json.optJSONObject("user") ?: json
        val userId = userObj.optString("id", "")
        val userEmail = userObj.optString("email", email)
        val token = json.optString("access_token", "")
        val refreshToken = json.optString("refresh_token", "")

        val user = SupabaseUser(
          id = userId,
          email = userEmail,
          fullName = fullName,
          accessToken = token,
          refreshToken = refreshToken
        )

        if (token.isNotEmpty()) {
          saveUserSession(user)
          // Also create/update profile record in Supabase REST database
          createOrUpdateProfile(user, fullName)
        }

        AuthResult.Success(
          user = user,
          message = if (token.isNotEmpty()) "Signed up & logged in successfully!" else "Registration successful! Please check your email to confirm."
        )
      } catch (e: Exception) {
        Log.e("SupabaseAuth", "Sign up error", e)
        AuthResult.Error(e.localizedMessage ?: "Network error during sign up")
      }
    }

  suspend fun signInWithEmail(email: String, pass: String): AuthResult =
    withContext(Dispatchers.IO) {
      try {
        val endpoint = "$supabaseUrl/auth/v1/token?grant_type=password"
        val payload = JSONObject().apply {
          put("email", email.trim())
          put("password", pass)
        }

        val request = Request.Builder()
          .url(endpoint)
          .addHeader("apikey", anonKey)
          .addHeader("Authorization", "Bearer $anonKey")
          .addHeader("Content-Type", "application/json")
          .post(payload.toString().toRequestBody("application/json; charset=utf-8".toMediaType()))
          .build()

        val response = client.newCall(request).execute()
        val responseBody = response.body?.string() ?: ""

        if (!response.isSuccessful) {
          val errorMsg = parseErrorMessage(responseBody) ?: "Invalid email or password"
          return@withContext AuthResult.Error(errorMsg)
        }

        val json = JSONObject(responseBody)
        val token = json.getString("access_token")
        val refreshToken = json.optString("refresh_token", "")
        val userObj = json.getJSONObject("user")
        val userId = userObj.getString("id")
        val userEmail = userObj.optString("email", email)
        val userMeta = userObj.optJSONObject("user_metadata")
        val fullName = userMeta?.optString("full_name") ?: userEmail.substringBefore("@")

        val user = SupabaseUser(
          id = userId,
          email = userEmail,
          fullName = fullName,
          accessToken = token,
          refreshToken = refreshToken
        )

        saveUserSession(user)
        AuthResult.Success(user = user, message = "Welcome back, $fullName!")
      } catch (e: Exception) {
        Log.e("SupabaseAuth", "Sign in error", e)
        AuthResult.Error(e.localizedMessage ?: "Failed to sign in")
      }
    }

  suspend fun signOut(): Boolean = withContext(Dispatchers.IO) {
    try {
      val cached = getCachedUser()
      if (cached != null && cached.accessToken.isNotEmpty()) {
        val endpoint = "$supabaseUrl/auth/v1/logout"
        val request = Request.Builder()
          .url(endpoint)
          .addHeader("apikey", anonKey)
          .addHeader("Authorization", "Bearer ${cached.accessToken}")
          .post("{}".toRequestBody("application/json".toMediaType()))
          .build()
        client.newCall(request).execute()
      }
    } catch (e: Exception) {
      Log.w("SupabaseAuth", "Sign out remote call notice: ${e.message}")
    } finally {
      clearSession()
    }
    true
  }

  suspend fun resetPasswordForEmail(email: String): AuthResult = withContext(Dispatchers.IO) {
    try {
      val endpoint = "$supabaseUrl/auth/v1/recover"
      val payload = JSONObject().apply {
        put("email", email.trim())
      }

      val request = Request.Builder()
        .url(endpoint)
        .addHeader("apikey", anonKey)
        .addHeader("Authorization", "Bearer $anonKey")
        .addHeader("Content-Type", "application/json")
        .post(payload.toString().toRequestBody("application/json".toMediaType()))
        .build()

      val response = client.newCall(request).execute()
      if (response.isSuccessful) {
        AuthResult.Success(
          user = SupabaseUser("", email),
          message = "Password reset link sent to $email"
        )
      } else {
        val err = parseErrorMessage(response.body?.string() ?: "") ?: "Password recovery failed"
        AuthResult.Error(err)
      }
    } catch (e: Exception) {
      AuthResult.Error(e.localizedMessage ?: "Failed to send reset email")
    }
  }

  private suspend fun createOrUpdateProfile(user: SupabaseUser, fullName: String) =
    withContext(Dispatchers.IO) {
      try {
        val endpoint = "$supabaseUrl/rest/v1/profiles"
        val payload = JSONObject().apply {
          put("id", user.id)
          put("email", user.email)
          put("full_name", fullName)
          put("user_tier", "VIP Member")
          put("wallet_balance", 250.00)
          put("reward_points", 1500)
        }

        val request = Request.Builder()
          .url(endpoint)
          .addHeader("apikey", anonKey)
          .addHeader("Authorization", "Bearer ${user.accessToken}")
          .addHeader("Content-Type", "application/json")
          .addHeader("Prefer", "resolution=merge-duplicates")
          .post(payload.toString().toRequestBody("application/json".toMediaType()))
          .build()

        client.newCall(request).execute()
      } catch (e: Exception) {
        Log.w("SupabaseAuth", "Profile upsert notice: ${e.message}")
      }
    }

  private fun parseErrorMessage(jsonStr: String): String? {
    return try {
      val obj = JSONObject(jsonStr)
      obj.optString("msg", null)
        ?: obj.optString("message", null)
        ?: obj.optString("error_description", null)
    } catch (e: Exception) {
      null
    }
  }
}
