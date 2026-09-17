package com.example.data

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.supabase.AuthResult
import com.example.data.supabase.SupabaseAuthManager
import com.example.data.supabase.SupabaseUser
import com.example.model.CartItem
import com.example.model.Order
import com.example.model.OrderStatus
import com.example.model.Product
import com.example.model.ProductCategory
import com.example.model.SampleData
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class SortOption(val displayName: String) {
  POPULAR("Popularity"),
  PRICE_LOW("Price: Low to High"),
  PRICE_HIGH("Price: High to Low"),
  RATING("Customer Rating")
}

data class PostaMarketUiState(
  val selectedTab: Int = 0, // 0: Home, 1: Search, 2: Cart, 3: Orders, 4: Account
  val selectedCategory: ProductCategory = ProductCategory.ALL,
  val searchQuery: String = "",
  val searchCategoryFilter: ProductCategory = ProductCategory.ALL,
  val searchSortOption: SortOption = SortOption.POPULAR,
  val products: List<Product> = SampleData.products,
  val favorites: Set<String> = setOf("p_1", "p_3"),
  val cartItems: List<CartItem> = listOf(
    CartItem(product = SampleData.products[0], quantity = 1, selectedColor = "Cosmic Violet", selectedSize = "Standard"),
    CartItem(product = SampleData.products[2], quantity = 1, selectedColor = "Titanium Violet", selectedSize = "Standard")
  ),
  val orders: List<Order> = SampleData.initialOrders,
  val appliedPromoCode: String? = null,
  val promoDiscountPercent: Double = 0.0,
  val promoFixedDiscount: Double = 0.0,
  val selectedProductForDetail: Product? = null,
  val userFeedbackMessage: String? = null,
  val userName: String = "Alex Chen",
  val userEmail: String = "alex.chen@posta2026.market",
  val userTier: String = "VIP Diamond Member",
  val walletBalance: Double = 245.80,
  val rewardPoints: Int = 1850,
  val isDarkMode: Boolean = false,
  val recipientContactName: String = "Alex Chen",
  val recipientContactPhone: String = "+1 (555) 019-2834",
  val deliveryLocationAddress: String = "742 Evergreen Terrace, Tech District",
  val deliveryCoordinates: String? = null,
  val notificationsEnabled: Boolean = true,
  val visualSearchPhotoUri: String? = null,
  val isSupabaseLoggedIn: Boolean = false,
  val supabaseUserId: String? = null,
  val isAuthDialogVisible: Boolean = false,
  val isAuthLoading: Boolean = false,
  val authErrorMessage: String? = null
) {
  val cartCount: Int get() = cartItems.sumOf { it.quantity }

  val subtotal: Double get() = cartItems.sumOf { it.product.price * it.quantity }

  val discountAmount: Double
    get() {
      var d = subtotal * promoDiscountPercent
      if (promoFixedDiscount > 0.0) {
        d += promoFixedDiscount
      }
      return d.coerceAtMost(subtotal)
    }

  val shippingAmount: Double get() = if (subtotal == 0.0 || subtotal >= 50.0) 0.0 else 7.99

  val taxAmount: Double get() = (subtotal - discountAmount) * 0.08

  val totalAmount: Double get() = if (cartItems.isEmpty()) 0.0 else (subtotal - discountAmount + shippingAmount + taxAmount)
}

class PostaMarketViewModel(application: Application) : AndroidViewModel(application) {

  private val supabaseAuthManager = SupabaseAuthManager.getInstance(application)

  private val _uiState = MutableStateFlow(PostaMarketUiState())
  val uiState: StateFlow<PostaMarketUiState> = _uiState.asStateFlow()

  init {
    // Check if user is already logged in with Supabase
    val cachedUser = supabaseAuthManager.getCachedUser()
    if (cachedUser != null) {
      _uiState.update {
        it.copy(
          isSupabaseLoggedIn = true,
          supabaseUserId = cachedUser.id,
          userName = cachedUser.fullName.ifBlank { cachedUser.email.substringBefore("@") },
          userEmail = cachedUser.email,
          userTier = "VIP Supabase Member"
        )
      }
    }
  }

  fun showAuthDialog(visible: Boolean) {
    _uiState.update { it.copy(isAuthDialogVisible = visible, authErrorMessage = null) }
  }

