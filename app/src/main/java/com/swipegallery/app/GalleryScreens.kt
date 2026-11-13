package com.swipegallery.app

import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.layout.onGloballyPositioned
import kotlinx.coroutines.delay
import java.util.Calendar
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.zIndex
import androidx.compose.material3.HorizontalDivider
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateCentroid
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Album
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DeleteForever
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.FilterList
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.FolderSpecial
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Restore
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.SettingsBrightness
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.BugReport
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Swipe
import androidx.compose.material.icons.rounded.Videocam
import androidx.compose.material.icons.rounded.ZoomInMap
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

@Composable
internal fun Header(
    page: Page,
    count: Int,
    album: String?,
    isSearching: Boolean = false,
    onToggleSearch: (() -> Unit)? = null,
    onBack: () -> Unit
) {
    val colors = LocalAppColors.current
    Row(
        Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 22.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (album != null || page == Page.Albums) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back", tint = colors.ink)
            }
        }
        Column(Modifier.weight(1f)) {
            Text(
                when {
                    album != null -> album
                    page == Page.Photos -> "Pictures"
                    page == Page.Albums -> "Albums"
                    page == Page.CleanUp -> "Clean Up"
                    page == Page.More -> "Library"
                    else -> "Pictures"
                },
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = colors.ink
            )
            Text(
                when {
                    album != null -> "ALBUM"
                    page == Page.Photos -> "$count items · all memories"
                    page == Page.Albums -> "YOUR COLLECTIONS"
                    page == Page.CleanUp -> "SWIPE TIDY & DUPLICATES"
                    page == Page.More -> "TOOLS & SECURE VAULT"
                    else -> "LUMINA"
                },
                fontSize = 10.sp,
                letterSpacing = 1.2.sp,
                color = colors.muted,
                fontWeight = FontWeight.SemiBold
            )
        }
        if (onToggleSearch != null && (page == Page.Photos || album != null)) {
            IconButton(
                onClick = onToggleSearch,
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(if (isSearching) colors.green.copy(alpha = 0.18f) else colors.chipBackground)
            ) {
                Icon(
                    if (isSearching) Icons.Rounded.Close else Icons.Rounded.Search,
                    "Search photos",
                    tint = if (isSearching) colors.green else colors.muted,
                    modifier = Modifier.size(21.dp)
                )
            }
        }
    }
}

/**
 * Search input field for filtering media by file name.
 */
@Composable
internal fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 6.dp),
        shape = RoundedCornerShape(20.dp),
        color = colors.card,
        border = BorderStroke(1.dp, colors.border),
        shadowElevation = 3.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Rounded.Search,
                contentDescription = null,
                tint = colors.green,
                modifier = Modifier.size(22.dp)
            )
            Spacer(Modifier.width(10.dp))
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier
                    .weight(1f)
                    .focusRequester(focusRequester),
                textStyle = TextStyle(
                    color = colors.ink,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
                ),
                singleLine = true,
                cursorBrush = SolidColor(colors.green),
                decorationBox = { innerTextField ->
                    Box(contentAlignment = Alignment.CenterStart) {
                        if (query.isEmpty()) {
                            Text(
                                "Search by file name...",
                                color = colors.muted,
                                fontSize = 14.sp
                            )
                        }
                        innerTextField()
                    }
                }
            )
            if (query.isNotEmpty()) {
                IconButton(
                    onClick = { onQueryChange("") },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        Icons.Rounded.Close,
                        contentDescription = "Clear search",
                        tint = colors.muted,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
internal fun MediaFilterBar(
    selected: MediaFilter,
    count: Int,
    currentGroup: MediaGroup,
    onGroupChange: (MediaGroup) -> Unit,
    onFilter: (MediaFilter) -> Unit,
    onOpenSortGroup: () -> Unit
) {
    val colors = LocalAppColors.current
    Column(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 4.dp)
    ) {
        // Timeline grouping pills (Days | Months | Years) + FilterList button
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(colors.chipBackground)
                    .padding(3.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                listOf(
                    GroupBy.Date to "Days",
                    GroupBy.Month to "Months",
                    GroupBy.Year to "Years"
                ).forEach { (g, label) ->
                    val active = currentGroup.groupBy == g
                    Text(
                        label,
                        Modifier
                            .clip(RoundedCornerShape(18.dp))
                            .background(if (active) colors.card else Color.Transparent)
                            .clickable { onGroupChange(currentGroup.copy(groupBy = g)) }
                            .padding(horizontal = 12.dp, vertical = 5.dp),
                        fontSize = 12.sp,
                        fontWeight = if (active) FontWeight.Bold else FontWeight.Medium,
                        color = if (active) colors.green else colors.muted
                    )
                }
            }

            Spacer(Modifier.weight(1f))

            FilledTonalIconButton(
                onClick = onOpenSortGroup,
                modifier = Modifier.size(36.dp),
                shape = CircleShape,
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = colors.chipBackground,
                    contentColor = colors.ink
                )
            ) {
                Icon(Icons.Rounded.FilterList, "Sort & Group", modifier = Modifier.size(18.dp))
            }
        }

        Spacer(Modifier.height(8.dp))

        // Media Filter chips (All / Photos / Videos / Favorites) + Count
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "$count ITEMS",
                fontSize = 10.sp,
                letterSpacing = 1.2.sp,
                fontWeight = FontWeight.Bold,
                color = colors.muted
            )

            Spacer(Modifier.weight(1f))

            Row(
                Modifier
                    .clip(RoundedCornerShape(50))
                    .background(colors.chipBackground)
                    .padding(3.dp)
            ) {
                MediaFilter.entries.forEach { filter ->
                    val active = filter == selected
                    Text(
                        filter.label,
                        Modifier
                            .clip(RoundedCornerShape(50))
                            .background(if (active) colors.card else Color.Transparent)
                            .clickable { onFilter(filter) }
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                        fontSize = 11.sp,
                        fontWeight = if (active) FontWeight.SemiBold else FontWeight.Medium,
                        color = if (active) colors.green else colors.muted
                    )
                }
            }
        }
    }
}

