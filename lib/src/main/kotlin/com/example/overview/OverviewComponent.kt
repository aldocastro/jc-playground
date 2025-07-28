package com.example.overview

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.carousel.HorizontalUncontainedCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.capitalize
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import coil3.ImageLoader
import coil3.compose.AsyncImage
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import coil3.request.crossfade
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import androidx.compose.ui.geometry.Size as ComposeSize

enum class CardState {
    ACTIVE, DISABLED
}
enum class CardType {
    STANDARD, PREMIUM
}

data class Card(val id: String, val ownerName: String, val customName: String?, val number: String, val iban: String?, val state: CardState, val type: CardType, val imageUrl: String)

private val defaultPaddingValues = PaddingValues(start = 0.dp, top = 24.dp, end = 0.dp, bottom = 24.dp)

class OverviewViewModel: ViewModel() {
    private val cards = listOf(
        Card("1", "Owen Smith", "Personal Card", "1234 5678 9012 3456", "DE89370400440532013000", CardState.ACTIVE, CardType.STANDARD, "https://www.dummyimage.com/300x200/0011ff/fafafa.jpg&text=standard"),
        Card("2", "Owen Smith", null, "2345 6789 0123 4567", "DE89370400440532013001", CardState.DISABLED, CardType.STANDARD, "https://www.dummyimage.com/300x200/0011ff/fafafa.jpg&text=standard"),
        Card("3", "Owen Smith", "Premium Card", "3456 7890 1234 5678", null, CardState.ACTIVE, CardType.PREMIUM, "https://www.dummyimage.com/300x200/0011ff/fafafa.jpg&text=premium"),
    )
    val cardState = MutableStateFlow<List<Card>>(cards).asStateFlow()
    val selectedCardState = MutableStateFlow<Card?>(cards.firstOrNull())
    val standardCardState = MutableStateFlow<String?>(cards.firstOrNull()?.id).asStateFlow()

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
private fun getAsyncImagePainter(context: Context = LocalContext.current): ImageLoader {
    return remember(context) {
        ImageLoader.Builder(context)
            .diskCachePolicy(CachePolicy.ENABLED)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .build()
    }
}


@Composable
fun OverviewComponent(innerPadding: PaddingValues = defaultPaddingValues, viewModel: OverviewViewModel = OverviewViewModel()) {
    Column(
        modifier = Modifier
            .padding(innerPadding)
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Title(viewModel)
        Spacer(modifier = Modifier.height(16.dp))
//        val selectedCard by viewModel.selectedCardState.collectAsState()
//        selectedCard?.let {
//            CardDetails(card = it, modifier = Modifier.fillMaxWidth())
//        }
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
    val cardState by viewModel.cardState.collectAsState()
    val selectedCardState by viewModel.selectedCardState.collectAsState()
    val standardCardState by viewModel.standardCardState.collectAsState()

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val pagerState = rememberPagerState(pageCount = cardState::size)
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
                Carousel(pagerState = pagerState)
            }
            LaunchedEffect(pagerState.currentPage) {
                if (cardState.size > pagerState.currentPage) {
                    viewModel.selectedCardState.value = cardState[pagerState.currentPage]
                }
            }
        } else {
            Text("No cards available", style = MaterialTheme.typography.bodySmall, fontSize = 16.sp)
        }
    }
}

@Composable
fun OptionalCardName(cards: List<Card>, currentPage: Int) {
    if (cards.any { !it.customName.isNullOrEmpty() }) {
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = cards[currentPage].customName ?: "No custom name",
            style = MaterialTheme.typography.bodySmall,
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
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE
    val cardMaxHeight = if (isLandscape) configuration.screenHeightDp.dp * 0.8f else Dp.Unspecified
    val aspectRatio = 1.586f
    Card(
        modifier = Modifier
            .fillMaxWidth() // Card itself fills the width provided by HorizontalPager's contentPadding
            .heightIn(max = cardMaxHeight)
            .aspectRatio(aspectRatio), // Keep the aspect ratio for consistent card shape
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(1.5.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .heightIn(max = cardMaxHeight)
                .aspectRatio(aspectRatio)
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
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(4.dp))
        if (card.type == CardType.STANDARD) {
            Text(
                text = "Card Number: ${card.number}",
                style = MaterialTheme.typography.bodySmall,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        } else {
            Text(
                text = "Iban: ${card.iban ?: "N/A"}",
                style = MaterialTheme.typography.bodySmall,
                fontSize = 14.sp,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "PREMIUM DEBIT",
                style = MaterialTheme.typography.bodySmall,
                fontSize = 14.sp,
                fontStyle = FontStyle.Italic
            )
        }
    }
}

@Composable
fun RemoteImageCard(imageUrl: String) {
    val defaultPainter = remember { ColoredRectanglePainter(Color.LightGray) }
    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(imageUrl)
            .crossfade(true)
            .build(),
        modifier = Modifier.fillMaxSize(),
        contentDescription = "Card Image",
        imageLoader = getAsyncImagePainter(),
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
                style = MaterialTheme.typography.headlineSmall,
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
fun Carousel(pagerState: PagerState) {
    DotsIndicator(
        totalDots = pagerState.pageCount,
        selectedIndex = pagerState.currentPage,
        modifier = Modifier.padding(16.dp)
    )
}

@Composable
fun DotsIndicator(
    totalDots: Int,
    selectedIndex: Int,
    modifier: Modifier = Modifier,
    dotSize: Dp = 20.dp,
    selectedDotSize: Dp = 30.dp,
    dotSpacing: Dp = 14.dp,
    selectedColor: Color = Color.Blue,
    unSelectedColor: Color = Color.DarkGray
) {
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        repeat(totalDots) { index ->
            Box(
                modifier = Modifier
                    .size(if (index == selectedIndex) selectedDotSize else dotSize)
                    .padding(horizontal = dotSpacing / 2)
                    .clip(CircleShape)
                    .background(if (index == selectedIndex) selectedColor else unSelectedColor)
            )
        }
    }
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
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            text = "Owner: ${card.ownerName}",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Text(
            text = "Card Number: ${card.number}",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        card.iban?.let {
            Text(
                text = "IBAN: $it",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }
        Text(
            text = "Type: ${card.type.name.capitalize(Locale.current)}",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Text(
            text = "Status: ${card.state.name.capitalize(Locale.current)}",
            style = MaterialTheme.typography.bodySmall,
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