  fun signInWithSupabase(email: String, pass: String) {
    viewModelScope.launch {
      _uiState.update { it.copy(isAuthLoading = true, authErrorMessage = null) }
      when (val result = supabaseAuthManager.signInWithEmail(email, pass)) {
        is AuthResult.Success -> {
          _uiState.update {
            it.copy(
              isAuthLoading = false,
              isAuthDialogVisible = false,
              isSupabaseLoggedIn = true,
              supabaseUserId = result.user.id,
              userName = result.user.fullName.ifBlank { result.user.email.substringBefore("@") },
              userEmail = result.user.email,
              userTier = "VIP Supabase Member",
              userFeedbackMessage = result.message
            )
          }
        }
        is AuthResult.Error -> {
          _uiState.update {
            it.copy(
              isAuthLoading = false,
              authErrorMessage = result.errorMessage
            )
          }
        }
      }
    }
  }

  fun signUpWithSupabase(email: String, pass: String, fullName: String) {
    viewModelScope.launch {
      _uiState.update { it.copy(isAuthLoading = true, authErrorMessage = null) }
      when (val result = supabaseAuthManager.signUpWithEmail(email, pass, fullName)) {
        is AuthResult.Success -> {
          _uiState.update {
            it.copy(
              isAuthLoading = false,
              isAuthDialogVisible = false,
              isSupabaseLoggedIn = result.user.accessToken.isNotEmpty(),
              supabaseUserId = result.user.id,
              userName = fullName.ifBlank { result.user.email.substringBefore("@") },
              userEmail = result.user.email,
              userTier = "VIP Supabase Member",
              userFeedbackMessage = result.message
            )
          }
        }
        is AuthResult.Error -> {
          _uiState.update {
            it.copy(
              isAuthLoading = false,
              authErrorMessage = result.errorMessage
            )
          }
        }
      }
    }
  }

  fun forgotPasswordWithSupabase(email: String) {
    viewModelScope.launch {
      _uiState.update { it.copy(isAuthLoading = true, authErrorMessage = null) }
      when (val result = supabaseAuthManager.resetPasswordForEmail(email)) {
        is AuthResult.Success -> {
          _uiState.update {
            it.copy(
              isAuthLoading = false,
              isAuthDialogVisible = false,
              userFeedbackMessage = result.message
            )
          }
        }
        is AuthResult.Error -> {
          _uiState.update {
            it.copy(
              isAuthLoading = false,
              authErrorMessage = result.errorMessage
            )
          }
        }
      }
    }
  }

  fun signOutFromSupabase() {
    viewModelScope.launch {
      supabaseAuthManager.signOut()
      _uiState.update {
        it.copy(
          isSupabaseLoggedIn = false,
          supabaseUserId = null,
          userName = "Posta Guest",
          userEmail = "guest@posta2026.market",
          userTier = "Standard Shopper",
          userFeedbackMessage = "Logged out from Supabase account"
        )
      }
    }
  }

  fun selectTab(tabIndex: Int) {
    _uiState.update { it.copy(selectedTab = tabIndex) }
  }

  fun selectCategory(category: ProductCategory) {
    _uiState.update { it.copy(selectedCategory = category) }
  }

  fun setSearchQuery(query: String) {
    _uiState.update { it.copy(searchQuery = query) }
  }

  fun setSearchCategoryFilter(category: ProductCategory) {
    _uiState.update { it.copy(searchCategoryFilter = category) }
  }

  fun setSearchSortOption(sortOption: SortOption) {
    _uiState.update { it.copy(searchSortOption = sortOption) }
  }

  fun toggleFavorite(productId: String) {
    _uiState.update { state ->
      val newFavorites = state.favorites.toMutableSet()
      val isFav = if (newFavorites.contains(productId)) {
        newFavorites.remove(productId)
        false
      } else {
        newFavorites.add(productId)
        true
      }
      state.copy(
        favorites = newFavorites,
        userFeedbackMessage = if (isFav) "Added to your Favorites ❤️" else "Removed from Favorites"
      )
    }
  }

  fun addToCart(product: Product, quantity: Int = 1, color: String = "Cosmic Violet", size: String = "Standard") {
    _uiState.update { state ->
      val existingIndex = state.cartItems.indexOfFirst {
        it.product.id == product.id && it.selectedColor == color && it.selectedSize == size
      }
      val updatedItems = state.cartItems.toMutableList()
      if (existingIndex >= 0) {
        val existing = updatedItems[existingIndex]
        updatedItems[existingIndex] = existing.copy(quantity = existing.quantity + quantity)
      } else {
        updatedItems.add(CartItem(product = product, quantity = quantity, selectedColor = color, selectedSize = size))
      }
      state.copy(
        cartItems = updatedItems,
        userFeedbackMessage = "${product.name} added to cart!"
      )
    }
  }

  fun updateCartQuantity(cartItem: CartItem, delta: Int) {
    _uiState.update { state ->
      val updated = state.cartItems.mapNotNull { item ->
        if (item.product.id == cartItem.product.id && item.selectedColor == cartItem.selectedColor && item.selectedSize == cartItem.selectedSize) {
          val newQty = item.quantity + delta
          if (newQty > 0) item.copy(quantity = newQty) else null
        } else {
          item
        }
      }
      state.copy(cartItems = updated)
    }
  }

