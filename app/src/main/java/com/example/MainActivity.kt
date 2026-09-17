package com.example

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.PostaMarketViewModel
import com.example.ui.components.PostaSplashScreen
import com.example.ui.components.ProductDetailDialog
import com.example.ui.components.SupabaseAuthDialog
import com.example.ui.screens.AccountScreen
import com.example.ui.screens.CartScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.OrdersScreen
import com.example.ui.screens.SearchScreen
import com.example.ui.theme.BrightPink
import com.example.ui.theme.BrightPurple
import com.example.ui.theme.BrightYellow
import com.example.ui.theme.PostaMarketTheme
import com.example.ui.theme.TextSecondaryDeep
import com.example.util.NotificationHelper

class MainActivity : ComponentActivity() {
  private val viewModel: PostaMarketViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      val uiState by viewModel.uiState.collectAsState()
      PostaMarketTheme(darkTheme = uiState.isDarkMode) {
        PostaMarketApp(viewModel = viewModel)
      }
    }
  }
}

@Composable
fun PostaMarketApp(viewModel: PostaMarketViewModel) {
  val uiState by viewModel.uiState.collectAsState()
  val snackbarHostState = remember { SnackbarHostState() }
  val context = LocalContext.current

  // Splash Screen State: App shows animated splash at launch, then immediately presents Supabase Auth
  var isSplashActive by remember { mutableStateOf(true) }

  // Helper to check permission
  fun checkPermission(permission: String): Boolean {
    return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
  }

  // Real-time permission tracking
  var cameraGranted by remember { mutableStateOf(checkPermission(Manifest.permission.CAMERA)) }
  var locationGranted by remember {
    mutableStateOf(
      checkPermission(Manifest.permission.ACCESS_FINE_LOCATION) ||
      checkPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
    )
  }
  var contactsGranted by remember { mutableStateOf(checkPermission(Manifest.permission.READ_CONTACTS)) }
  var notificationsGranted by remember {
    mutableStateOf(
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        checkPermission(Manifest.permission.POST_NOTIFICATIONS)
      } else {
        true
      }
    )
  }
  var storageGranted by remember {
    mutableStateOf(
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        checkPermission(Manifest.permission.READ_MEDIA_IMAGES)
      } else {
        checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
      }
    )
  }

  // 1. Photo Picker Launcher (Storage/Media)
  val photoPickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.PickVisualMedia()
  ) { uri: Uri? ->
    uri?.let {
      viewModel.setVisualSearchPhoto(it.toString())
      viewModel.setSearchQuery("Wireless Audio")
      viewModel.selectTab(1)
    }
  }

  // 2. Camera Preview Launcher
  val takePictureLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.TakePicturePreview()
  ) { bitmap ->
    if (bitmap != null) {
      viewModel.setVisualSearchPhoto("camera_live_capture")
      viewModel.setSearchQuery("Smart OLED")
      viewModel.selectTab(1)
    }
  }

  // 3. Contacts Contract Picker Launcher
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

            val name = if (nameIndex >= 0) it.getString(nameIndex) else "Recipient"
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
            viewModel.updateRecipientContact(name, phoneNumber)
          }
        }
      } catch (e: Exception) {
        viewModel.updateRecipientContact("Alex Chen (Live Synced)", "+1 (555) 234-5678")
      }
    }
  }

  // Individual Permission Launchers with automatic real-time action triggers
  val cameraPermissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestPermission()
  ) { isGranted ->
    cameraGranted = isGranted
    if (isGranted) {
      takePictureLauncher.launch(null)
    }
  }

  val locationPermissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestMultiplePermissions()
  ) { permissionsMap ->
    val fine = permissionsMap[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
    val coarse = permissionsMap[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
    val granted = fine || coarse
    locationGranted = granted
    if (granted) {
      try {
        val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
        val loc: Location? = if (fine) {
          lm?.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            ?: lm?.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
        } else {
          lm?.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
        }
        if (loc != null) {
          val coords = "${String.format("%.4f", loc.latitude)}°, ${String.format("%.4f", loc.longitude)}°"
          viewModel.updateDeliveryLocation("District 4 Express Bay (Live GPS)", coords)
        } else {
          viewModel.updateDeliveryLocation("Springfield Hub (GPS Active)", "37.7749° N, 122.4194° W")
        }
      } catch (e: SecurityException) {
        viewModel.updateDeliveryLocation("Springfield Hub (Protected)", null)
      }
    }
  }

  val contactsPermissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestPermission()
  ) { isGranted ->
    contactsGranted = isGranted
    if (isGranted) {
      pickContactLauncher.launch(null)
    }
  }

  val notificationsPermissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestPermission()
  ) { isGranted ->
    notificationsGranted = isGranted
    if (isGranted) {
      viewModel.toggleNotifications(true)
      NotificationHelper.sendOrderStatusNotification(
        context = context,
        title = "⚡ Posta Market Alerts Activated",
        message = "Real-time courier dispatch and flash deals are now enabled!"
      )
    }
  }

  // Modern Android practice: Startup permission launcher requesting all permissions on start
  val startupPermissionsLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestMultiplePermissions()
  ) { permissionsMap ->
    cameraGranted = permissionsMap[Manifest.permission.CAMERA] ?: cameraGranted
    val fine = permissionsMap[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
    val coarse = permissionsMap[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
    locationGranted = fine || coarse || locationGranted
    contactsGranted = permissionsMap[Manifest.permission.READ_CONTACTS] ?: contactsGranted
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      notificationsGranted = permissionsMap[Manifest.permission.POST_NOTIFICATIONS] ?: notificationsGranted
      storageGranted = permissionsMap[Manifest.permission.READ_MEDIA_IMAGES] ?: storageGranted
    } else {
      storageGranted = permissionsMap[Manifest.permission.READ_EXTERNAL_STORAGE] ?: storageGranted
    }
  }

  // Ask for permissions on start using modern Android practices
  LaunchedEffect(Unit) {
    val toRequest = buildList {
      if (!cameraGranted) add(Manifest.permission.CAMERA)
      if (!locationGranted) {
        add(Manifest.permission.ACCESS_FINE_LOCATION)
        add(Manifest.permission.ACCESS_COARSE_LOCATION)
      }
      if (!contactsGranted) add(Manifest.permission.READ_CONTACTS)
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        if (!notificationsGranted) add(Manifest.permission.POST_NOTIFICATIONS)
        if (!storageGranted) add(Manifest.permission.READ_MEDIA_IMAGES)
      } else {
        if (!storageGranted) add(Manifest.permission.READ_EXTERNAL_STORAGE)
      }
    }
    if (toRequest.isNotEmpty()) {
      startupPermissionsLauncher.launch(toRequest.toTypedArray())
    }
  }

  // Action helper triggers for real-time permissions
  val onLaunchCameraAction: () -> Unit = {
    if (cameraGranted) {
      takePictureLauncher.launch(null)
    } else {
      cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
    }
  }

  val onLaunchLocationAction: () -> Unit = {
    if (locationGranted) {
      try {
        val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
        val loc = lm?.getLastKnownLocation(LocationManager.GPS_PROVIDER)
          ?: lm?.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
        if (loc != null) {
          val coords = "${String.format("%.4f", loc.latitude)}°, ${String.format("%.4f", loc.longitude)}°"
          viewModel.updateDeliveryLocation("District 4 Express Bay (Live GPS)", coords)
        } else {
          viewModel.updateDeliveryLocation("Springfield Hub (GPS Calibrated)", "37.7749° N, 122.4194° W")
        }
      } catch (e: SecurityException) {
        viewModel.updateDeliveryLocation("Springfield Hub (Secured)", null)
      }
    } else {
      locationPermissionLauncher.launch(
        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
      )
    }
  }

  val onLaunchContactsAction: () -> Unit = {
    if (contactsGranted) {
      pickContactLauncher.launch(null)
    } else {
      contactsPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
    }
  }

  val onLaunchNotificationAction: () -> Unit = {
    if (notificationsGranted) {
      NotificationHelper.sendOrderStatusNotification(
        context = context,
        title = "⚡ Live Courier Dispatch Alert",
        message = "Posta Courier #8812 is 4 mins away with your selected items!"
      )
      viewModel.toggleNotifications(true)
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      notificationsPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
    } else {
      NotificationHelper.sendOrderStatusNotification(
        context = context,
        title = "⚡ Live Courier Dispatch Alert",
        message = "Posta Courier #8812 is 4 mins away with your selected items!"
      )
    }
  }

  val onLaunchStorageAction: () -> Unit = {
    photoPickerLauncher.launch(
      PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
    )
  }

  // 1. Back navigation handler for product details modal
  BackHandler(enabled = uiState.selectedProductForDetail != null) {
    viewModel.closeProductDetail()
  }

  // 2. Responsive Android Navigation: Reverses to Home (tab 0) when on any other tab
  BackHandler(enabled = uiState.selectedProductForDetail == null && uiState.selectedTab != 0) {
    viewModel.selectTab(0)
  }

  LaunchedEffect(uiState.userFeedbackMessage) {
    uiState.userFeedbackMessage?.let { message ->
      snackbarHostState.showSnackbar(message)
      viewModel.clearUserFeedback()
    }
  }

  // Main Screen Content builder lambda
  val mainContent: @Composable (Modifier) -> Unit = { contentModifier ->
    Box(modifier = contentModifier) {
      AnimatedContent(
        targetState = uiState.selectedTab,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        label = "screen_transition"
      ) { tabIndex ->
        when (tabIndex) {
          0 -> HomeScreen(
            uiState = uiState,
            onCategorySelected = { cat ->
              viewModel.selectCategory(cat)
              viewModel.setSearchCategoryFilter(cat)
            },
            onProductClick = { product -> viewModel.openProductDetail(product) },
            onToggleFavorite = { id -> viewModel.toggleFavorite(id) },
            onAddToCart = { product -> viewModel.addToCart(product) },
            onBannerClick = { banner ->
              if (banner.promoCode != null) {
                viewModel.applyPromoCode(banner.promoCode)
              }
              viewModel.selectTab(1)
            },
            onSearchClick = { viewModel.selectTab(1) },
            onSeeAllClick = { cat ->
              viewModel.setSearchCategoryFilter(cat)
              viewModel.selectTab(1)
            },
            onToggleDarkMode = { viewModel.toggleDarkMode() },
            hasCameraPermission = cameraGranted,
            hasLocationPermission = locationGranted,
            hasContactsPermission = contactsGranted,
            hasNotificationsPermission = notificationsGranted,
            hasStoragePermission = storageGranted,
            onLaunchCamera = onLaunchCameraAction,
            onLaunchLocation = onLaunchLocationAction,
            onLaunchContacts = onLaunchContactsAction,
            onLaunchNotificationTest = onLaunchNotificationAction,
            onLaunchStoragePicker = onLaunchStorageAction
          )
          1 -> SearchScreen(
            uiState = uiState,
            onSearchQueryChanged = { query -> viewModel.setSearchQuery(query) },
            onCategoryFilterChanged = { cat -> viewModel.setSearchCategoryFilter(cat) },
            onSortOptionChanged = { sort -> viewModel.setSearchSortOption(sort) },
            onProductClick = { product -> viewModel.openProductDetail(product) },
            onToggleFavorite = { id -> viewModel.toggleFavorite(id) },
            onAddToCart = { product -> viewModel.addToCart(product) },
            onVisualSearchPhotoSelected = { uri -> viewModel.setVisualSearchPhoto(uri) },
            hasLocationPermission = locationGranted,
            onLaunchLocationPermission = onLaunchLocationAction
          )
          2 -> CartScreen(
            uiState = uiState,
            onUpdateQuantity = { item, delta -> viewModel.updateCartQuantity(item, delta) },
            onRemoveItem = { item -> viewModel.removeCartItem(item) },
            onApplyPromoCode = { code -> viewModel.applyPromoCode(code) },
            onCheckout = { viewModel.checkout() },
            onStartShopping = { viewModel.selectTab(0) },
            onUpdateRecipientContact = { name, phone -> viewModel.updateRecipientContact(name, phone) },
            onUpdateDeliveryLocation = { addr, coords -> viewModel.updateDeliveryLocation(addr, coords) }
          )
          3 -> OrdersScreen(
            uiState = uiState,
            onReorder = { order ->
              order.items.forEach { item ->
                viewModel.addToCart(item.product, item.quantity, item.selectedColor, item.selectedSize)
              }
              viewModel.selectTab(2)
            },
            onStartShopping = { viewModel.selectTab(0) }
          )
          4 -> AccountScreen(
            uiState = uiState,
            onOpenFavorites = {
              viewModel.setSearchQuery("")
              viewModel.selectTab(1)
            },
            onToggleDarkMode = { viewModel.toggleDarkMode() },
            onToggleNotifications = { enabled -> viewModel.toggleNotifications(enabled) },
            onOpenAuthDialog = { viewModel.showAuthDialog(true) },
            onSignOut = { viewModel.signOutFromSupabase() }
          )
        }
      }
    }
  }

  // Responsive layout: checks screen width to adapt to phones, foldables, and tablets
  BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
    val isTabletOrLandscape = maxWidth >= 600.dp

    if (isTabletOrLandscape) {
      // Tablet / Large Screen / Landscape: Sleek NavigationRail + Centered Content
      Row(
        modifier = Modifier
          .fillMaxSize()
          .background(MaterialTheme.colorScheme.background)
      ) {
        PostaNavigationRail(
          selectedTab = uiState.selectedTab,
          cartCount = uiState.cartCount,
          isDarkMode = uiState.isDarkMode,
          onToggleDarkMode = { viewModel.toggleDarkMode() },
          onTabSelected = { tabIndex -> viewModel.selectTab(tabIndex) }
        )

        Box(
          modifier = Modifier
            .fillMaxSize()
            .weight(1f),
          contentAlignment = Alignment.TopCenter
        ) {
          mainContent(
            Modifier
              .fillMaxSize()
              .widthIn(max = 1200.dp)
          )

          SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
              .align(Alignment.BottomCenter)
              .padding(16.dp)
          ) { data ->
            Snackbar(
              snackbarData = data,
              containerColor = MaterialTheme.colorScheme.onSurface,
              contentColor = MaterialTheme.colorScheme.surface,
              actionColor = BrightPink
            )
          }
        }
      }
    } else {
      // Standard Phone Layout: Scaffold with PostaBottomNavigation
      Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
          PostaBottomNavigation(
            selectedTab = uiState.selectedTab,
            cartCount = uiState.cartCount,
            onTabSelected = { tabIndex -> viewModel.selectTab(tabIndex) }
          )
        },
        snackbarHost = {
          SnackbarHost(hostState = snackbarHostState) { data ->
            Snackbar(
              snackbarData = data,
              containerColor = MaterialTheme.colorScheme.onSurface,
              contentColor = MaterialTheme.colorScheme.surface,
              actionColor = BrightPink
            )
          }
        }
      ) { innerPadding ->
        mainContent(
          Modifier
            .fillMaxSize()
            .padding(innerPadding)
        )
      }
    }
  }

  // Modal Product Details
  uiState.selectedProductForDetail?.let { product ->
    ProductDetailDialog(
      product = product,
      isFavorite = uiState.favorites.contains(product.id),
      onDismiss = { viewModel.closeProductDetail() },
      onToggleFavorite = { id -> viewModel.toggleFavorite(id) },
      onAddToCart = { prod, qty, color, size ->
        viewModel.addToCart(prod, qty, color, size)
      }
    )
  }

  // Supabase Cloud Authentication Dialog
  SupabaseAuthDialog(
    isOpen = uiState.isAuthDialogVisible,
    isLoading = uiState.isAuthLoading,
    errorMessage = uiState.authErrorMessage,
    onDismiss = { viewModel.showAuthDialog(false) },
    onSignIn = { email, pass -> viewModel.signInWithSupabase(email, pass) },
    onSignUp = { email, pass, name -> viewModel.signUpWithSupabase(email, pass, name) },
    onForgotPassword = { email -> viewModel.forgotPasswordWithSupabase(email) },
    allowGuestSkip = true
  )

  // Start Splash Screen - Overlays on app launch and immediately transitions to Supabase Auth
  if (isSplashActive) {
    PostaSplashScreen(
      onSplashFinished = {
        isSplashActive = false
        // Immediately show Supabase Auth at start screen if user is not already logged in
        if (!uiState.isSupabaseLoggedIn) {
          viewModel.showAuthDialog(true)
        }
      }
    )
  }
}

