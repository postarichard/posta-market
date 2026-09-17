package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import com.example.R
import com.example.data.PostaMarketUiState
import com.example.model.Order
import com.example.model.OrderStatus
import com.example.ui.theme.BrightPink
import com.example.ui.theme.BrightPurple
import com.example.ui.theme.HighlightAmber
import com.example.ui.theme.LavenderContainerBorder
import com.example.ui.theme.SuccessGreen

@Composable
fun OrdersScreen(
  uiState: PostaMarketUiState,
  onReorder: (Order) -> Unit,
  onStartShopping: () -> Unit,
  modifier: Modifier = Modifier
) {
  var selectedFilter by remember { mutableStateOf("All") }
  val filterOptions = listOf("All", "In Transit", "Delivered")

  val filteredOrders = remember(uiState.orders, selectedFilter) {
    when (selectedFilter) {
      "In Transit" -> uiState.orders.filter { it.status == OrderStatus.IN_TRANSIT || it.status == OrderStatus.PROCESSING || it.status == OrderStatus.PLACED }
      "Delivered" -> uiState.orders.filter { it.status == OrderStatus.DELIVERED }
      else -> uiState.orders
    }
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .testTag("orders_screen")
  ) {
    // Top Bar
    Surface(
      color = MaterialTheme.colorScheme.surface,
      shadowElevation = 2.dp
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 20.dp, vertical = 16.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "My Orders",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )
          Surface(
            shape = RoundedCornerShape(10.dp),
            color = BrightPurple.copy(alpha = 0.12f)
          ) {
            Text(
              text = "${uiState.orders.size} Total Orders",
              color = BrightPurple,
              fontWeight = FontWeight.Bold,
              fontSize = 11.sp,
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Filter Pills
        Row(
          modifier = Modifier.horizontalScroll(rememberScrollState()),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          filterOptions.forEach { filter ->
            val isSel = filter == selectedFilter
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(if (isSel) BrightPurple else MaterialTheme.colorScheme.surfaceVariant)
                .clickable { selectedFilter = filter }
                .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
              Text(
                text = filter,
                fontSize = 12.sp,
                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurface
              )
            }
          }
        }
      }
    }

    if (filteredOrders.isEmpty()) {
      // Empty orders state
      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Box(
          modifier = Modifier
            .size(90.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Filled.ShoppingBag,
            contentDescription = null,
            tint = BrightPurple,
            modifier = Modifier.size(42.dp)
          )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
          text = "No Orders in this category",
          fontWeight = FontWeight.Bold,
          fontSize = 18.sp,
          color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
          text = "Your recent order activity will appear here with live tracking.",
          fontSize = 13.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(20.dp))
        Button(
          onClick = onStartShopping,
          colors = ButtonDefaults.buttonColors(containerColor = BrightPurple),
          shape = RoundedCornerShape(12.dp)
        ) {
          Text("Start Shopping")
        }
      }
    } else {
      LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
      ) {
        items(filteredOrders, key = { it.id }) { order ->
          OrderCard(
            order = order,
            onReorder = { onReorder(order) }
          )
        }
      }
    }
  }
}

