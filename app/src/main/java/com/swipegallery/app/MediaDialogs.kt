package com.swipegallery.app

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.DeleteForever
import androidx.compose.material.icons.rounded.DriveFileRenameOutline
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Movie
import androidx.compose.material.icons.rounded.SortByAlpha
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.ImageShader
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.TileMode
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import android.graphics.BitmapFactory
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

/**
 * Dialog to configure Sorting and Grouping for images and videos.
 */
@Composable
internal fun SortAndGroupDialog(
    sort: MediaSort,
    group: MediaGroup,
    onApply: (MediaSort, MediaGroup) -> Unit,
    onDismiss: () -> Unit
) {
    val colors = LocalAppColors.current
    val context = LocalContext.current
    var selectedSortField by remember { mutableStateOf(sort.field) }
    var selectedSortOrder by remember { mutableStateOf(sort.order) }
    var selectedGroupBy by remember { mutableStateOf(group.groupBy) }
    var selectedGroupOrder by remember { mutableStateOf(group.order) }

    val paperNoiseBrush = remember(context) {
        try {
            val bmp = BitmapFactory.decodeResource(context.resources, R.drawable.bg_paper_grain)
            if (bmp != null) {
                val imageBitmap = bmp.asImageBitmap()
                ShaderBrush(ImageShader(imageBitmap, TileMode.Repeated, TileMode.Repeated))
            } else null
        } catch (_: Throwable) {
            null
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            shape = RoundedCornerShape(24.dp),
            color = colors.card,
            border = BorderStroke(1.dp, colors.border.copy(alpha = 0.6f)),
            shadowElevation = 12.dp
        ) {
            Box(Modifier.fillMaxWidth()) {
                if (paperNoiseBrush != null) {
                    Box(
                        Modifier
                            .matchParentSize()
                            .background(paperNoiseBrush, alpha = if (colors.isDark) 0.05f else 0.07f)
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(22.dp)
                ) {
                    Text(
                        "Sort & Group Media",
                        fontWeight = FontWeight.Bold,
                        color = colors.ink,
                        fontSize = 20.sp
                    )

                    Spacer(Modifier.height(16.dp))

                    Column(
                        Modifier
                            .fillMaxWidth()
                            .heightIn(max = 440.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // SORT SECTION
                        Column {
                            Text("SORT BY", fontSize = 11.sp, letterSpacing = 1.2.sp, color = colors.muted, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(8.dp))
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                SortField.entries.forEach { field ->
                                    FilterChip(
                                        selected = selectedSortField == field,
                                        onClick = { selectedSortField = field },
                                        label = { Text(field.label) },
                                        leadingIcon = {
                                            Icon(
                                                if (field == SortField.Date) Icons.Rounded.CalendarMonth else Icons.Rounded.SortByAlpha,
                                                null,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = colors.green.copy(alpha = 0.15f),
                                            selectedLabelColor = colors.green
                                        )
                                    )
                                }
                            }
                            Spacer(Modifier.height(6.dp))
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                FilterChip(
                                    modifier = Modifier.weight(1f),
                                    selected = selectedSortOrder == SortOrder.Descending,
                                    onClick = { selectedSortOrder = SortOrder.Descending },
                                    label = { Text(if (selectedSortField == SortField.Date) "Newest first" else "Z to A", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                                    leadingIcon = { Icon(Icons.Rounded.ArrowDownward, null, modifier = Modifier.size(16.dp)) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = colors.green.copy(alpha = 0.15f),
                                        selectedLabelColor = colors.green
                                    )
                                )
                                FilterChip(
                                    modifier = Modifier.weight(1f),
                                    selected = selectedSortOrder == SortOrder.Ascending,
                                    onClick = { selectedSortOrder = SortOrder.Ascending },
                                    label = { Text(if (selectedSortField == SortField.Date) "Oldest first" else "A to Z", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                                    leadingIcon = { Icon(Icons.Rounded.ArrowUpward, null, modifier = Modifier.size(16.dp)) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = colors.green.copy(alpha = 0.15f),
                                        selectedLabelColor = colors.green
                                    )
                                )
                            }
                        }

                        HorizontalDivider(color = colors.border)

                        // GROUP SECTION
                        Column {
                            Text("GROUP BY", fontSize = 11.sp, letterSpacing = 1.2.sp, color = colors.muted, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(8.dp))
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                GroupBy.entries.forEach { g ->
                                    FilterChip(
                                        selected = selectedGroupBy == g,
                                        onClick = { selectedGroupBy = g },
                                        label = { Text(g.label, maxLines = 1) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = colors.green.copy(alpha = 0.15f),
                                            selectedLabelColor = colors.green
                                        )
                                    )
                                }
                            }
                            if (selectedGroupBy != GroupBy.None) {
                                Spacer(Modifier.height(6.dp))
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    FilterChip(
                                        modifier = Modifier.weight(1f),
                                        selected = selectedGroupOrder == SortOrder.Descending,
                                        onClick = { selectedGroupOrder = SortOrder.Descending },
                                        label = {
                                            Text(
                                                if (selectedGroupBy == GroupBy.Name) "Z to A" else "Newest first",
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        },
                                        leadingIcon = { Icon(Icons.Rounded.ArrowDownward, null, modifier = Modifier.size(16.dp)) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = colors.green.copy(alpha = 0.15f),
                                            selectedLabelColor = colors.green
                                        )
                                    )
                                    FilterChip(
                                        modifier = Modifier.weight(1f),
                                        selected = selectedGroupOrder == SortOrder.Ascending,
                                        onClick = { selectedGroupOrder = SortOrder.Ascending },
                                        label = {
                                            Text(
                                                if (selectedGroupBy == GroupBy.Name) "A to Z" else "Oldest first",
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        },
                                        leadingIcon = { Icon(Icons.Rounded.ArrowUpward, null, modifier = Modifier.size(16.dp)) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = colors.green.copy(alpha = 0.15f),
                                            selectedLabelColor = colors.green
                                        )
                                    )
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = onDismiss) {
                            Text("Cancel", color = colors.muted)
                        }
                        Spacer(Modifier.width(8.dp))
                        Button(
                            onClick = {
                                onApply(
                                    MediaSort(selectedSortField, selectedSortOrder),
                                    MediaGroup(selectedGroupBy, selectedGroupOrder)
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = colors.green),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Apply")
                        }
                    }
                }
            }
        }
    }
}

/**
 * Details Bottom Sheet shown when tapping the "i" info button.
 * Displays all available media details, and provides options to:
 * - Rename the image/video
 * - Delete all metadata from the image
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MediaDetailsSheet(
    photo: Photo,
    onDismiss: () -> Unit,
    onRenamed: (Photo, String) -> Unit,
    onMetadataStripped: (Photo) -> Unit
) {
    val context = LocalContext.current
    val colors = LocalAppColors.current
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var details by remember { mutableStateOf<MediaDetails?>(null) }
    var loading by remember { mutableStateOf(true) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var showStripMetadataConfirm by remember { mutableStateOf(false) }
    var strippingMetadata by remember { mutableStateOf(false) }

    suspend fun reloadDetails() {
        loading = true
        details = MediaDetailsHelper.getDetails(context, photo)
        loading = false
    }

    LaunchedEffect(photo.id) {
        reloadDetails()
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colors.card,
        contentColor = colors.ink
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(colors.chipBackground),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        if (photo.isVideo) Icons.Rounded.Movie else Icons.Rounded.Info,
                        null,
                        tint = colors.green,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text("Details", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = colors.ink)
                    Text(if (photo.isVideo) "VIDEO INFORMATION" else "IMAGE INFORMATION", fontSize = 10.sp, letterSpacing = 1.1.sp, color = colors.muted, fontWeight = FontWeight.SemiBold)
                }
            }

            if (loading || details == null) {
                Box(Modifier.fillMaxWidth().height(160.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = colors.green)
                }
            } else {
                val d = details!!

                // Actions: Rename & Delete Metadata
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Rename button
                    OutlinedButton(
                        onClick = { showRenameDialog = true },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Rounded.DriveFileRenameOutline, null, modifier = Modifier.size(18.dp), tint = colors.green)
                        Spacer(Modifier.width(6.dp))
                        Text("Rename", color = colors.ink, fontSize = 13.sp)
                    }

                    // Delete Metadata button (for images)
                    if (!photo.isVideo) {
                        OutlinedButton(
                            onClick = { showStripMetadataConfirm = true },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            enabled = !strippingMetadata
                        ) {
                            Icon(Icons.Rounded.DeleteForever, null, modifier = Modifier.size(18.dp), tint = colors.red)
                            Spacer(Modifier.width(6.dp))
                            Text("Delete metadata", color = colors.red, fontSize = 13.sp)
                        }
                    }
                }

                // File Info Section
                DetailSectionCard(title = "FILE INFO") {
                    DetailRow("File name", d.name)
                    DetailRow("Size", d.formattedSize)
                    DetailRow("Resolution", d.megapixels)
                    DetailRow("Date", d.dateTaken)
                    d.mimeType?.let { DetailRow("Format", it) }
                    d.relativePath?.let { DetailRow("Folder", it) }
                    d.durationFormatted?.let { DetailRow("Duration", it) }
                }

                // Camera & Lens Details (EXIF)
                if (d.cameraMake != null || d.cameraModel != null || d.aperture != null || d.exposureTime != null || d.iso != null) {
                    DetailSectionCard(title = "CAMERA & SHOT") {
                        val cameraName = listOfNotNull(d.cameraMake, d.cameraModel).distinct().joinToString(" ")
                        if (cameraName.isNotEmpty()) DetailRow("Device", cameraName)
                        d.lensModel?.let { DetailRow("Lens", it) }
                        d.aperture?.let { DetailRow("Aperture", it) }
                        d.exposureTime?.let { DetailRow("Exposure", it) }
                        d.iso?.let { DetailRow("ISO", it) }
                        d.focalLength?.let { DetailRow("Focal length", it) }
                        d.flash?.let { DetailRow("Flash", it) }
                        d.whiteBalance?.let { DetailRow("White balance", it) }
                        d.software?.let { DetailRow("Software", it) }
                    }
                }

                // Location Details (GPS)
                if (d.latitude != null && d.longitude != null) {
                    DetailSectionCard(title = "LOCATION") {
                        DetailRow("Coordinates", "${String.format(java.util.Locale.US, "%.5f", d.latitude)}, ${String.format(java.util.Locale.US, "%.5f", d.longitude)}")
                        d.altitude?.let { DetailRow("Altitude", "${it.toInt()} m") }
                        Spacer(Modifier.height(10.dp))
                        FilledTonalButton(
                            onClick = {
                                val uri = Uri.parse("geo:${d.latitude},${d.longitude}?q=${d.latitude},${d.longitude}(${Uri.encode(photo.name)})")
                                val intent = Intent(Intent.ACTION_VIEW, uri)
                                try {
                                    context.startActivity(intent)
                                } catch (_: Exception) {
                                    Toast.makeText(context, "No maps app installed", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = colors.green.copy(alpha = 0.15f),
                                contentColor = colors.green
                            )
                        ) {
                            Icon(Icons.Rounded.LocationOn, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Open in Maps", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        }
                    }
                }

                // Video Details
                if (photo.isVideo && (d.frameRate != null || d.bitrate != null || d.orientation != null)) {
                    DetailSectionCard(title = "VIDEO PROPERTIES") {
                        d.frameRate?.let { DetailRow("Frame rate", it) }
                        d.bitrate?.let { DetailRow("Bitrate", it) }
                        d.orientation?.let { DetailRow("Orientation", it) }
                    }
                }
            }
        }
    }

    // Rename Dialog
    if (showRenameDialog) {
        RenameMediaDialog(
            currentName = photo.name,
            onDismiss = { showRenameDialog = false },
            onConfirm = { newName ->
                showRenameDialog = false
                scope.launch {
                    val result = MediaDetailsHelper.renameMedia(context, photo, newName)
                    if (result.isSuccess) {
                        val updatedName = result.getOrNull() ?: newName
                        Toast.makeText(context, "Renamed to $updatedName", Toast.LENGTH_SHORT).show()
                        onRenamed(photo, updatedName)
                        reloadDetails()
                    } else {
                        Toast.makeText(context, "Could not rename file", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        )
    }

    // Confirm Delete Metadata Dialog
    if (showStripMetadataConfirm) {
        AlertDialog(
            onDismissRequest = { showStripMetadataConfirm = false },
            title = { Text("Delete all metadata?", fontWeight = FontWeight.Bold, color = colors.ink) },
            text = {
                Text(
                    "This permanently removes camera info, exposure settings, device details, and GPS location from this image.",
                    color = colors.muted,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showStripMetadataConfirm = false
                        strippingMetadata = true
                        scope.launch {
                            val result = MediaDetailsHelper.stripAllMetadata(context, photo.uri)
                            strippingMetadata = false
                            if (result.isSuccess) {
                                Toast.makeText(context, "All metadata removed", Toast.LENGTH_SHORT).show()
                                onMetadataStripped(photo)
                                reloadDetails()
                            } else {
                                Toast.makeText(context, "Failed to strip metadata: ${result.exceptionOrNull()?.message ?: "Unknown"}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = colors.red),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Delete metadata")
                }
            },
            dismissButton = {
                TextButton(onClick = { showStripMetadataConfirm = false }) {
                    Text("Cancel", color = colors.muted)
                }
            },
            containerColor = colors.card
        )
    }
}

@Composable
private fun DetailSectionCard(title: String, content: @Composable () -> Unit) {
    val colors = LocalAppColors.current
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(colors.cardSecondary)
            .padding(14.dp)
    ) {
        Text(title, fontSize = 10.sp, letterSpacing = 1.2.sp, color = colors.muted, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        content()
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    val colors = LocalAppColors.current
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(label, color = colors.muted, fontSize = 13.sp, modifier = Modifier.weight(0.4f))
        Text(
            value,
            color = colors.ink,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(0.6f),
            maxLines = 3,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
internal fun RenameMediaDialog(
    currentName: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    val colors = LocalAppColors.current
    val extension = currentName.substringAfterLast('.', "")
    val nameWithoutExt = if (extension.isNotEmpty()) currentName.substringBeforeLast('.') else currentName
    var newName by remember { mutableStateOf(nameWithoutExt) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Rename item", fontWeight = FontWeight.Bold, color = colors.ink) },
        text = {
            Column(Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    singleLine = true,
                    label = { Text("Name") },
                    supportingText = {
                        if (extension.isNotEmpty()) Text("Extension .$extension will be preserved")
                    }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(newName.trim()) },
                enabled = newName.isNotBlank() && newName.trim() != nameWithoutExt,
                colors = ButtonDefaults.buttonColors(containerColor = colors.green),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Rename")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = colors.muted) }
        },
        containerColor = colors.card
    )
}