@Composable
internal fun VideoBadge(durationMs: Long, modifier: Modifier = Modifier) {
    Row(
        modifier
            .clip(RoundedCornerShape(6.dp))
            .background(Color.Black.copy(0.65f))
            .padding(horizontal = 6.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Rounded.PlayArrow, "Video", tint = Color.White, modifier = Modifier.size(13.dp))
        if (durationMs > 0) {
            Text(
                formatDuration(durationMs),
                color = Color.White,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

internal data class MediaSection(
    val title: String,
    val items: List<Photo>
)

internal fun groupAndSortPhotos(
    photos: List<Photo>,
    sort: MediaSort,
    group: MediaGroup
): List<MediaSection> {
    // 1. Sort items
    val sorted = when (sort.field) {
        SortField.Date -> {
            if (sort.order == SortOrder.Descending) photos.sortedByDescending { it.date }
            else photos.sortedBy { it.date }
        }
        SortField.Name -> {
            if (sort.order == SortOrder.Descending) photos.sortedByDescending { it.name.lowercase() }
            else photos.sortedBy { it.name.lowercase() }
        }
    }

    // 2. Group items
    return when (group.groupBy) {
        GroupBy.None -> listOf(MediaSection("", sorted))
        GroupBy.Date -> {
            val dayKeyFormat = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
            val currentYearFormat = SimpleDateFormat("yyyy", Locale.getDefault())
            val sameYearFormat = SimpleDateFormat("d MMMM", Locale.getDefault())
            val otherYearFormat = SimpleDateFormat("d MMMM yyyy", Locale.getDefault())

            val now = System.currentTimeMillis()
            val todayStr = dayKeyFormat.format(Date(now))
            val yesterdayStr = dayKeyFormat.format(Date(now - 86400000L))
            val currentYearStr = currentYearFormat.format(Date(now))

            val grouped = LinkedHashMap<String, MutableList<Photo>>()
            sorted.forEach { photo ->
                val date = Date(photo.date * 1000)
                val dayKey = dayKeyFormat.format(date)
                val title = when (dayKey) {
                    todayStr -> "Today"
                    yesterdayStr -> "Yesterday"
                    else -> {
                        if (currentYearFormat.format(date) == currentYearStr) sameYearFormat.format(date)
                        else otherYearFormat.format(date)
                    }
                }
                grouped.getOrPut(title) { mutableListOf() }.add(photo)
            }
            val sections = grouped.map { (title, items) -> MediaSection(title, items) }
            if (group.order == SortOrder.Descending) sections else sections.reversed()
        }
        GroupBy.Month -> {
            val monthFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
            val grouped = LinkedHashMap<String, MutableList<Photo>>()
            sorted.forEach { photo ->
                val title = monthFormat.format(Date(photo.date * 1000))
                grouped.getOrPut(title) { mutableListOf() }.add(photo)
            }
            val sections = grouped.map { (title, items) -> MediaSection(title, items) }
            if (group.order == SortOrder.Descending) sections else sections.reversed()
        }
        GroupBy.Year -> {
            val yearFormat = SimpleDateFormat("yyyy", Locale.getDefault())
            val grouped = LinkedHashMap<String, MutableList<Photo>>()
            sorted.forEach { photo ->
                val title = yearFormat.format(Date(photo.date * 1000))
                grouped.getOrPut(title) { mutableListOf() }.add(photo)
            }
            val sections = grouped.map { (title, items) -> MediaSection(title, items) }
            if (group.order == SortOrder.Descending) sections else sections.reversed()
        }
        GroupBy.Name -> {
            val grouped = sorted.groupBy { photo ->
                val first = photo.name.firstOrNull()?.uppercaseChar() ?: '#'
                if (first in 'A'..'Z') first.toString() else "#"
            }
            val sections = grouped.map { (title, items) -> MediaSection(title, items) }
            if (group.order == SortOrder.Ascending) sections.sortedBy { it.title }
            else sections.sortedByDescending { it.title }
        }
    }
}

/**
 * Multi-touch 2-finger pinch gesture detector with continuous tactile zoom scaling
 * and spring transitions to dynamically change grid column count.
 * - Spreading fingers apart (zooming in): decreases column count (larger pictures).
 * - Pinching fingers together (zooming out): increases column count (smaller pictures).
 * Single finger touch and vertical drag are left unconsumed so normal list/grid scrolling is 100% smooth.
 */
@Composable
internal fun Modifier.detectPinchGrid(
    currentColumns: Int,
    minColumns: Int = 1,
    maxColumns: Int = 6,
    onColumnsChange: (Int) -> Unit
): Modifier {
    var pinchScale by remember { mutableFloatStateOf(1f) }
    var pinchCentroid by remember { mutableStateOf(Offset.Zero) }
    val animatedScale by animateFloatAsState(
        targetValue = pinchScale,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "pinchGridScaleAnimation"
    )

    return this
        .graphicsLayer {
            scaleX = animatedScale
            scaleY = animatedScale
            if (pinchCentroid != Offset.Zero && size.width > 0 && size.height > 0) {
                transformOrigin = TransformOrigin(
                    pivotFractionX = (pinchCentroid.x / size.width).coerceIn(0.1f, 0.9f),
                    pivotFractionY = (pinchCentroid.y / size.height).coerceIn(0.1f, 0.9f)
                )
            }
        }
        .pointerInput(currentColumns, minColumns, maxColumns) {
            awaitEachGesture {
                awaitFirstDown(requireUnconsumed = false)
                var accumulatedZoom = 1.0f

                do {
                    val event = awaitPointerEvent()
                    val pointerCount = event.changes.count { it.pressed }

                    if (pointerCount >= 2) {
                        val zoom = event.calculateZoom()
                        val centroid = event.calculateCentroid(useCurrent = true)
                        pinchCentroid = centroid
                        accumulatedZoom *= zoom
                        pinchScale = accumulatedZoom.coerceIn(0.72f, 1.40f)

                        if (accumulatedZoom > 1.20f) {
                            if (currentColumns > minColumns) {
                                onColumnsChange(currentColumns - 1)
                                accumulatedZoom = 1.0f
                                pinchScale = 1.0f
                            } else {
                                accumulatedZoom = 1.20f
                            }
                        } else if (accumulatedZoom < 0.83f) {
                            if (currentColumns < maxColumns) {
                                onColumnsChange(currentColumns + 1)
                                accumulatedZoom = 1.0f
                                pinchScale = 1.0f
                            } else {
                                accumulatedZoom = 0.83f
                            }
                        }

                        event.changes.forEach { it.consume() }
                    }
                } while (event.changes.any { it.pressed })

                pinchScale = 1.0f
            }
        }
}

/**
 * Floating feedback pill displaying current grid column count when pinch-zooming.
 */
@Composable
internal fun GridDensityBadge(
    columns: Int,
    visible: Boolean,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(140)) + scaleIn(initialScale = 0.85f),
        exit = fadeOut(tween(220)) + scaleOut(targetScale = 0.85f),
        modifier = modifier
    ) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color.Black.copy(alpha = 0.75f),
            shadowElevation = 6.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Rounded.GridView,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = when (columns) {
                        1 -> "1 column (large)"
                        else -> "$columns columns"
                    },
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

/**
 * Modern fast-scroll scrollbar for lazy grids:
 * - Comfortable 42dp touch scrubbing lane with smooth thumb.
 * - Dynamic auto-fade: fades in on scroll/drag, smoothly hides after 1.4s of inactivity.
 * - Thumb expands in width and glows green when dragging.
 * - Floating date bubble preview shows formatted month/year next to the thumb while scrubbing.
 */
@Composable
internal fun ModernGridScrollbar(
    gridState: LazyGridState,
    photos: List<Photo>,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current
    val haptic = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()
    var isDragging by remember { mutableStateOf(false) }
    var dragProgress by remember { mutableFloatStateOf(0f) }
    var lastInteractionTime by remember { mutableLongStateOf(0L) }

    val totalItems = gridState.layoutInfo.totalItemsCount
    val visibleItems = gridState.layoutInfo.visibleItemsInfo.size

    // Don't show scrollbar if content does not exceed visible screen
    if (totalItems <= visibleItems || totalItems <= 10) return

    LaunchedEffect(gridState.firstVisibleItemIndex, gridState.firstVisibleItemScrollOffset, isDragging) {
        lastInteractionTime = System.currentTimeMillis()
    }

    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(lastInteractionTime, isDragging, gridState.isScrollInProgress) {
        if (isDragging || gridState.isScrollInProgress) {
            isVisible = true
        } else {
            delay(1400)
            isVisible = false
        }
    }

    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 250),
        label = "scrollbar_alpha"
    )

    val scrollFraction = remember(gridState.firstVisibleItemIndex, totalItems, visibleItems) {
        val maxScrollIndex = (totalItems - visibleItems).coerceAtLeast(1)
        (gridState.firstVisibleItemIndex.toFloat() / maxScrollIndex).coerceIn(0f, 1f)
    }

    val activeFraction = if (isDragging) dragProgress else scrollFraction

    val currentPhoto = remember(activeFraction, photos) {
        if (photos.isEmpty()) null
        else {
            val idx = (activeFraction * (photos.size - 1)).roundToInt().coerceIn(0, photos.size - 1)
            photos.getOrNull(idx)
        }
    }

    val dateBubbleText = remember(currentPhoto) {
        currentPhoto?.let { photo ->
            val date = Date(photo.date * 1000L)
            val cal = Calendar.getInstance().apply { time = date }
            val nowCal = Calendar.getInstance()
            if (cal.get(Calendar.YEAR) == nowCal.get(Calendar.YEAR)) {
                SimpleDateFormat("MMMM", Locale.getDefault()).format(date)
            } else {
                SimpleDateFormat("MMM yyyy", Locale.getDefault()).format(date)
            }
        } ?: ""
    }

    // Trigger haptic feedback when date section changes while scrubbing
    LaunchedEffect(dateBubbleText) {
        if (isDragging && dateBubbleText.isNotEmpty()) {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
    }

    val thumbWidth by animateDpAsState(if (isDragging) 8.dp else 4.dp, label = "thumb_w")
    val thumbColor by animateColorAsState(if (isDragging) colors.green else colors.muted.copy(alpha = 0.6f), label = "thumb_c")

    Box(
        modifier = modifier
            .fillMaxHeight()
            .wrapContentWidth(Alignment.End)
            .graphicsLayer { this.alpha = alpha }
    ) {
        val density = LocalDensity.current
        var trackHeightPx by remember { mutableFloatStateOf(1f) }
        val thumbHeightPx = with(density) { 54.dp.toPx() }
        val availableTrack = (trackHeightPx - thumbHeightPx).coerceAtLeast(1f)
        val thumbOffsetPx = activeFraction * availableTrack
        val maxBubbleY = (trackHeightPx - with(density) { 50.dp.toPx() }).roundToInt()

        // Floating date preview bubble - never wraps text!
        if (isDragging && dateBubbleText.isNotEmpty()) {
            Surface(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(end = 48.dp)
                    .offset {
                        IntOffset(
                            x = 0,
                            y = (thumbOffsetPx - with(density) { 6.dp.roundToPx() }).roundToInt().coerceIn(10, maxBubbleY.coerceAtLeast(10))
                        )
                    },
                shape = RoundedCornerShape(16.dp),
                color = colors.card,
                border = BorderStroke(1.dp, colors.border),
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = dateBubbleText,
                        color = colors.ink,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        }

        // Thumb bar & touch lane - inset by 6dp to avoid Android system back gesture collision
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .width(36.dp)
                .fillMaxHeight()
                .padding(end = 6.dp)
                .onGloballyPositioned { coordinates ->
                    trackHeightPx = coordinates.size.height.toFloat()
                }
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset { IntOffset(0, thumbOffsetPx.roundToInt()) }
                    .size(width = thumbWidth, height = 54.dp)
                    .clip(CircleShape)
                    .background(thumbColor)
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(totalItems) {
                        detectVerticalDragGestures(
                            onDragStart = { offset ->
                                isDragging = true
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                dragProgress = (offset.y / trackHeightPx.coerceAtLeast(1f)).coerceIn(0f, 1f)
                                val targetIndex = (dragProgress * (totalItems - 1)).roundToInt().coerceIn(0, totalItems - 1)
                                coroutineScope.launch {
                                    gridState.scrollToItem(targetIndex)
                                }
                            },
                            onVerticalDrag = { change, _ ->
                                change.consume()
                                dragProgress = (change.position.y / trackHeightPx.coerceAtLeast(1f)).coerceIn(0f, 1f)
                                val targetIndex = (dragProgress * (totalItems - 1)).roundToInt().coerceIn(0, totalItems - 1)
                                coroutineScope.launch {
                                    gridState.scrollToItem(targetIndex)
                                }
                            },
                            onDragEnd = { isDragging = false },
                            onDragCancel = { isDragging = false }
                        )
                    }
            )
        }
    }
}

/**
 * Reusable flat photo grid with pinch-to-zoom columns and modern scrollbar.
 */
@Composable
internal fun SimplePhotoGrid(
    photos: List<Photo>,
    gridColumns: Int = 3,
    onGridColumnsChange: (Int) -> Unit = {},
    onPhoto: (Photo) -> Unit,
    modifier: Modifier = Modifier
) {
    val gridState = rememberLazyGridState()
    var showDensityBadge by remember { mutableStateOf(false) }

    LaunchedEffect(gridColumns) {
        showDensityBadge = true
        delay(1200)
        showDensityBadge = false
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .detectPinchGrid(
                currentColumns = gridColumns,
                minColumns = 1,
                maxColumns = 6,
                onColumnsChange = onGridColumnsChange
            )
    ) {
        LazyVerticalGrid(
            state = gridState,
            columns = GridCells.Fixed(gridColumns),
            contentPadding = PaddingValues(start = 3.dp, end = 3.dp, bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(3.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            items(photos, key = { it.id }) { photo ->
                Box(
                    Modifier
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(4.dp))
                        .clickable { onPhoto(photo) }
                ) {
                    GalleryImage(photo.uri, photo.name, Modifier.fillMaxSize())
                    if (photo.isVideo) {
                        VideoBadge(photo.durationMs, Modifier.align(Alignment.BottomEnd).padding(5.dp))
                    }
                }
            }
        }

        ModernGridScrollbar(
            gridState = gridState,
            photos = photos,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .fillMaxHeight()
        )

        GridDensityBadge(
            columns = gridColumns,
            visible = showDensityBadge,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 28.dp)
        )
    }
}

/**
 * Top option pill selector for time folders: "All in [Folder]" vs "Group".
 */
@Composable
internal fun FolderViewModeSelector(
    allLabel: String,
    mode: FolderViewMode,
    onModeChange: (FolderViewMode) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(colors.chipBackground)
            .padding(3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val isAll = mode == FolderViewMode.All
        Text(
            text = allLabel,
            modifier = Modifier
                .clip(RoundedCornerShape(18.dp))
                .background(if (isAll) colors.card else Color.Transparent)
                .clickable { onModeChange(FolderViewMode.All) }
                .padding(horizontal = 14.dp, vertical = 6.dp),
            fontSize = 12.sp,
            fontWeight = if (isAll) FontWeight.Bold else FontWeight.Medium,
            color = if (isAll) colors.green else colors.muted
        )

        val isGroup = mode == FolderViewMode.Grouped
        Text(
            text = "Group",
            modifier = Modifier
                .clip(RoundedCornerShape(18.dp))
                .background(if (isGroup) colors.card else Color.Transparent)
                .clickable { onModeChange(FolderViewMode.Grouped) }
                .padding(horizontal = 14.dp, vertical = 6.dp),
            fontSize = 12.sp,
            fontWeight = if (isGroup) FontWeight.Bold else FontWeight.Medium,
            color = if (isGroup) colors.green else colors.muted
        )
    }
}

/**
 * Visual card item representing a time folder (Year, Month, or Date).
 */
@Composable
internal fun FolderCard(
    coverUri: Uri?,
    title: String,
    subtitle: String,
    badgeText: String? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.95f)
                .clip(RoundedCornerShape(18.dp))
                .background(colors.cardSecondary)
        ) {
            if (coverUri != null) {
                GalleryImage(
                    model = coverUri,
                    description = title,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Rounded.Folder,
                        contentDescription = null,
                        tint = colors.muted.copy(alpha = 0.5f),
                        modifier = Modifier.size(48.dp)
                    )
                }
            }

            if (badgeText != null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.Black.copy(alpha = 0.6f))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        badgeText,
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, start = 2.dp, end = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = colors.ink
                )
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = colors.muted
                )
            }
            Icon(
                Icons.Rounded.ChevronRight,
                contentDescription = null,
                tint = colors.muted,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

/**
 * Hierarchical Time Folder Explorer:
 * - Root: Year folders (2026, 2025, ...)
 * - Inside Year: "All in [Year]" flat grid OR "Group" into Month folders
 * - Inside Month: "All in [Month]" flat grid OR "Group" into Date folders
 * - Inside Date: Day photos flat grid
 * Pinch-to-zoom grid density and modern scrollbar apply to every photo view!
 */
@Composable
internal fun TimeFoldersView(
    photos: List<Photo>,
    gridColumns: Int,
    onGridColumnsChange: (Int) -> Unit,
    onPhoto: (Photo) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current
    var selectedYear by remember { mutableStateOf<Int?>(null) }
    var yearMode by remember { mutableStateOf(FolderViewMode.Grouped) }
    var selectedMonth by remember { mutableStateOf<Int?>(null) }
    var monthMode by remember { mutableStateOf(FolderViewMode.Grouped) }
    var selectedDay by remember { mutableStateOf<Int?>(null) }

    // System BackHandler for hierarchical stepping
    BackHandler(enabled = selectedDay != null || selectedMonth != null || selectedYear != null) {
        when {
            selectedDay != null -> selectedDay = null
            selectedMonth != null -> selectedMonth = null
            selectedYear != null -> selectedYear = null
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        when {
            // Level 3: Day view
            selectedYear != null && selectedMonth != null && selectedDay != null -> {
                val dayPhotos = remember(photos, selectedYear, selectedMonth, selectedDay) {
                    photos.filter { it.year == selectedYear && it.month == selectedMonth && it.day == selectedDay }
                }
                val dayLabel = dayPhotos.firstOrNull()?.dayLabel ?: "$selectedDay"

                // Breadcrumb Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { selectedDay = null }) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = colors.ink)
                    }
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = "$dayLabel $selectedYear",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.ink
                        )
                        Text(
                            text = "${dayPhotos.size} items",
                            fontSize = 11.sp,
                            color = colors.muted
                        )
                    }
                }

                SimplePhotoGrid(
                    photos = dayPhotos,
                    gridColumns = gridColumns,
                    onGridColumnsChange = onGridColumnsChange,
                    onPhoto = onPhoto
                )
            }

            // Level 2: Month view
            selectedYear != null && selectedMonth != null -> {
                val monthPhotos = remember(photos, selectedYear, selectedMonth) {
                    photos.filter { it.year == selectedYear && it.month == selectedMonth }
                }
                val monthName = monthPhotos.firstOrNull()?.monthName ?: "Month $selectedMonth"

                // Breadcrumb Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { selectedMonth = null }) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = colors.ink)
                    }
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = "$monthName $selectedYear",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.ink
                        )
                        Text(
                            text = "${monthPhotos.size} items",
                            fontSize = 11.sp,
                            color = colors.muted
                        )
                    }

                    // Options: "All in [Month]" vs "Group"
                    FolderViewModeSelector(
                        allLabel = "All in $monthName",
                        mode = monthMode,
                        onModeChange = { monthMode = it }
                    )
                }

                if (monthMode == FolderViewMode.All) {
                    SimplePhotoGrid(
                        photos = monthPhotos,
                        gridColumns = gridColumns,
                        onGridColumnsChange = onGridColumnsChange,
                        onPhoto = onPhoto
                    )
                } else {
                    val dateGroups = remember(monthPhotos) {
                        monthPhotos.groupBy { it.day }.toList().sortedByDescending { it.first }
                    }
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(dateGroups, key = { it.first }) { (day, items) ->
                            val label = items.firstOrNull()?.dayLabel ?: "$day $monthName"
                            FolderCard(
                                coverUri = items.firstOrNull()?.uri,
                                title = label,
                                subtitle = "${items.size} ${if (items.size == 1) "item" else "items"}",
                                badgeText = "${items.size}",
                                onClick = {
                                    selectedDay = day
                                }
                            )
                        }
                    }
                }
            }

            // Level 1: Year view
            selectedYear != null -> {
                val yearPhotos = remember(photos, selectedYear) {
                    photos.filter { it.year == selectedYear }
                }

                // Breadcrumb Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { selectedYear = null }) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = colors.ink)
                    }
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = "$selectedYear",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.ink
                        )
                        Text(
                            text = "${yearPhotos.size} items",
                            fontSize = 11.sp,
                            color = colors.muted
                        )
                    }

                    // Options: "All in 2026" vs "Group"
                    FolderViewModeSelector(
                        allLabel = "All in $selectedYear",
                        mode = yearMode,
                        onModeChange = { yearMode = it }
                    )
                }

                if (yearMode == FolderViewMode.All) {
                    SimplePhotoGrid(
                        photos = yearPhotos,
                        gridColumns = gridColumns,
                        onGridColumnsChange = onGridColumnsChange,
                        onPhoto = onPhoto
                    )
                } else {
                    val monthGroups = remember(yearPhotos) {
                        yearPhotos.groupBy { it.month }.toList().sortedByDescending { it.first }
                    }
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(monthGroups, key = { it.first }) { (month, items) ->
                            val monthName = items.firstOrNull()?.monthName ?: "Month $month"
                            FolderCard(
                                coverUri = items.firstOrNull()?.uri,
                                title = monthName,
                                subtitle = "${items.size} ${if (items.size == 1) "item" else "items"}",
                                badgeText = "${items.size}",
                                onClick = {
                                    selectedMonth = month
                                    monthMode = FolderViewMode.Grouped
                                }
                            )
                        }
                    }
                }
            }

            // Level 0: Root - Year list
            else -> {
                val yearGroups = remember(photos) {
                    photos.groupBy { it.year }.toList().sortedByDescending { it.first }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "${yearGroups.size} YEARS · ${photos.size} ITEMS",
                        fontSize = 10.sp,
                        letterSpacing = 1.2.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.muted
                    )
                }

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(yearGroups, key = { it.first }) { (year, items) ->
                        val monthsCount = items.map { it.month }.distinct().size
                        FolderCard(
                            coverUri = items.firstOrNull()?.uri,
                            title = "$year",
                            subtitle = "$monthsCount ${if (monthsCount == 1) "month" else "months"} · ${items.size} items",
                            badgeText = "${items.size}",
                            onClick = {
                                selectedYear = year
                                yearMode = FolderViewMode.Grouped
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
internal fun PhotoGrid(
    photos: List<Photo>,
    filter: MediaFilter,
    onFilter: (MediaFilter) -> Unit,
    sort: MediaSort,
    group: MediaGroup,
    onGroupChange: (MediaGroup) -> Unit,
    onOpenSortGroup: () -> Unit,
    selectedIds: Set<Long>,
    favoriteIds: Set<Long>,
    onPhoto: (Photo) -> Unit,
    onLongPress: (Photo) -> Unit,
    onRangeSelect: ((Set<Long>) -> Unit)? = null,
    empty: Boolean,
    searchQuery: String = "",
    gridColumns: Int = 3,
    onGridColumnsChange: (Int) -> Unit = {}
) {
    val colors = LocalAppColors.current
    val haptic = LocalHapticFeedback.current
    val selecting = selectedIds.isNotEmpty()
    val sections = remember(photos, sort, group) { groupAndSortPhotos(photos, sort, group) }
    val flatPhotos = remember(sections) { sections.flatMap { it.items } }
    val gridState = rememberLazyGridState()
    var showDensityBadge by remember { mutableStateOf(false) }
    var dragStartIndex by remember { mutableIntStateOf(-1) }
    var dragCurrentIndex by remember { mutableIntStateOf(-1) }

    LaunchedEffect(gridColumns) {
        showDensityBadge = true
        delay(1200)
        showDensityBadge = false
    }

    Column(Modifier.fillMaxSize()) {
        MediaFilterBar(
            selected = filter,
            count = photos.size,
            currentGroup = group,
            onGroupChange = onGroupChange,
            onFilter = onFilter,
            onOpenSortGroup = onOpenSortGroup
        )

        if (photos.isEmpty() && empty) {
            if (searchQuery.isNotBlank()) {
                EmptyState(
                    Icons.Rounded.Search,
                    "No results found",
                    "No photos or videos matching \"$searchQuery\"",
                    null
                )
            } else {
                when (filter) {
                    MediaFilter.All -> EmptyState(Icons.Rounded.Image, "A fresh start", "Your photos and videos will show up here once gallery access is on.", null)
                    MediaFilter.Photos -> EmptyState(Icons.Rounded.Image, "No photos here", "Photos you take or save will show up here.", null)
                    MediaFilter.Videos -> EmptyState(Icons.Rounded.Videocam, "No videos here", "Videos you record or save will show up here.", null)
                    MediaFilter.Favorites -> EmptyState(Icons.Rounded.Favorite, "No favorites yet", "Tap the heart icon on any photo or video to add it to your favorites.", null)
                }
            }
        } else {
            Box(
                Modifier
                    .fillMaxSize()
                    .detectPinchGrid(
                        currentColumns = gridColumns,
                        minColumns = 1,
                        maxColumns = 6,
                        onColumnsChange = onGridColumnsChange
                    )
                    .pointerInput(selecting, flatPhotos) {
                        if (selecting) {
                            detectDragGestures(
                                onDragStart = { offset ->
                                    val hitItem = gridState.layoutInfo.visibleItemsInfo.firstOrNull { item ->
                                        offset.x >= item.offset.x && offset.x <= item.offset.x + item.size.width &&
                                        offset.y >= item.offset.y && offset.y <= item.offset.y + item.size.height
                                    }
                                    val photoId = hitItem?.key as? Long
                                    val idx = flatPhotos.indexOfFirst { it.id == photoId }
                                    if (idx >= 0) {
                                        dragStartIndex = idx
                                        dragCurrentIndex = idx
                                    }
                                },
                                onDrag = { change, _ ->
                                    if (dragStartIndex >= 0) {
                                        change.consume()
                                        val pos = change.position
                                        val hitItem = gridState.layoutInfo.visibleItemsInfo.firstOrNull { item ->
                                            pos.x >= item.offset.x && pos.x <= item.offset.x + item.size.width &&
                                            pos.y >= item.offset.y && pos.y <= item.offset.y + item.size.height
                                        }
                                        val photoId = hitItem?.key as? Long
                                        val idx = flatPhotos.indexOfFirst { it.id == photoId }
                                        if (idx >= 0 && idx != dragCurrentIndex) {
                                            dragCurrentIndex = idx
                                            val start = minOf(dragStartIndex, idx)
                                            val end = maxOf(dragStartIndex, idx)
                                            val rangeIds = (start..end).map { flatPhotos[it].id }.toSet()
                                            onRangeSelect?.invoke(rangeIds)
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        }
                                    }
                                },
                                onDragEnd = {
                                    dragStartIndex = -1
                                    dragCurrentIndex = -1
                                },
                                onDragCancel = {
                                    dragStartIndex = -1
                                    dragCurrentIndex = -1
                                }
                            )
                        }
                    }
            ) {
                LazyVerticalGrid(
                    state = gridState,
                    columns = GridCells.Fixed(gridColumns),
                    contentPadding = PaddingValues(start = 3.dp, end = 3.dp, bottom = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    sections.forEach { section ->
                        if (section.title.isNotEmpty()) {
                            item(span = { GridItemSpan(maxLineSpan) }) {
                                Row(
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        section.title,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = colors.ink
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        "${section.items.size}",
                                        fontSize = 12.sp,
                                        color = colors.muted
                                    )
                                }
                            }
                        }

                        items(section.items, key = { it.id }) { photo ->
                            val selected = photo.id in selectedIds
                            val isFav = photo.id in favoriteIds
                            val inset by animateFloatAsState(if (selected) 0.86f else 1f, label = "selection inset")
                            Box(
                                Modifier
                                    .aspectRatio(1f)
                                    .background(if (selected) colors.green.copy(alpha = 0.15f) else Color.Transparent)
                                    .combinedClickable(onLongClick = { onLongPress(photo) }) { onPhoto(photo) }
                            ) {
                                Box(
                                    Modifier
                                        .fillMaxSize()
                                        .graphicsLayer { scaleX = inset; scaleY = inset }
                                        .clip(RoundedCornerShape(if (selected) 10.dp else 4.dp))
                                ) {
                                    GalleryImage(photo.uri, photo.name, Modifier.fillMaxSize())
                                    if (photo.isVideo) VideoBadge(photo.durationMs, Modifier.align(Alignment.BottomEnd).padding(5.dp))
                                }
                                if (isFav && !selecting) {
                                    Box(
                                        Modifier
                                            .align(Alignment.TopStart)
                                            .padding(5.dp)
                                            .size(20.dp)
                                            .clip(CircleShape)
                                            .background(Color.Black.copy(alpha = 0.5f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Rounded.Favorite, "Favorite", tint = Color(0xFFE53935), modifier = Modifier.size(12.dp))
                                    }
                                }
                                if (selecting) SelectionMark(selected, Modifier.align(Alignment.TopEnd).padding(6.dp))
                            }
                        }
                    }
                }

                ModernGridScrollbar(
                    gridState = gridState,
                    photos = photos,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .fillMaxHeight()
                )

                GridDensityBadge(
                    columns = gridColumns,
                    visible = showDensityBadge,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 28.dp)
                )
            }
        }
    }
}

@Composable
internal fun Albums(
    photos: List<Photo>,
    favoritePhotos: List<Photo> = emptyList(),
    gridColumns: Int = 3,
    onGridColumnsChange: (Int) -> Unit = {},
    onAlbum: (String) -> Unit,
    onOpenFavorites: () -> Unit = {},
    onPhoto: (Photo) -> Unit = {}
) {
    val colors = LocalAppColors.current
    var currentTab by remember { mutableStateOf(AlbumTab.Device) }

    Column(Modifier.fillMaxSize()) {
        // Top Switcher: [ Device Albums ] | [ Year Folders ]
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(22.dp))
                    .background(colors.chipBackground)
                    .padding(3.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                listOf(
                    AlbumTab.Device to "Device Albums",
                    AlbumTab.Years to "Year Folders"
                ).forEach { (tab, label) ->
                    val active = currentTab == tab
                    Text(
                        text = label,
                        modifier = Modifier
                            .clip(RoundedCornerShape(18.dp))
                            .background(if (active) colors.card else Color.Transparent)
                            .clickable { currentTab = tab }
                            .padding(horizontal = 16.dp, vertical = 7.dp),
                        fontSize = 12.sp,
                        fontWeight = if (active) FontWeight.Bold else FontWeight.Medium,
                        color = if (active) colors.green else colors.muted
                    )
                }
            }
        }

        if (currentTab == AlbumTab.Years) {
            TimeFoldersView(
                photos = photos,
                gridColumns = gridColumns,
                onGridColumnsChange = onGridColumnsChange,
                onPhoto = onPhoto
            )
        } else {
            val albums = remember(photos) {
                photos.groupBy { it.album }.toList().sortedByDescending { it.second.size }
            }
            if (albums.isEmpty() && favoritePhotos.isEmpty()) {
                EmptyState(Icons.Rounded.Folder, "No albums yet", "Albums from your device will appear here.", null)
                return@Column
            }
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                if (favoritePhotos.isNotEmpty()) {
                    item {
                        Column(Modifier.clickable { onOpenFavorites() }) {
                            Box(
                                Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(0.92f)
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(colors.cardSecondary)
                            ) {
                                GalleryImage(
                                    favoritePhotos.first().uri,
                                    "Favorites",
                                    Modifier.fillMaxSize()
                                )
                                Box(
                                    Modifier
                                        .fillMaxSize()
                                        .background(Color.Black.copy(alpha = 0.35f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Rounded.Favorite,
                                        "Favorites",
                                        tint = Color(0xFFE53935),
                                        modifier = Modifier.size(44.dp)
                                    )
                                }
                            }
                            Row(Modifier.fillMaxWidth().padding(top = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f)) {
                                    Text("Favorites", fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis, color = colors.ink)
                                    Text("${favoritePhotos.size} items", fontSize = 12.sp, color = colors.muted)
                                }
                                Icon(Icons.Rounded.ChevronRight, null, tint = colors.muted)
                            }
                        }
                    }
                }
                items(albums, key = { it.first }) { (name, items) ->
                    Column(Modifier.clickable { onAlbum(name) }) {
                        GalleryImage(items.first().uri, name, Modifier.fillMaxWidth().aspectRatio(0.92f).clip(RoundedCornerShape(18.dp)))
                        Row(Modifier.fillMaxWidth().padding(top = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(name, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis, color = colors.ink)
                                Text("${items.size} items", fontSize = 12.sp, color = colors.muted)
                            }
                            Icon(Icons.Rounded.ChevronRight, null, tint = colors.muted)
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun SwipePage(
    photos: List<Photo>,
    onKeep: (Photo) -> Unit,
    onTrash: (Photo) -> Unit,
    onOpen: (Photo) -> Unit
) {
    val colors = LocalAppColors.current
    var selectedAlbum by remember { mutableStateOf<String?>(null) }
    var albumMenuOpen by remember { mutableStateOf(false) }
    val albums = remember(photos) { photos.groupBy { it.album }.toList().sortedByDescending { it.second.size } }
    val albumPhotos = remember(photos, selectedAlbum) { photos.filter { selectedAlbum == null || it.album == selectedAlbum } }
    val queue = remember(selectedAlbum) { mutableStateListOf<Photo>().apply { addAll(albumPhotos) } }
    val total = remember(selectedAlbum) { albumPhotos.size }
    var drag by remember { mutableFloatStateOf(0f) }
    var settling by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val rotation by animateFloatAsState(drag / 28f, label = "card rotation")
    val current = queue.firstOrNull()

    Column(Modifier.fillMaxSize().padding(horizontal = 24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(Modifier.height(6.dp))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("SORT ALBUM", fontSize = 10.sp, letterSpacing = 1.1.sp, color = colors.muted, fontWeight = FontWeight.Bold)
            Spacer(Modifier.weight(1f))
            Box {
                OutlinedButton(
                    onClick = { albumMenuOpen = true },
                    shape = RoundedCornerShape(14.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 7.dp)
                ) {
                    Icon(Icons.Rounded.Folder, null, tint = colors.green, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(7.dp))
                    Text(selectedAlbum ?: "All albums", maxLines = 1, overflow = TextOverflow.Ellipsis, fontSize = 12.sp, color = colors.ink)
                    Spacer(Modifier.width(2.dp))
                    Icon(Icons.Rounded.ChevronRight, null, tint = colors.muted, modifier = Modifier.size(18.dp).rotate(90f))
                }
                DropdownMenu(expanded = albumMenuOpen, onDismissRequest = { albumMenuOpen = false }) {
                    DropdownMenuItem(
                        text = { Text("All albums (${photos.size})", maxLines = 1) },
                        leadingIcon = { Icon(Icons.Rounded.GridView, null) },
                        trailingIcon = { if (selectedAlbum == null) Icon(Icons.Rounded.Check, null, tint = colors.green) },
                        onClick = { selectedAlbum = null; albumMenuOpen = false }
                    )
                    albums.forEach { (name, items) ->
                        DropdownMenuItem(
                            text = { Text("$name (${items.size})", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                            leadingIcon = { Icon(Icons.Rounded.Folder, null) },
                            trailingIcon = { if (selectedAlbum == name) Icon(Icons.Rounded.Check, null, tint = colors.green) },
                            onClick = { selectedAlbum = name; albumMenuOpen = false }
                        )
                    }
                }
            }
        }
        Spacer(Modifier.height(9.dp))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Rounded.Swipe, null, tint = colors.green, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(if (current == null) "ALL CAUGHT UP" else "${queue.size} LEFT TO SORT", fontSize = 10.sp, letterSpacing = 1.2.sp, color = colors.muted, fontWeight = FontWeight.Bold)
            Spacer(Modifier.weight(1f))
            Text("${(total - queue.size).coerceAtMost(total)} / $total", color = colors.muted, fontSize = 12.sp)
        }
        Spacer(Modifier.height(13.dp))
        if (current != null) {
            Box(Modifier.weight(1f).fillMaxWidth().padding(bottom = 14.dp), contentAlignment = Alignment.Center) {
                Box(Modifier.fillMaxSize().padding(horizontal = 10.dp, vertical = 8.dp).clip(RoundedCornerShape(30.dp)).background(colors.cardSecondary))
                Box(
                    Modifier
                        .fillMaxSize()
                        .offset { IntOffset(drag.roundToInt(), 0) }
                        .rotate(rotation)
                        .clip(RoundedCornerShape(30.dp))
                        .background(colors.card)
                        .pointerInput(current.id, "tap") {
                            detectTapGestures(onTap = { if (!settling) onOpen(current) })
                        }
                        .pointerInput(current.id) {
                            detectDragGestures(
                                onDragEnd = {
                                    if (settling) return@detectDragGestures
                                    val decision = when { drag > 120 -> 1; drag < -120 -> -1; else -> 0 }
                                    settling = true
                                    scope.launch {
                                        val motion = Animatable(drag)
                                        if (decision == 0) {
                                            motion.animateTo(0f, spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow)) { drag = value }
                                        } else {
                                            val exitX = size.width.toFloat() * if (decision > 0) 1.35f else -1.35f
                                            motion.animateTo(exitX, tween(260, easing = FastOutSlowInEasing)) { drag = value }
                                            if (decision > 0) onKeep(current) else onTrash(current)
                                            drag = 0f
                                            queue.remove(current)
                                        }
                                        settling = false
                                    }
                                },
                                onDragCancel = {
                                    if (!settling) {
                                        scope.launch {
                                            val motion = Animatable(drag)
                                            motion.animateTo(0f, spring(dampingRatio = Spring.DampingRatioNoBouncy)) { drag = value }
                                        }
                                    }
                                }
                            ) { change, amount ->
                                change.consume()
                                if (!settling) drag += amount.x
                            }
                        }
                ) {
                    GalleryImage(current.uri, current.name, Modifier.fillMaxSize())
                    if (current.isVideo) VideoBadge(current.durationMs, Modifier.align(Alignment.BottomEnd).padding(22.dp))
                    if (drag > 35) SwipeStamp("KEEP", colors.green, Modifier.align(Alignment.TopStart).padding(22.dp).rotate(-12f))
                    if (drag < -35) SwipeStamp("TRASH", colors.red, Modifier.align(Alignment.TopEnd).padding(22.dp).rotate(12f))
                    Box(Modifier.fillMaxWidth().align(Alignment.BottomCenter).height(130.dp).background(Color.Black.copy(alpha = 0.5f)))
                    Column(Modifier.align(Alignment.BottomStart).padding(22.dp)) {
                        Text(SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(current.date * 1000)), color = Color.White.copy(0.75f), fontSize = 12.sp)
                        Text(current.name, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
            Text("TAP TO VIEW  ·  RIGHT TO KEEP  ·  LEFT TO TRASH", fontSize = 9.sp, letterSpacing = 1.1.sp, color = colors.muted, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(14.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                RoundAction(Icons.Rounded.DeleteOutline, "Move to trash", colors.red) { onTrash(current); queue.remove(current) }
                Spacer(Modifier.width(34.dp))
                RoundAction(Icons.Rounded.Check, "Keep photo", colors.green, large = true) { onKeep(current); queue.remove(current) }
            }
            Spacer(Modifier.height(14.dp))
        } else {
            val title = selectedAlbum?.let { "All sorted in $it" } ?: "Nicely done"
            val body = selectedAlbum?.let { "You’re all caught up in this album. Choose another album above to keep sorting." }
                ?: "You’re all caught up. Your keepers are right where they belong."
            EmptyState(Icons.Rounded.Check, title, body, null)
        }
    }
}

@Composable
internal fun SwipeStamp(label: String, color: Color, modifier: Modifier = Modifier) {
    Text(
        label,
        modifier
            .clip(RoundedCornerShape(9.dp))
            .background(Color.White.copy(0.9f))
            .padding(horizontal = 14.dp, vertical = 8.dp),
        color = color,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 22.sp,
        letterSpacing = 1.sp
    )
}

@Composable
internal fun RoundAction(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, color: Color, large: Boolean = false, onClick: () -> Unit) {
    val colors = LocalAppColors.current
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        FilledIconButton(
            onClick = onClick,
            modifier = Modifier.size(if (large) 66.dp else 54.dp),
            shape = CircleShape,
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = if (large) colors.green else colors.card,
                contentColor = if (large) Color.White else color
            )
        ) {
            Icon(icon, label, modifier = Modifier.size(if (large) 29.dp else 23.dp))
        }
        Spacer(Modifier.height(5.dp))
        Text(label, fontSize = 10.sp, color = colors.muted)
    }
}

/**
 * Trash bin screen:
 * - Sorted by reverse order (newest trashed items on top)
 * - Standard Material 3 design
 * - Icon-only buttons (no text labels like "Delete" or "Restore")
 * - Restore button uses BLACK color only
 * - Delete button uses correct standard RED shade
 */
@Composable
internal fun TrashPage(
    photos: List<Photo>,
    onRestore: (Photo) -> Unit,
    onDelete: (Photo) -> Unit,
    onEmpty: (Set<String>) -> Unit
) {
    val colors = LocalAppColors.current

    if (photos.isEmpty()) {
        EmptyState(
            Icons.Rounded.DeleteOutline,
            "Your bin is empty",
            "Photos you swipe away will wait here in case you change your mind.",
            null
        )
        return
    }

    Column(Modifier.fillMaxSize()) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("${photos.size} items", color = colors.muted, fontSize = 13.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
            TextButton(
                onClick = { onEmpty(photos.map { it.id.toString() }.toSet()) }
            ) {
                Icon(Icons.Rounded.DeleteForever, null, tint = colors.red, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(5.dp))
                Text("Empty bin", color = colors.red, fontWeight = FontWeight.SemiBold)
            }
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(photos, key = { it.id }) { photo ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = colors.card),
                    border = BorderStroke(1.dp, colors.border),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column {
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .aspectRatio(1f)
                        ) {
                            GalleryImage(
                                photo.uri,
                                photo.name,
                                Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                            )
                            if (photo.isVideo) VideoBadge(photo.durationMs, Modifier.align(Alignment.BottomEnd).padding(6.dp))
                        }

                        Column(Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
                            Text(
                                photo.name,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = colors.ink
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(photo.date * 1000)),
                                fontSize = 11.sp,
                                color = colors.muted
                            )
                            Spacer(Modifier.height(8.dp))

                            // Action buttons: ICON ONLY, CONSIDERED AS BUTTONS
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Restore Button: BLACK COLOR ONLY
                                FilledTonalIconButton(
                                    onClick = { onRestore(photo) },
                                    modifier = Modifier.size(42.dp),
                                    shape = CircleShape,
                                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                                        containerColor = colors.chipBackground,
                                        contentColor = RestoreBlack
                                    )
                                ) {
                                    Icon(
                                        Icons.Rounded.Restore,
                                        contentDescription = "Restore",
                                        tint = RestoreBlack,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }

                                // Delete Permanently Button: CORRECT RED SHADE
                                // Direct deletion invokes Android OS confirmation prompt directly (no duplicate in-app dialog)
                                FilledTonalIconButton(
                                    onClick = { onDelete(photo) },
                                    modifier = Modifier.size(42.dp),
                                    shape = CircleShape,
                                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                                        containerColor = colors.red.copy(alpha = 0.12f),
                                        contentColor = colors.red
                                    )
                                ) {
                                    Icon(
                                        Icons.Rounded.DeleteForever,
                                        contentDescription = "Delete permanently",
                                        tint = colors.red,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Single item inside the pager with pinch-to-zoom and pan support for both images and videos.
 */
@Composable
internal fun ViewerItem(
    item: Photo,
    modifier: Modifier,
    onPlay: (() -> Unit)?
) {
    Box(modifier, contentAlignment = Alignment.Center) {
        GalleryImage(item.uri, item.name, Modifier.fillMaxSize(), ContentScale.Fit)
        if (item.isVideo) {
            FilledIconButton(
                onClick = { onPlay?.invoke() },
                enabled = onPlay != null,
                modifier = Modifier.size(72.dp),
                shape = CircleShape,
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = Color.Black.copy(0.55f),
                    contentColor = Color.White
                )
            ) {
                Icon(Icons.Rounded.PlayArrow, "Play video", modifier = Modifier.size(40.dp))
            }
        }
    }
}

/**
 * Zoomable item inside HorizontalPager. Supports finger pinch-to-zoom & pan on images and videos.
 * When scale is 1x (not zoomed), single-finger drag events are NOT consumed, allowing HorizontalPager
 * to smoothly swipe left and right between photos.
 */
@Composable
internal fun ZoomablePagerItem(
    photo: Photo,
    isCurrent: Boolean,
    onTap: () -> Unit,
    onPlay: () -> Unit,
    onZoomStateChanged: (Boolean) -> Unit
) {
    var scale by remember(photo.id) { mutableFloatStateOf(1f) }
    var offset by remember(photo.id) { mutableStateOf(Offset.Zero) }

    LaunchedEffect(isCurrent) {
        if (!isCurrent && scale != 1f) {
            scale = 1f
            offset = Offset.Zero
            onZoomStateChanged(false)
        }
    }

    BoxWithConstraints(
        Modifier
            .fillMaxSize()
            .pointerInput(photo.id) {
                awaitEachGesture {
                    awaitFirstDown(requireUnconsumed = false)
                    do {
                        val event = awaitPointerEvent()
                        val pointerCount = event.changes.count { it.pressed }
                        if (pointerCount >= 2) {
                            // Two-finger pinch-to-zoom and two-finger pan
                            val zoomChange = event.calculateZoom()
                            val panChange = event.calculatePan()
                            if (zoomChange != 1f || panChange != Offset.Zero) {
                                val newScale = (scale * zoomChange).coerceIn(1f, 5f)
                                scale = newScale
                                val maxOffsetX = (size.width * (newScale - 1f) / 2f).coerceAtLeast(0f)
                                val maxOffsetY = (size.height * (newScale - 1f) / 2f).coerceAtLeast(0f)
                                offset = if (newScale <= 1.02f) {
                                    Offset.Zero
                                } else {
                                    Offset(
                                        (offset.x + panChange.x).coerceIn(-maxOffsetX, maxOffsetX),
                                        (offset.y + panChange.y).coerceIn(-maxOffsetY, maxOffsetY)
                                    )
                                }
                                onZoomStateChanged(newScale > 1.05f)
                                event.changes.forEach { it.consume() }
                            }
                        } else if (pointerCount == 1 && scale > 1.05f) {
                            // Single-finger pan only when zoomed in
                            val panChange = event.calculatePan()
                            if (panChange != Offset.Zero) {
                                val maxOffsetX = (size.width * (scale - 1f) / 2f).coerceAtLeast(0f)
                                val maxOffsetY = (size.height * (scale - 1f) / 2f).coerceAtLeast(0f)
                                offset = Offset(
                                    (offset.x + panChange.x).coerceIn(-maxOffsetX, maxOffsetX),
                                    (offset.y + panChange.y).coerceIn(-maxOffsetY, maxOffsetY)
                                )
                                event.changes.forEach { it.consume() }
                            }
                        }
                        // Note: When pointerCount == 1 and scale <= 1.05f, we DO NOT consume any drag events!
                        // This allows HorizontalPager to seamlessly swipe left/right between next and previous photos.
                    } while (event.changes.any { it.pressed })
                }
            }
            .pointerInput(photo.id) {
                detectTapGestures(
                    onTap = { onTap() },
                    onDoubleTap = {
                        if (scale > 1.05f) {
                            scale = 1f
                            offset = Offset.Zero
                            onZoomStateChanged(false)
                        } else {
                            scale = 2.5f
                            onZoomStateChanged(true)
                        }
                    }
                )
            }
    ) {
        ViewerItem(
            item = photo,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    translationX = offset.x
                    translationY = offset.y
                },
            onPlay = { onPlay() }
        )

        // Reset zoom pill button if zoomed
        if (scale > 1.05f) {
            Box(
                Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(top = 64.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.Black.copy(alpha = 0.65f))
                    .clickable {
                        scale = 1f
                        offset = Offset.Zero
                        onZoomStateChanged(false)
                    }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.ZoomInMap, null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "${String.format(java.util.Locale.US, "%.1fx", scale)} · Tap to reset",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

/**
 * Comfy bottom thumbnail filmstrip for quick jumping between photos in the album.
 */
@Composable
internal fun ThumbnailStrip(
    photos: List<Photo>,
    currentIndex: Int,
    onSelectIndex: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    LaunchedEffect(currentIndex) {
        if (photos.isNotEmpty() && currentIndex in photos.indices) {
            listState.animateScrollToItem((currentIndex - 2).coerceAtLeast(0))
        }
    }

    LazyRow(
        state = listState,
        modifier = modifier
            .fillMaxWidth()
            .height(58.dp)
            .background(Color.Black.copy(alpha = 0.45f))
            .padding(vertical = 6.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        itemsIndexed(photos, key = { _, item -> item.id }) { index, item ->
            val isSelected = index == currentIndex
            Box(
                modifier = Modifier
                    .size(width = if (isSelected) 46.dp else 36.dp, height = 46.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .then(
                        if (isSelected) Modifier.background(Color.White, RoundedCornerShape(8.dp)).padding(2.dp).clip(RoundedCornerShape(6.dp))
                        else Modifier
                    )
                    .clickable { onSelectIndex(index) }
            ) {
                GalleryImage(
                    item.uri,
                    item.name,
                    Modifier.fillMaxSize(),
                    ContentScale.Crop
                )
                if (item.isVideo) {
                    Box(
                        Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.25f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Rounded.PlayArrow,
                            null,
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Modern floating bottom action capsule with comfy spacing and haptic-friendly buttons.
 */
@Composable
internal fun BottomActionCapsule(
    photo: Photo,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onShare: () -> Unit,
    onDetails: () -> Unit,
    onHide: (() -> Unit)? = null,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(32.dp),
        color = Color(0xFF1E1E1E).copy(alpha = 0.90f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Share
            IconButton(onClick = onShare, modifier = Modifier.size(42.dp)) {
                Icon(Icons.Rounded.Share, "Share", tint = Color.White, modifier = Modifier.size(20.dp))
            }

            // Favorite
            IconButton(onClick = onToggleFavorite, modifier = Modifier.size(42.dp)) {
                Icon(
                    if (isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                    "Favorite",
                    tint = if (isFavorite) Color(0xFFE53935) else Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Hide to Vault
            if (onHide != null) {
                IconButton(onClick = onHide, modifier = Modifier.size(42.dp)) {
                    Icon(Icons.Rounded.Lock, "Hide to Vault", tint = Color.White, modifier = Modifier.size(20.dp))
                }
            }

            // Details / "i"
            IconButton(onClick = onDetails, modifier = Modifier.size(42.dp)) {
                Icon(Icons.Rounded.Info, "Details", tint = Color.White, modifier = Modifier.size(20.dp))
            }

            // Delete / Trash
            IconButton(onClick = onDelete, modifier = Modifier.size(42.dp)) {
                Icon(Icons.Rounded.DeleteOutline, "Move to trash", tint = Color.White, modifier = Modifier.size(20.dp))
            }
        }
    }
}

/**
 * Fullscreen Photo & Video Detail viewer.
 * - Swipe left/right smoothly between next/previous photos
 * - Pinch-to-zoom & pan on images and videos
 * - Tap image/video to toggle controls visibility
 * - Controls with stylish translucent gradient backgrounds
 * - Comfy bottom thumbnail filmstrip for quick scrubbing
 * - Floating modern action capsule (Share, Favorite, Hide, Info, Delete)
 * - "i" info icon button to view all details, rename, or delete metadata
 */
@Composable
internal fun PhotoDetail(
    initialPhoto: Photo,
    photos: List<Photo>,
    favoriteIds: Set<Long>,
    onToggleFavorite: (Long) -> Unit,
    onClose: () -> Unit,
    onPlay: (Photo) -> Unit,
    onTrash: (Photo) -> Unit,
    onHide: ((Photo) -> Unit)? = null,
    onRenamed: (Photo, String) -> Unit,
    onMetadataStripped: (Photo) -> Unit,
    onShare: (Photo) -> Unit = {}
) {
    val pagerState = rememberPagerState(initialPage = photos.indexOfFirst { it.id == initialPhoto.id }.coerceAtLeast(0)) { photos.size }
    val scope = rememberCoroutineScope()
    val colors = LocalAppColors.current

    var liftY by remember { mutableFloatStateOf(0f) }
    var removing by remember { mutableStateOf(false) }
    var controlsVisible by remember { mutableStateOf(true) }
    var isCurrentZoomed by remember { mutableStateOf(false) }
    var showDetailsSheet by remember { mutableStateOf(false) }
    var dragAllowed by remember { mutableStateOf(false) }

    BoxWithConstraints(Modifier.fillMaxSize().background(Color.Black)) {
        val heightPx = with(LocalDensity.current) { maxHeight.toPx() }
        val currentIndex = pagerState.currentPage.coerceIn(0, (photos.size - 1).coerceAtLeast(0))
        val photo = photos.getOrNull(currentIndex) ?: initialPhoto

        fun springBack() {
            scope.launch { Animatable(liftY).animateTo(0f, spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow)) { liftY = value } }
        }
        fun trashCurrent() {
            if (removing) return
            val target = photo
            removing = true
            scope.launch {
                Animatable(liftY).animateTo(-heightPx, tween(220, easing = FastOutSlowInEasing)) { liftY = value }
                onTrash(target)
                liftY = 0f
                removing = false
            }
        }

        Box(
            Modifier
                .fillMaxSize()
                .pointerInput(photo.id, heightPx, isCurrentZoomed) {
                    if (!isCurrentZoomed) {
                        val tracker = VelocityTracker()
                        detectVerticalDragGestures(
                            onDragStart = { offset ->
                                // Safe zone: swipe up to delete is only triggered when drag starts in upper/middle 82% of screen.
                                // Bottom 18% is left completely unconsumed for Android system home / gesture navigation.
                                dragAllowed = offset.y < heightPx * 0.82f
                                if (dragAllowed) tracker.resetTracking()
                            },
                            onDragEnd = {
                                if (dragAllowed && !removing) {
                                    val velocityY = tracker.calculateVelocity().y
                                    if (liftY < -heightPx * 0.20f || (velocityY < -1600f && liftY < -44f)) trashCurrent() else springBack()
                                }
                                dragAllowed = false
                            },
                            onDragCancel = {
                                if (!removing) springBack()
                                dragAllowed = false
                            },
                            onVerticalDrag = { change, amount ->
                                if (dragAllowed && !removing && !pagerState.isScrollInProgress) {
                                    val newLiftY = liftY + (if (liftY + amount > 0f) amount * 0.2f else amount)
                                    // Only consume if actually pulling upwards into trash
                                    if (newLiftY < -12f || liftY < -12f) {
                                        change.consume()
                                    }
                                    tracker.addPosition(change.uptimeMillis, change.position)
                                    liftY = newLiftY.coerceIn(-heightPx, heightPx * 0.15f)
                                }
                            }
                        )
                    }
                }
        ) {
            HorizontalPager(
                state = pagerState,
                key = { photos.getOrNull(it)?.id ?: it },
                beyondViewportPageCount = 1,
                pageSpacing = 16.dp,
                userScrollEnabled = !removing && liftY == 0f && !isCurrentZoomed,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                photos.getOrNull(page)?.let { item ->
                    Box(
                        Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                if (page == pagerState.currentPage) {
                                    val progress = (-liftY / heightPx).coerceIn(0f, 1f)
                                    translationY = liftY
                                    alpha = 1f - progress * 0.6f
                                    scaleX = 1f - progress * 0.12f
                                    scaleY = 1f - progress * 0.12f
                                }
                            }
                    ) {
                        ZoomablePagerItem(
                            photo = item,
                            isCurrent = page == pagerState.currentPage,
                            onTap = { controlsVisible = !controlsVisible },
                            onPlay = { onPlay(item) },
                            onZoomStateChanged = { zoomed ->
                                if (page == pagerState.currentPage) isCurrentZoomed = zoomed
                            }
                        )
                    }
                }
            }
        }

        // Lift progress indicator (Swipe up to delete) - BIG RED ICON IN CENTER OF SCREEN
        val liftProgress = (-liftY / (heightPx * 0.22f)).coerceIn(0f, 1f)
        if (liftProgress > 0.05f) {
            val isReady = liftProgress >= 0.85f
            Column(
                Modifier
                    .align(Alignment.Center)
                    .graphicsLayer {
                        alpha = (liftProgress * 1.25f).coerceIn(0f, 1f)
                        val scale = 0.75f + liftProgress * 0.35f
                        scaleX = scale
                        scaleY = scale
                    },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    shape = CircleShape,
                    color = if (isReady) colors.red else Color.Black.copy(alpha = 0.72f),
                    border = BorderStroke(2.dp, if (isReady) Color.White.copy(alpha = 0.85f) else Color.White.copy(alpha = 0.25f)),
                    shadowElevation = 14.dp,
                    modifier = Modifier.size(96.dp)
                ) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Icon(
                            if (isReady) Icons.Rounded.DeleteForever else Icons.Rounded.DeleteOutline,
                            contentDescription = "Release to delete",
                            tint = Color.White,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = (if (isReady) colors.red else Color.Black).copy(alpha = 0.75f),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f))
                ) {
                    Text(
                        if (isReady) "RELEASE TO DELETE" else "SWIPE UP TO DELETE",
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp
                    )
                }
            }
        }

        // TOP CONTROLS BAR WITH MODERN TRANSLUCENT GRADIENT
        AnimatedVisibility(
            visible = controlsVisible,
            enter = fadeIn(tween(200)),
            exit = fadeOut(tween(200)),
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Black.copy(alpha = 0.85f), Color.Transparent)
                        )
                    )
                    .statusBarsPadding()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilledTonalIconButton(
                    onClick = onClose,
                    shape = CircleShape,
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = Color.Black.copy(alpha = 0.45f),
                        contentColor = Color.White
                    ),
                    modifier = Modifier.size(42.dp)
                ) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back", modifier = Modifier.size(20.dp))
                }

                Column(
                    Modifier
                        .weight(1f)
                        .padding(horizontal = 14.dp)
                ) {
                    Text(
                        photo.name,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp
                    )
                    Text(
                        "${currentIndex + 1} of ${photos.size}",
                        color = Color.White.copy(0.72f),
                        fontSize = 12.sp
                    )
                }

                FilledTonalIconButton(
                    onClick = { showDetailsSheet = true },
                    shape = CircleShape,
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = Color.Black.copy(alpha = 0.45f),
                        contentColor = Color.White
                    ),
                    modifier = Modifier.size(42.dp)
                ) {
                    Icon(Icons.Rounded.Info, "Details", modifier = Modifier.size(20.dp))
                }
            }
        }

        // BOTTOM CONTROLS BAR (THUMBNAIL STRIP + FLOATING ACTION CAPSULE)
        AnimatedVisibility(
            visible = controlsVisible,
            enter = fadeIn(tween(200)),
            exit = fadeOut(tween(200)),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, Color.Black.copy(alpha = 0.88f))
                        )
                    )
                    .navigationBarsPadding()
                    .padding(bottom = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Comfy Thumbnail Filmstrip
                if (photos.size > 1) {
                    ThumbnailStrip(
                        photos = photos,
                        currentIndex = currentIndex,
                        onSelectIndex = { targetIdx ->
                            scope.launch { pagerState.animateScrollToPage(targetIdx) }
                        },
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                }

                // Modern comfy floating action capsule
                BottomActionCapsule(
                    photo = photo,
                    isFavorite = photo.id in favoriteIds,
                    onToggleFavorite = { onToggleFavorite(photo.id) },
                    onShare = { onShare(photo) },
                    onDetails = { showDetailsSheet = true },
                    onHide = { onHide?.invoke(photo) },
                    onDelete = { if (!pagerState.isScrollInProgress) trashCurrent() }
                )

                Spacer(Modifier.height(8.dp))

                // Date & Time subtitle
                Text(
                    SimpleDateFormat("MMM d, yyyy · h:mm a", Locale.getDefault()).format(Date(photo.date * 1000)) +
                        if (photo.isVideo && photo.durationMs > 0) " · ${formatDuration(photo.durationMs)}" else "",
                    color = Color.White.copy(0.7f),
                    fontSize = 11.sp
                )
            }
        }
    }

    if (showDetailsSheet) {
        val currentPhoto = photos.getOrNull(pagerState.currentPage) ?: initialPhoto
        MediaDetailsSheet(
            photo = currentPhoto,
            onDismiss = { showDetailsSheet = false },
            onRenamed = { p, newName -> onRenamed(p, newName) },
            onMetadataStripped = { p -> onMetadataStripped(p) }
        )
    }
}

/**
 * Modern Popover Menu:
 * Opens as an anchored popover above the "Menu" bottom tab without navigating away from the current page.
 */
@Composable
internal fun MenuPopover(
    trashedCount: Int,
    favoritesCount: Int,
    duplicatesCount: Int,
    onDismiss: () -> Unit,
    onOpenCleanUp: () -> Unit,
    onOpenTrash: () -> Unit,
    onOpenFavorites: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val colors = LocalAppColors.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .zIndex(100f)
    ) {
        // Scrim backdrop
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.4f))
                .clickable(
                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss
                )
        )

        // Floating Popover Card anchored right above the bottom bar on the right side
        Card(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .navigationBarsPadding()
                .padding(end = 16.dp, bottom = 86.dp)
                .widthIn(min = 280.dp, max = 320.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = colors.card),
            border = BorderStroke(1.dp, colors.border),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp, horizontal = 10.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "MENU",
                        fontSize = 11.sp,
                        letterSpacing = 1.2.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.muted,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            Icons.Rounded.Close,
                            contentDescription = "Close menu",
                            tint = colors.muted,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(Modifier.height(4.dp))

                // 1. Clean Up Item
                PopoverItemRow(
                    title = "Clean up",
                    subtitle = if (duplicatesCount > 0) "$duplicatesCount duplicates found" else "Find duplicates & same size",
                    badge = if (duplicatesCount > 0) "$duplicatesCount" else null,
                    icon = Icons.Rounded.AutoAwesome,
                    iconTint = colors.green,
                    iconBg = colors.green.copy(alpha = 0.14f),
                    onClick = onOpenCleanUp
                )

                // 2. Trash Bin Item
                PopoverItemRow(
                    title = "Trash bin",
                    subtitle = if (trashedCount > 0) "$trashedCount items recoverable" else "Empty bin",
                    badge = if (trashedCount > 0) "$trashedCount" else null,
                    icon = Icons.Rounded.DeleteOutline,
                    iconTint = colors.red,
                    iconBg = colors.red.copy(alpha = 0.12f),
                    onClick = onOpenTrash
                )

                // 3. Favorites Item
                PopoverItemRow(
                    title = "Favorites",
                    subtitle = if (favoritesCount > 0) "$favoritesCount starred items" else "No favorites yet",
                    badge = if (favoritesCount > 0) "$favoritesCount" else null,
                    icon = Icons.Rounded.Favorite,
                    iconTint = Color(0xFFE53935),
                    iconBg = Color(0xFFE53935).copy(alpha = 0.12f),
                    onClick = onOpenFavorites
                )

                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                    color = colors.border.copy(alpha = 0.6f)
                )

                // 4. Settings Item
                PopoverItemRow(
                    title = "Settings",
                    subtitle = "Theme, permissions & author",
                    badge = null,
                    icon = Icons.Rounded.Settings,
                    iconTint = colors.ink,
                    iconBg = colors.chipBackground,
                    onClick = onOpenSettings
                )
            }
        }
    }
}

@Composable
private fun PopoverItemRow(
    title: String,
    subtitle: String,
    badge: String?,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    iconBg: Color,
    onClick: () -> Unit
) {
    val colors = LocalAppColors.current
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
            }

            Spacer(Modifier.width(12.dp))

            Column(Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.ink
                )
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = colors.muted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (badge != null) {
                Spacer(Modifier.width(8.dp))
                Text(
                    text = badge,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = iconTint,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(iconBg)
                        .padding(horizontal = 7.dp, vertical = 2.dp)
                )
            }
        }
    }
}

@Composable
internal fun BottomBar(
    page: Page,
    onPage: (Page) -> Unit
) {
    val colors = LocalAppColors.current
    Column(Modifier.fillMaxWidth().background(colors.card)) {
        NavigationBar(containerColor = colors.card, tonalElevation = 0.dp, windowInsets = WindowInsets.navigationBars) {
            val entries = listOf(
                Triple(Page.Photos, Icons.Rounded.GridView, "Pictures"),
                Triple(Page.Albums, Icons.Rounded.PhotoLibrary, "Albums"),
                Triple(Page.CleanUp, Icons.Rounded.AutoAwesome, "Clean Up"),
                Triple(Page.More, Icons.Rounded.MoreHoriz, "More")
            )
            entries.forEach { (target, icon, label) ->
                NavigationBarItem(
                    selected = page == target,
                    onClick = { onPage(target) },
                    icon = { Icon(icon, label) },
                    label = { Text(label, fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = colors.green,
                        selectedTextColor = colors.green,
                        indicatorColor = colors.green.copy(alpha = 0.15f),
                        unselectedIconColor = colors.muted,
                        unselectedTextColor = colors.muted
                    )
                )
            }
        }
    }
}

@Composable
internal fun MorePage(
    trashedCount: Int,
    vaultCount: Int,
    currentTheme: ThemeMode,
    gridColumns: Int,
    hasAccess: Boolean,
    onThemeChange: (ThemeMode) -> Unit,
    onGridColumnsChange: (Int) -> Unit,
    onOpenTrash: () -> Unit,
    onOpenVault: () -> Unit
) {
    val colors = LocalAppColors.current
    val context = LocalContext.current

    LazyColumn(
        Modifier
            .fillMaxSize()
            .background(colors.canvas)
            .statusBarsPadding()
            .padding(horizontal = 18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(Modifier.height(14.dp))
            Text(
                "More",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = colors.ink,
                modifier = Modifier.padding(start = 4.dp)
            )
            Text(
                "TOOLS, SECURITY & PREFERENCES",
                fontSize = 10.sp,
                letterSpacing = 1.2.sp,
                color = colors.muted,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(start = 4.dp, top = 2.dp, bottom = 8.dp)
            )
        }

        // 1. Security & Vault Section
        item {
            Text(
                "SECURITY & VAULT",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.4.sp,
                color = colors.muted,
                modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
            )
        }

        item {
            LibraryCard(
                icon = Icons.Rounded.Lock,
                iconTint = colors.green,
                iconBg = colors.green.copy(alpha = 0.12f),
                title = "Hidden Vault",
                subtitle = if (vaultCount == 0) "PIN-protected private photos" else "$vaultCount hidden items · locked",
                badge = if (vaultCount > 0) "$vaultCount" else null,
                onClick = onOpenVault
            )
        }

        item {
            LibraryCard(
                icon = Icons.Rounded.DeleteOutline,
                iconTint = colors.red,
                iconBg = colors.red.copy(alpha = 0.12f),
                title = "Trash Bin",
                subtitle = if (trashedCount == 0) "Bin is empty" else "$trashedCount items · recoverable",
                badge = if (trashedCount > 0) "$trashedCount" else null,
                onClick = onOpenTrash
            )
        }

        // 2. Appearance & Display Section
        item {
            Spacer(Modifier.height(6.dp))
            Text(
                "APPEARANCE & DISPLAY",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.4.sp,
                color = colors.muted,
                modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
            )
        }

        // Inline Theme Mode Card
        item {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = colors.card,
                border = BorderStroke(1.dp, colors.border.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(colors.chipBackground),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Rounded.SettingsBrightness, null, tint = colors.ink, modifier = Modifier.size(20.dp))
                        }
                        Spacer(Modifier.width(14.dp))
                        Column {
                            Text("Theme Mode", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = colors.ink)
                            Text(currentTheme.label, fontSize = 12.sp, color = colors.muted)
                        }
                    }
                    Spacer(Modifier.height(14.dp))
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(colors.chipBackground)
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        val themes = listOf(
                            ThemeMode.System to "System",
                            ThemeMode.Light to "Light",
                            ThemeMode.Dark to "Dark"
                        )
                        themes.forEach { (mode, label) ->
                            val isSelected = currentTheme == mode
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) colors.card else Color.Transparent)
                                    .clickable { onThemeChange(mode) }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    label,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) colors.green else colors.muted
                                )
                            }
                        }
                    }
                }
            }
        }

        // Inline Default Grid Density Card
        item {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = colors.card,
                border = BorderStroke(1.dp, colors.border.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(colors.chipBackground),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Rounded.GridView, null, tint = colors.ink, modifier = Modifier.size(20.dp))
                        }
                        Spacer(Modifier.width(14.dp))
                        Column {
                            Text("Default Grid Columns", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = colors.ink)
                            Text("$gridColumns columns per row", fontSize = 12.sp, color = colors.muted)
                        }
                    }
                    Spacer(Modifier.height(14.dp))
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(colors.chipBackground)
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        (2..5).forEach { cols ->
                            val isSelected = gridColumns == cols
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) colors.card else Color.Transparent)
                                    .clickable { onGridColumnsChange(cols) }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "$cols",
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) colors.green else colors.muted
                                )
                            }
                        }
                    }
                }
            }
        }

        // 3. System & Permissions Section
        item {
            Spacer(Modifier.height(6.dp))
            Text(
                "STORAGE & SYSTEM",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.4.sp,
                color = colors.muted,
                modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
            )
        }

        item {
            LibraryCard(
                icon = Icons.Rounded.PhotoLibrary,
                iconTint = colors.green,
                iconBg = colors.green.copy(alpha = 0.12f),
                title = "Photo & Video Access",
                subtitle = if (hasAccess) "Full access granted · Android MediaStore" else "Access restricted · Tap to grant permissions",
                badge = if (hasAccess) "Active" else "Off",
                onClick = { openAppSettings(context) }
            )
        }

        item {
            LibraryCard(
                icon = Icons.Rounded.BugReport,
                iconTint = colors.ink,
                iconBg = colors.chipBackground,
                title = "Report an Issue",
                subtitle = "Send crash reports or feedback via email",
                onClick = {
                    val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("mailto:support@swipegallery.app")
                        putExtra(Intent.EXTRA_SUBJECT, "Lumina Gallery Feedback v2.0.0")
                    }
                    try { context.startActivity(emailIntent) } catch (_: Exception) {}
                }
            )
        }

        // 4. About Lumina Footer
        item {
            Spacer(Modifier.height(10.dp))
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/initeshjain"))
                        try { context.startActivity(intent) } catch (_: Exception) {}
                    },
                shape = RoundedCornerShape(20.dp),
                color = colors.card,
                border = BorderStroke(1.dp, colors.border.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "LUMINA GALLERY",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp,
                        color = colors.ink
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Version 2.0.0 · Fast, Comfy & Private",
                        fontSize = 12.sp,
                        color = colors.muted
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "100% offline · Zero tracking · Hardware-accelerated",
                        fontSize = 11.sp,
                        color = colors.muted.copy(alpha = 0.8f),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(
                        "Created by @initeshjain on GitHub",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.green
                    )
                }
            }
            Spacer(Modifier.height(28.dp))
        }
    }
}

