package com.example.overview

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.StateFlow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.layout.height
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.foundation.layout.calculateLeftPadding
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.*
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.CardDefaults
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.compose.rememberImagePainter
import coil.request.ImageResult
import coil.compose.rememberAsyncImagePainter
import coil.compose.AsyncImagePainter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import android.graphics.Rect
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.graphicsLayer
import coil.SingletonImageLoader
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.request.CachePolicy
import coil.request.Disposable
import coil.request.ErrorResult
import coil.request.SuccessResult
import coil.size.Size
import coil.size.px2dp
import androidx.compose.ui.geometry.Size as ComposeSize

enum class CardState {
    ACTIVE, DISABLED
}
enum class CardType {
    STANDARD, PREMIUM
}

data class Card(val id: String, val ownerName: String, val customName: String?, val number: String, val iban: String?, val state: CardState, val type: CardType, val imageUrl: String)

class OverviewViewModel: ViewModel() {
    private val cards = listOf(
        Card("1", "Owen Smith", "Personal Card", "1234 5678 9012 3456", "DE89370400440532013000", CardState.ACTIVE, CardType.STANDARD, "https://www.dummyimage.com/300x200/0011ff/fafafa.jpg&text=standard"),
        Card("2", "Owen Smith", null, "2345 6789 0123 4567", "DE89370400440532013001", CardState.DISABLED, CardType.STANDARD, "https://www.dummyimage.com/300x200/0011ff/fafafa.jpg&text=standard"),
        Card("3", "Owen Smith", "Premium Card", "3456 7890 1234 5678", null, CardState.ACTIVE, CardType.PREMIUM, "https://www.dummyimage.com/300x200/0011ff/fafafa.jpg&text=premium"),
    )
    val cardState = StateFlow<List<Card>>(initialValue = cards)
    val selectedCardState = StateFlow<Card?>(initialValue = cards.firstOrNull())
    val standardCardState = StateFlow<String?>(initialValue = cards.firstOrNull()?.id)

}

/**
 * Custom Painter for drawing a colored rectangle.
 * Used as a placeholder or error painter for AsyncImage.
 */
private class ColoredRectanglePainter(private val color: Color) : Painter() {
    override val intrinsicSize: ComposeSize = ComposeSize.Unspecified

    override fun DrawScope.onDraw() {
        drawRect(color = color)
    }
}

// Function to get a singleton ImageLoader for Coil, can be customized if needed
@Composable
private fun getAsyncImagePainter(context: android.content.Context): ImageLoader {
    return remember(context) {
        ImageLoader.Builder(context)
            .diskCachePolicy(CachePolicy.ENABLED)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .build()
    }
}


@Composable
fun OverviewComponent(viewModel: OverviewViewModel = viewModel()) {
    // We explicitly want a single-column layout, always vertically scrollable if content overflows.
    Column(
        modifier = Modifier
            .padding(top = 24.dp)
            .fillMaxSize()
            .verticalScroll(rememberScrollState()) // Always enable vertical scrolling
    ) {
        // The title and card pager
        Title(viewModel)
        Spacer(modifier = Modifier.height(16.dp))

        // Card details always appear below the pager, regardless of orientation or screen height
        val selectedCard by viewModel.selectedCardState.collectAsStateWithLifecycle(LocalLifecycleOwner.current.lifecycle)
        selectedCard?.let {
            CardDetails(card = it, modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
fun Title(viewModel: OverviewViewModel) {
    Column(horizontalAlignment = Alignment.Start) {
        GalleryAndTitle(viewModel = viewModel)
    }
}

@Composable
fun GalleryAndTitle(viewModel: OverviewViewModel, modifier: Modifier = Modifier) {
    val cardState by viewModel.cardState.collectAsStateWithLifecycle(LocalLifecycleOwner.current.lifecycle)
    val selectedCardState by viewModel.selectedCardState.collectAsStateWithLifecycle(LocalLifecycleOwner.current.lifecycle)
    val standardCardState by viewModel.standardCardState.collectAsStateWithLifecycle(LocalLifecycleOwner.current.lifecycle)


    Column(
        modifier = modifier.fillMaxWidth(), // Ensure the column fills width, allowing padding calculations
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val pagerState = rememberPagerState(pageCount = { cardState.size })
        LaunchedEffect(key1 = cardState, selectedCardState) {
            val index = cardState.indexOf(selectedCardState)
            if (index != -1 && index != pagerState.currentPage) {
                pagerState.animateScrollToPage(index)
            }
        }

        // Dynamically determine pager padding based on screen width to maintain proportion
        val pagerPadding = calculateHorizontalPagerPadding()

        if (pagerState.pageCount > 0) {
            HorizontalPager(
                contentPadding = pagerPadding,
                pageSpacing = 16.dp,
                modifier = Modifier.fillMaxWidth(), // This modifier is crucial for padding calculation
                state = pagerState,
                pageContent = { index ->
                    Column {
                        val card = cardState[index]
                        StandardCardLabel(
                            modifier = Modifier
                                .align(Alignment.Start)
                                .height(22.dp)
                                .padding(horizontal = pagerPadding.calculateLeftPadding(LayoutDirection.Ltr)),
                            card = card,
                            cardId = standardCardState
                        )
                        CardComponent(card)
                    }
                }
            )
            OptionalCardName(cards = cardState, currentPage = pagerState.currentPage)
            if (pagerState.pageCount > 1) {
                Carousel(
                    pagerState = pagerState,
                    endless = false,
                )
            }
            LaunchedEffect(pagerState.currentPage) {
                if (cardState.size > pagerState.currentPage) {
                    viewModel.selectedCardState.value = cardState[pagerState.currentPage]
                }
            }
        } else {
            Text("No cards available", style = MaterialTheme.typography.body1, fontSize = 16.sp)
        }
    }
}

@Composable
fun OptionalCardName(cards: List<Card>, currentPage: Int) {
    if (cards.any { it.customName.isNullOrEmpty() }) {
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = cards[currentPage].customName ?: "No custom name",
            style = MaterialTheme.typography.body1,
            fontSize = 14.sp,
            textAlign = TextAlign.Left,
        )
    }
    Spacer(modifier = Modifier.height(4.dp))
}

@Composable
fun StandardCardLabel(modifier: Modifier, card: Card, cardId: String?) {
    cardId?.let { id ->
        Column(modifier = modifier) {
            if (id == card.id) {
                Text(
                    text = "Standard Card"
                )
            }
        }
    }
}

@Composable
fun CardComponent(card: Card) {
    Card(
        modifier = Modifier
            .fillMaxWidth() // Card itself fills the width provided by HorizontalPager's contentPadding
            .aspectRatio(1.586f), // Keep the aspect ratio for consistent card shape
        shape = RoundedCornerShape(8.dp),
        elevation = 1.5.dp
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            RemoteImageCard(imageUrl = card.imageUrl)
            CardContent(card)
            if (card.state == CardState.DISABLED) {
                CardDisabledOverlay()
            }
        }
    }
}

@Composable
fun CardContent(card: Card) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 24.dp, bottom = 24.dp),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Bottom
    ) {
        Text(
            text = card.ownerName,
            style = MaterialTheme.typography.h6,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(4.dp))
        if (card.type == CardType.STANDARD) {
            Text(
                text = "Card Number: ${card.number}",
                style = MaterialTheme.typography.body1,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        } else {
            Text(
                text = "Iban: ${card.iban ?: "N/A"}",
                style = MaterialTheme.typography.body1,
                fontSize = 14.sp,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "PREMIUM DEBIT",
                style = MaterialTheme.typography.body1,
                fontSize = 14.sp,
                fontStyle = FontStyle.Italic
            )
        }
    }
}

