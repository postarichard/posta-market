package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Product
import com.example.model.ProductCategory
import com.example.ui.theme.BrightPink
import com.example.ui.theme.BrightPurple
import com.example.ui.theme.BrightYellow
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.LavenderContainerBorder
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.launch

@Composable
fun NearbyListingsMap(
  products: List<Product>,
  selectedCategory: ProductCategory,
  onCategoryFilterChanged: (ProductCategory) -> Unit,
  onProductClick: (Product) -> Unit,
  onAddToCart: (Product) -> Unit,
  onToggleFavorite: (String) -> Unit,
  favorites: Set<String>,
  hasLocationPermission: Boolean,
  onLaunchLocationPermission: () -> Unit,
  currentAddress: String,
  currentCoordinates: String?,
  modifier: Modifier = Modifier
) {
  // Center: Kampala, Uganda (0.3476° N, 32.5825° E)
  val defaultCenter = LatLng(0.3250, 32.5850)
  val cameraPositionState = rememberCameraPositionState {
    position = CameraPosition.fromLatLngZoom(defaultCenter, 13.5f)
  }
  val coroutineScope = rememberCoroutineScope()

  var selectedProductOnMap by remember { mutableStateOf<Product?>(null) }
  var mapType by remember { mutableStateOf(MapType.NORMAL) }
  var showApiKeyGuide by remember { mutableStateOf(false) }

  // Filter products by selected category
  val filteredProducts = remember(products, selectedCategory) {
    if (selectedCategory == ProductCategory.ALL) products
    else products.filter { it.category == selectedCategory }
  }

  // Camera animation when selected product changes
  LaunchedEffect(selectedProductOnMap) {
    selectedProductOnMap?.let { product ->
      cameraPositionState.animate(
        CameraUpdateFactory.newLatLngZoom(LatLng(product.latitude, product.longitude), 15f),
        800
      )
    }
  }

  Box(modifier = modifier.fillMaxSize().testTag("nearby_listings_map_container")) {
    // 1. Real Google Maps View via GoogleMap Composable
    GoogleMap(
      modifier = Modifier.fillMaxSize().testTag("google_map_view"),
      cameraPositionState = cameraPositionState,
      properties = MapProperties(
        isMyLocationEnabled = hasLocationPermission,
        mapType = mapType
      ),
      uiSettings = MapUiSettings(
        zoomControlsEnabled = false,
        myLocationButtonEnabled = false,
        compassEnabled = true,
        rotationGesturesEnabled = true,
        scrollGesturesEnabled = true,
        tiltGesturesEnabled = true,
        zoomGesturesEnabled = true
      ),
      onMapClick = {
        selectedProductOnMap = null
      }
    ) {
      // Render interactive markers for each nearby product listing
      filteredProducts.forEach { product ->
        val isSelected = selectedProductOnMap?.id == product.id
        val markerColor = when (product.category) {
          ProductCategory.TECH -> BitmapDescriptorFactory.HUE_VIOLET
          ProductCategory.FASHION -> BitmapDescriptorFactory.HUE_ROSE
          ProductCategory.HOME -> BitmapDescriptorFactory.HUE_ORANGE
          ProductCategory.BEAUTY -> BitmapDescriptorFactory.HUE_MAGENTA
          ProductCategory.GAMING -> BitmapDescriptorFactory.HUE_CYAN
          ProductCategory.SPORTS -> BitmapDescriptorFactory.HUE_GREEN
          ProductCategory.ACCESSORIES -> BitmapDescriptorFactory.HUE_YELLOW
          else -> BitmapDescriptorFactory.HUE_AZURE
        }

        Marker(
          state = MarkerState(position = LatLng(product.latitude, product.longitude)),
          title = product.name,
          snippet = "${product.locationName} • $${product.price}",
          icon = BitmapDescriptorFactory.defaultMarker(
            if (isSelected) BitmapDescriptorFactory.HUE_RED else markerColor
          ),
          onClick = {
            selectedProductOnMap = product
            true
          }
        )
      }
    }

    // 2. Top Floating Controls: Category Pills & Map Layer Toggle
    Column(
      modifier = Modifier
        .align(Alignment.TopCenter)
        .fillMaxWidth()
        .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
      // Top Location Bar Pill
      Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
        shadowElevation = 4.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, LavenderContainerBorder),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 10.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
          ) {
            Box(
              modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(BrightPurple.copy(alpha = 0.15f)),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Filled.NearMe,
                contentDescription = null,
                tint = BrightPurple,
                modifier = Modifier.size(18.dp)
              )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                  text = "Uganda Marketplace Map",
                  fontWeight = FontWeight.Bold,
                  fontSize = 13.sp,
                  color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(6.dp))
                Surface(
                  shape = RoundedCornerShape(6.dp),
                  color = BrightYellow.copy(alpha = 0.25f)
                ) {
                  Text(
                    text = "LIVE",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF7A5900),
                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                  )
                }
              }
              Text(
                text = if (hasLocationPermission && currentCoordinates != null) {
                  "GPS Active • $currentAddress"
                } else {
                  "Kampala Metro Hub (${filteredProducts.size} nearby items)"
                },
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
            }
          }

          // API Key Help / Info
          IconButton(
            onClick = { showApiKeyGuide = !showApiKeyGuide },
            modifier = Modifier
              .size(36.dp)
              .testTag("map_api_key_help_btn")
          ) {
            Icon(
              imageVector = Icons.Filled.Info,
              contentDescription = "Maps Setup Info",
              tint = if (showApiKeyGuide) BrightPink else BrightPurple,
              modifier = Modifier.size(20.dp)
            )
          }

          // Map Type Toggle Button (Normal <-> Satellite/Hybrid)
          IconButton(
            onClick = {
              mapType = if (mapType == MapType.NORMAL) MapType.HYBRID else MapType.NORMAL
            },
            modifier = Modifier
              .size(36.dp)
              .testTag("toggle_map_layer_btn")
          ) {
            Icon(
              imageVector = Icons.Filled.Layers,
              contentDescription = "Toggle Map View",
              tint = if (mapType == MapType.HYBRID) BrightPink else BrightPurple,
              modifier = Modifier.size(20.dp)
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      // Category filter chips floating over map
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        ProductCategory.values().forEach { category ->
          val isSelected = category == selectedCategory
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = if (isSelected) BrightPurple else MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
            shadowElevation = 3.dp,
            border = androidx.compose.foundation.BorderStroke(
              1.dp,
              if (isSelected) BrightPurple else LavenderContainerBorder
            ),
            modifier = Modifier
              .clickable { onCategoryFilterChanged(category) }
              .testTag("map_cat_${category.name.lowercase()}")
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
              Text(
                text = category.displayName,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
              )
            }
          }
        }
      }

      // Expandable Maps Key Setup Guide
      AnimatedVisibility(
        visible = showApiKeyGuide,
        enter = fadeIn() + slideInVertically(),
        exit = fadeOut() + slideOutVertically()
      ) {
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
          border = androidx.compose.foundation.BorderStroke(1.dp, LavenderContainerBorder),
          modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
            .testTag("map_api_key_guide_card")
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = Icons.Filled.Info,
                  contentDescription = null,
                  tint = BrightPurple,
                  modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = "Google Maps API Key Setup",
                  fontWeight = FontWeight.Bold,
                  fontSize = 13.sp,
                  color = MaterialTheme.colorScheme.onSurface
                )
              }
              IconButton(
                onClick = { showApiKeyGuide = false },
                modifier = Modifier.size(24.dp)
              ) {
                Icon(Icons.Filled.Close, contentDescription = "Close", modifier = Modifier.size(16.dp))
              }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
              text = "If map tiles appear blank or show an authorization notice in Google Console:",
              fontSize = 11.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(4.dp))

            Surface(
              shape = RoundedCornerShape(8.dp),
              color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
              modifier = Modifier.fillMaxWidth()
            ) {
              Column(modifier = Modifier.padding(8.dp)) {
                Text(
                  text = "1. Enable 'Maps SDK for Android' in Google Cloud Console\n2. In API Key restrictions, allow Android app:\n   • Package: com.aistudio.postamarket.kmxql\n   • SHA-1: C4:F6:63:BD:D9:56:17:2B:7E:26:66:3D:B4:FF:DB:2F:64:07:7A:1A",
                  fontSize = 10.sp,
                  lineHeight = 15.sp,
                  fontWeight = FontWeight.Medium,
                  color = MaterialTheme.colorScheme.onSurface
                )
              }
            }
          }
        }
      }
    }

    // 3. Right-Side Action FABs (Re-center Kampala & My Location GPS)
    Column(
      modifier = Modifier
        .align(Alignment.CenterEnd)
        .padding(end = 16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      // Recenter Kampala Hub
      FloatingActionButton(
        onClick = {
          coroutineScope.launch {
            cameraPositionState.animate(
              CameraUpdateFactory.newLatLngZoom(defaultCenter, 13.5f),
              600
            )
          }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = BrightPurple,
        shape = CircleShape,
        modifier = Modifier.size(44.dp).testTag("recenter_kampala_fab")
      ) {
        Icon(
          imageVector = Icons.Filled.Place,
          contentDescription = "Center Kampala Hub",
          modifier = Modifier.size(22.dp)
        )
      }

      // My GPS Location Button
      FloatingActionButton(
        onClick = {
          if (!hasLocationPermission) {
            onLaunchLocationPermission()
          } else {
            coroutineScope.launch {
              cameraPositionState.animate(
                CameraUpdateFactory.newLatLngZoom(defaultCenter, 14.5f),
                600
              )
            }
          }
        },
        containerColor = if (hasLocationPermission) BrightPurple else MaterialTheme.colorScheme.surface,
        contentColor = if (hasLocationPermission) Color.White else MaterialTheme.colorScheme.onSurface,
        shape = CircleShape,
        modifier = Modifier.size(44.dp).testTag("my_location_map_fab")
      ) {
        Icon(
          imageVector = Icons.Filled.MyLocation,
          contentDescription = "My GPS Location",
          modifier = Modifier.size(22.dp)
        )
      }
    }

    // 4. Bottom Selected Product Card Preview
    AnimatedVisibility(
      visible = selectedProductOnMap != null,
      enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
      exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
      modifier = Modifier
        .align(Alignment.BottomCenter)
        .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
      selectedProductOnMap?.let { product ->
        Card(
          shape = RoundedCornerShape(20.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
          border = androidx.compose.foundation.BorderStroke(1.dp, LavenderContainerBorder),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("selected_map_product_card")
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            // Header: Close and Distance badge
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                  shape = RoundedCornerShape(8.dp),
                  color = BrightPurple.copy(alpha = 0.12f)
                ) {
                  Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                  ) {
                    Icon(
                      imageVector = Icons.Filled.Directions,
                      contentDescription = null,
                      tint = BrightPurple,
                      modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                      text = "${product.distanceKm} km away",
                      fontSize = 11.sp,
                      fontWeight = FontWeight.Bold,
                      color = BrightPurple
                    )
                  }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Surface(
                  shape = RoundedCornerShape(8.dp),
                  color = BrightYellow.copy(alpha = 0.18f)
                ) {
                  Text(
                    text = product.category.displayName,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF8B6508),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                  )
                }
              }

              IconButton(
                onClick = { selectedProductOnMap = null },
                modifier = Modifier.size(28.dp)
              ) {
                Icon(
                  imageVector = Icons.Filled.Close,
                  contentDescription = "Close Preview",
                  tint = MaterialTheme.colorScheme.onSurfaceVariant,
                  modifier = Modifier.size(18.dp)
                )
              }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Title and Merchant Location
            Text(
              text = product.name,
              fontWeight = FontWeight.Bold,
              fontSize = 16.sp,
              color = MaterialTheme.colorScheme.onSurface,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(3.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Filled.LocationOn,
                contentDescription = null,
                tint = BrightPink,
                modifier = Modifier.size(14.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = product.locationName,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Price, Rating & Action Buttons
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column {
                Row(verticalAlignment = Alignment.Bottom) {
                  Text(
                    text = "$${product.price}",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 20.sp,
                    color = BrightPurple
                  )
                  if (product.originalPrice != null) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                      text = "$${product.originalPrice}",
                      fontSize = 13.sp,
                      color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                      textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
                    )
                  }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(
                    imageVector = Icons.Filled.Star,
                    contentDescription = null,
                    tint = BrightYellow,
                    modifier = Modifier.size(14.dp)
                  )
                  Spacer(modifier = Modifier.width(3.dp))
                  Text(
                    text = "${product.rating} (${product.reviewCount})",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                  )
                }
              }

              // Actions
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                // Favorite
                IconButton(
                  onClick = { onToggleFavorite(product.id) },
                  modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                ) {
                  Icon(
                    imageVector = if (favorites.contains(product.id)) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescription = "Favorite",
                    tint = if (favorites.contains(product.id)) BrightPink else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                  )
                }

                // Add to Cart
                Button(
                  onClick = { onAddToCart(product) },
                  colors = ButtonDefaults.buttonColors(containerColor = BrightPurple),
                  shape = RoundedCornerShape(12.dp),
                  contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                  modifier = Modifier.testTag("map_add_to_cart_btn")
                ) {
                  Icon(
                    imageVector = Icons.Filled.AddShoppingCart,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                  )
                  Spacer(modifier = Modifier.width(6.dp))
                  Text(text = "Add to Cart", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                // View Detail
                Button(
                  onClick = { onProductClick(product) },
                  colors = ButtonDefaults.buttonColors(containerColor = BrightPink),
                  shape = RoundedCornerShape(12.dp),
                  contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                  modifier = Modifier.testTag("map_view_detail_btn")
                ) {
                  Text(text = "View", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
              }
            }
          }
        }
      }
    }
  }
}
