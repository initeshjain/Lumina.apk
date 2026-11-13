package com.swipegallery.app

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.abs

/**
 * Result of ML analysis on a photo:
 * - dHash: 64-bit gradient difference hash
 * - sharpnessScore: Laplacian variance normalized (0-100)
 */
internal data class PhotoMlMetrics(
    val dHash: Long,
    val sharpnessScore: Int
)

internal data class ScoredPhoto(
    val photo: Photo,
    val metrics: PhotoMlMetrics?,
    val isBestShot: Boolean = false
)

internal enum class ClusterType {
    BurstSequence,
    IdenticalName,
    IdenticalSize
}

internal data class DuplicateCluster(
    val id: String,
    val title: String,
    val subtitle: String,
    val type: ClusterType,
    val items: List<Photo>,
    val scoredItems: List<ScoredPhoto> = emptyList(),
    val recommendedDeleteIds: Set<Long> = emptySet()
)

private val metricsCache = ConcurrentHashMap<Long, PhotoMlMetrics>()

internal fun computePhotoMetrics(context: Context, photo: Photo): PhotoMlMetrics? {
    metricsCache[photo.id]?.let { return it }
    return try {
        val resolver = context.contentResolver
        val options = BitmapFactory.Options().apply {
            inSampleSize = 8
            inPreferredConfig = Bitmap.Config.RGB_565
        }
        val stream = resolver.openInputStream(photo.uri) ?: return null
        val bmp = BitmapFactory.decodeStream(stream, null, options) ?: return null
        stream.close()

        val dHash = computeDHash(bmp)
        val sharpness = computeLaplacianVariance(bmp)
        bmp.recycle()

        val metrics = PhotoMlMetrics(dHash = dHash, sharpnessScore = sharpness)
        metricsCache[photo.id] = metrics
        metrics
    } catch (_: Exception) {
        null
    }
}

internal fun computeDHash(bitmap: Bitmap): Long {
    val scaled = Bitmap.createScaledBitmap(bitmap, 9, 8, true)
    var hash = 0L
    for (y in 0 until 8) {
        for (x in 0 until 8) {
            val left = scaled.getPixel(x, y)
            val right = scaled.getPixel(x + 1, y)
            val lumLeft = (0.299 * ((left shr 16) and 0xFF) + 0.587 * ((left shr 8) and 0xFF) + 0.114 * (left and 0xFF)).toInt()
            val lumRight = (0.299 * ((right shr 16) and 0xFF) + 0.587 * ((right shr 8) and 0xFF) + 0.114 * (right and 0xFF)).toInt()
            if (lumLeft > lumRight) {
                hash = hash or (1L shl (y * 8 + x))
            }
        }
    }
    if (scaled != bitmap) scaled.recycle()
    return hash
}

internal fun computeLaplacianVariance(bitmap: Bitmap): Int {
    val size = 64
    val scaled = Bitmap.createScaledBitmap(bitmap, size, size, true)
    val gray = Array(size) { IntArray(size) }
    for (y in 0 until size) {
        for (x in 0 until size) {
            val p = scaled.getPixel(x, y)
            gray[y][x] = (0.299 * ((p shr 16) and 0xFF) + 0.587 * ((p shr 8) and 0xFF) + 0.114 * (p and 0xFF)).toInt()
        }
    }
    if (scaled != bitmap) scaled.recycle()

    var sum = 0.0
    var sumSq = 0.0
    var count = 0
    for (y in 1 until size - 1) {
        for (x in 1 until size - 1) {
            val lap = 4 * gray[y][x] - gray[y - 1][x] - gray[y + 1][x] - gray[y][x - 1] - gray[y + 1][x]
            sum += lap
            sumSq += (lap * lap)
            count++
        }
    }
    val mean = sum / count
    val variance = (sumSq / count) - (mean * mean)
    return (Math.sqrt(variance.coerceAtLeast(0.0)) * 2.8).coerceIn(15.0, 99.0).toInt()
}

internal fun hammingDistance(h1: Long, h2: Long): Int {
    return java.lang.Long.bitCount(h1 xor h2)
}

/**
 * Option C Hybrid ML:
 * Step 1: EXIF timestamp proximity clustering (burst sequences <= 3s)
 * Step 2: Perceptual dHash difference verification across gradients
 * Step 3: 2D discrete Laplacian edge-variance sharpness scoring to crown "Best Shot"
 */