@Composable
fun RemoteImageCard(imageUrl: String) {
    val defaultPainter = remember { ColoredRectanglePainter(Color.LightGray) }
    val imageLoader = remember { getAsyncImagePainter(LocalContext.current) }
    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(imageUrl)
            .crossfade(true)
            .build(),
        modifier = Modifier.fillMaxSize(),
        contentDescription = "Card Image",
        imageLoader = imageLoader,
        placeholder = defaultPainter,
        error = defaultPainter,
    )
}

@Composable
fun CardDisabledOverlay() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f)),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .background(Color.Black.copy(alpha = 0.7f), shape = RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Disabled",
                color = Color.White,
                style = MaterialTheme.typography.h6,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * Calculates the horizontal padding for the HorizontalPager to keep the card's
 * width proportional to the screen width.
 */
@Composable
private fun calculateHorizontalPagerPadding(): PaddingValues {
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    // Define a target proportion for the card's width relative to the screen width.
    // E.g., 0.85f means the card will take 85% of the screen width.
    val cardWidthProportion = 0.85f // Adjust this value to your desired card size
    val cardTargetWidth = screenWidth * cardWidthProportion
    val horizontalPadding = (screenWidth - cardTargetWidth) / 2

    return PaddingValues(horizontal = horizontalPadding)
}


@Composable
fun Carousel(pagerState: androidx.compose.foundation.pager.PagerState, endless: Boolean) {
    // Placeholder for your Carousel implementation.
    Text("Carousel (Page ${pagerState.currentPage + 1} of ${pagerState.pageCount})")
}

@Composable
fun CardDetails(card: Card, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .padding(16.dp)
            .verticalScroll(rememberScrollState()), // Ensure details are scrollable if they overflow
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = card.customName ?: "Card Details",
            style = MaterialTheme.typography.h5,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            text = "Owner: ${card.ownerName}",
            style = MaterialTheme.typography.body1,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Text(
            text = "Card Number: ${card.number}",
            style = MaterialTheme.typography.body1,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        card.iban?.let {
            Text(
                text = "IBAN: $it",
                style = MaterialTheme.typography.body1,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }
        Text(
            text = "Type: ${card.type.name.lowercase().capitalize()}",
            style = MaterialTheme.typography.body1,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Text(
            text = "Status: ${card.state.name.lowercase().capitalize()}",
            style = MaterialTheme.typography.body1,
            color = if (card.state == CardState.ACTIVE) Color.Green else Color.Red
        )
    }
}


@Preview(showBackground = true, widthDp = 360, heightDp = 640) // Standard phone portrait
@Composable
fun OverviewComponentPortraitPreview() {
    OverviewComponent()
}

@Preview(showBackground = true, widthDp = 640, heightDp = 360) // Standard phone landscape (shorter height)
@Composable
fun OverviewComponentLandscapePreview() {
    OverviewComponent()
}

@Preview(showBackground = true, widthDp = 800, heightDp = 1200) // Tablet portrait
@Composable
fun OverviewComponentTabletPortraitPreview() {
    OverviewComponent()
}

@Preview(showBackground = true, widthDp = 1200, heightDp = 800) // Tablet landscape (still single column)
@Composable
fun OverviewComponentTabletLandscapeWidePreview() {
    OverviewComponent()
}