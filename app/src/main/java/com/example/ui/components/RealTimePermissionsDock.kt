package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContactPhone
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BrightPink
import com.example.ui.theme.BrightPurple
import com.example.ui.theme.BrightYellow
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.GoldenYellow
import com.example.ui.theme.LavenderContainerBorder
import com.example.ui.theme.SuccessGreen

data class PermissionStatusItem(
  val id: String,
  val title: String,
  val subtitle: String,
  val icon: ImageVector,
  val isGranted: Boolean,
  val accentColor: Color,
  val onClick: () -> Unit
)

@Composable
fun RealTimePermissionsDock(
  hasCamera: Boolean,
  hasLocation: Boolean,
  hasContacts: Boolean,
  hasNotifications: Boolean,
  hasStorage: Boolean,
  onLaunchCamera: () -> Unit,
  onLaunchLocation: () -> Unit,
  onLaunchContacts: () -> Unit,
  onLaunchNotificationTest: () -> Unit,
  onLaunchStoragePicker: () -> Unit,
  modifier: Modifier = Modifier
) {
  val items = listOf(
    PermissionStatusItem(
      id = "perm_camera",
      title = "Camera",
      subtitle = if (hasCamera) "Live visual scan" else "Tap to scan",
      icon = Icons.Filled.CameraAlt,
      isGranted = hasCamera,
      accentColor = BrightPurple,
      onClick = onLaunchCamera
    ),
    PermissionStatusItem(
      id = "perm_location",
      title = "Location",
      subtitle = if (hasLocation) "GPS calibrated" else "Tap to locate",
      icon = Icons.Filled.MyLocation,
      isGranted = hasLocation,
      accentColor = BrightPink,
      onClick = onLaunchLocation
    ),
    PermissionStatusItem(
      id = "perm_contacts",
      title = "Contacts",
      subtitle = if (hasContacts) "Recipient synced" else "Tap to pick",
      icon = Icons.Filled.ContactPhone,
      isGranted = hasContacts,
      accentColor = ElectricViolet,
      onClick = onLaunchContacts
    ),
    PermissionStatusItem(
      id = "perm_notifications",
      title = "Alerts",
      subtitle = if (hasNotifications) "Push active" else "Tap to test",
      icon = Icons.Filled.NotificationsActive,
      isGranted = hasNotifications,
      accentColor = GoldenYellow,
      onClick = onLaunchNotificationTest
    ),
    PermissionStatusItem(
      id = "perm_storage",
      title = "Photos",
      subtitle = if (hasStorage) "Gallery ready" else "Tap to browse",
      icon = Icons.Filled.PhotoLibrary,
      isGranted = hasStorage,
      accentColor = Color(0xFF00B4D8),
      onClick = onLaunchStoragePicker
    )
  )

  Card(
    shape = RoundedCornerShape(20.dp),
    border = androidx.compose.foundation.BorderStroke(1.dp, LavenderContainerBorder),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surface
    ),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp, vertical = 6.dp)
      .testTag("real_time_permissions_dock")
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(8.dp)
              .clip(CircleShape)
              .background(SuccessGreen)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "LIVE DEVICE SENSORS & PERMISSIONS",
            fontSize = 11.sp,
            fontWeight = FontWeight.ExtraBold,
            color = BrightPurple,
            letterSpacing = 0.5.sp
          )
        }

        Surface(
          shape = RoundedCornerShape(8.dp),
          color = BrightPurple.copy(alpha = 0.1f)
        ) {
          Text(
            text = "Real-Time ⚡",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = BrightPurple,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Horizontal list of real-time permission icons
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        items.forEach { item ->
          Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
            border = androidx.compose.foundation.BorderStroke(
              width = if (item.isGranted) 1.5.dp else 1.dp,
              color = if (item.isGranted) item.accentColor.copy(alpha = 0.6f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
            ),
            modifier = Modifier
              .width(108.dp)
              .clip(RoundedCornerShape(16.dp))
              .clickable { item.onClick() }
              .testTag(item.id)
          ) {
            Column(
              horizontalAlignment = Alignment.CenterHorizontally,
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp)
            ) {
              Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(44.dp)
              ) {
                Box(
                  modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(item.accentColor.copy(alpha = 0.15f)),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(
                    imageVector = item.icon,
                    contentDescription = item.title,
                    tint = item.accentColor,
                    modifier = Modifier.size(22.dp)
                  )
                }

                // Status Badge in corner
                Box(
                  modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(14.dp)
                    .clip(CircleShape)
                    .background(if (item.isGranted) SuccessGreen else BrightYellow)
                    .border(1.dp, Color.White, CircleShape),
                  contentAlignment = Alignment.Center
                ) {
                  if (item.isGranted) {
                    Icon(
                      imageVector = Icons.Filled.Check,
                      contentDescription = null,
                      tint = Color.White,
                      modifier = Modifier.size(10.dp)
                    )
                  } else {
                    Box(
                      modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF8A6A00))
                    )
                  }
                }
              }

              Spacer(modifier = Modifier.height(6.dp))

              Text(
                text = item.title,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface
              )

              Text(
                text = item.subtitle,
                fontSize = 9.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
              )
            }
          }
        }
      }
    }
  }
}
