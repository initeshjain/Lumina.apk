package com.swipegallery.app

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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class DuplicateCluster(
    val id: String,
    val title: String,
    val subtitle: String,
    val isSameName: Boolean,
    val items: List<Photo>
)

internal fun detectDuplicateClusters(photos: List<Photo>): List<DuplicateCluster> {
    val clusters = mutableListOf<DuplicateCluster>()

    // 1. Same filename across different folders/albums
    val byName = photos.groupBy { it.name.trim().lowercase() }
    byName.forEach { (name, group) ->
        if (group.size > 1) {
            val folders = group.map { it.album }.distinct()
            clusters.add(
                DuplicateCluster(
                    id = "name_$name",
                    title = group.first().name,
                    subtitle = if (folders.size > 1) "Found in ${folders.joinToString(", ")}" else "${group.size} copies in gallery",
                    isSameName = true,
                    items = group.sortedByDescending { it.date }
                )
            )
        }
    }

    // 2. Exact same file size (where sizeBytes > 2048, not already clustered by same name)
    val clusteredIds = clusters.flatMap { it.items.map { p -> p.id } }.toSet()
    val remaining = photos.filter { it.id !in clusteredIds && it.sizeBytes > 2048L }
    val bySize = remaining.groupBy { it.sizeBytes }
    bySize.forEach { (size, group) ->
        if (group.size > 1) {
            clusters.add(
                DuplicateCluster(
                    id = "size_$size",
                    title = "Identical size: ${formatFileSize(size)}",
                    subtitle = "${group.size} files with identical file size",
                    isSameName = false,
                    items = group.sortedByDescending { it.date }
                )
            )
        }
    }

    return clusters
}

/**
 * Screen to review duplicates (same names in different folders or same size) and clean them up.
 */
@Composable
internal fun CleanUpPage(
    photos: List<Photo>,
    onBack: () -> Unit,
    onPreviewPhoto: (Photo) -> Unit = {},
    onDeleteItems: (List<Photo>) -> Unit
) {
    val colors = LocalAppColors.current
    val clusters = remember(photos) { detectDuplicateClusters(photos) }
    var selectedForDelete by remember(clusters) { mutableStateOf(setOf<Long>()) }

    val allClusterPhotos = remember(clusters) { clusters.flatMap { it.items }.associateBy { it.id } }
    val totalReclaimableBytes = remember(selectedForDelete, allClusterPhotos) {
        selectedForDelete.sumOf { allClusterPhotos[it]?.sizeBytes ?: 0L }
    }

    Scaffold(
        containerColor = colors.canvas,
        topBar = {
            Row(
                Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 22.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back", tint = colors.ink)
                }
                Column(Modifier.weight(1f)) {
                    Text("Clean up", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = colors.ink)
                    Text("DUPLICATES & REDUNDANCIES", fontSize = 10.sp, letterSpacing = 1.2.sp, color = colors.muted, fontWeight = FontWeight.SemiBold)
                }
            }
        },
        bottomBar = {
            if (selectedForDelete.isNotEmpty()) {
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
        if (clusters.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                EmptyState(
                    Icons.Rounded.CheckCircle,
                    "Everything looks tidy",
                    "No duplicate photo names across folders or identical file sizes found in your gallery.",
                    null
                )
            }
        } else {
            LazyColumn(
                Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Card(
                        Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = colors.green.copy(alpha = 0.12f))
                    ) {
                        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.AutoAwesome, null, tint = colors.green, modifier = Modifier.size(26.dp))
                            Spacer(Modifier.width(14.dp))
                            Column {
                                Text(
                                    "${clusters.size} duplicate sets detected",
                                    fontWeight = FontWeight.Bold,
                                    color = colors.ink,
                                    fontSize = 14.sp
                                )
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    "Tap to select redundant copies. One original is kept safe.",
                                    fontSize = 12.sp,
                                    color = colors.muted
                                )
                            }
                        }
                    }
                }

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
                                    Text(
                                        cluster.title,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = colors.ink,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(cluster.subtitle, fontSize = 11.sp, color = colors.muted)
                                }
                                TextButton(
                                    onClick = {
                                        // Auto-select all except the first (oldest/primary)
                                        val toSelect = cluster.items.drop(1).map { it.id }.toSet()
                                        selectedForDelete = if (cluster.items.drop(1).all { it.id in selectedForDelete }) {
                                            selectedForDelete - toSelect
                                        } else {
                                            selectedForDelete + toSelect
                                        }
                                    }
                                ) {
                                    val allSelected = cluster.items.drop(1).all { it.id in selectedForDelete }
                                    Text(if (allSelected) "Deselect" else "Select copies", fontSize = 12.sp, color = colors.green)
                                }
                            }

                            Spacer(Modifier.height(10.dp))

                            // Show all versions of this file
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                cluster.items.forEachIndexed { index, photo ->
                                    val isSelected = photo.id in selectedForDelete
                                    val isPrimary = index == 0

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
                                                if (isPrimary && !isSelected) {
                                                    Spacer(Modifier.width(6.dp))
                                                    Text(
                                                        "Original",
                                                        fontSize = 10.sp,
                                                        color = colors.green,
                                                        fontWeight = FontWeight.Bold,
                                                        modifier = Modifier
                                                            .clip(RoundedCornerShape(6.dp))
                                                            .background(colors.green.copy(alpha = 0.15f))
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
