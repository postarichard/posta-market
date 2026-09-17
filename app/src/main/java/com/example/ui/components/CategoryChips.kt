package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Watch
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ProductCategory
import com.example.ui.theme.BrightPurple
import com.example.ui.theme.LavenderContainerBorder

@Composable
fun CategoryChips(
  selectedCategory: ProductCategory,
  onCategorySelected: (ProductCategory) -> Unit,
  modifier: Modifier = Modifier
) {
  val scrollState = rememberScrollState()

  Row(
    modifier = modifier
      .fillMaxWidth()
      .horizontalScroll(scrollState)
      .padding(horizontal = 16.dp),
    horizontalArrangement = Arrangement.spacedBy(8.dp)
  ) {
    ProductCategory.values().forEach { category ->
      val isSelected = category == selectedCategory

      val icon: ImageVector = when (category) {
        ProductCategory.ALL -> Icons.Filled.Apps
        ProductCategory.TECH -> Icons.Filled.Devices
        ProductCategory.FASHION -> Icons.Filled.Checkroom
        ProductCategory.BEAUTY -> Icons.Filled.Spa
        ProductCategory.HOME -> Icons.Filled.Home
        ProductCategory.GAMING -> Icons.Filled.SportsEsports
        ProductCategory.SPORTS -> Icons.Filled.FitnessCenter
        ProductCategory.ACCESSORIES -> Icons.Filled.Watch
      }

      Box(
        modifier = Modifier
          .clip(RoundedCornerShape(16.dp))
          .background(
            if (isSelected) BrightPurple else MaterialTheme.colorScheme.surface
          )
          .border(
            width = 1.dp,
            color = if (isSelected) BrightPurple else LavenderContainerBorder,
            shape = RoundedCornerShape(16.dp)
          )
          .clickable { onCategorySelected(category) }
          .padding(horizontal = 14.dp, vertical = 9.dp)
          .testTag("category_chip_${category.name}"),
        contentAlignment = Alignment.Center
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = icon,
            contentDescription = category.displayName,
            tint = if (isSelected) Color.White else MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(16.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = category.displayName,
            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
          )
        }
      }
    }
  }
}
