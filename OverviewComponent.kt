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
import androidx.compose.runtime.StateFlow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.layout.height
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.text.style.*
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.CardDefaults
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import coil.compose.AsyncImagePainter
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.geometry.Size as ComposeSize

// (Data classes, Enums, ViewModel, and other helper functions like ColoredRectanglePainter, getAsyncImagePainter, CardDetails, etc. remain the same as the previous response)

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
    // The entire layout is now a single, vertically scrollable column.
    Column(
        modifier = Modifier
            .padding(top = 24.dp)
            .fillMaxSize()
            .verticalScroll(rememberScrollState()) // Ensures the content is always scrollable
    ) {
        // The title and card gallery component
        Title(viewModel)
        Spacer(modifier = Modifier.height(16.dp))

        // Card details are always placed below the gallery.
        val selectedCard by viewModel.selectedCardState.collectAsStateWithLifecycle(LocalLifecycleOwner.current.lifecycle)
        selectedCard?.let {
            CardDetails(card = it, modifier = Modifier.fillMaxWidth())
        }

        // Add more components here as needed, they will also be stacked vertically.
        // Info(viewModel)
        // Spacer(modifier = Modifier.weight(1f))
        // Footer(viewModel)
    }
}

@Composable
fun Title(viewModel: OverviewViewModel) {
    Column(horizontalAlignment = Alignment.Start) {
        GalleryAndTitle(
            viewModel = viewModel
        )
    }
}

@Composable
fun GalleryAndTitle(viewModel: OverviewViewModel, modifier: Modifier = Modifier) {
    val cardState by viewModel.cardState.collectAsStateWithLifecycle(LocalLifecycleOwner.current.lifecycle)
    val selectedCardState by viewModel.selectedCardState.collectAsStateWithLifecycle(LocalLifecycleOwner.current.lifecycle)
    val standardCardState by viewModel.standardCardState.collectAsStateWithLifecycle(LocalLifecycleOwner.current.lifecycle)


    Column(
        modifier = modifier, // The modifier is still useful for padding or other parent-driven properties.
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val pagerState = rememberPagerState(pageCount = { cardState.size })
        LaunchedEffect(key1 = cardState, selectedCardState) {
            val index = cardState.indexOf(selectedCardState)
            if (index != -1 && index != pagerState.currentPage) {
                pagerState.animateScrollToPage(index)
            }
        }
        val pagerPadding = calculateHorizontalPagerPadding()

        if (pagerState.pageCount > 0) {
            HorizontalPager(
                contentPadding = pagerPadding,
                pageSpacing = 16.dp,
                modifier = Modifier.fillMaxWidth(),
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

/**
 * Calculates the horizontal padding for the HorizontalPager to keep the card's
 * width proportional to the screen width. This logic is preserved as requested.
 */
@Composable
private fun calculateHorizontalPagerPadding(): PaddingValues {
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val cardWidthProportion = 0.85f // Adjust this value as needed
    val cardTargetWidth = screenWidth * cardWidthProportion
    val horizontalPadding = (screenWidth - cardTargetWidth) / 2

    return PaddingValues(horizontal = horizontalPadding)
}

// All other helper composables (CardComponent, CardContent, RemoteImageCard, etc.) remain unchanged.

@Composable
fun CardComponent(card: Card) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1.586f),
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

// ... other composables ...

@Composable
fun CardDetails(card: Card, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.Start
    ) {
        // ... CardDetails content ...
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 640) // Portrait
@Composable
fun OverviewComponentPortraitPreview() {
    OverviewComponent()
}

@Preview(showBackground = true, widthDp = 640, heightDp = 360) // Landscape
@Composable
fun OverviewComponentLandscapePreview() {
    OverviewComponent()
}