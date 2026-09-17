package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.BrightPink
import com.example.ui.theme.BrightPurple
import com.example.ui.theme.BrightYellow
import com.example.ui.theme.LavenderContainerBorder
import com.example.ui.theme.SuccessGreen

enum class AuthMode {
  SIGN_IN,
  SIGN_UP,
  FORGOT_PASSWORD
}

@Composable
fun SupabaseAuthDialog(
  isOpen: Boolean,
  isLoading: Boolean,
  errorMessage: String?,
  onDismiss: () -> Unit,
  onSignIn: (email: String, pass: String) -> Unit,
  onSignUp: (email: String, pass: String, fullName: String) -> Unit,
  onForgotPassword: (email: String) -> Unit,
  modifier: Modifier = Modifier,
  allowGuestSkip: Boolean = true
) {
  if (!isOpen) return

  var authMode by remember { mutableStateOf(AuthMode.SIGN_IN) }
  var email by remember { mutableStateOf("") }
  var password by remember { mutableStateOf("") }
  var fullName by remember { mutableStateOf("") }
  var passwordVisible by remember { mutableStateOf(false) }

  Dialog(
    onDismissRequest = { if (!isLoading) onDismiss() },
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Card(
      shape = RoundedCornerShape(24.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
      border = androidx.compose.foundation.BorderStroke(1.dp, LavenderContainerBorder),
      modifier = modifier
        .fillMaxWidth(0.92f)
        .padding(16.dp)
        .testTag("supabase_auth_dialog")
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(22.dp)
      ) {
        // Header
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(BrightPurple.copy(alpha = 0.15f)),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Filled.VpnKey,
                contentDescription = null,
                tint = BrightPurple,
                modifier = Modifier.size(20.dp)
              )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text(
                text = when (authMode) {
                  AuthMode.SIGN_IN -> "Welcome Back"
                  AuthMode.SIGN_UP -> "Create Account"
                  AuthMode.FORGOT_PASSWORD -> "Reset Password"
                },
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onSurface
              )
              Text(
                text = "Supabase Auth • pkfbqoqfisxmjkhrnhpp",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }

          IconButton(
            onClick = { if (!isLoading) onDismiss() },
            modifier = Modifier.size(32.dp)
          ) {
            Icon(
              imageVector = Icons.Filled.Close,
              contentDescription = "Close",
              tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Mode tabs
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(modifier = Modifier.fillMaxWidth().padding(4.dp)) {
            listOf(
              AuthMode.SIGN_IN to "Sign In",
              AuthMode.SIGN_UP to "Sign Up"
            ).forEach { (mode, label) ->
              val isSelected = authMode == mode
              Button(
                onClick = { authMode = mode },
                colors = ButtonDefaults.buttonColors(
                  containerColor = if (isSelected) BrightPurple else Color.Transparent,
                  contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                ),
                shape = RoundedCornerShape(8.dp),
                elevation = null,
                modifier = Modifier.weight(1f).height(36.dp)
              ) {
                Text(text = label, fontWeight = FontWeight.Bold, fontSize = 12.sp)
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Error message banner
        AnimatedVisibility(visible = errorMessage != null) {
          errorMessage?.let { msg ->
            Surface(
              shape = RoundedCornerShape(10.dp),
              color = MaterialTheme.colorScheme.errorContainer,
              modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
            ) {
              Text(
                text = msg,
                color = MaterialTheme.colorScheme.onErrorContainer,
                fontSize = 12.sp,
                modifier = Modifier.padding(10.dp)
              )
            }
          }
        }

        // Full Name Field (only for sign up)
        if (authMode == AuthMode.SIGN_UP) {
          OutlinedTextField(
            value = fullName,
            onValueChange = { fullName = it },
            label = { Text("Full Name") },
            leadingIcon = {
              Icon(Icons.Filled.Person, contentDescription = null, tint = BrightPurple)
            },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = BrightPurple,
              unfocusedBorderColor = MaterialTheme.colorScheme.outline
            ),
            modifier = Modifier.fillMaxWidth().testTag("auth_name_field")
          )
          Spacer(modifier = Modifier.height(12.dp))
        }

        // Email Field
        OutlinedTextField(
          value = email,
          onValueChange = { email = it },
          label = { Text("Email Address") },
          leadingIcon = {
            Icon(Icons.Filled.Email, contentDescription = null, tint = BrightPurple)
          },
          keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Email,
            imeAction = if (authMode == AuthMode.FORGOT_PASSWORD) ImeAction.Done else ImeAction.Next
          ),
          singleLine = true,
          shape = RoundedCornerShape(14.dp),
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = BrightPurple,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline
          ),
          modifier = Modifier.fillMaxWidth().testTag("auth_email_field")
        )

        // Password Field
        if (authMode != AuthMode.FORGOT_PASSWORD) {
          Spacer(modifier = Modifier.height(12.dp))
          OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            leadingIcon = {
              Icon(Icons.Filled.Lock, contentDescription = null, tint = BrightPurple)
            },
            trailingIcon = {
              IconButton(onClick = { passwordVisible = !passwordVisible }) {
                Icon(
                  imageVector = if (passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
              keyboardType = KeyboardType.Password,
              imeAction = ImeAction.Done
            ),
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = BrightPurple,
              unfocusedBorderColor = MaterialTheme.colorScheme.outline
            ),
            modifier = Modifier.fillMaxWidth().testTag("auth_password_field")
          )
        }

        // Forgot password / switch action
        if (authMode == AuthMode.SIGN_IN) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
          ) {
            TextButton(onClick = { authMode = AuthMode.FORGOT_PASSWORD }) {
              Text(
                text = "Forgot Password?",
                fontSize = 12.sp,
                color = BrightPurple,
                fontWeight = FontWeight.SemiBold
              )
            }
          }
        } else if (authMode == AuthMode.FORGOT_PASSWORD) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
          ) {
            TextButton(onClick = { authMode = AuthMode.SIGN_IN }) {
              Text(
                text = "Back to Sign In",
                fontSize = 12.sp,
                color = BrightPurple,
                fontWeight = FontWeight.SemiBold
              )
            }
          }
        } else {
          Spacer(modifier = Modifier.height(10.dp))
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Action Button
        Button(
          onClick = {
            when (authMode) {
              AuthMode.SIGN_IN -> onSignIn(email, password)
              AuthMode.SIGN_UP -> onSignUp(email, password, fullName)
              AuthMode.FORGOT_PASSWORD -> onForgotPassword(email)
            }
          },
          enabled = !isLoading && email.isNotBlank() && (authMode == AuthMode.FORGOT_PASSWORD || password.isNotBlank()),
          shape = RoundedCornerShape(14.dp),
          colors = ButtonDefaults.buttonColors(
            containerColor = BrightPurple,
            contentColor = Color.White
          ),
          modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .testTag("auth_submit_button")
        ) {
          if (isLoading) {
            CircularProgressIndicator(
              color = Color.White,
              modifier = Modifier.size(22.dp),
              strokeWidth = 2.5.dp
            )
          } else {
            Text(
              text = when (authMode) {
                AuthMode.SIGN_IN -> "Sign In with Supabase"
                AuthMode.SIGN_UP -> "Create Supabase Account"
                AuthMode.FORGOT_PASSWORD -> "Send Reset Link"
              },
              fontWeight = FontWeight.Bold,
              fontSize = 14.sp
            )
          }
        }

        if (allowGuestSkip) {
          Spacer(modifier = Modifier.height(8.dp))
          TextButton(
            onClick = { if (!isLoading) onDismiss() },
            modifier = Modifier.fillMaxWidth()
          ) {
            Text(
              text = "Continue as Guest for now",
              fontSize = 13.sp,
              fontWeight = FontWeight.Medium,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Live Cloud Security Footnote
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.Center,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(
            imageVector = Icons.Filled.CheckCircle,
            contentDescription = null,
            tint = SuccessGreen,
            modifier = Modifier.size(13.dp)
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = "Connected to Supabase PostgreSQL & Auth API",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }
    }
  }
}
