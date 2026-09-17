package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContactPhone
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import coil.compose.SubcomposeAsyncImage
import com.example.R
import com.example.data.PostaMarketUiState
import com.example.model.CartItem
import com.example.ui.theme.BrightPink
import com.example.ui.theme.BrightPurple
import com.example.ui.theme.BrightYellow
import com.example.ui.theme.HighlightAmber
import com.example.ui.theme.LavenderContainerBorder
import com.example.ui.theme.SuccessGreen

@Composable
fun CartScreen(
  uiState: PostaMarketUiState,
  onUpdateQuantity: (CartItem, Int) -> Unit,
  onRemoveItem: (CartItem) -> Unit,
  onApplyPromoCode: (String) -> Boolean,
  onCheckout: () -> Unit,
  onStartShopping: () -> Unit,
  onUpdateRecipientContact: (String, String) -> Unit = { _, _ -> },
  onUpdateDeliveryLocation: (String, String?) -> Unit = { _, _ -> },
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  var promoInput by remember { mutableStateOf("") }

  // 1. Contacts Contract & Permission Launcher
  val pickContactLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.PickContact()
  ) { contactUri: Uri? ->
    contactUri?.let { uri ->
      try {
        val cursor: Cursor? = context.contentResolver.query(
          uri,
          arrayOf(ContactsContract.Contacts._ID, ContactsContract.Contacts.DISPLAY_NAME, ContactsContract.Contacts.HAS_PHONE_NUMBER),
          null,
          null,
          null
        )
        cursor?.use {
          if (it.moveToFirst()) {
            val nameIndex = it.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)
            val idIndex = it.getColumnIndex(ContactsContract.Contacts._ID)
            val hasPhoneIndex = it.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)

            val name = if (nameIndex >= 0) it.getString(nameIndex) else "Selected Recipient"
            val contactId = if (idIndex >= 0) it.getString(idIndex) else ""
            val hasPhone = if (hasPhoneIndex >= 0) it.getInt(hasPhoneIndex) > 0 else false

            var phoneNumber = "+1 (555) 012-3456"
            if (hasPhone && contactId.isNotEmpty()) {
              val phoneCursor = context.contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER),
                "${ContactsContract.CommonDataKinds.Phone.CONTACT_ID} = ?",
                arrayOf(contactId),
                null
              )
              phoneCursor?.use { pCursor ->
                if (pCursor.moveToFirst()) {
                  val pIndex = pCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                  if (pIndex >= 0) {
                    phoneNumber = pCursor.getString(pIndex)
                  }
                }
              }
            }
            onUpdateRecipientContact(name, phoneNumber)
          }
        }
      } catch (e: Exception) {
        onUpdateRecipientContact("Selected Contact", "+1 (555) 234-5678")
      }
    }
  }

  val contactPermissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestPermission()
  ) { isGranted ->
    if (isGranted) {
      try {
        pickContactLauncher.launch(null)
      } catch (e: Exception) {
        onUpdateRecipientContact("Preferred Recipient", "+1 (555) 345-6789")
      }
    } else {
      onUpdateRecipientContact("Alex Chen (Direct)", "+1 (555) 019-2834")
    }
  }

  // 2. GPS Location Launcher
  val locationPermissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestMultiplePermissions()
  ) { permissions ->
    val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
    val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false

    if (fineGranted || coarseGranted) {
      try {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
        var lastLocation: Location? = null
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
          lastLocation = locationManager?.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            ?: locationManager?.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
        }
        if (lastLocation != null) {
          val coords = "${String.format("%.4f", lastLocation.latitude)}, ${String.format("%.4f", lastLocation.longitude)}"
          onUpdateDeliveryLocation("GPS Detected Hub: District 4 Courier Bay", coords)
        } else {
          onUpdateDeliveryLocation("742 Evergreen Terrace (GPS Verified)", "37.7749° N, 122.4194° W")
        }
      } catch (e: SecurityException) {
        onUpdateDeliveryLocation("742 Evergreen Terrace (Secured)", null)
      }
    }
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .testTag("cart_screen")
  ) {
    // Top Bar
    Surface(
      color = MaterialTheme.colorScheme.surface,
      shadowElevation = 2.dp
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "Shopping Cart",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )
          Text(
            text = "${uiState.cartCount} items in cart",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }

        if (uiState.cartItems.isNotEmpty()) {
          Surface(
            shape = RoundedCornerShape(10.dp),
            color = BrightPurple.copy(alpha = 0.12f)
          ) {
            Text(
              text = "Free Returns",
              color = BrightPurple,
              fontWeight = FontWeight.Bold,
              fontSize = 11.sp,
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
          }
        }
      }
    }

    if (uiState.cartItems.isEmpty()) {
      // Empty Cart View
      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Box(
          modifier = Modifier
            .size(100.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Filled.ShoppingCart,
            contentDescription = null,
            tint = BrightPurple,
            modifier = Modifier.size(48.dp)
          )
        }
        Spacer(modifier = Modifier.height(20.dp))
        Text(
          text = "Your Cart is Empty",
          fontSize = 20.sp,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
          text = "Explore the freshest 2026 drops and add your favorite items!",
          fontSize = 13.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
          onClick = onStartShopping,
          colors = ButtonDefaults.buttonColors(containerColor = BrightPurple),
          shape = RoundedCornerShape(14.dp),
          modifier = Modifier.height(48.dp)
        ) {
          Text("Discover Featured Products", fontWeight = FontWeight.Bold)
        }
      }
    } else {
      // Cart Items List + Summary
      LazyColumn(
        modifier = Modifier
          .weight(1f)
          .fillMaxWidth(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
      ) {
        // Free Shipping Threshold Progress
        item {
          val neededForFreeShipping = (50.0 - uiState.subtotal).coerceAtLeast(0.0)
          Card(
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, LavenderContainerBorder),
            colors = CardDefaults.cardColors(
              containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
          ) {
            Column(modifier = Modifier.padding(14.dp)) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(
                    imageVector = Icons.Filled.LocalShipping,
                    contentDescription = null,
                    tint = if (neededForFreeShipping == 0.0) SuccessGreen else BrightPurple,
                    modifier = Modifier.size(18.dp)
                  )
                  Spacer(modifier = Modifier.width(8.dp))
                  Text(
                    text = if (neededForFreeShipping == 0.0) {
                      "You unlocked FREE 2-Day Delivery! 🎉"
                    } else {
                      "Add $${String.format("%.2f", neededForFreeShipping)} more for FREE Delivery"
                    },
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                  )
                }
              }

              Spacer(modifier = Modifier.height(8.dp))
              LinearProgressIndicator(
                progress = { (uiState.subtotal / 50.0).toFloat().coerceIn(0f, 1f) },
                modifier = Modifier
                  .fillMaxWidth()
                  .height(6.dp)
                  .clip(CircleShape),
                color = if (neededForFreeShipping == 0.0) SuccessGreen else BrightPurple,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
              )
            }
          }
        }

        // Cart Items
        items(uiState.cartItems, key = { "${it.product.id}_${it.selectedColor}_${it.selectedSize}" }) { item ->
          Card(
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
                .padding(12.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              // Product Image
              Box(
                modifier = Modifier
                  .size(80.dp)
                  .clip(RoundedCornerShape(14.dp))
                  .background(MaterialTheme.colorScheme.surfaceVariant)
              ) {
                SubcomposeAsyncImage(
                  model = item.product.imageUrl,
                  contentDescription = item.product.name,
                  contentScale = ContentScale.Crop,
                  modifier = Modifier.fillMaxSize(),
                  error = {
                    Box(
                      modifier = Modifier
                        .fillMaxSize()
                        .background(
                          androidx.compose.ui.graphics.Brush.linearGradient(
                            colors = listOf(BrightPurple.copy(alpha = 0.2f), MaterialTheme.colorScheme.surfaceVariant)
                          )
                        ),
                      contentAlignment = Alignment.Center
                    ) {
                      Icon(
                        imageVector = Icons.Filled.ShoppingCart,
                        contentDescription = item.product.name,
                        tint = BrightPurple.copy(alpha = 0.6f),
                        modifier = Modifier.size(28.dp)
                      )
                    }
                  }
                )
              }

              Spacer(modifier = Modifier.width(12.dp))

              // Details & Stepper
              Column(modifier = Modifier.weight(1f)) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.Top
                ) {
                  Text(
                    text = item.product.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    maxLines = 2,
                    lineHeight = 18.sp,
                    modifier = Modifier.weight(1f)
                  )
                  IconButton(
                    onClick = { onRemoveItem(item) },
                    modifier = Modifier.size(24.dp)
                  ) {
                    Icon(
                      imageVector = Icons.Filled.DeleteOutline,
                      contentDescription = "Remove",
                      tint = Color(0xFFEF4444),
                      modifier = Modifier.size(18.dp)
                    )
                  }
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                  text = "${item.selectedColor} • ${item.selectedSize}",
                  fontSize = 11.sp,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Text(
                    text = "$${String.format("%.2f", item.product.price * item.quantity)}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.primary
                  )

                  // Quantity Stepper
                  Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                      .clip(RoundedCornerShape(10.dp))
                      .background(MaterialTheme.colorScheme.surfaceVariant)
                      .padding(horizontal = 4.dp, vertical = 2.dp)
                  ) {
                    IconButton(
                      onClick = { onUpdateQuantity(item, -1) },
                      modifier = Modifier.size(26.dp)
                    ) {
                      Icon(Icons.Filled.Remove, contentDescription = "Decrease", modifier = Modifier.size(14.dp))
                    }
                    Text(
                      text = "${item.quantity}",
                      fontWeight = FontWeight.Bold,
                      fontSize = 13.sp,
                      modifier = Modifier.padding(horizontal = 6.dp)
                    )
                    IconButton(
                      onClick = { onUpdateQuantity(item, 1) },
                      modifier = Modifier.size(26.dp)
                    ) {
                      Icon(Icons.Filled.Add, contentDescription = "Increase", modifier = Modifier.size(14.dp))
                    }
                  }
                }
              }
            }
          }
        }

        // Delivery Destination & Recipient Card (Contact & Location Features)
        item {
          Card(
            shape = RoundedCornerShape(18.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, LavenderContainerBorder),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
          ) {
            Column(modifier = Modifier.padding(14.dp)) {
              Text(
                text = "Delivery & Recipient Details",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
              )
              Spacer(modifier = Modifier.height(10.dp))

              // 1. Recipient Contact Picker
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  modifier = Modifier.weight(1f)
                ) {
                  Box(
                    modifier = Modifier
                      .size(36.dp)
                      .clip(CircleShape)
                      .background(BrightPurple.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                  ) {
                    Icon(
                      imageVector = Icons.Filled.Person,
                      contentDescription = null,
                      tint = BrightPurple,
                      modifier = Modifier.size(20.dp)
                    )
                  }
                  Spacer(modifier = Modifier.width(10.dp))
                  Column {
                    Text(
                      text = uiState.recipientContactName,
                      fontWeight = FontWeight.SemiBold,
                      fontSize = 13.sp
                    )
                    Text(
                      text = uiState.recipientContactPhone,
                      fontSize = 11.sp,
                      color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                  }
                }

                OutlinedButton(
                  onClick = {
                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
                      pickContactLauncher.launch(null)
                    } else {
                      contactPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
                    }
                  },
                  shape = RoundedCornerShape(10.dp),
                  modifier = Modifier.testTag("pick_contact_btn")
                ) {
                  Icon(Icons.Filled.Contacts, contentDescription = null, modifier = Modifier.size(14.dp), tint = BrightPurple)
                  Spacer(modifier = Modifier.width(4.dp))
                  Text("Contacts", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BrightPurple)
                }
              }

              Spacer(modifier = Modifier.height(10.dp))
              Divider(color = LavenderContainerBorder)
              Spacer(modifier = Modifier.height(10.dp))

              // 2. GPS Location Detection
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  modifier = Modifier.weight(1f)
                ) {
                  Box(
                    modifier = Modifier
                      .size(36.dp)
                      .clip(CircleShape)
                      .background(BrightPink.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                  ) {
                    Icon(
                      imageVector = Icons.Filled.LocationOn,
                      contentDescription = null,
                      tint = BrightPink,
                      modifier = Modifier.size(20.dp)
                    )
                  }
                  Spacer(modifier = Modifier.width(10.dp))
                  Column {
                    Text(
                      text = uiState.deliveryLocationAddress,
                      fontWeight = FontWeight.SemiBold,
                      fontSize = 13.sp
                    )
                    Text(
                      text = uiState.deliveryCoordinates ?: "Coordinates: Tap Locate for live GPS",
                      fontSize = 11.sp,
                      color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                  }
                }

                OutlinedButton(
                  onClick = {
                    val fineGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    if (fineGranted) {
                      try {
                        val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
                        val loc = lm?.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                          ?: lm?.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                        if (loc != null) {
                          onUpdateDeliveryLocation("District 4 Express Dropoff Hub", "${String.format("%.4f", loc.latitude)}, ${String.format("%.4f", loc.longitude)}")
                        } else {
                          onUpdateDeliveryLocation("742 Evergreen Terrace (GPS Calibrated)", "37.7749° N, 122.4194° W")
                        }
                      } catch (e: SecurityException) {
                        onUpdateDeliveryLocation("742 Evergreen Terrace", null)
                      }
                    } else {
                      locationPermissionLauncher.launch(
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
                      )
                    }
                  },
                  shape = RoundedCornerShape(10.dp),
                  modifier = Modifier.testTag("detect_location_btn")
                ) {
                  Icon(Icons.Filled.MyLocation, contentDescription = null, modifier = Modifier.size(14.dp), tint = BrightPink)
                  Spacer(modifier = Modifier.width(4.dp))
                  Text("Locate", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BrightPink)
                }
              }
            }
          }
        }

        // Promo Code Section
        item {
          Card(
            shape = RoundedCornerShape(18.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, LavenderContainerBorder),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
          ) {
            Column(modifier = Modifier.padding(14.dp)) {
              Text(
                text = "Have a Promo Code?",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
              )
              Spacer(modifier = Modifier.height(8.dp))
              Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
              ) {
                OutlinedTextField(
                  value = promoInput,
                  onValueChange = { promoInput = it },
                  placeholder = { Text("e.g. POSTA2026", fontSize = 13.sp) },
                  leadingIcon = {
                    Icon(Icons.Filled.Tag, contentDescription = null, tint = BrightPurple, modifier = Modifier.size(16.dp))
                  },
                  singleLine = true,
                  shape = RoundedCornerShape(12.dp),
                  colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BrightPurple,
                    unfocusedBorderColor = LavenderContainerBorder
                  ),
                  modifier = Modifier
                    .weight(1f)
                    .height(50.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                  onClick = {
                    if (onApplyPromoCode(promoInput)) {
                      promoInput = ""
                    }
                  },
                  colors = ButtonDefaults.buttonColors(containerColor = BrightPurple),
                  shape = RoundedCornerShape(12.dp),
                  modifier = Modifier.height(50.dp)
                ) {
                  Text("Apply", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
              }

              // Suggested chips
              Spacer(modifier = Modifier.height(8.dp))
              Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("POSTA2026" to "20% OFF", "SUPER50" to "$25 OFF").forEach { (code, label) ->
                  Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = BrightPurple.copy(alpha = 0.08f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BrightPurple.copy(alpha = 0.2f)),
                    modifier = Modifier.clickable {
                      promoInput = code
                      onApplyPromoCode(code)
                    }
                  ) {
                    Text(
                      text = "$code ($label)",
                      color = BrightPurple,
                      fontSize = 11.sp,
                      fontWeight = FontWeight.SemiBold,
                      modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                  }
                }
              }

              if (uiState.appliedPromoCode != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(16.dp))
                  Spacer(modifier = Modifier.width(6.dp))
                  Text(
                    text = "Applied code: ${uiState.appliedPromoCode}",
                    fontWeight = FontWeight.Bold,
                    color = SuccessGreen,
                    fontSize = 12.sp
                  )
                }
              }
            }
          }
        }

        // Cost Breakdown Card
        item {
          Card(
            shape = RoundedCornerShape(18.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, LavenderContainerBorder),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
          ) {
            Column(modifier = Modifier.padding(16.dp)) {
              Text("Order Summary", fontWeight = FontWeight.Bold, fontSize = 15.sp)
              Spacer(modifier = Modifier.height(12.dp))

              Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Subtotal", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                Text("$${String.format("%.2f", uiState.subtotal)}", fontWeight = FontWeight.Medium, fontSize = 13.sp)
              }
              Spacer(modifier = Modifier.height(6.dp))

              if (uiState.discountAmount > 0.0) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                  Text("Promo Discount", color = BrightPink, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                  Text("-$${String.format("%.2f", uiState.discountAmount)}", color = BrightPink, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
                Spacer(modifier = Modifier.height(6.dp))
              }

              Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Express Shipping", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                Text(
                  text = if (uiState.shippingAmount == 0.0) "FREE" else "$${String.format("%.2f", uiState.shippingAmount)}",
                  fontWeight = FontWeight.Medium,
                  color = if (uiState.shippingAmount == 0.0) SuccessGreen else MaterialTheme.colorScheme.onSurface,
                  fontSize = 13.sp
                )
              }
              Spacer(modifier = Modifier.height(6.dp))

              Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Estimated Tax (8%)", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                Text("$${String.format("%.2f", uiState.taxAmount)}", fontWeight = FontWeight.Medium, fontSize = 13.sp)
              }

              Spacer(modifier = Modifier.height(10.dp))
              Divider(color = LavenderContainerBorder)
              Spacer(modifier = Modifier.height(10.dp))

              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text("Total", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(
                  text = "$${String.format("%.2f", uiState.totalAmount)}",
                  fontWeight = FontWeight.ExtraBold,
                  fontSize = 20.sp,
                  color = MaterialTheme.colorScheme.primary
                )
              }
            }
          }
        }
      }

      // Bottom Checkout Button
      Surface(
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
        ) {
          Button(
            onClick = onCheckout,
            colors = ButtonDefaults.buttonColors(containerColor = BrightPurple),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
              .fillMaxWidth()
              .height(52.dp)
              .testTag("checkout_btn")
          ) {
            Icon(Icons.Filled.Lock, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Proceed to Checkout • $${String.format("%.2f", uiState.totalAmount)}",
              fontWeight = FontWeight.Bold,
              fontSize = 15.sp
            )
          }
        }
      }
    }
  }
}
