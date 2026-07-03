package com.telugustockpro.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.telugustockpro.data.model.Stock
import com.telugustockpro.ui.theme.TradingViewColors
import java.text.DecimalFormat

val priceFormat = DecimalFormat("#,##0.00")
val volumeFormat = DecimalFormat("#,##,###")
val percentFormat = DecimalFormat("0.00")

@Composable
fun MarketIndexCard(
    name: String,
    price: Double,
    change: Double,
    changePercent: Double,
    isPositive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .width(180.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = TradingViewColors.CardBackground
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = name,
                style = MaterialTheme.typography.labelMedium,
                color = TradingViewColors.TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = priceFormat.format(price),
                style = MaterialTheme.typography.titleLarge,
                color = TradingViewColors.TextPrimary,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = if (isPositive) Icons.Filled.TrendingUp else Icons.Filled.TrendingDown,
                    contentDescription = null,
                    tint = if (isPositive) TradingViewColors.Green else TradingViewColors.Red,
                    modifier = Modifier.size(16.dp)
                )

                Text(
                    text = "${if (isPositive) "+" else ""}${percentFormat.format(changePercent)}%",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isPositive) TradingViewColors.Green else TradingViewColors.Red,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = "(${if (isPositive) "+" else ""}${priceFormat.format(change)})",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isPositive) TradingViewColors.Green else TradingViewColors.Red
                )
            }
        }
    }
}

@Composable
fun StockListItem(
    stock: Stock,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    showVolume: Boolean = true
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = TradingViewColors.Surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = stock.symbol,
                    style = MaterialTheme.typography.titleSmall,
                    color = TradingViewColors.TextPrimary,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = stock.name,
                    style = MaterialTheme.typography.bodySmall,
                    color = TradingViewColors.TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (showVolume) {
                Column(
                    horizontalAlignment = Alignment.End,
                    modifier = Modifier.padding(horizontal = 12.dp)
                ) {
                    Text(
                        text = "Vol",
                        style = MaterialTheme.typography.labelSmall,
                        color = TradingViewColors.TextTertiary
                    )
                    Text(
                        text = volumeFormat.format(stock.volume),
                        style = MaterialTheme.typography.labelSmall,
                        color = TradingViewColors.TextSecondary
                    )
                }
            }

            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = priceFormat.format(stock.price),
                    style = MaterialTheme.typography.titleSmall,
                    color = TradingViewColors.TextPrimary,
                    fontWeight = FontWeight.Bold
                )

                val bgColor = if (stock.isPositive) TradingViewColors.GreenBackground else TradingViewColors.RedBackground
                val textColor = if (stock.isPositive) TradingViewColors.Green else TradingViewColors.Red

                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = bgColor
                ) {
                    Text(
                        text = "${if (stock.isPositive) "+" else ""}${percentFormat.format(stock.changePercent)}%",
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = textColor,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
fun SectionHeader(
    title: String,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = TradingViewColors.TextPrimary,
            fontWeight = FontWeight.SemiBold
        )

        if (actionText != null && onActionClick != null) {
            TextButton(onClick = onActionClick) {
                Text(
                    text = actionText,
                    style = MaterialTheme.typography.labelMedium,
                    color = TradingViewColors.Blue
                )
            }
        }
    }
}

@Composable
fun PriceChangeChip(
    changePercent: Double,
    isPositive: Boolean,
    modifier: Modifier = Modifier
) {
    val bgColor = if (isPositive) TradingViewColors.GreenBackground else TradingViewColors.RedBackground
    val textColor = if (isPositive) TradingViewColors.Green else TradingViewColors.Red

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(4.dp),
        color = bgColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Icon(
                imageVector = if (isPositive) Icons.Filled.ArrowUpward else Icons.Filled.ArrowDownward,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(12.dp)
            )
            Text(
                text = "${if (isPositive) "+" else ""}${percentFormat.format(changePercent)}%",
                style = MaterialTheme.typography.labelSmall,
                color = textColor,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun LoadingIndicator(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color = TradingViewColors.Blue,
            modifier = Modifier.size(48.dp),
            strokeWidth = 4.dp
        )
    }
}

@Composable
fun ShimmerEffect(
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val alpha = transition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shimmer_alpha"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        TradingViewColors.Surface.copy(alpha = alpha.value),
                        TradingViewColors.SurfaceVariant.copy(alpha = alpha.value),
                        TradingViewColors.Surface.copy(alpha = alpha.value)
                    )
                )
            )
    )
}

@Composable
fun MiniChartPlaceholder(
    isPositive: Boolean,
    modifier: Modifier = Modifier
) {
    val color = if (isPositive) TradingViewColors.Green else TradingViewColors.Red

    Box(
        modifier = modifier
            .width(60.dp)
            .height(30.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(color.copy(alpha = 0.1f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = if (isPositive) Icons.Filled.TrendingUp else Icons.Filled.TrendingDown,
            contentDescription = null,
            tint = color.copy(alpha = 0.5f),
            modifier = Modifier.size(20.dp)
        )
    }
}