@Composable
private fun LibraryCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    iconBg: Color,
    title: String,
    subtitle: String,
    badge: String? = null,
    onClick: () -> Unit
) {
    val colors = LocalAppColors.current
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = colors.card,
        border = BorderStroke(1.dp, colors.border.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = iconTint, modifier = Modifier.size(24.dp))
            }
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(title, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = colors.ink)
                Spacer(Modifier.height(2.dp))
                Text(subtitle, fontSize = 12.sp, color = colors.muted)
            }
            if (badge != null) {
                Text(
                    badge,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.green,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(colors.green.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                )
                Spacer(Modifier.width(8.dp))
            }
            Icon(Icons.Rounded.ChevronRight, null, tint = colors.muted.copy(alpha = 0.6f))
        }
    }
}

@Composable
internal fun PermissionEmpty(blocked: Boolean, onGrant: () -> Unit, onOpenSettings: () -> Unit) {
    if (blocked) {
        EmptyState(
            Icons.Rounded.Image,
            "Photo access is off",
            "Android won't ask again from inside the app. Open Settings, tap Permissions › Photos and videos, and choose Allow.",
            onOpenSettings,
            actionLabel = "Open settings",
            secondaryLabel = "Try again",
            secondaryAction = onGrant
        )
    } else {
        EmptyState(Icons.Rounded.Image, "Your memories, together", "Allow access to your photos and videos to browse albums, sort favorites, and tidy up.", onGrant)
    }
}