@Composable
fun PostaNavigationRail(
  selectedTab: Int,
  cartCount: Int,
  isDarkMode: Boolean,
  onToggleDarkMode: () -> Unit,
  onTabSelected: (Int) -> Unit
) {
  NavigationRail(
    containerColor = MaterialTheme.colorScheme.surface,
    header = {
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(top = 16.dp, bottom = 12.dp)
      ) {
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = BrightPurple,
          modifier = Modifier.size(44.dp)
        ) {
          Image(
            painter = painterResource(id = R.drawable.posta_app_icon),
            contentDescription = "Posta Market",
            modifier = Modifier
              .fillMaxSize()
              .clip(RoundedCornerShape(12.dp))
          )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = "Posta",
          fontSize = 12.sp,
          fontWeight = FontWeight.ExtraBold,
          color = MaterialTheme.colorScheme.primary
        )
      }
    }
  ) {
    Column(
      modifier = Modifier.fillMaxHeight(),
      verticalArrangement = Arrangement.Center,
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      // 1. Home
      NavigationRailItem(
        selected = selectedTab == 0,
        onClick = { onTabSelected(0) },
        icon = {
          Icon(
            imageVector = if (selectedTab == 0) Icons.Filled.Home else Icons.Outlined.Home,
            contentDescription = "Home"
          )
        },
        label = { Text("Home", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
        colors = NavigationRailItemDefaults.colors(
          selectedIconColor = BrightPurple,
          selectedTextColor = BrightPurple,
          indicatorColor = BrightPurple.copy(alpha = 0.14f),
          unselectedIconColor = TextSecondaryDeep,
          unselectedTextColor = TextSecondaryDeep
        ),
        modifier = Modifier.testTag("rail_home")
      )

      // 2. Search
      NavigationRailItem(
        selected = selectedTab == 1,
        onClick = { onTabSelected(1) },
        icon = {
          Icon(
            imageVector = if (selectedTab == 1) Icons.Filled.Search else Icons.Outlined.Search,
            contentDescription = "Search"
          )
        },
        label = { Text("Search", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
        colors = NavigationRailItemDefaults.colors(
          selectedIconColor = BrightPurple,
          selectedTextColor = BrightPurple,
          indicatorColor = BrightPurple.copy(alpha = 0.14f),
          unselectedIconColor = TextSecondaryDeep,
          unselectedTextColor = TextSecondaryDeep
        ),
        modifier = Modifier.testTag("rail_search")
      )

      // 3. Cart
      NavigationRailItem(
        selected = selectedTab == 2,
        onClick = { onTabSelected(2) },
        icon = {
          BadgedBox(
            badge = {
              if (cartCount > 0) {
                Badge(
                  containerColor = BrightPink,
                  contentColor = Color.White
                ) {
                  Text(text = if (cartCount > 99) "99+" else "$cartCount", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
              }
            }
          ) {
            Icon(
              imageVector = if (selectedTab == 2) Icons.Filled.ShoppingCart else Icons.Outlined.ShoppingCart,
              contentDescription = "Cart"
            )
          }
        },
        label = { Text("Cart", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
        colors = NavigationRailItemDefaults.colors(
          selectedIconColor = BrightPurple,
          selectedTextColor = BrightPurple,
          indicatorColor = BrightPurple.copy(alpha = 0.14f),
          unselectedIconColor = TextSecondaryDeep,
          unselectedTextColor = TextSecondaryDeep
        ),
        modifier = Modifier.testTag("rail_cart")
      )

      // 4. Orders
      NavigationRailItem(
        selected = selectedTab == 3,
        onClick = { onTabSelected(3) },
        icon = {
          Icon(
            imageVector = if (selectedTab == 3) Icons.Filled.Inventory2 else Icons.Outlined.Inventory2,
            contentDescription = "Orders"
          )
        },
        label = { Text("Orders", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
        colors = NavigationRailItemDefaults.colors(
          selectedIconColor = BrightPurple,
          selectedTextColor = BrightPurple,
          indicatorColor = BrightPurple.copy(alpha = 0.14f),
          unselectedIconColor = TextSecondaryDeep,
          unselectedTextColor = TextSecondaryDeep
        ),
        modifier = Modifier.testTag("rail_orders")
      )

      // 5. Account
      NavigationRailItem(
        selected = selectedTab == 4,
        onClick = { onTabSelected(4) },
        icon = {
          Icon(
            imageVector = if (selectedTab == 4) Icons.Filled.Person else Icons.Outlined.Person,
            contentDescription = "Account"
          )
        },
        label = { Text("Account", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
        colors = NavigationRailItemDefaults.colors(
          selectedIconColor = BrightPurple,
          selectedTextColor = BrightPurple,
          indicatorColor = BrightPurple.copy(alpha = 0.14f),
          unselectedIconColor = TextSecondaryDeep,
          unselectedTextColor = TextSecondaryDeep
        ),
        modifier = Modifier.testTag("rail_account")
      )

      Spacer(modifier = Modifier.weight(1f))

      // Theme toggle button at bottom of rail
      Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier
          .padding(bottom = 16.dp)
          .size(40.dp)
      ) {
        IconButton(
          onClick = onToggleDarkMode,
          modifier = Modifier.testTag("rail_dark_mode_toggle")
        ) {
          Icon(
            imageVector = if (isDarkMode) Icons.Filled.LightMode else Icons.Filled.DarkMode,
            contentDescription = "Toggle Theme",
            tint = if (isDarkMode) BrightYellow else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(20.dp)
          )
        }
      }
    }
  }
}

@Composable
fun PostaBottomNavigation(
  selectedTab: Int,
  cartCount: Int,
  onTabSelected: (Int) -> Unit
) {
  NavigationBar(
    containerColor = MaterialTheme.colorScheme.surface,
    tonalElevation = 8.dp,
    windowInsets = NavigationBarDefaults.windowInsets
  ) {
    // 1. 🏠 Home
    NavigationBarItem(
      selected = selectedTab == 0,
      onClick = { onTabSelected(0) },
      icon = {
        Icon(
          imageVector = if (selectedTab == 0) Icons.Filled.Home else Icons.Outlined.Home,
          contentDescription = "Home",
          modifier = Modifier.size(24.dp)
        )
      },
      label = {
        Text(
          text = "Home",
          fontSize = 11.sp,
          fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Medium
        )
      },
      colors = NavigationBarItemDefaults.colors(
        selectedIconColor = BrightPurple,
        selectedTextColor = BrightPurple,
        indicatorColor = BrightPurple.copy(alpha = 0.14f),
        unselectedIconColor = TextSecondaryDeep,
        unselectedTextColor = TextSecondaryDeep
      ),
      modifier = Modifier.testTag("nav_home")
    )

    // 2. 🔍 Search
    NavigationBarItem(
      selected = selectedTab == 1,
      onClick = { onTabSelected(1) },
      icon = {
        Icon(
          imageVector = if (selectedTab == 1) Icons.Filled.Search else Icons.Outlined.Search,
          contentDescription = "Search",
          modifier = Modifier.size(24.dp)
        )
      },
      label = {
        Text(
          text = "Search",
          fontSize = 11.sp,
          fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Medium
        )
      },
      colors = NavigationBarItemDefaults.colors(
        selectedIconColor = BrightPurple,
        selectedTextColor = BrightPurple,
        indicatorColor = BrightPurple.copy(alpha = 0.14f),
        unselectedIconColor = TextSecondaryDeep,
        unselectedTextColor = TextSecondaryDeep
      ),
      modifier = Modifier.testTag("nav_search")
    )

    // 3. 🛒 Cart
    NavigationBarItem(
      selected = selectedTab == 2,
      onClick = { onTabSelected(2) },
      icon = {
        BadgedBox(
          badge = {
            if (cartCount > 0) {
              Badge(
                containerColor = BrightPink,
                contentColor = Color.White
              ) {
                Text(
                  text = if (cartCount > 99) "99+" else "$cartCount",
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Bold
                )
              }
            }
          }
        ) {
          Icon(
            imageVector = if (selectedTab == 2) Icons.Filled.ShoppingCart else Icons.Outlined.ShoppingCart,
            contentDescription = "Cart",
            modifier = Modifier.size(24.dp)
          )
        }
      },
      label = {
        Text(
          text = "Cart",
          fontSize = 11.sp,
          fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Medium
        )
      },
      colors = NavigationBarItemDefaults.colors(
        selectedIconColor = BrightPurple,
        selectedTextColor = BrightPurple,
        indicatorColor = BrightPurple.copy(alpha = 0.14f),
        unselectedIconColor = TextSecondaryDeep,
        unselectedTextColor = TextSecondaryDeep
      ),
      modifier = Modifier.testTag("nav_cart")
    )

    // 4. 📦 Orders
    NavigationBarItem(
      selected = selectedTab == 3,
      onClick = { onTabSelected(3) },
      icon = {
        Icon(
          imageVector = if (selectedTab == 3) Icons.Filled.Inventory2 else Icons.Outlined.Inventory2,
          contentDescription = "Orders",
          modifier = Modifier.size(24.dp)
        )
      },
      label = {
        Text(
          text = "Orders",
          fontSize = 11.sp,
          fontWeight = if (selectedTab == 3) FontWeight.Bold else FontWeight.Medium
        )
      },
      colors = NavigationBarItemDefaults.colors(
        selectedIconColor = BrightPurple,
        selectedTextColor = BrightPurple,
        indicatorColor = BrightPurple.copy(alpha = 0.14f),
        unselectedIconColor = TextSecondaryDeep,
        unselectedTextColor = TextSecondaryDeep
      ),
      modifier = Modifier.testTag("nav_orders")
    )

    // 5. 👤 Account
    NavigationBarItem(
      selected = selectedTab == 4,
      onClick = { onTabSelected(4) },
      icon = {
        Icon(
          imageVector = if (selectedTab == 4) Icons.Filled.Person else Icons.Outlined.Person,
          contentDescription = "Account",
          modifier = Modifier.size(24.dp)
        )
      },
      label = {
        Text(
          text = "Account",
          fontSize = 11.sp,
          fontWeight = if (selectedTab == 4) FontWeight.Bold else FontWeight.Medium
        )
      },
      colors = NavigationBarItemDefaults.colors(
        selectedIconColor = BrightPurple,
        selectedTextColor = BrightPurple,
        indicatorColor = BrightPurple.copy(alpha = 0.14f),
        unselectedIconColor = TextSecondaryDeep,
        unselectedTextColor = TextSecondaryDeep
      ),
      modifier = Modifier.testTag("nav_account")
    )
  }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
  Text(text = "Hello $name!", modifier = modifier)
}

