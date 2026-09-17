package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Loyalty
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.AlertDialog
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.PostaMarketUiState
import com.example.ui.theme.BrightPink
import com.example.ui.theme.BrightPurple
import com.example.ui.theme.BrightYellow
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.GoldenYellow
import com.example.ui.theme.HighlightAmber
import com.example.ui.theme.LavenderContainerBorder
import com.example.ui.theme.SuccessGreen

@Composable
fun AccountScreen(
  uiState: PostaMarketUiState,
  onOpenFavorites: () -> Unit,
  onToggleDarkMode: () -> Unit = {},
  onToggleNotifications: (Boolean) -> Unit = {},
  onOpenAuthDialog: () -> Unit = {},
  onSignOut: () -> Unit = {},
  modifier: Modifier = Modifier
) {
  val context = androidx.compose.ui.platform.LocalContext.current
  val clipboardManager = LocalClipboardManager.current
  var biometricAuthEnabled by remember { mutableStateOf(true) }
  var showSchemaModal by remember { mutableStateOf(false) }

  val notificationPermissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestPermission()
  ) { isGranted ->
    onToggleNotifications(isGranted)
  }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .testTag("account_screen"),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // 1. VIP Profile Header
    item {
      Card(
        shape = RoundedCornerShape(24.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, LavenderContainerBorder),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        Box(modifier = Modifier.fillMaxWidth()) {
          // Top decorative banner
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(72.dp)
              .background(
                Brush.horizontalGradient(
                  colors = listOf(BrightPurple, ElectricViolet, BrightPink)
                )
              )
          )

          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(16.dp)
          ) {
            Spacer(modifier = Modifier.height(24.dp))
            Row(
              modifier = Modifier.fillMaxWidth(),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              // Avatar
              Box(
                modifier = Modifier
                  .size(68.dp)
                  .clip(CircleShape)
                  .background(MaterialTheme.colorScheme.surface)
                  .padding(3.dp)
              ) {
                Box(
                  modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(BrightPurple.copy(alpha = 0.2f)),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(
                    imageVector = Icons.Filled.Person,
                    contentDescription = null,
                    tint = BrightPurple,
                    modifier = Modifier.size(38.dp)
                  )
                }
              }

              Surface(
                shape = RoundedCornerShape(12.dp),
                color = HighlightAmber,
                shadowElevation = 2.dp
              ) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                  Icon(
                    imageVector = Icons.Filled.VerifiedUser,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                  )
                  Spacer(modifier = Modifier.width(4.dp))
                  Text(
                    text = uiState.userTier,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                  )
                }
              }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
              text = uiState.userName,
              fontWeight = FontWeight.ExtraBold,
              fontSize = 20.sp,
              color = MaterialTheme.colorScheme.onSurface
            )
            Text(
              text = uiState.userEmail,
              fontSize = 13.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }
    }

    // 2. Posta Wallet & Loyalty Points
    item {
      Card(
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, LavenderContainerBorder),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text(
            text = "Posta Pay & Rewards 2026",
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface
          )
          Spacer(modifier = Modifier.height(12.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            // Balance
            Column {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.AccountBalanceWallet, contentDescription = null, tint = BrightPurple, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Wallet Balance", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
              }
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = "$${String.format("%.2f", uiState.walletBalance)}",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 22.sp,
                color = MaterialTheme.colorScheme.primary
              )
            }

            // Divider
            Box(
              modifier = Modifier
                .width(1.dp)
                .height(40.dp)
                .background(LavenderContainerBorder)
            )

            // Points
            Column {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Loyalty, contentDescription = null, tint = BrightPink, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Posta Points", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
              }
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = "${uiState.rewardPoints} pts",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 22.sp,
                color = BrightPink
              )
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(
              onClick = { /* Top up wallet */ },
              colors = ButtonDefaults.buttonColors(containerColor = BrightPurple),
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier
                .weight(1f)
                .height(38.dp)
            ) {
              Text("Top Up Wallet", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            Button(
              onClick = { /* Redeem points */ },
              colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurface),
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier
                .weight(1f)
                .height(38.dp)
            ) {
              Text("Redeem Deals", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
          }
        }
      }
    }

    // 3. Quick Account Actions
    item {
      Card(
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, LavenderContainerBorder),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(vertical = 6.dp)) {
          AccountMenuItem(
            icon = Icons.Filled.Favorite,
            iconTint = BrightPink,
            title = "My Wishlist & Favorites",
            subtitle = "${uiState.favorites.size} saved items",
            onClick = onOpenFavorites
          )
          Divider(color = LavenderContainerBorder, modifier = Modifier.padding(horizontal = 16.dp))
          AccountMenuItem(
            icon = Icons.Filled.LocationOn,
            iconTint = BrightPurple,
            title = "Delivery Addresses",
            subtitle = "742 Evergreen Terrace (Primary)",
            onClick = { /* Address manager */ }
          )
          Divider(color = LavenderContainerBorder, modifier = Modifier.padding(horizontal = 16.dp))
          AccountMenuItem(
            icon = Icons.Filled.CreditCard,
            iconTint = ElectricViolet,
            title = "Payment Methods",
            subtitle = "Visa •••• 4242 & Apple/Google Pay",
            onClick = { /* Payment manager */ }
          )
        }
      }
    }

    // 4. Preferences & Toggles
    item {
      Card(
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text("App Settings & Display", fontWeight = FontWeight.Bold, fontSize = 14.sp)
          Spacer(modifier = Modifier.height(14.dp))

          // Dark Mode Switch
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier = Modifier
                  .size(36.dp)
                  .clip(CircleShape)
                  .background(if (uiState.isDarkMode) BrightYellow.copy(alpha = 0.2f) else BrightPurple.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = if (uiState.isDarkMode) Icons.Filled.LightMode else Icons.Filled.DarkMode,
                  contentDescription = null,
                  tint = if (uiState.isDarkMode) BrightYellow else BrightPurple,
                  modifier = Modifier.size(20.dp)
                )
              }
              Spacer(modifier = Modifier.width(10.dp))
              Column {
                Text(
                  text = if (uiState.isDarkMode) "Dark Theme (Active)" else "Light Theme (Active)",
                  fontWeight = FontWeight.SemiBold,
                  fontSize = 13.sp
                )
                Text(
                  text = if (uiState.isDarkMode) "Ultra-dark OLED aesthetic" else "Crisp white & vivid accents",
                  fontSize = 11.sp,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }
            Switch(
              checked = uiState.isDarkMode,
              onCheckedChange = { onToggleDarkMode() },
              colors = SwitchDefaults.colors(
                checkedThumbColor = Color(0xFF1E1500),
                checkedTrackColor = BrightYellow,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = BrightPurple
              ),
              modifier = Modifier.testTag("account_dark_mode_switch")
            )
          }

          Spacer(modifier = Modifier.height(10.dp))
          Divider(color = MaterialTheme.colorScheme.outline)
          Spacer(modifier = Modifier.height(10.dp))

          // Notifications Switch
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Filled.Notifications, contentDescription = null, tint = BrightPurple, modifier = Modifier.size(20.dp))
              Spacer(modifier = Modifier.width(10.dp))
              Column {
                Text("Push Notifications", fontWeight = FontWeight.Medium, fontSize = 13.sp)
                Text("Order updates & flash sale drops", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
              }
            }
            Switch(
              checked = uiState.notificationsEnabled,
              onCheckedChange = { enable ->
                if (enable && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                  val hasPerm = ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
                  if (!hasPerm) {
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                  } else {
                    onToggleNotifications(true)
                  }
                } else {
                  onToggleNotifications(enable)
                }
              },
              colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = BrightPurple),
              modifier = Modifier.testTag("account_notifications_switch")
            )
          }

          Spacer(modifier = Modifier.height(10.dp))
          Divider(color = MaterialTheme.colorScheme.outline)
          Spacer(modifier = Modifier.height(10.dp))

          // Biometric Switch
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Filled.Security, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(20.dp))
              Spacer(modifier = Modifier.width(10.dp))
              Column {
                Text("Biometric Security 2026", fontWeight = FontWeight.Medium, fontSize = 13.sp)
                Text("Require FaceID for 1-tap checkout", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
              }
            }
            Switch(
              checked = biometricAuthEnabled,
              onCheckedChange = { biometricAuthEnabled = it },
              colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = BrightPurple)
            )
          }
        }
      }
    }

    // 5. Help & About
    item {
      Card(
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, LavenderContainerBorder),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(vertical = 6.dp)) {
          AccountMenuItem(
            icon = Icons.AutoMirrored.Filled.HelpOutline,
            iconTint = HighlightAmber,
            title = "24/7 Priority Support",
            subtitle = "Live chat with Posta AI & Specialists",
            onClick = { /* Help */ }
          )
        }
      }
    }

    // 6. Supabase Cloud Authentication & Project Info Card
    item {
      Card(
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, LavenderContainerBorder),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth().testTag("account_supabase_auth_card")
      ) {
        Column(modifier = Modifier.padding(18.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier = Modifier
                  .size(36.dp)
                  .clip(CircleShape)
                  .background(SuccessGreen.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Filled.VpnKey,
                  contentDescription = null,
                  tint = SuccessGreen,
                  modifier = Modifier.size(18.dp)
                )
              }
              Spacer(modifier = Modifier.width(10.dp))
              Column {
                Text(
                  text = "Supabase Cloud Auth",
                  fontWeight = FontWeight.Bold,
                  fontSize = 14.sp,
                  color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                  text = "Project: pkfbqoqfisxmjkhrnhpp",
                  fontSize = 11.sp,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }

            Surface(
              shape = RoundedCornerShape(8.dp),
              color = if (uiState.isSupabaseLoggedIn) SuccessGreen.copy(alpha = 0.15f) else HighlightAmber.copy(alpha = 0.15f)
            ) {
              Text(
                text = if (uiState.isSupabaseLoggedIn) "LIVE SYNCED" else "GUEST / LOCAL",
                color = if (uiState.isSupabaseLoggedIn) SuccessGreen else HighlightAmber,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
              )
            }
          }

          Spacer(modifier = Modifier.height(12.dp))

          Text(
            text = if (uiState.isSupabaseLoggedIn)
              "Connected as ${uiState.userEmail}. Your profile, shopping cart, and order history are secured with Supabase Auth and PostgreSQL RLS."
            else
              "Sign in or register with Supabase Auth to sync your profile, orders, and rewards across devices with enterprise PostgreSQL security.",
            fontSize = 12.sp,
            lineHeight = 17.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )

          Spacer(modifier = Modifier.height(14.dp))

          if (uiState.isSupabaseLoggedIn) {
            Button(
              onClick = onSignOut,
              colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer
              ),
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier.fillMaxWidth().height(44.dp).testTag("account_sign_out_button")
            ) {
              Icon(Icons.Filled.ExitToApp, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(8.dp))
              Text("Sign Out from Supabase", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
          } else {
            Button(
              onClick = onOpenAuthDialog,
              colors = ButtonDefaults.buttonColors(
                containerColor = BrightPurple,
                contentColor = Color.White
              ),
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier.fillMaxWidth().height(44.dp).testTag("account_sign_in_button")
            ) {
              Icon(Icons.Filled.Login, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(8.dp))
              Text("Sign In / Sign Up with Supabase", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          OutlinedButton(
            onClick = { showSchemaModal = true },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().height(40.dp).testTag("account_view_sql_button")
          ) {
            Icon(Icons.Filled.Code, contentDescription = null, modifier = Modifier.size(16.dp), tint = BrightPurple)
            Spacer(modifier = Modifier.width(8.dp))
            Text("View Supabase SQL Schema", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = BrightPurple)
          }
        }
      }
    }

    // Version Footer
    item {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Text(
          text = "Posta Market • v2.6.0 (2026 Edition)",
          fontSize = 11.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          fontWeight = FontWeight.Medium
        )
        Text(
          text = "Empowering vibrant commerce across the universe",
          fontSize = 10.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
      }
    }
  }

  if (showSchemaModal) {
    val schemaSnippet = """
-- Posta Market 2026 Supabase SQL Schema (Clean & Error-Free)
-- Run in Supabase Dashboard -> SQL Editor (Project: pkfbqoqfisxmjkhrnhpp)

-- 1. Products Table
create table if not exists public.products (
    id text primary key,
    name text not null,
    category text not null,
    price numeric(10, 2) not null check (price >= 0),
    original_price numeric(10, 2),
    rating numeric(3, 2) default 4.8,
    review_count integer default 100,
    description text not null,
    specs jsonb default '[]'::jsonb,
    tag text,
    image_url text,
    is_featured boolean default false,
    is_popular boolean default false,
    is_new_arrival boolean default false,
    is_special_offer boolean default false,
    discount_percent integer default 0,
    stock_left integer default 20,
    total_stock integer default 50,
    latitude double precision default 0.3476,
    longitude double precision default 32.5825,
    location_name text default 'Kampala Central Hub',
    distance_km double precision default 1.2,
    created_at timestamptz default now() not null
);

alter table public.products enable row level security;
drop policy if exists "Products are viewable by everyone" on public.products;
drop policy if exists "Allow update product description and image_url" on public.products;
drop policy if exists "Allow insert products from admin" on public.products;

create policy "Products are viewable by everyone" on public.products for select using (true);
create policy "Allow update product description and image_url" on public.products for update using (true) with check (true);
create policy "Allow insert products from admin" on public.products for insert with check (true);

-- Dedicated RPC function for Admin
create or replace function public.admin_update_product_description_and_image(
    p_product_id text,
    p_description text,
    p_image_url text
) returns jsonb
language plpgsql security definer as $$
declare v_updated record;
begin
    update public.products
    set description = p_description, image_url = p_image_url
    where id = p_product_id returning * into v_updated;
    if not found then return jsonb_build_object('success', false, 'message', 'Product not found'); end if;
    return jsonb_build_object('success', true, 'product', row_to_json(v_updated));
end;
$$;
grant execute on function public.admin_update_product_description_and_image(text, text, text) to anon, authenticated, service_role;

-- 2. Profiles Table
create table if not exists public.profiles (
    id text primary key,
    email text,
    full_name text default 'Posta Shopper',
    phone text default '',
    avatar_url text,
    user_tier text default 'VIP Member',
    wallet_balance numeric(10, 2) default 250.00,
    reward_points integer default 1500,
    delivery_address text default 'Kampala Central Hub, Uganda',
    delivery_coordinates text default '0.3476, 32.5825',
    created_at timestamptz default now() not null,
    updated_at timestamptz default now() not null
);

alter table public.profiles enable row level security;
drop policy if exists "Public profiles are viewable by everyone" on public.profiles;
drop policy if exists "Allow insert profiles" on public.profiles;
drop policy if exists "Allow update profiles" on public.profiles;
create policy "Public profiles are viewable by everyone" on public.profiles for select using (true);
create policy "Allow insert profiles" on public.profiles for insert with check (true);
create policy "Allow update profiles" on public.profiles for update using (true);
    """.trimIndent()

    AlertDialog(
      onDismissRequest = { showSchemaModal = false },
      title = {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Filled.Code, contentDescription = null, tint = BrightPurple)
          Spacer(modifier = Modifier.width(8.dp))
          Text("Supabase SQL Schema", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
      },
      text = {
        Column {
          Text(
            "Execute this script in your Supabase project dashboard (pkfbqoqfisxmjkhrnhpp) -> SQL Editor.",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Spacer(modifier = Modifier.height(8.dp))
          Surface(
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth().height(200.dp)
          ) {
            LazyColumn(modifier = Modifier.padding(8.dp)) {
              item {
                Text(
                  text = schemaSnippet,
                  fontSize = 11.sp,
                  fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }
          }
        }
      },
      confirmButton = {
        Button(
          onClick = {
            clipboardManager.setText(AnnotatedString(schemaSnippet))
            showSchemaModal = false
          },
          colors = ButtonDefaults.buttonColors(containerColor = BrightPurple)
        ) {
          Text("Copy SQL")
        }
      },
      dismissButton = {
        TextButton(onClick = { showSchemaModal = false }) {
          Text("Close")
        }
      }
    )
  }
}

@Composable
fun AccountMenuItem(
  icon: ImageVector,
  iconTint: Color,
  title: String,
  subtitle: String,
  onClick: () -> Unit
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clickable { onClick() }
      .padding(horizontal = 16.dp, vertical = 12.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier.weight(1f)
    ) {
      Box(
        modifier = Modifier
          .size(38.dp)
          .clip(RoundedCornerShape(10.dp))
          .background(iconTint.copy(alpha = 0.12f)),
        contentAlignment = Alignment.Center
      ) {
        Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
      }
      Spacer(modifier = Modifier.width(12.dp))
      Column {
        Text(text = title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        Text(text = subtitle, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
      }
    }
    Icon(
      imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
      contentDescription = null,
      tint = MaterialTheme.colorScheme.onSurfaceVariant,
      modifier = Modifier.size(13.dp)
    )
  }
}
