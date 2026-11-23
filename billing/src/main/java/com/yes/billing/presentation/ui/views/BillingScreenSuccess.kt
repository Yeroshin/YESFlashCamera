package com.yes.billing.presentation.ui.views

import android.app.Activity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yes.billing.presentation.model.BillingUI
import com.yes.shared.R
import com.yes.shared.presentation.ui.theme.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BillingScreenSuccess(
    billingUI: BillingUI,
    onBackClick: () -> Unit,
    onSubscribe: (Activity, String) -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity

    var selectedProductId by remember(billingUI.products) {
        mutableStateOf(billingUI.products.firstOrNull()?.productId ?: "yes_camera_subscription_monthly")
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("PRO Подписка", color = AppTheme.colors.textPrimary) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Text("←", color = AppTheme.colors.textPrimary, style = MaterialTheme.typography.titleLarge)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AppTheme.colors.surface)
            )
        },
        containerColor = AppTheme.colors.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Spacer(modifier = Modifier.height(12.dp))
                // Crown icon header
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(36.dp))
                        .background(AppTheme.colors.primaryAccent.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.crown),
                        contentDescription = "PRO",
                        modifier = Modifier.size(40.dp),
                        colorFilter = ColorFilter.tint(AppTheme.colors.primaryAccent)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Сохраняйте фото в режиме PRO",
                    style = TextStyle(
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppTheme.colors.textPrimary
                    ),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (billingUI.isActiveSubscription) "Ваша подписка активна ⭐" else "Разблокируйте полный функционал сохранения",
                    style = TextStyle(
                        fontSize = 16.sp,
                        color = if (billingUI.isActiveSubscription) AppTheme.colors.primaryAccent else AppTheme.colors.textSecondary
                    ),
                    textAlign = TextAlign.Center
                )
            }

            // Benefits list
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = AppTheme.colors.surface),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        BenefitItem("Сохранение снимков в режиме внешней вспышки")
                    }
                }
            }

            item {
                Text(
                    text = "Выберите тариф",
                    style = TextStyle(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AppTheme.colors.textPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            items(billingUI.products) { product ->
                val isSelected = product.productId == selectedProductId
                val borderColor = if (isSelected) AppTheme.colors.primaryAccent else AppTheme.colors.stroke
                val containerColor = if (isSelected) AppTheme.colors.primaryAccent.copy(alpha = 0.1f) else AppTheme.colors.surface

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(width = if (isSelected) 2.dp else 1.dp, color = borderColor, shape = RoundedCornerShape(12.dp))
                        .clickable { selectedProductId = product.productId },
                    colors = CardDefaults.cardColors(containerColor = containerColor),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = product.title,
                                style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold, color = AppTheme.colors.textPrimary)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = product.description,
                                style = TextStyle(fontSize = 13.sp, color = AppTheme.colors.textSecondary)
                            )
                        }
                        Text(
                            text = product.price,
                            style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold, color = AppTheme.colors.primaryAccent)
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        if (activity != null) {
                            onSubscribe(activity, selectedProductId)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AppTheme.colors.primaryAccent),
                    shape = RoundedCornerShape(27.dp)
                ) {
                    Text(
                        text = if (billingUI.isActiveSubscription) "Подписка активна" else "Оформить подписку",
                        style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold, color = AppTheme.colors.background)
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun BenefitItem(text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(AppTheme.colors.primaryAccent)
        )
        Text(
            text = text,
            style = TextStyle(fontSize = 14.sp, color = AppTheme.colors.textPrimary)
        )
    }
}