internal suspend fun detectHybridClusters(context: Context, photos: List<Photo>): List<DuplicateCluster> = withContext(Dispatchers.Default) {
    val clusters = mutableListOf<DuplicateCluster>()
    val processedPhotoIds = mutableSetOf<Long>()

    // STEP 1: EXIF Timestamp Clustering (Burst sequence candidates taken within <= 3 seconds)
    val sortedByDate = photos.filter { !it.isVideo }.sortedBy { it.date }
    val timeClusters = mutableListOf<MutableList<Photo>>()
    var currentGroup = mutableListOf<Photo>()

    for (p in sortedByDate) {
        if (currentGroup.isEmpty()) {
            currentGroup.add(p)
        } else {
            val last = currentGroup.last()
            if (abs(p.date - last.date) <= 3) {
                currentGroup.add(p)
            } else {
                if (currentGroup.size >= 2) timeClusters.add(currentGroup)
                currentGroup = mutableListOf(p)
            }
        }
    }
    if (currentGroup.size >= 2) timeClusters.add(currentGroup)

    // STEP 2 & 3: Perceptual Hashing & Laplacian Sharpness Scoring on Burst Candidates
    for ((index, burstCandidates) in timeClusters.withIndex()) {
        val scoredList = mutableListOf<ScoredPhoto>()
        for (photo in burstCandidates) {
            val metrics = computePhotoMetrics(context, photo)
            scoredList.add(ScoredPhoto(photo, metrics, isBestShot = false))
        }

        var isVisualBurst = false
        if (scoredList.size >= 2) {
            val firstHash = scoredList.first().metrics?.dHash
            if (firstHash != null) {
                val similarCount = scoredList.count { item ->
                    val h = item.metrics?.dHash
                    h != null && hammingDistance(firstHash, h) <= 14
                }
                if (similarCount >= 2) isVisualBurst = true
            }
        }

        if (isVisualBurst) {
            val best = scoredList.maxByOrNull { it.metrics?.sharpnessScore ?: 0 }
            val finalScored = scoredList.map { item ->
                item.copy(isBestShot = item.photo.id == best?.photo?.id)
            }
            val redundantIds = finalScored.filter { !it.isBestShot }.map { it.photo.id }.toSet()
            finalScored.forEach { processedPhotoIds.add(it.photo.id) }

            clusters.add(
                DuplicateCluster(
                    id = "burst_$index",
                    title = "Burst Sequence (${finalScored.size} photos)",
                    subtitle = "AI analyzed sharpness · Best shot kept safe",
                    type = ClusterType.BurstSequence,
                    items = finalScored.map { it.photo },
                    scoredItems = finalScored,
                    recommendedDeleteIds = redundantIds
                )
            )
        }
    }

    // Traditional Cross-folder name duplicates
    val byName = photos.filter { it.id !in processedPhotoIds }.groupBy { it.name.trim().lowercase() }
    byName.forEach { (name, group) ->
        if (group.size > 1) {
            val folders = group.map { it.album }.distinct()
            val oldest = group.minByOrNull { it.date }
            val copies = group.filter { it.id != oldest?.id }.map { it.id }.toSet()
            group.forEach { processedPhotoIds.add(it.id) }

            clusters.add(
                DuplicateCluster(
                    id = "name_$name",
                    title = group.first().name,
                    subtitle = if (folders.size > 1) "Found in ${folders.joinToString(", ")}" else "${group.size} copies in gallery",
                    type = ClusterType.IdenticalName,
                    items = group.sortedByDescending { it.date },
                    recommendedDeleteIds = copies
                )
            )
        }
    }

    // Traditional Identical file size duplicates
    val remaining = photos.filter { it.id !in processedPhotoIds && it.sizeBytes > 2048L }
    val bySize = remaining.groupBy { it.sizeBytes }
    bySize.forEach { (size, group) ->
        if (group.size > 1) {
            val oldest = group.minByOrNull { it.date }
            val copies = group.filter { it.id != oldest?.id }.map { it.id }.toSet()
            clusters.add(
                DuplicateCluster(
                    id = "size_$size",
                    title = "Identical size: ${formatFileSize(size)}",
                    subtitle = "${group.size} files sharing identical size (${formatFileSize(size)})",
                    type = ClusterType.IdenticalSize,
                    items = group.sortedByDescending { it.date },
                    recommendedDeleteIds = copies
                )
            )
        }
    }

    clusters
}

enum class CleanUpTab(val label: String) {
    SwipeTidy("Swipe Tidy"),
    Duplicates("Duplicate Finder")
}

