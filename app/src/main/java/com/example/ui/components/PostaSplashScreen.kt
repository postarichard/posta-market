package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.BrightPink
import com.example.ui.theme.BrightPurple
import com.example.ui.theme.BrightYellow
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.GoldenYellow
import com.example.ui.theme.SuccessGreen
import kotlinx.coroutines.delay

@Composable
fun PostaSplashScreen(
  onSplashFinished: () -> Unit,
  modifier: Modifier = Modifier
) {
  var visible by remember { mutableStateOf(false) }

  val infiniteTransition = rememberInfiniteTransition(label = "pulse")
  val pulseScale by infiniteTransition.animateFloat(
    initialValue = 0.95f,
    targetValue = 1.05f,
    animationSpec = infiniteRepeatable(
      animation = tween(1200, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "logo_pulse"
  )

  val glowAlpha by infiniteTransition.animateFloat(
    initialValue = 0.4f,
    targetValue = 0.85f,
    animationSpec = infiniteRepeatable(
      animation = tween(1000, easing = LinearEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "glow_pulse"
  )

  LaunchedEffect(Unit) {
    visible = true
    delay(1800) // Splash duration
    onSplashFinished()
  }

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(
        Brush.verticalGradient(
          colors = listOf(
            Color(0xFF0F0B1E),
            Color(0xFF1E1338),
            Color(0xFF28114B)
          )
        )
      )
      .testTag("posta_splash_screen"),
    contentAlignment = Alignment.Center
  ) {
    // Ambient glowing neon orbs behind the brand card
    Box(
      modifier = Modifier
        .size(280.dp)
        .scale(pulseScale)
        .alpha(glowAlpha)
        .clip(CircleShape)
        .background(
          Brush.radialGradient(
            colors = listOf(
              BrightPurple.copy(alpha = 0.6f),
              ElectricViolet.copy(alpha = 0.25f),
              Color.Transparent
            )
          )
        )
    )

    AnimatedVisibility(
      visible = visible,
      enter = fadeIn(tween(600)) + scaleIn(tween(600, easing = FastOutSlowInEasing)),
      exit = fadeOut(tween(400))
    ) {
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.padding(24.dp)
      ) {
        // App Icon with glow and shadow
        Box(
          contentAlignment = Alignment.Center,
          modifier = Modifier.scale(pulseScale)
        ) {
          // Circular glow rim
          Surface(
            shape = RoundedCornerShape(28.dp),
            color = BrightYellow.copy(alpha = 0.3f),
            modifier = Modifier.size(108.dp)
          ) {}

          Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color.Transparent,
            shadowElevation = 16.dp,
            modifier = Modifier
              .size(100.dp)
              .clip(RoundedCornerShape(24.dp))
          ) {
            Image(
              painter = painterResource(id = R.drawable.posta_app_icon),
              contentDescription = "Posta Market",
              modifier = Modifier.fillMaxSize()
            )
          }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Brand Name
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = "Posta",
            fontSize = 36.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White,
            letterSpacing = (-1).sp
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = "Market",
            fontSize = 36.sp,
            fontWeight = FontWeight.ExtraBold,
            color = BrightYellow,
            letterSpacing = (-1).sp
          )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Tagline
        Text(
          text = "Next-Gen 2026 Commerce & Rapid Logistics",
          fontSize = 13.sp,
          fontWeight = FontWeight.Medium,
          color = Color(0xFFD4C8FA),
          letterSpacing = 0.5.sp
        )

        Spacer(modifier = Modifier.height(36.dp))

        // Supabase Cloud indicator pill
        Surface(
          shape = RoundedCornerShape(20.dp),
          color = Color.White.copy(alpha = 0.10f)
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
          ) {
            Icon(
              imageVector = Icons.Filled.Security,
              contentDescription = null,
              tint = SuccessGreen,
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Secured with Supabase Cloud Auth",
              fontSize = 11.sp,
              fontWeight = FontWeight.SemiBold,
              color = Color.White
            )
          }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Loading spinner
        CircularProgressIndicator(
          color = BrightYellow,
          strokeWidth = 2.5.dp,
          modifier = Modifier.size(24.dp)
        )
      }
    }

    // Bottom subtle build version indicator
    Text(
      text = "Posta Market v2.6.0 • Kampala Core Engine",
      fontSize = 11.sp,
      color = Color.White.copy(alpha = 0.5f),
      modifier = Modifier
        .align(Alignment.BottomCenter)
        .padding(bottom = 24.dp)
    )
  }
}
