package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.PostaMarketUiState
import com.example.model.Product
import com.example.model.ProductCategory
import com.example.model.PromoBanner
import com.example.model.SampleData
import com.example.ui.components.CategoryChips
import com.example.ui.components.ProductCard
import com.example.ui.components.PromoBannerCarousel
import com.example.ui.components.RealTimePermissionsDock
import com.example.ui.components.SectionHeader
import com.example.ui.theme.BrightPink
import com.example.ui.theme.BrightPurple
import com.example.ui.theme.BrightYellow
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.GoldenYellow
import com.example.ui.theme.HighlightAmber
import com.example.ui.theme.LavenderContainerBorder
import kotlinx.coroutines.delay

@Composable
fun HomeScreen(
  uiState: PostaMarketUiState,
  onCategorySelected: (ProductCategory) -> Unit,
  onProductClick: (Product) -> Unit,
  onToggleFavorite: (String) -> Unit,
  onAddToCart: (Product) -> Unit,
  onBannerClick: (PromoBanner) -> Unit,
  onSearchClick: () -> Unit,
  onSeeAllClick: (ProductCategory) -> Unit,
  onToggleDarkMode: () -> Unit = {},
  hasCameraPermission: Boolean = false,
  hasLocationPermission: Boolean = false,
  hasContactsPermission: Boolean = false,
  hasNotificationsPermission: Boolean = false,
  hasStoragePermission: Boolean = false,
  onLaunchCamera: () -> Unit = {},
  onLaunchLocation: () -> Unit = {},
  onLaunchContacts: () -> Unit = {},
  onLaunchNotificationTest: () -> Unit = {},
  onLaunchStoragePicker: () -> Unit = {},
  modifier: Modifier = Modifier
) {
  // Live flash sale countdown simulation
  var countdownSeconds by remember { mutableIntStateOf(5 * 3600 + 42 * 60 + 19) }
  LaunchedEffect(Unit) {
    while (true) {
      delay(1000)
      if (countdownSeconds > 0) countdownSeconds--
    }
  }

  val hours = countdownSeconds / 3600
  val minutes = (countdownSeconds % 3600) / 60
  val seconds = countdownSeconds % 60
  val countdownStr = String.format("%02d:%02d:%02d", hours, minutes, seconds)

  val filteredProducts = if (uiState.selectedCategory == ProductCategory.ALL) {
    uiState.products
  } else {
    uiState.products.filter { it.category == uiState.selectedCategory }
  }

  val featuredProducts = filteredProducts.filter { it.isFeatured }
  val popularProducts = filteredProducts.filter { it.isPopular }
  val newArrivals = filteredProducts.filter { it.isNewArrival }
  val specialOffers = filteredProducts.filter { it.isSpecialOffer }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .testTag("home_screen_content"),
    contentPadding = PaddingValues(bottom = 24.dp)
  ) {
    // 1. Posta Market Header & Delivery Pill
    item {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .background(MaterialTheme.colorScheme.surface)
          .padding(horizontal = 16.dp, vertical = 12.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          // Brand Logo + 2026 Badge
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(
                  Brush.linearGradient(
                    colors = listOf(BrightPurple, ElectricViolet)
                  )
                ),
              contentAlignment = Alignment.Center
            ) {
              Image(
                painter = painterResource(id = R.drawable.posta_app_icon),
                contentDescription = "Posta Market Logo",
                modifier = Modifier
                  .size(38.dp)
                  .clip(RoundedCornerShape(10.dp))
              )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                  text = "Posta",
                  fontSize = 20.sp,
                  fontWeight = FontWeight.ExtraBold,
                  color = MaterialTheme.colorScheme.onSurface,
                  letterSpacing = (-0.5).sp
                )
                Text(
                  text = "Market",
                  fontSize = 20.sp,
                  fontWeight = FontWeight.ExtraBold,
                  color = BrightPurple,
                  letterSpacing = (-0.5).sp
                )
              }
              Text(
                text = "Next-Gen 2026 Commerce",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = if (uiState.isDarkMode) BrightYellow else ElectricViolet,
                letterSpacing = 0.5.sp
              )
            }
          }

          // Dark Mode Toggle & Notification Bell & VIP Badge
          Row(verticalAlignment = Alignment.CenterVertically) {
            // Bright Yellow & Gold VIP Badge
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(
                  Brush.horizontalGradient(
                    colors = listOf(BrightYellow, GoldenYellow)
                  )
                )
                .padding(horizontal = 9.dp, vertical = 4.dp)
            ) {
              Text(
                text = "⚡ VIP GOLD",
                color = Color(0xFF1E1500),
                fontSize = 10.sp,
                fontWeight = FontWeight.ExtraBold
              )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Dark Mode Toggle Button
            Surface(
              shape = CircleShape,
              color = MaterialTheme.colorScheme.surfaceVariant,
              modifier = Modifier.size(40.dp)
            ) {
              IconButton(
                onClick = onToggleDarkMode,
                modifier = Modifier.testTag("home_dark_mode_toggle")
              ) {
                Icon(
                  imageVector = if (uiState.isDarkMode) Icons.Filled.LightMode else Icons.Filled.DarkMode,
                  contentDescription = "Toggle Dark Mode",
                  tint = if (uiState.isDarkMode) BrightYellow else MaterialTheme.colorScheme.onSurface,
                  modifier = Modifier.size(20.dp)
                )
              }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Surface(
              shape = CircleShape,
              color = MaterialTheme.colorScheme.surfaceVariant,
              modifier = Modifier.size(40.dp)
            ) {
              Box(contentAlignment = Alignment.Center) {
                IconButton(onClick = onLaunchNotificationTest, modifier = Modifier.testTag("home_notification_bell")) {
                  Icon(
                    imageVector = Icons.Filled.Notifications,
                    contentDescription = "Notifications",
                    tint = if (hasNotificationsPermission) BrightPurple else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(20.dp)
                  )
                }
                // Unread dot
                Box(
                  modifier = Modifier
                    .size(8.dp)
                    .align(Alignment.TopEnd)
                    .padding(end = 6.dp, top = 6.dp)
                    .clip(CircleShape)
                    .background(BrightPink)
                )
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Delivery Address Pill (reflects live GPS location)
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
            .clickable { onLaunchLocation() }
            .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
          Icon(
            imageVector = Icons.Filled.LocationOn,
            contentDescription = null,
            tint = BrightPurple,
            modifier = Modifier.size(14.dp)
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = "Deliver to: ${uiState.deliveryLocationAddress} • Express",
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }
    }

    // 2. Real-Time Permissions & Live Sensors Dock
    item {
      RealTimePermissionsDock(
        hasCamera = hasCameraPermission,
        hasLocation = hasLocationPermission,
        hasContacts = hasContactsPermission,
        hasNotifications = hasNotificationsPermission,
        hasStorage = hasStoragePermission,
        onLaunchCamera = onLaunchCamera,
        onLaunchLocation = onLaunchLocation,
        onLaunchContacts = onLaunchContacts,
        onLaunchNotificationTest = onLaunchNotificationTest,
        onLaunchStoragePicker = onLaunchStoragePicker
      )
    }

    // 3. Modern Search Bar
    item {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 8.dp)
      ) {
        Surface(
          shape = RoundedCornerShape(16.dp),
          color = MaterialTheme.colorScheme.surface,
          border = androidx.compose.foundation.BorderStroke(1.dp, LavenderContainerBorder),
          shadowElevation = 2.dp,
          modifier = Modifier
            .fillMaxWidth()
            .clickable { onSearchClick() }
            .testTag("home_search_bar")
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Filled.Search,
                contentDescription = "Search",
                tint = BrightPurple,
                modifier = Modifier.size(22.dp)
              )
              Spacer(modifier = Modifier.width(12.dp))
              Text(
                text = "Search 10,000+ modern products...",
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                fontSize = 14.sp
              )
            }

            Box(
              modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(BrightPurple.copy(alpha = 0.12f)),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Filled.Tune,
                contentDescription = "Filters",
                tint = BrightPurple,
                modifier = Modifier.size(16.dp)
              )
            }
          }
        }
      }
    }

    // 3. Promotional Carousel
    item {
      Spacer(modifier = Modifier.height(6.dp))
      PromoBannerCarousel(
        banners = SampleData.promoBanners,
        onBannerClick = onBannerClick
      )
      Spacer(modifier = Modifier.height(10.dp))
    }

    // Nearby Listings Map Banner
    item {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp)
          .clickable { onSearchClick() }
          .testTag("home_nearby_map_banner"),
        shape = RoundedCornerShape(18.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, LavenderContainerBorder),
        colors = CardDefaults.cardColors(
          containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
          ) {
            Box(
              modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(BrightPurple),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Filled.Map,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(22.dp)
              )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                  text = "Nearby Listings Map",
                  fontWeight = FontWeight.Bold,
                  fontSize = 15.sp,
                  color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(6.dp))
                Surface(
                  shape = RoundedCornerShape(6.dp),
                  color = BrightPink.copy(alpha = 0.15f)
                ) {
                  Text(
                    text = "UGANDA",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = BrightPink,
                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                  )
                }
              }
              Spacer(modifier = Modifier.height(2.dp))
              Text(
                text = "View live merchant hubs & products around Kampala",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }

          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(10.dp))
              .background(BrightPurple.copy(alpha = 0.12f))
              .padding(horizontal = 10.dp, vertical = 6.dp)
          ) {
            Text(
              text = "Open Map",
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold,
              color = BrightPurple
            )
          }
        }
      }
      Spacer(modifier = Modifier.height(14.dp))
    }

    // 4. Categories Section
    item {
      SectionHeader(
        title = "Explore Categories",
        subtitle = "Curated collections for 2026 living",
        onSeeAllClick = { onSeeAllClick(ProductCategory.ALL) }
      )
      Spacer(modifier = Modifier.height(6.dp))
      CategoryChips(
        selectedCategory = uiState.selectedCategory,
        onCategorySelected = onCategorySelected
      )
      Spacer(modifier = Modifier.height(16.dp))
    }

    // 5. Special Offers (Flash Sale with Countdown)
    if (specialOffers.isNotEmpty()) {
      item {
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
          shape = RoundedCornerShape(20.dp),
          border = androidx.compose.foundation.BorderStroke(1.dp, LavenderContainerBorder),
          colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
          ),
          elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(BrightYellow),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(
                    imageVector = Icons.Filled.ElectricBolt,
                    contentDescription = null,
                    tint = Color(0xFF1E1500),
                    modifier = Modifier.size(16.dp)
                  )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                  Text(
                    text = "Flash Deals",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                  )
                  Text(
                    text = "Up to 50% discount",
                    fontSize = 11.sp,
                    color = BrightPink,
                    fontWeight = FontWeight.SemiBold
                  )
                }
              }

              // Countdown Pill
              Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFF1E1B2E),
                modifier = Modifier.padding(2.dp)
              ) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                  Text(
                    text = "Ends in ",
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.7f)
                  )
                  Text(
                    text = countdownStr,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = BrightYellow
                  )
                }
              }
            }

            Spacer(modifier = Modifier.height(12.dp))

            LazyRow(
              horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
              items(specialOffers) { product ->
                ProductCard(
                  product = product,
                  isFavorite = uiState.favorites.contains(product.id),
                  onProductClick = onProductClick,
                  onToggleFavorite = onToggleFavorite,
                  onAddToCart = onAddToCart,
                  modifier = Modifier.width(180.dp)
                )
              }
            }
          }
        }
        Spacer(modifier = Modifier.height(16.dp))
      }
    }

    // 6. Featured Products
    item {
      SectionHeader(
        title = "Featured Products",
        badgeText = "2026 PICKS",
        badgeColor = BrightPurple,
        onSeeAllClick = { onSeeAllClick(ProductCategory.TECH) }
      )
      Spacer(modifier = Modifier.height(6.dp))
      LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp)
      ) {
        items(featuredProducts) { product ->
          ProductCard(
            product = product,
            isFavorite = uiState.favorites.contains(product.id),
            onProductClick = onProductClick,
            onToggleFavorite = onToggleFavorite,
            onAddToCart = onAddToCart,
            modifier = Modifier.width(190.dp)
          )
        }
      }
      Spacer(modifier = Modifier.height(20.dp))
    }

    // 7. Popular Products
    item {
      SectionHeader(
        title = "Popular Right Now",
        badgeText = "🔥 TRENDING",
        badgeColor = BrightPink,
        onSeeAllClick = { onSeeAllClick(ProductCategory.FASHION) }
      )
      Spacer(modifier = Modifier.height(6.dp))
      LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp)
      ) {
        items(popularProducts) { product ->
          ProductCard(
            product = product,
            isFavorite = uiState.favorites.contains(product.id),
            onProductClick = onProductClick,
            onToggleFavorite = onToggleFavorite,
            onAddToCart = onAddToCart,
            modifier = Modifier.width(190.dp)
          )
        }
      }
      Spacer(modifier = Modifier.height(20.dp))
    }

    // 8. New Arrivals
    item {
      SectionHeader(
        title = "New Arrivals",
        subtitle = "Fresh drops added this week",
        badgeText = "NEW",
        badgeColor = ElectricViolet,
        onSeeAllClick = { onSeeAllClick(ProductCategory.ALL) }
      )
      Spacer(modifier = Modifier.height(6.dp))
      LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp)
      ) {
        items(newArrivals) { product ->
          ProductCard(
            product = product,
            isFavorite = uiState.favorites.contains(product.id),
            onProductClick = onProductClick,
            onToggleFavorite = onToggleFavorite,
            onAddToCart = onAddToCart,
            modifier = Modifier.width(190.dp)
          )
        }
      }
    }
  }
}