@Composable
fun OrderCard(
  order: Order,
  onReorder: () -> Unit,
  modifier: Modifier = Modifier
) {
  var isExpanded by remember { mutableStateOf(false) }

  Card(
    shape = RoundedCornerShape(20.dp),
    border = androidx.compose.foundation.BorderStroke(1.dp, LavenderContainerBorder),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    modifier = modifier.fillMaxWidth()
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      // Header: Order Number, Date, Status Chip
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = order.orderNumber,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = MaterialTheme.colorScheme.onSurface
          )
          Text(
            text = "Placed on ${order.date}",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }

        val statusBg = when (order.status) {
          OrderStatus.DELIVERED -> SuccessGreen.copy(alpha = 0.15f)
          OrderStatus.IN_TRANSIT -> HighlightAmber.copy(alpha = 0.15f)
          else -> BrightPurple.copy(alpha = 0.15f)
        }
        val statusText = when (order.status) {
          OrderStatus.DELIVERED -> SuccessGreen
          OrderStatus.IN_TRANSIT -> HighlightAmber
          else -> BrightPurple
        }

        Surface(
          shape = RoundedCornerShape(10.dp),
          color = statusBg
        ) {
          Text(
            text = order.status.label,
            color = statusText,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // 4-Step Progress Stepper
      OrderProgressStepper(currentStatus = order.status)

      Spacer(modifier = Modifier.height(14.dp))

      // Tracking & ETA
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(12.dp))
          .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
          .padding(10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Filled.LocalShipping, contentDescription = null, tint = BrightPurple, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = order.trackingNumber,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = BrightPurple
          )
        }
        Text(
          text = order.estimatedArrival,
          fontSize = 11.sp,
          fontWeight = FontWeight.Medium,
          color = MaterialTheme.colorScheme.onSurface
        )
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Items Thumbnails Row
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          order.items.take(3).forEach { item ->
            Box(
              modifier = Modifier
                .size(46.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
              SubcomposeAsyncImage(
                model = item.product.imageUrl,
                contentDescription = item.product.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
                error = {
                  Box(
                    modifier = Modifier.fillMaxSize().background(BrightPurple.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                  ) {
                    Icon(
                      imageVector = Icons.Filled.ShoppingBag,
                      contentDescription = item.product.name,
                      tint = BrightPurple,
                      modifier = Modifier.size(20.dp)
                    )
                  }
                }
              )
            }
          }
          if (order.items.size > 3) {
            Box(
              modifier = Modifier
                .size(46.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = "+${order.items.size - 3}",
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.primary
              )
            }
          }
        }

        Column(horizontalAlignment = Alignment.End) {
          Text(
            text = "Total Price",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Text(
            text = "$${String.format("%.2f", order.total)}",
            fontWeight = FontWeight.ExtraBold,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.primary
          )
        }
      }

      // Expandable items detail
      AnimatedVisibility(visible = isExpanded) {
        Column(modifier = Modifier.padding(top = 12.dp)) {
          Divider(color = LavenderContainerBorder)
          Spacer(modifier = Modifier.height(10.dp))
          Text("Ordered Items:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
          Spacer(modifier = Modifier.height(6.dp))
          order.items.forEach { cartItem ->
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text(
                text = "${cartItem.quantity}x ${cartItem.product.name}",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
              )
              Text(
                text = "$${String.format("%.2f", cartItem.product.price * cartItem.quantity)}",
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
              )
            }
          }
          Spacer(modifier = Modifier.height(8.dp))
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = order.deliveryAddress,
              fontSize = 11.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Bottom buttons: Expand/Details and Reorder
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          modifier = Modifier
            .clickable { isExpanded = !isExpanded }
            .padding(4.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = if (isExpanded) "Hide details" else "View items (${order.items.sumOf { it.quantity }})",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold
          )
          Icon(
            imageVector = if (isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(16.dp)
          )
        }

        OutlinedButton(
          onClick = onReorder,
          shape = RoundedCornerShape(10.dp),
          modifier = Modifier.height(34.dp),
          colors = ButtonDefaults.outlinedButtonColors(contentColor = BrightPurple)
        ) {
          Icon(Icons.Filled.Replay, contentDescription = null, modifier = Modifier.size(14.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("Reorder", fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
      }
    }
  }
}

@Composable
fun OrderProgressStepper(currentStatus: OrderStatus) {
  val steps = listOf("Placed", "Packed", "In Transit", "Delivered")
  val activeIndex = currentStatus.stepIndex

  Row(
    modifier = Modifier.fillMaxWidth(),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    steps.forEachIndexed { index, label ->
      val isDone = index <= activeIndex
      val isCurrent = index == activeIndex

      Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
          modifier = Modifier
            .size(22.dp)
            .clip(CircleShape)
            .background(if (isDone) BrightPurple else MaterialTheme.colorScheme.surfaceVariant),
          contentAlignment = Alignment.Center
        ) {
          if (isDone) {
            Icon(Icons.Filled.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
          }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = label,
          fontSize = 10.sp,
          fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
          color = if (isDone) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
        )
      }

      if (index < steps.size - 1) {
        val connectorDone = index < activeIndex
        Box(
          modifier = Modifier
            .weight(1f)
            .height(2.dp)
            .padding(horizontal = 4.dp)
            .background(if (connectorDone) BrightPurple else MaterialTheme.colorScheme.surfaceVariant)
        )
      }
    }
  }
}
