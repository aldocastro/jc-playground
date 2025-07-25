package com.example.overview

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

@Composable
fun OverviewComponent(viewModel: OverviewViewModel) {
    Column(
        modifier = Modifier
        .padding(top = 24.dp)
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
    ) {
        Title(viewModel)
        Spacer(modifier = Modifier.height(16.dp))
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
GalleryAndTitle(viewModel: OverviewViewModel) {
    val cardState = viewModel.cardState.collectAsState()
    Column(
        horizontalAlignment = Alignment.CenterHorizontally) {
            val pagerState = rememberPagerState(pageCount = cardState.value.size)
            LaunchedEffect(key1 = cardState) {
                with(cardState.indexOf(selectedCardState)) {
                    when {
                        this == -1 -> pagerState.scrollToPage(0)
                        this != pagerState.currentPage -> pagerState.scrollToPage(this)
                    }
                }
            }
            val pagerPadding = determinePagerPadding(pageCount = pagerState.pageCount, currentPage = pagerState.currentPage)
            if (pagerState.pageCount > 0) {
                HorizontalPager(
                    contentPadding = pagerPadding,
                    pageSpacing = 16.dp,
                    modifier = Modifier.fillMaxWidth(),
                    state = pagerState,
                    pageContent = { index ->
                        Column {
                            val card = cardState.value[index]
                            StandardCardLabel(
                                modifier = Modifier
                                    .align(Alignment.Start)
                                    .height(22.dp)
                                    .padding(horizontal = pagerPadding.calculateLeftPadding(LayoutDirection.Ltr)),
                                card = cardState[index],
                                cardId = standardCardState
                            )
                            CardComponent(cardState[index])
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
                if (cardState.size > pagerState.currentPage) {
                    viewModel.selectedCardState.value = cardState.value[pagerState.currentPage]
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
            if (id == card.cardId) {
                Text(
                    text = "Standard Card"
                )
            }
        }
    }
}

@Composable
fun CardContent(card: Card) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .paddingFromBaseline(bottom = 24.dp)
            .padding(start = 24.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = card.ownerName,
            style = MaterialTheme.typography.h6,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
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
fun CardComponent(card: Card) {
    Card(
        modifier = Modifier
        .fillMaxWidth()
        .aspectRatio(1.586f),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
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
@ReadOnlyComposable
fun setSingletonImageLoaderFactory(factory: (context: PlatformContext) -> ImageLoader) {
    SingletonImageLoader.setSafe(factory)
}

@Composable
fun RemoteImageCard(imageUrl: String) {
    val defaultPainter = DefaultPainter()
    setSingletonImageLoaderFactory { getAsyncImagePainter(it) }
    AsyncImage(
        model = imageUrl,
        modifier = Modifier.fillMaxSize(),
        contentDescription = "Image",
        placeholder = defaultPainter,
        error = defaultPainter,
    )
}

@Composable
fun DefaultPainter() {
    val painter = remember { ColoredRectanglePainter(Color.Grey) }

    Canvas(
        modifier = Modifier
            .size(300.dp, 200.dp)
    ) {
        with(painter) {
            draw(size)
        }
    }
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
                .background(Color.Black.copy(alpha = 0.7f)),
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