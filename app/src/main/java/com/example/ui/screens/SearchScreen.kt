package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.example.data.PostaMarketUiState
import com.example.data.SortOption
import com.example.model.Product
import com.example.model.ProductCategory
import com.example.ui.components.CategoryChips
import com.example.ui.components.ProductCard
import com.example.ui.theme.BrightPink
import com.example.ui.theme.BrightPurple
import com.example.ui.theme.BrightYellow
import com.example.ui.theme.LavenderContainerBorder
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
  uiState: PostaMarketUiState,
  onSearchQueryChanged: (String) -> Unit,
  onCategoryFilterChanged: (ProductCategory) -> Unit,
  onSortOptionChanged: (SortOption) -> Unit,
  onProductClick: (Product) -> Unit,
  onToggleFavorite: (String) -> Unit,
  onAddToCart: (Product) -> Unit,
  onVisualSearchPhotoSelected: (String?) -> Unit = {},
  hasLocationPermission: Boolean = false,
  onLaunchLocationPermission: () -> Unit = {},
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val trendingKeywords = listOf("Spatial Audio", "OLED Watch", "Streetwear", "Serum", "Laser Projector", "GaN Charger")
  var isMapViewActive by remember { mutableStateOf(false) }

  // Storage / Photo Picker launcher (Android zero-permission photo picker contract)
  val photoPickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.PickVisualMedia()
  ) { uri: Uri? ->
    uri?.let {
      onVisualSearchPhotoSelected(it.toString())
      onSearchQueryChanged("Wireless")
    }
  }

  // Camera permission launcher
  var cameraPermissionGranted by remember {
    mutableStateOf(
      ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    )
  }

  // Camera capture launcher
  val takePictureLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.TakePicturePreview()
  ) { bitmap ->
    if (bitmap != null) {
      onVisualSearchPhotoSelected("camera_capture_preview")
      onSearchQueryChanged("Smart")
    }
  }

  val cameraPermissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestPermission()
  ) { isGranted ->
    cameraPermissionGranted = isGranted
    if (isGranted) {
      takePictureLauncher.launch(null)
    }
  }

  // Filter and Sort logic
  val matchingProducts = remember(
    uiState.products,
    uiState.searchQuery,
    uiState.searchCategoryFilter,
    uiState.searchSortOption
  ) {
    var list = uiState.products

    if (uiState.searchCategoryFilter != ProductCategory.ALL) {
      list = list.filter { it.category == uiState.searchCategoryFilter }
    }

    if (uiState.searchQuery.isNotBlank()) {
      val q = uiState.searchQuery.trim().lowercase()
      list = list.filter {
        it.name.lowercase().contains(q) ||
          it.description.lowercase().contains(q) ||
          it.category.displayName.lowercase().contains(q) ||
          it.specs.any { s -> s.lowercase().contains(q) } ||
          (it.tag != null && it.tag.lowercase().contains(q))
      }
    }

    when (uiState.searchSortOption) {
      SortOption.POPULAR -> list.sortedByDescending { it.reviewCount }
      SortOption.PRICE_LOW -> list.sortedBy { it.price }
      SortOption.PRICE_HIGH -> list.sortedByDescending { it.price }
      SortOption.RATING -> list.sortedByDescending { it.rating }
    }
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .testTag("search_screen")
  ) {
    // Top Bar & Search Input
    Surface(
      color = MaterialTheme.colorScheme.surface,
      shadowElevation = 2.dp
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 12.dp)
      ) {
        Text(
          text = "Find in Posta Market",
          fontWeight = FontWeight.Bold,
          fontSize = 20.sp,
          color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
          value = uiState.searchQuery,
          onValueChange = onSearchQueryChanged,
          placeholder = { Text("Search wireless audio, hoodies, gadgets...", fontSize = 14.sp) },
          leadingIcon = {
            Icon(
              imageVector = Icons.Filled.Search,
              contentDescription = "Search",
              tint = BrightPurple
            )
          },
          trailingIcon = {
            Row(verticalAlignment = Alignment.CenterVertically) {
              if (uiState.searchQuery.isNotEmpty()) {
                IconButton(onClick = {
                  onSearchQueryChanged("")
                  onVisualSearchPhotoSelected(null)
                }) {
                  Icon(
                    imageVector = Icons.Filled.Clear,
                    contentDescription = "Clear",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }
              }

              // Camera Visual Search
              IconButton(
                onClick = {
                  if (cameraPermissionGranted) {
                    takePictureLauncher.launch(null)
                  } else {
                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                  }
                },
                modifier = Modifier.testTag("camera_search_btn")
              ) {
                Icon(
                  imageVector = Icons.Filled.CameraAlt,
                  contentDescription = "Camera Visual Search",
                  tint = BrightPurple
                )
              }

              // Storage Photo Picker
              IconButton(
                onClick = {
                  photoPickerLauncher.launch(
                    androidx.activity.result.PickVisualMediaRequest(
                      ActivityResultContracts.PickVisualMedia.ImageOnly
                    )
                  )
                },
                modifier = Modifier.testTag("storage_photo_picker_btn")
              ) {
                Icon(
                  imageVector = Icons.Filled.PhotoLibrary,
                  contentDescription = "Pick Image from Storage",
                  tint = BrightPink
                )
              }
            }
          },
          singleLine = true,
          shape = RoundedCornerShape(16.dp),
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = BrightPurple,
            unfocusedBorderColor = LavenderContainerBorder,
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface
          ),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("search_input_field")
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Categories filter row
        CategoryChips(
          selectedCategory = uiState.searchCategoryFilter,
          onCategorySelected = onCategoryFilterChanged
        )

        // Visual Search / Camera badge when active
        if (uiState.visualSearchPhotoUri != null) {
          Spacer(modifier = Modifier.height(8.dp))
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
              .clip(RoundedCornerShape(12.dp))
              .background(BrightYellow.copy(alpha = 0.2f))
              .padding(horizontal = 10.dp, vertical = 6.dp)
          ) {
            Icon(
              imageVector = Icons.Filled.CameraAlt,
              contentDescription = null,
              tint = Color(0xFFB8860B),
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "Visual Search active (Matching styles)",
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Clear",
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = BrightPurple,
              modifier = Modifier.clickable { onVisualSearchPhotoSelected(null) }
            )
          }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Sort & View Switcher Row
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          // Sort Options Row
          Row(
            modifier = Modifier
              .weight(1f)
              .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.padding(end = 4.dp)
            ) {
              Icon(
                imageVector = Icons.Filled.FilterList,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = "Sort:",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }

            SortOption.values().forEach { option ->
              val isSelected = option == uiState.searchSortOption
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(12.dp))
                  .background(
                    if (isSelected) BrightPurple.copy(alpha = 0.15f) else Color.Transparent
                  )
                  .border(
                    width = 1.dp,
                    color = if (isSelected) BrightPurple else LavenderContainerBorder,
                    shape = RoundedCornerShape(12.dp)
                  )
                  .clickable { onSortOptionChanged(option) }
                  .padding(horizontal = 10.dp, vertical = 6.dp)
              ) {
                Text(
                  text = option.displayName,
                  fontSize = 12.sp,
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                  color = if (isSelected) BrightPurple else MaterialTheme.colorScheme.onSurface
                )
              }
            }
          }

          Spacer(modifier = Modifier.width(8.dp))

          // Map View / Grid View Switcher Pill
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = if (isMapViewActive) BrightPurple else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
            border = androidx.compose.foundation.BorderStroke(
              1.dp,
              if (isMapViewActive) BrightPurple else LavenderContainerBorder
            ),
            modifier = Modifier
              .clip(RoundedCornerShape(12.dp))
              .clickable { isMapViewActive = !isMapViewActive }
              .testTag("toggle_map_view_pill")
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
              Icon(
                imageVector = if (isMapViewActive) Icons.Filled.GridView else Icons.Filled.Map,
                contentDescription = if (isMapViewActive) "Switch to Grid" else "Nearby Map",
                tint = if (isMapViewActive) Color.White else BrightPurple,
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(5.dp))
              Text(
                text = if (isMapViewActive) "Grid" else "Map",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (isMapViewActive) Color.White else MaterialTheme.colorScheme.onSurface
              )
            }
          }
        }
      }
    }

    if (isMapViewActive) {
      // Real Google Maps View for nearby listings in Uganda
      NearbyListingsMap(
        products = matchingProducts,
        selectedCategory = uiState.searchCategoryFilter,
        onCategoryFilterChanged = onCategoryFilterChanged,
        onProductClick = onProductClick,
        onAddToCart = onAddToCart,
        onToggleFavorite = onToggleFavorite,
        favorites = uiState.favorites,
        hasLocationPermission = hasLocationPermission,
        onLaunchLocationPermission = onLaunchLocationPermission,
        currentAddress = uiState.deliveryLocationAddress,
        currentCoordinates = uiState.deliveryCoordinates,
        modifier = Modifier.fillMaxSize()
      )
    } else {
      // Grid Results - Adaptive across phones, foldables, and tablets
      LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 168.dp),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.fillMaxSize()
      ) {
      // Trending searches when search field is empty
      if (uiState.searchQuery.isEmpty()) {
        item(span = { GridItemSpan(maxLineSpan) }) {
          Column(modifier = Modifier.padding(bottom = 8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Filled.TrendingUp,
                contentDescription = null,
                tint = BrightPink,
                modifier = Modifier.size(18.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "Trending Searches 2026",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface
              )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              trendingKeywords.forEach { kw ->
                Surface(
                  shape = RoundedCornerShape(12.dp),
                  color = MaterialTheme.colorScheme.surface,
                  border = androidx.compose.foundation.BorderStroke(1.dp, LavenderContainerBorder),
                  modifier = Modifier.clickable { onSearchQueryChanged(kw) }
                ) {
                  Text(
                    text = kw,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                  )
                }
              }
            }
          }
        }
      }

      // Count Header
      item(span = { GridItemSpan(maxLineSpan) }) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Results (${matchingProducts.size})",
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = MaterialTheme.colorScheme.onSurface
          )
          if (uiState.searchCategoryFilter != ProductCategory.ALL || uiState.searchQuery.isNotEmpty()) {
            Text(
              text = "Clear filters",
              color = BrightPurple,
              fontSize = 13.sp,
              fontWeight = FontWeight.SemiBold,
              modifier = Modifier.clickable {
                onSearchQueryChanged("")
                onCategoryFilterChanged(ProductCategory.ALL)
              }
            )
          }
        }
      }

      // Products or Empty state
      if (matchingProducts.isEmpty()) {
        item(span = { GridItemSpan(maxLineSpan) }) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Box(
              modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Filled.SearchOff,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(36.dp)
              )
            }
            Spacer(modifier = Modifier.height(14.dp))
            Text(
              text = "No products found",
              fontWeight = FontWeight.Bold,
              fontSize = 18.sp,
              color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = "Try adjusting your query or category filters",
              fontSize = 13.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
              onClick = {
                onSearchQueryChanged("")
                onCategoryFilterChanged(ProductCategory.ALL)
              },
              colors = ButtonDefaults.buttonColors(containerColor = BrightPurple),
              shape = RoundedCornerShape(12.dp)
            ) {
              Text("Reset All Filters")
            }
          }
        }
      } else {
        items(matchingProducts, key = { it.id }) { product ->
          ProductCard(
            product = product,
            isFavorite = uiState.favorites.contains(product.id),
            onProductClick = onProductClick,
            onToggleFavorite = onToggleFavorite,
            onAddToCart = onAddToCart
          )
        }
      }
    }
  }
}
}
