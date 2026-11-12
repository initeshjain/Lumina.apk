package com.swipegallery.app

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.DriveFileMove
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.CreateNewFolder
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Replaces the page header while items are selected. */
@Composable
internal fun SelectionTopBar(count: Int, allSelected: Boolean, onClose: () -> Unit, onToggleAll: () -> Unit) {
    val colors = LocalAppColors.current
    Row(
        Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 12.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onClose) { Icon(Icons.Rounded.Close, "Cancel selection", tint = colors.ink) }
        Column(Modifier.weight(1f).padding(start = 4.dp)) {
            Text("$count selected", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = colors.ink)
            Text("TAP TO ADD OR REMOVE", fontSize = 10.sp, letterSpacing = 1.2.sp, color = colors.muted, fontWeight = FontWeight.SemiBold)
        }
        TextButton(onClick = onToggleAll) {
            Text(if (allSelected) "Deselect all" else "Select all", color = colors.green, fontWeight = FontWeight.SemiBold)
        }
    }
}

/** Replaces the bottom navigation while items are selected. */
@Composable
internal fun SelectionActionBar(enabled: Boolean, canMove: Boolean, onShare: () -> Unit, onMove: () -> Unit, onDelete: () -> Unit) {
    val colors = LocalAppColors.current
    Row(
        Modifier
            .fillMaxWidth()
            .background(colors.card)
            .navigationBarsPadding()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        SelectionAction(Icons.Rounded.Share, "Share", colors.ink, enabled, onShare)
        if (canMove) SelectionAction(Icons.AutoMirrored.Rounded.DriveFileMove, "Move", colors.ink, enabled, onMove)
        SelectionAction(Icons.Rounded.DeleteOutline, "Delete", colors.red, enabled, onDelete)
    }
}

@Composable
private fun SelectionAction(icon: ImageVector, label: String, color: Color, enabled: Boolean, onClick: () -> Unit) {
    val colors = LocalAppColors.current
    val tint = if (enabled) color else colors.muted.copy(0.4f)
    Column(
        Modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 22.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(icon, label, tint = tint)
        Text(label, fontSize = 11.sp, color = tint, fontWeight = FontWeight.Medium)
    }
}

/** A destination album: its display name and MediaStore relative path (e.g. "Pictures/Trips/"). */
internal data class AlbumTarget(val name: String, val relativePath: String, val count: Int)

@Composable
internal fun MoveToAlbumDialog(count: Int, albums: List<AlbumTarget>, onDismiss: () -> Unit, onMove: (String) -> Unit) {
    val colors = LocalAppColors.current
    var creating by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf("") }
    val cleanName = newName.trim().replace(Regex("[\\\\/:*?\"<>|]"), "")
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (creating) "New album" else "Move $count ${if (count == 1) "item" else "items"} to", color = colors.ink, fontWeight = FontWeight.Bold) },
        text = {
            if (creating) {
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    singleLine = true,
                    label = { Text("Album name") },
                    supportingText = { Text("Saved under Pictures/") }
                )
            } else {
                LazyColumn(Modifier.heightIn(max = 380.dp)) {
                    item {
                        AlbumOption(Icons.Rounded.CreateNewFolder, "New album", null) { creating = true }
                    }
                    items(albums, key = { it.relativePath }) { album ->
                        AlbumOption(Icons.Rounded.Folder, album.name, "${album.count} items · ${album.relativePath.trimEnd('/')}") { onMove(album.relativePath) }
                    }
                }
            }
        },
        confirmButton = {
            if (creating) {
                TextButton(onClick = { onMove("Pictures/$cleanName/") }, enabled = cleanName.isNotEmpty()) {
                    Text("Move", color = if (cleanName.isNotEmpty()) colors.green else colors.muted, fontWeight = FontWeight.SemiBold)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = { if (creating) creating = false else onDismiss() }) {
                Text(if (creating) "Back" else "Cancel", color = colors.muted)
            }
        },
        containerColor = colors.card
    )
}

@Composable
private fun AlbumOption(icon: ImageVector, title: String, subtitle: String?, onClick: () -> Unit) {
    val colors = LocalAppColors.current
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = colors.green)
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.SemiBold, color = colors.ink, maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (subtitle != null) Text(subtitle, fontSize = 12.sp, color = colors.muted, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

/** Full-screen look at a single item (used from the Sort page). Both photos and videos support pinch and double-tap zoom. */
@Composable
internal fun MediaPreview(
    item: Photo,
    isFavorite: Boolean = false,
    onToggleFavorite: (() -> Unit)? = null,
    onClose: () -> Unit,
    onPlay: (Photo) -> Unit
) {
    var scale by remember(item.id) { mutableFloatStateOf(1f) }
    var offset by remember(item.id) { mutableStateOf(Offset.Zero) }
    var controlsVisible by remember { mutableStateOf(true) }

    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(item.id) {
                detectTransformGestures { _, pan, zoom, _ ->
                    val newScale = (scale * zoom).coerceIn(1f, 5f)
                    scale = newScale
                    offset = if (newScale <= 1.02f) Offset.Zero else offset + pan
                }
            }
            .pointerInput(item.id) {
                detectTapGestures(
                    onTap = { controlsVisible = !controlsVisible },
                    onDoubleTap = {
                        if (scale > 1f) {
                            scale = 1f
                            offset = Offset.Zero
                        } else {
                            scale = 2.5f
                        }
                    }
                )
            }
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    translationX = offset.x
                    translationY = offset.y
                },
            contentAlignment = Alignment.Center
        ) {
            if (item.isVideo) {
                ViewerItem(item, Modifier.fillMaxSize(), onPlay = { onPlay(item) })
            } else {
                GalleryImage(item.uri, item.name, Modifier.fillMaxSize(), ContentScale.Fit)
            }
        }

        AnimatedVisibility(
            visible = controlsVisible,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.65f))
                    .statusBarsPadding()
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onClose, modifier = Modifier.clip(CircleShape).background(Color.Black.copy(0.38f))) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back to sorting", tint = Color.White)
                }
                Text(item.name, Modifier.weight(1f).padding(horizontal = 12.dp), color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.SemiBold)
                if (onToggleFavorite != null) {
                    IconButton(
                        onClick = onToggleFavorite,
                        modifier = Modifier.clip(CircleShape).background(Color.Black.copy(0.38f))
                    ) {
                        Icon(
                            if (isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                            "Favorite",
                            tint = if (isFavorite) Color(0xFFE53935) else Color.White
                        )
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = controlsVisible,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.65f))
                    .navigationBarsPadding()
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "PINCH OR DOUBLE-TAP TO ZOOM",
                    color = Color.White.copy(0.7f),
                    fontSize = 10.sp,
                    letterSpacing = 1.1.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

/** Check mark shown on grid tiles while selecting. */
@Composable
internal fun SelectionMark(selected: Boolean, modifier: Modifier = Modifier) {
    val colors = LocalAppColors.current
    Box(
        modifier
            .size(24.dp)
            .clip(CircleShape)
            .background(if (selected) colors.green else Color.Black.copy(0.25f))
            .then(if (selected) Modifier else Modifier.padding(2.dp).clip(CircleShape).background(Color.White.copy(0.35f))),
        contentAlignment = Alignment.Center
    ) {
        if (selected) Icon(Icons.Rounded.Check, "Selected", tint = Color.White, modifier = Modifier.size(16.dp))
    }
}