@Composable
internal fun EmptyState(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    body: String,
    action: (() -> Unit)?,
    actionLabel: String = "Allow photo access",
    secondaryLabel: String? = null,
    secondaryAction: (() -> Unit)? = null,
) {
    val colors = LocalAppColors.current
    Column(
        Modifier
            .fillMaxSize()
            .padding(30.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            Modifier
                .size(78.dp)
                .clip(CircleShape)
                .background(colors.chipBackground),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = colors.green, modifier = Modifier.size(34.dp))
        }
        Spacer(Modifier.height(20.dp))
        Text(title, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = colors.ink)
        Spacer(Modifier.height(8.dp))
        Text(body, color = colors.muted, fontSize = 14.sp, lineHeight = 21.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        if (action != null) {
            Spacer(Modifier.height(18.dp))
            Button(
                onClick = action,
                colors = ButtonDefaults.buttonColors(containerColor = colors.green),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(actionLabel)
            }
        }
        if (secondaryLabel != null && secondaryAction != null) {
            TextButton(onClick = secondaryAction) {
                Text(secondaryLabel, color = colors.green)
            }
        }
    }
}

@Composable
internal fun AppSplashScreen(modifier: Modifier = Modifier) {
    val colors = LocalAppColors.current
    val infiniteTransition = rememberInfiniteTransition(label = "pulse transition")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Box(
        modifier
            .fillMaxSize()
            .background(colors.canvas),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                Modifier
                    .size(100.dp)
                    .graphicsLayer {
                        scaleX = pulseScale
                        scaleY = pulseScale
                    }
                    .clip(RoundedCornerShape(26.dp))
                    .background(colors.chipBackground),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.foundation.Image(
                    painter = painterResource(id = R.drawable.ic_launcher),
                    contentDescription = "Lumina Logo",
                    modifier = Modifier.size(86.dp).clip(RoundedCornerShape(20.dp))
                )
            }
            Spacer(Modifier.height(24.dp))
            Text(
                "LUMINA",
                fontSize = 20.sp,
                letterSpacing = 4.sp,
                fontWeight = FontWeight.Bold,
                color = colors.ink
            )
            Spacer(Modifier.height(6.dp))
            Text(
                "Your memories, illuminated",
                fontSize = 12.sp,
                color = colors.muted
            )
            Spacer(Modifier.height(30.dp))
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = colors.green,
                strokeWidth = 2.5.dp
            )
        }
    }
}
