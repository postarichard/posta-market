package com.example.ui.components

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.SubcomposeAsyncImage
import com.example.model.Product
import com.example.ui.theme.BrightPink
import com.example.ui.theme.BrightPurple
import com.example.ui.theme.BrightYellow
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.HighlightAmber
import com.example.ui.theme.LavenderContainerBorder
import com.example.ui.theme.SuccessGreen

@Composable
fun ProductDetailDialog(
  product: Product,
  isFavorite: Boolean,
  onDismiss: () -> Unit,
  onToggleFavorite: (String) -> Unit,
  onAddToCart: (Product, Int, String, String) -> Unit,
  modifier: Modifier = Modifier
) {
  var quantity by remember { mutableIntStateOf(1) }
  val availableColors = listOf("Cosmic Violet", "Electric Indigo", "Obsidian Black", "Pure Lavender")
  var selectedColor by remember { mutableStateOf(availableColors[0]) }

  val availableSizes = listOf("Small", "Medium", "Large", "Standard")
  var selectedSize by remember { mutableStateOf(availableSizes[3]) }

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Surface(
      modifier = modifier
        .fillMaxSize()
        .padding(top = 28.dp),
      shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
      color = MaterialTheme.colorScheme.surface
    ) {
      Column(modifier = Modifier.fillMaxSize()) {
        // Top action bar: Back/Close & Favorite
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.size(40.dp)
          ) {
            IconButton(onClick = onDismiss, modifier = Modifier.testTag("close_detail_btn")) {
              Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = "Close",
                tint = MaterialTheme.colorScheme.onSurface
              )
            }
          }

          Text(
            text = "Product Details",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurface
          )

          Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.size(40.dp)
          ) {
            IconButton(onClick = { onToggleFavorite(product.id) }) {
              Icon(
                imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                contentDescription = "Favorite",
                tint = if (isFavorite) BrightPink else MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
        }

        // Scrollable Body
        Column(
          modifier = Modifier
            .weight(1f)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
        ) {
          // Hero Image Container
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .aspectRatio(1.25f)
              .clip(RoundedCornerShape(24.dp))
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
                    color = BrightPurple,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(28.dp)
                  )
                }
              },
              error = {
                Box(
                  modifier = Modifier
                    .fillMaxSize()
                    .background(
                      androidx.compose.ui.graphics.Brush.linearGradient(
                        colors = listOf(BrightPurple.copy(alpha = 0.25f), MaterialTheme.colorScheme.surfaceVariant)
                      )
                    ),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(
                    imageVector = Icons.Filled.ShoppingBag,
                    contentDescription = product.name,
                    tint = BrightPurple.copy(alpha = 0.7f),
                    modifier = Modifier.size(54.dp)
                  )
                }
              }
            )

            if (product.discountPercent > 0) {
              Surface(
                modifier = Modifier
                  .padding(12.dp)
                  .align(Alignment.TopStart),
                shape = RoundedCornerShape(10.dp),
                color = BrightPink
              ) {
                Text(
                  text = "${product.discountPercent}% OFF FLASH DEAL",
                  color = Color.White,
                  fontWeight = FontWeight.Bold,
                  fontSize = 12.sp,
                  modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(16.dp))

          // Category & Rating
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = product.category.displayName,
              color = MaterialTheme.colorScheme.primary,
              fontWeight = FontWeight.Bold,
              fontSize = 13.sp
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = null,
                tint = HighlightAmber,
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = "${product.rating} (${product.reviewCount} verified reviews)",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }

          Spacer(modifier = Modifier.height(6.dp))

          // Product Name
          Text(
            text = product.name,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            lineHeight = 28.sp
          )

          Spacer(modifier = Modifier.height(10.dp))

          // Price Row
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = "$${String.format("%.2f", product.price)}",
              fontSize = 26.sp,
              fontWeight = FontWeight.ExtraBold,
              color = MaterialTheme.colorScheme.primary
            )
            if (product.originalPrice != null && product.originalPrice > product.price) {
              Spacer(modifier = Modifier.width(10.dp))
              Text(
                text = "$${String.format("%.2f", product.originalPrice)}",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textDecoration = TextDecoration.LineThrough
              )
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          // Stock Bar
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(12.dp))
              .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
              .padding(12.dp)
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text(
                text = "⚡ Almost gone!",
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = HighlightAmber
              )
              Text(
                text = "Only ${product.stockLeft} left in stock",
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface
              )
            }
            Spacer(modifier = Modifier.height(6.dp))
            LinearProgressIndicator(
              progress = { (product.stockLeft.toFloat() / product.totalStock.toFloat()).coerceIn(0f, 1f) },
              modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(CircleShape),
              color = HighlightAmber,
              trackColor = MaterialTheme.colorScheme.surface
            )
          }

          Spacer(modifier = Modifier.height(16.dp))

          // Color Selector
          Text(
            text = "Select Color: $selectedColor",
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface
          )
          Spacer(modifier = Modifier.height(8.dp))
          Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            availableColors.forEach { colorName ->
              val isSel = colorName == selectedColor
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(12.dp))
                  .background(if (isSel) BrightPurple.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant)
                  .border(
                    width = if (isSel) 2.dp else 1.dp,
                    color = if (isSel) BrightPurple else Color.Transparent,
                    shape = RoundedCornerShape(12.dp)
                  )
                  .clickable { selectedColor = colorName }
                  .padding(horizontal = 12.dp, vertical = 8.dp)
              ) {
                Text(
                  text = colorName,
                  fontSize = 12.sp,
                  fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                  color = if (isSel) BrightPurple else MaterialTheme.colorScheme.onSurface
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(16.dp))

          // Description
          Text(
            text = "Description",
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = MaterialTheme.colorScheme.onSurface
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = product.description,
            fontSize = 13.sp,
            lineHeight = 20.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )

          Spacer(modifier = Modifier.height(14.dp))

          // Merchant & Pickup Hub Location in Uganda
          Surface(
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            border = BorderStroke(1.dp, LavenderContainerBorder),
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
              ) {
                Box(
                  modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(BrightPurple.copy(alpha = 0.15f)),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(
                    imageVector = Icons.Filled.LocationOn,
                    contentDescription = null,
                    tint = BrightPurple,
                    modifier = Modifier.size(20.dp)
                  )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                  Text(
                    text = "Nearby Pickup & Dispatch Hub",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface
                  )
                  Text(
                    text = product.locationName,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }
              }

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
                  Spacer(modifier = Modifier.width(3.dp))
                  Text(
                    text = "${product.distanceKm} km",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = BrightPurple
                  )
                }
              }
            }
          }

          if (product.specs.isNotEmpty()) {
            Spacer(modifier = Modifier.height(14.dp))
            Text(
              text = "Key Specifications",
              fontWeight = FontWeight.Bold,
              fontSize = 14.sp,
              color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))
            product.specs.forEach { spec ->
              Row(
                modifier = Modifier.padding(vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(
                  imageVector = Icons.Filled.CheckCircle,
                  contentDescription = null,
                  tint = SuccessGreen,
                  modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = spec,
                  fontSize = 13.sp,
                  color = MaterialTheme.colorScheme.onSurface
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(16.dp))

          // Guarantees badges
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(12.dp))
              .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
              .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceAround
          ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Icon(Icons.Filled.LocalShipping, contentDescription = null, tint = BrightPurple, modifier = Modifier.size(20.dp))
              Spacer(modifier = Modifier.height(4.dp))
              Text("Free 2-Day Delivery", fontSize = 11.sp, fontWeight = FontWeight.Medium)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Icon(Icons.Filled.Shield, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(20.dp))
              Spacer(modifier = Modifier.height(4.dp))
              Text("2-Year Warranty", fontSize = 11.sp, fontWeight = FontWeight.Medium)
            }
          }

          Spacer(modifier = Modifier.height(20.dp))
        }

        // Bottom Action Bar (elevated with navigationBarsPadding to stay fully visible above Android navigation controls)
        Surface(
          modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
          shadowElevation = 14.dp,
          tonalElevation = 4.dp,
          color = MaterialTheme.colorScheme.surface,
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 16.dp)
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              // Quantity Stepper
              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                  .clip(RoundedCornerShape(14.dp))
                  .background(MaterialTheme.colorScheme.surfaceVariant)
                  .padding(horizontal = 4.dp, vertical = 4.dp)
              ) {
                IconButton(
                  onClick = { if (quantity > 1) quantity-- },
                  modifier = Modifier.size(32.dp)
                ) {
                  Icon(Icons.Filled.Remove, contentDescription = "Decrease", modifier = Modifier.size(16.dp))
                }
                Text(
                  text = "$quantity",
                  fontWeight = FontWeight.Bold,
                  fontSize = 15.sp,
                  modifier = Modifier.padding(horizontal = 6.dp)
                )
                IconButton(
                  onClick = { quantity++ },
                  modifier = Modifier.size(32.dp)
                ) {
                  Icon(Icons.Filled.Add, contentDescription = "Increase", modifier = Modifier.size(16.dp))
                }
              }

              // Add to Cart Button (Secondary action)
              OutlinedButton(
                onClick = {
                  onAddToCart(product, quantity, selectedColor, selectedSize)
                  onDismiss()
                },
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.5.dp, BrightPurple),
                modifier = Modifier
                  .weight(0.85f)
                  .height(52.dp)
                  .testTag("add_to_cart_dialog_btn")
              ) {
                Icon(
                  imageVector = Icons.Filled.ShoppingBag,
                  contentDescription = null,
                  tint = BrightPurple,
                  modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = "Add to Cart",
                  fontWeight = FontWeight.Bold,
                  fontSize = 13.sp,
                  color = BrightPurple
                )
              }

              // Order Now Button (Primary high-visibility action)
              Button(
                onClick = {
                  onAddToCart(product, quantity, selectedColor, selectedSize)
                  onDismiss()
                },
                colors = ButtonDefaults.buttonColors(
                  containerColor = BrightPurple,
                  contentColor = Color.White
                ),
                elevation = ButtonDefaults.buttonElevation(
                  defaultElevation = 6.dp,
                  pressedElevation = 2.dp
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                  .weight(1.15f)
                  .height(52.dp)
                  .testTag("order_now_dialog_btn")
              ) {
                Icon(
                  imageVector = Icons.Filled.Bolt,
                  contentDescription = null,
                  tint = BrightYellow,
                  modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = "Order • $${String.format("%.2f", product.price * quantity)}",
                  fontWeight = FontWeight.ExtraBold,
                  fontSize = 13.sp,
                  color = Color.White
                )
              }
            }
          }
        }
      }
    }
  }
}