  fun removeCartItem(cartItem: CartItem) {
    _uiState.update { state ->
      val updated = state.cartItems.filterNot {
        it.product.id == cartItem.product.id && it.selectedColor == cartItem.selectedColor && it.selectedSize == cartItem.selectedSize
      }
      state.copy(
        cartItems = updated,
        userFeedbackMessage = "Item removed from cart"
      )
    }
  }

  fun applyPromoCode(code: String): Boolean {
    val cleanCode = code.trim().uppercase()
    var success = false
    var msg = "Invalid promo code"
    var discPercent = 0.0
    var discFixed = 0.0

    when (cleanCode) {
      "POSTA2026" -> {
        discPercent = 0.20
        msg = "Promo POSTA2026 applied! 20% discount"
        success = true
      }
      "POSTATECH" -> {
        discPercent = 0.15
        msg = "Tech code applied! 15% discount"
        success = true
      }
      "SUPER50" -> {
        discFixed = 25.0
        msg = "Flash code applied! $25 OFF"
        success = true
      }
      "WELCOME10" -> {
        discPercent = 0.10
        msg = "Welcome code applied! 10% discount"
        success = true
      }
      else -> {
        success = false
        msg = "Promo code '$code' is not valid or expired."
      }
    }

    _uiState.update {
      it.copy(
        appliedPromoCode = if (success) cleanCode else it.appliedPromoCode,
        promoDiscountPercent = if (success) discPercent else it.promoDiscountPercent,
        promoFixedDiscount = if (success) discFixed else it.promoFixedDiscount,
        userFeedbackMessage = msg
      )
    }
    return success
  }

  fun openProductDetail(product: Product) {
    _uiState.update { it.copy(selectedProductForDetail = product) }
  }

  fun closeProductDetail() {
    _uiState.update { it.copy(selectedProductForDetail = null) }
  }

  fun clearUserFeedback() {
    _uiState.update { it.copy(userFeedbackMessage = null) }
  }

  fun toggleDarkMode() {
    _uiState.update { it.copy(isDarkMode = !it.isDarkMode) }
  }

  fun setDarkMode(enabled: Boolean) {
    _uiState.update { it.copy(isDarkMode = enabled) }
  }

  fun updateRecipientContact(name: String, phone: String) {
    _uiState.update {
      it.copy(
        recipientContactName = name,
        recipientContactPhone = phone,
        userFeedbackMessage = "Recipient updated: $name ($phone)"
      )
    }
  }

  fun updateDeliveryLocation(address: String, coordinates: String? = null) {
    _uiState.update {
      it.copy(
        deliveryLocationAddress = address,
        deliveryCoordinates = coordinates,
        userFeedbackMessage = "Delivery location updated: $address"
      )
    }
  }

  fun toggleNotifications(enabled: Boolean) {
    _uiState.update {
      it.copy(
        notificationsEnabled = enabled,
        userFeedbackMessage = if (enabled) "Order status notifications enabled!" else "Notifications muted"
      )
    }
  }

  fun setVisualSearchPhoto(uriString: String?) {
    _uiState.update {
      it.copy(
        visualSearchPhotoUri = uriString,
        userFeedbackMessage = if (uriString != null) "Photo loaded! Searching matching visual styles..." else null
      )
    }
  }

  fun checkout(): Boolean {
    val state = _uiState.value
    if (state.cartItems.isEmpty()) return false

    val orderNum = "PM-2026-${(1000..9999).random()}"
    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.US)
    val currentDateStr = dateFormat.format(Date())

    val newOrder = Order(
      id = "ord_${System.currentTimeMillis()}",
      orderNumber = orderNum,
      date = currentDateStr,
      items = state.cartItems,
      subtotal = state.subtotal,
      discount = state.discountAmount,
      shipping = state.shippingAmount,
      total = state.totalAmount,
      status = OrderStatus.PLACED,
      trackingNumber = "PST-TRK-${(100000..999999).random()}",
      estimatedArrival = "In 2 business days",
      deliveryAddress = "742 Evergreen Terrace, Suite 4B, Springfield"
    )

    _uiState.update {
      it.copy(
        orders = listOf(newOrder) + it.orders,
        cartItems = emptyList(),
        appliedPromoCode = null,
        promoDiscountPercent = 0.0,
        promoFixedDiscount = 0.0,
        selectedTab = 3, // Navigate to Orders screen
        userFeedbackMessage = "Order $orderNum placed successfully! 🎉"
      )
    }
    return true
  }
}
