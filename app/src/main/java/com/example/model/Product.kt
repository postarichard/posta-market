package com.example.model

import androidx.annotation.DrawableRes

enum class ProductCategory(val displayName: String, val iconName: String) {
  ALL("All", "Apps"),
  TECH("Tech & Gadgets", "Devices"),
  FASHION("Fashion", "Checkroom"),
  BEAUTY("Beauty & Glow", "Spa"),
  HOME("Home & Living", "Home"),
  GAMING("Gaming & VR", "SportsEsports"),
  SPORTS("Fitness", "FitnessCenter"),
  ACCESSORIES("Accessories", "Watch")
}

data class Product(
  val id: String,
  val name: String,
  val category: ProductCategory,
  val price: Double,
  val originalPrice: Double? = null,
  val rating: Double = 4.8,
  val reviewCount: Int = 128,
  val description: String,
  val specs: List<String> = emptyList(),
  val tag: String? = null, // e.g. "Hot Deal", "New", "2026 Trend", "Best Seller"
  val imageUrl: String? = null,
  @DrawableRes val imageRes: Int? = null,
  val isFeatured: Boolean = false,
  val isPopular: Boolean = false,
  val isNewArrival: Boolean = false,
  val isSpecialOffer: Boolean = false,
  val discountPercent: Int = 0,
  val stockLeft: Int = 24,
  val totalStock: Int = 50,
  val latitude: Double = 0.3476,
  val longitude: Double = 32.5825,
  val locationName: String = "Kampala Central Hub",
  val distanceKm: Double = 1.2
)

data class CartItem(
  val product: Product,
  val quantity: Int = 1,
  val selectedColor: String = "Cosmic Violet",
  val selectedSize: String = "Standard"
)

enum class OrderStatus(val label: String, val stepIndex: Int) {
  PLACED("Order Placed", 0),
  PROCESSING("Processing & Packed", 1),
  IN_TRANSIT("Out for Delivery", 2),
  DELIVERED("Delivered", 3)
}

data class Order(
  val id: String,
  val orderNumber: String,
  val date: String,
  val items: List<CartItem>,
  val subtotal: Double,
  val discount: Double,
  val shipping: Double,
  val total: Double,
  val status: OrderStatus,
  val trackingNumber: String,
  val estimatedArrival: String,
  val deliveryAddress: String
)

data class PromoBanner(
  val id: String,
  val title: String,
  val subtitle: String,
  val tag: String,
  val discountLabel: String,
  val imageUrl: String = "",
  @DrawableRes val imageRes: Int? = null,
  val promoCode: String? = null
)