@Composable
internal fun CleanUpPage(
    photos: List<Photo>,
    onBack: (() -> Unit)? = null,
    onPreviewPhoto: (Photo) -> Unit = {},
    onKeepPhoto: (Photo) -> Unit = {},
    onTrashPhoto: (Photo) -> Unit = {},
    onDeleteItems: (List<Photo>) -> Unit
) {
    val context = LocalContext.current
    val colors = LocalAppColors.current
    var currentTab by remember { mutableStateOf(CleanUpTab.SwipeTidy) }
    var clusters by remember { mutableStateOf<List<DuplicateCluster>>(emptyList()) }
    var isAnalyzing by remember { mutableStateOf(false) }

    LaunchedEffect(photos) {
        isAnalyzing = true
        clusters = detectHybridClusters(context, photos)
        isAnalyzing = false
    }

    var selectedForDelete by remember(clusters) {
        mutableStateOf(clusters.flatMap { it.recommendedDeleteIds }.toSet())
    }

    val allClusterPhotos = remember(clusters) { clusters.flatMap { it.items }.associateBy { it.id } }
    val totalReclaimableBytes = remember(selectedForDelete, allClusterPhotos) {
        selectedForDelete.sumOf { allClusterPhotos[it]?.sizeBytes ?: 0L }
    }

    Scaffold(
        containerColor = colors.canvas,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            Column(Modifier.fillMaxWidth()) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 22.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (onBack != null) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back", tint = colors.ink)
                        }
                    }
                    Column(Modifier.weight(1f)) {
                        Text("Clean up", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = colors.ink)
                        Text(
                            if (currentTab == CleanUpTab.SwipeTidy) "QUICK SORT WITH GESTURES" else "HYBRID ML DUPLICATE & BURST FINDER",
                            fontSize = 10.sp,
                            letterSpacing = 1.2.sp,
                            color = colors.muted,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Switcher: [ Swipe Tidy ] | [ Duplicate Finder ]
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(22.dp))
                            .background(colors.chipBackground)
                            .padding(3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CleanUpTab.entries.forEach { tab ->
                            val active = currentTab == tab
                            Text(
                                text = tab.label,
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
                Spacer(Modifier.height(4.dp))
            }
        },
        bottomBar = {
            if (currentTab == CleanUpTab.Duplicates && selectedForDelete.isNotEmpty()) {
                Surface(
                    Modifier.fillMaxWidth(),
                    color = colors.card,
                    shadowElevation = 8.dp
                ) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(horizontal = 20.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                "${selectedForDelete.size} items selected",
                                fontWeight = FontWeight.Bold,
                                color = colors.ink,
                                fontSize = 14.sp
                            )
                            if (totalReclaimableBytes > 0) {
                                Text(
                                    "Frees up ${formatFileSize(totalReclaimableBytes)}",
                                    fontSize = 11.sp,
                                    color = colors.muted
                                )
                            }
                        }
                        Button(
                            onClick = {
                                val targets = selectedForDelete.mapNotNull { allClusterPhotos[it] }
                                onDeleteItems(targets)
                                selectedForDelete = emptySet()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = colors.red),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(Icons.Rounded.DeleteOutline, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Delete duplicates", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    ) { padding ->
        if (currentTab == CleanUpTab.SwipeTidy) {
            Box(Modifier.fillMaxSize().padding(padding)) {
                SwipePage(
                    photos = photos,
                    onKeep = onKeepPhoto,
                    onTrash = onTrashPhoto,
                    onOpen = onPreviewPhoto
                )
            }
        } else if (isAnalyzing && clusters.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(modifier = Modifier.size(32.dp), color = colors.green, strokeWidth = 3.dp)
                    Spacer(Modifier.height(16.dp))
                    Text("Analyzing photos with Hybrid ML...", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = colors.ink)
                    Spacer(Modifier.height(4.dp))
                    Text("Checking timestamps, dHash & sharpness variance", fontSize = 11.sp, color = colors.muted)
                }
            }
        } else if (clusters.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                EmptyState(
                    Icons.Rounded.CheckCircle,
                    "Everything looks tidy",
                    "No burst sequences, duplicate photo names, or redundant files found in your gallery.",
                    null
                )
            }
        } else {
            LazyColumn(
                Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(clusters, key = { it.id }) { cluster ->
                    Card(
                        Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = colors.card),
                        border = BorderStroke(1.dp, colors.border)
                    ) {
                        Column(Modifier.padding(14.dp)) {
                            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            cluster.title,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = colors.ink,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f, fill = false)
                                        )
                                        if (cluster.type == ClusterType.BurstSequence) {
                                            Spacer(Modifier.width(6.dp))
                                            Text(
                                                "⚡ Hybrid ML",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = colors.green,
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(colors.green.copy(alpha = 0.12f))
                                                    .padding(horizontal = 5.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    Text(cluster.subtitle, fontSize = 11.sp, color = colors.muted)
                                }
                                TextButton(
                                    onClick = {
                                        val candidates = if (cluster.type == ClusterType.BurstSequence) {
                                            cluster.scoredItems.filter { !it.isBestShot }.map { it.photo.id }.toSet()
                                        } else {
                                            cluster.items.drop(1).map { it.id }.toSet()
                                        }
                                        selectedForDelete = if (candidates.all { it in selectedForDelete }) {
                                            selectedForDelete - candidates
                                        } else {
                                            selectedForDelete + candidates
                                        }
                                    }
                                ) {
                                    val candidates = if (cluster.type == ClusterType.BurstSequence) {
                                        cluster.scoredItems.filter { !it.isBestShot }.map { it.photo.id }.toSet()
                                    } else {
                                        cluster.items.drop(1).map { it.id }.toSet()
                                    }
                                    val allSelected = candidates.isNotEmpty() && candidates.all { it in selectedForDelete }
                                    Text(if (allSelected) "Deselect" else "Select copies", fontSize = 12.sp, color = colors.green)
                                }
                            }

                            Spacer(Modifier.height(10.dp))

                            // Show all versions of this file
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                cluster.items.forEachIndexed { index, photo ->
                                    val isSelected = photo.id in selectedForDelete
                                    val scoredItem = cluster.scoredItems.firstOrNull { it.photo.id == photo.id }
                                    val isBest = scoredItem?.isBestShot ?: (index == 0 && cluster.type != ClusterType.BurstSequence)

                                    Row(
                                        Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(if (isSelected) colors.red.copy(alpha = 0.08f) else colors.chipBackground)
                                            .padding(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // 1. Thumbnail: Click opens full image/video preview
                                        Box(
                                            modifier = Modifier
                                                .size(54.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                                .clickable { onPreviewPhoto(photo) }
                                        ) {
                                            GalleryImage(
                                                photo.uri,
                                                photo.name,
                                                Modifier.fillMaxSize(),
                                                ContentScale.Crop
                                            )
                                            if (photo.isVideo) {
                                                VideoBadge(photo.durationMs, Modifier.align(Alignment.BottomEnd).padding(4.dp))
                                            }
                                        }

                                        Spacer(Modifier.width(12.dp))

                                        // 2. Name & Details: Click toggles selection
                                        Column(
                                            Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(8.dp))
                                                .clickable {
                                                    selectedForDelete = if (isSelected) {
                                                        selectedForDelete - photo.id
                                                    } else {
                                                        selectedForDelete + photo.id
                                                    }
                                                }
                                                .padding(vertical = 2.dp)
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    photo.name,
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = colors.ink,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis,
                                                    modifier = Modifier.weight(1f, fill = false)
                                                )
                                                if (isBest) {
                                                    Spacer(Modifier.width(6.dp))
                                                    Text(
                                                        if (cluster.type == ClusterType.BurstSequence) "⭐ Best Shot" else "Original",
                                                        fontSize = 10.sp,
                                                        color = colors.green,
                                                        fontWeight = FontWeight.Bold,
                                                        modifier = Modifier
                                                            .clip(RoundedCornerShape(6.dp))
                                                            .background(colors.green.copy(alpha = 0.15f))
                                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                } else if (cluster.type == ClusterType.BurstSequence) {
                                                    Spacer(Modifier.width(6.dp))
                                                    val score = scoredItem?.metrics?.sharpnessScore
                                                    Text(
                                                        if (score != null) "Blurry ($score%)" else "Redundant",
                                                        fontSize = 10.sp,
                                                        color = colors.red,
                                                        fontWeight = FontWeight.Medium,
                                                        modifier = Modifier
                                                            .clip(RoundedCornerShape(6.dp))
                                                            .background(colors.red.copy(alpha = 0.12f))
                                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }
                                            Spacer(Modifier.height(2.dp))
                                            Text(
                                                "Folder: ${photo.album}" + (if (photo.sizeBytes > 0) " · ${formatFileSize(photo.sizeBytes)}" else "") +
                                                    " · ${SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(photo.date * 1000))}",
                                                fontSize = 11.sp,
                                                color = colors.muted
                                            )
                                        }

                                        // 3. Selection checkbox circle: Click toggles selection
                                        Box(
                                            Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .clickable {
                                                    selectedForDelete = if (isSelected) {
                                                        selectedForDelete - photo.id
                                                    } else {
                                                        selectedForDelete + photo.id
                                                    }
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Box(
                                                Modifier
                                                    .size(24.dp)
                                                    .clip(CircleShape)
                                                    .background(if (isSelected) colors.red else Color.Transparent)
                                                    .then(if (!isSelected) Modifier.background(colors.chipBackground) else Modifier),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                if (isSelected) {
                                                    Icon(Icons.Rounded.Check, "Selected for deletion", tint = Color.White, modifier = Modifier.size(16.dp))
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
