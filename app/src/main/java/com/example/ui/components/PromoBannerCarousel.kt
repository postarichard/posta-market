package com.example.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import com.example.model.PromoBanner
import com.example.ui.theme.BrightPink
import com.example.ui.theme.BrightPurple
import com.example.ui.theme.HighlightAmber

@Composable
fun PromoBannerCarousel(
  banners: List<PromoBanner>,
  onBannerClick: (PromoBanner) -> Unit,
  modifier: Modifier = Modifier
) {
  if (banners.isEmpty()) return

  val pagerState = rememberPagerState { banners.size }

  Column(modifier = modifier.fillMaxWidth()) {
    HorizontalPager(
      state = pagerState,
      contentPadding = PaddingValues(horizontal = 16.dp),
      pageSpacing = 12.dp,
      modifier = Modifier
        .fillMaxWidth()
        .testTag("promo_banner_carousel")
    ) { page ->
      val banner = banners[page]
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .aspectRatio(16f / 9f)
          .clip(RoundedCornerShape(24.dp))
          .clickable { onBannerClick(banner) },
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
      ) {
        Box(modifier = Modifier.fillMaxSize()) {
          // Banner Background Image
          SubcomposeAsyncImage(
            model = banner.imageUrl,
            contentDescription = banner.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
            error = {
              Box(
                modifier = Modifier
                  .fillMaxSize()
                  .background(
                    Brush.verticalGradient(
                      colors = listOf(Color(0xFF2E1065), Color(0xFF130924))
                    )
                  )
              )
            }
          )

          // Modern 2026 Dual Gradient Overlay for readability
          Box(
            modifier = Modifier
              .fillMaxSize()
              .background(
                Brush.horizontalGradient(
                  colors = listOf(
                    Color(0xFF130924).copy(alpha = 0.88f),
                    Color(0xFF2E1065).copy(alpha = 0.55f),
                    Color.Transparent
                  )
                )
              )
          )

          // Banner Content
          Column(
            modifier = Modifier
              .fillMaxSize()
              .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
          ) {
            // Top Row: Tag & Discount Pill
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Surface(
                shape = RoundedCornerShape(8.dp),
                color = BrightPink,
                shadowElevation = 2.dp
              ) {
                Text(
                  text = banner.tag,
                  color = Color.White,
                  fontSize = 10.sp,
                  fontWeight = FontWeight.ExtraBold,
                  letterSpacing = 0.5.sp,
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
              }

              Surface(
                shape = RoundedCornerShape(12.dp),
                color = HighlightAmber,
                shadowElevation = 2.dp
              ) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                  Icon(
                    imageVector = Icons.Filled.LocalOffer,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(12.dp)
                  )
                  Spacer(modifier = Modifier.width(4.dp))
                  Text(
                    text = banner.discountLabel,
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                  )
                }
              }
            }

            // Middle & Bottom Info
            Column {
              Text(
                text = banner.title,
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 22.sp
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = banner.subtitle,
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 12.sp,
                maxLines = 2
              )
              Spacer(modifier = Modifier.height(10.dp))

              Row(verticalAlignment = Alignment.CenterVertically) {
                Button(
                  onClick = { onBannerClick(banner) },
                  colors = ButtonDefaults.buttonColors(
                    containerColor = BrightPurple,
                    contentColor = Color.White
                  ),
                  shape = RoundedCornerShape(12.dp),
                  contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                  modifier = Modifier.height(34.dp)
                ) {
                  Text(
                    text = if (banner.promoCode != null) "Use: ${banner.promoCode}" else "Explore",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                  )
                  Spacer(modifier = Modifier.width(4.dp))
                  Icon(
                    imageVector = Icons.Filled.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp)
                  )
                }
              }
            }
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(10.dp))

    // Dot Indicators
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp),
      horizontalArrangement = Arrangement.Center,
      verticalAlignment = Alignment.CenterVertically
    ) {
      repeat(banners.size) { index ->
        val isSelected = pagerState.currentPage == index
        val width by animateDpAsState(targetValue = if (isSelected) 22.dp else 6.dp, label = "dot_width")
        Box(
          modifier = Modifier
            .padding(horizontal = 3.dp)
            .height(6.dp)
            .width(width)
            .clip(CircleShape)
            .background(
              if (isSelected) BrightPurple else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
            )
        )
      }
    }
  }
}
