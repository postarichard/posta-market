package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import com.example.model.Product
import com.example.ui.theme.BrightPink
import com.example.ui.theme.BrightPurple
import com.example.ui.theme.BrightYellow
import com.example.ui.theme.HighlightAmber
import com.example.ui.theme.LavenderContainerBorder
import com.example.ui.theme.TextDeepPurple

@Composable
fun ProductCard(
  product: Product,
  isFavorite: Boolean,
  onProductClick: (Product) -> Unit,
  onToggleFavorite: (String) -> Unit,
  onAddToCart: (Product) -> Unit,
  modifier: Modifier = Modifier
) {
  Card(
    modifier = modifier
      .clip(RoundedCornerShape(20.dp))
      .clickable { onProductClick(product) }
      .testTag("product_card_${product.id}"),
    shape = RoundedCornerShape(20.dp),
    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surface
    ),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(bottom = 12.dp)
    ) {
      // Product Image Container with Badges
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .aspectRatio(1.15f)
          .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
          .background(MaterialTheme.colorScheme.surfaceVariant)
      ) {
        SubcomposeAsyncImage(
          model = product.imageUrl,
          contentDescription = product.name,
          contentScale = ContentScale.Crop,
          modifier = Modifier.fillMaxSize(),
          loading = {
            Box(
              modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceVariant),
              contentAlignment = Alignment.Center
            ) {
              CircularProgressIndicator(
                modifier = Modifier.size(22.dp),
                color = BrightPurple,
                strokeWidth = 2.dp
              )
            }
          },
          error = {
            Box(
              modifier = Modifier
                .fillMaxSize()
                .background(
                  Brush.linearGradient(
                    colors = listOf(
                      BrightPurple.copy(alpha = 0.25f),
                      MaterialTheme.colorScheme.surfaceVariant
                    )
                  )
                ),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Filled.ShoppingBag,
                contentDescription = product.name,
                tint = BrightPurple.copy(alpha = 0.6f),
                modifier = Modifier.size(36.dp)
              )
            }
          }
        )

        // Subtle gradient overlay at top for badge legibility
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .background(
              Brush.verticalGradient(
                colors = listOf(Color.Black.copy(alpha = 0.35f), Color.Transparent)
              )
            )
        )

        // Tag / Discount Badge
        if (product.discountPercent > 0 || product.tag != null) {
          Surface(
            modifier = Modifier
              .padding(8.dp)
              .align(Alignment.TopStart),
            shape = RoundedCornerShape(8.dp),
            color = if (product.discountPercent > 0) BrightPink else HighlightAmber,
            shadowElevation = 2.dp
          ) {
            Text(
              text = if (product.discountPercent > 0) "-${product.discountPercent}%" else (product.tag ?: ""),
              color = Color.White,
              fontWeight = FontWeight.Bold,
              fontSize = 11.sp,
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
            )
          }
        }

        // Favorite Button
        Surface(
          modifier = Modifier
            .padding(8.dp)
            .size(34.dp)
            .align(Alignment.TopEnd),
          shape = CircleShape,
          color = Color.White.copy(alpha = 0.88f),
          shadowElevation = 2.dp
        ) {
          IconButton(
            onClick = { onToggleFavorite(product.id) },
            modifier = Modifier
              .fillMaxSize()
              .testTag("favorite_btn_${product.id}")
          ) {
            Icon(
              imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
              contentDescription = "Toggle favorite",
              tint = if (isFavorite) BrightPink else Color(0xFF6B7280),
              modifier = Modifier.size(18.dp)
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Category & Rating
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Text(
          text = product.category.displayName.uppercase(),
          color = MaterialTheme.colorScheme.primary,
          fontSize = 11.sp,
          fontWeight = FontWeight.SemiBold,
          letterSpacing = 0.5.sp
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Filled.Star,
            contentDescription = "Rating",
            tint = BrightYellow,
            modifier = Modifier.size(13.dp)
          )
          Spacer(modifier = Modifier.width(3.dp))
          Text(
            text = String.format("%.1f", product.rating),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )
        }
      }

      Spacer(modifier = Modifier.height(4.dp))

      // Product Name
      Text(
        text = product.name,
        color = MaterialTheme.colorScheme.onSurface,
        fontSize = 14.sp,
        fontWeight = FontWeight.Medium,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
        lineHeight = 18.sp,
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 12.dp)
          .height(38.dp)
      )

      Spacer(modifier = Modifier.height(8.dp))

      // Price and Add to Cart Row
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Column {
          Text(
            text = "$${String.format("%.2f", product.price)}",
            color = MaterialTheme.colorScheme.primary,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
          )
          if (product.originalPrice != null && product.originalPrice > product.price) {
            Text(
              text = "$${String.format("%.2f", product.originalPrice)}",
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              fontSize = 12.sp,
              textDecoration = TextDecoration.LineThrough
            )
          }
        }

        Surface(
          onClick = { onAddToCart(product) },
          shape = RoundedCornerShape(12.dp),
          color = BrightPurple,
          modifier = Modifier
            .size(38.dp)
            .testTag("add_to_cart_${product.id}")
        ) {
          Box(contentAlignment = Alignment.Center) {
            Icon(
              imageVector = Icons.Filled.AddShoppingCart,
              contentDescription = "Add to cart",
              tint = Color.White,
              modifier = Modifier.size(18.dp)
            )
          }
        }
      }
    }
  }
}
