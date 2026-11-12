package com.swipegallery.app

import android.Manifest
import android.app.Activity
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private fun requiredPermissions(): Array<String> = when {
    Build.VERSION.SDK_INT >= 34 -> arrayOf(Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_VIDEO, Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED)
    Build.VERSION.SDK_INT >= 33 -> arrayOf(Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_VIDEO)
    else -> arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
}

private fun isGranted(context: Context, permission: String) =
    ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED

/** True when the app can read at least some media (full, images-only, videos-only, or Android 14 partial access). */
internal fun hasMediaAccess(context: Context) = requiredPermissions().any { isGranted(context, it) }

internal fun openAppSettings(context: Context) {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", context.packageName, null))
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    context.startActivity(intent)
}

private fun queryMedia(context: Context, isVideo: Boolean): List<Photo> {
    val collection = if (isVideo) MediaStore.Video.Media.EXTERNAL_CONTENT_URI else MediaStore.Images.Media.EXTERNAL_CONTENT_URI
    val projection = buildList {
        add(MediaStore.Images.Media._ID)
        add(MediaStore.Images.Media.DISPLAY_NAME)
        add(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)
        add(MediaStore.Images.Media.DATE_ADDED)
        add(MediaStore.MediaColumns.SIZE)
        if (isVideo) add(MediaStore.Video.Media.DURATION)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) add(MediaStore.MediaColumns.RELATIVE_PATH)
    }.toTypedArray()
    val result = mutableListOf<Photo>()
    try {
        context.contentResolver.query(collection, projection, null, null, "${MediaStore.Images.Media.DATE_ADDED} DESC")?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val nameCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
            val albumCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)
            val dateCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED)
            val sizeCol = cursor.getColumnIndex(MediaStore.MediaColumns.SIZE)
            val durationCol = if (isVideo) cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION) else -1
            val pathCol = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.RELATIVE_PATH) else -1
            while (cursor.moveToNext()) {
                val id = cursor.getLong(idCol)
                val sizeBytes = if (sizeCol >= 0 && !cursor.isNull(sizeCol)) cursor.getLong(sizeCol) else 0L
                result += Photo(
                    id = id,
                    uri = ContentUris.withAppendedId(collection, id),
                    name = cursor.getString(nameCol) ?: if (isVideo) "Video" else "Photo",
                    album = cursor.getString(albumCol) ?: "Other",
                    date = cursor.getLong(dateCol),
                    isVideo = isVideo,
                    durationMs = if (durationCol >= 0) cursor.getLong(durationCol) else 0,
                    relativePath = if (pathCol >= 0) cursor.getString(pathCol) else null,
                    sizeBytes = sizeBytes
                )
            }
        }
    } catch (_: SecurityException) {
        // Access to this media type was not granted; show what we can.
    }
    return result
}

@Composable
fun GalleryApp() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val prefs = remember { context.getSharedPreferences("gallery_state", Context.MODE_PRIVATE) }

    // Theme state
    var themeMode by remember {
        mutableStateOf(
            try {
                ThemeMode.valueOf(prefs.getString("theme_mode", ThemeMode.System.name) ?: ThemeMode.System.name)
            } catch (_: Exception) {
                ThemeMode.System
            }
        )
    }
    fun updateTheme(mode: ThemeMode) {
        themeMode = mode
        prefs.edit().putString("theme_mode", mode.name).apply()
    }

    // Sort & Group state
    var mediaSort by remember {
        mutableStateOf(
            try {
                val field = SortField.valueOf(prefs.getString("sort_field", SortField.Date.name) ?: SortField.Date.name)
                val order = SortOrder.valueOf(prefs.getString("sort_order", SortOrder.Descending.name) ?: SortOrder.Descending.name)
                MediaSort(field, order)
            } catch (_: Exception) {
                MediaSort()
            }
        )
    }
    var mediaGroup by remember {
        mutableStateOf(
            try {
                val group = GroupBy.valueOf(prefs.getString("group_by", GroupBy.Date.name) ?: GroupBy.Date.name)
                val order = SortOrder.valueOf(prefs.getString("group_order", SortOrder.Descending.name) ?: SortOrder.Descending.name)
                MediaGroup(group, order)
            } catch (_: Exception) {
                MediaGroup()
            }
        )
    }
    var sortGroupDialogOpen by remember { mutableStateOf(false) }

    fun updateSortAndGroup(sort: MediaSort, group: MediaGroup) {
        mediaSort = sort
        mediaGroup = group
        prefs.edit()
            .putString("sort_field", sort.field.name)
            .putString("sort_order", sort.order.name)
            .putString("group_by", group.groupBy.name)
            .putString("group_order", group.order.name)
            .apply()
    }

    var photos by remember { mutableStateOf<List<Photo>>(emptyList()) }
    var page by remember { mutableStateOf(Page.Photos) }
    var selectedAlbum by remember { mutableStateOf<String?>(null) }
    var selectedPhoto by remember { mutableStateOf<Photo?>(null) }
    var playingVideo by remember { mutableStateOf<Photo?>(null) }
    var videoDetailsPhoto by remember { mutableStateOf<Photo?>(null) }
    var showSettings by remember { mutableStateOf(false) }
    var viewingTrash by remember { mutableStateOf(false) }
    var viewingCleanUp by remember { mutableStateOf(false) }
    var menuOpen by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var isSearching by remember { mutableStateOf(false) }
    var mediaFilter by remember { mutableStateOf(MediaFilter.All) }
    var selectedIds by remember { mutableStateOf<Set<Long>>(emptySet()) }
    var moveDialogOpen by remember { mutableStateOf(false) }
    var pendingMove by remember { mutableStateOf<Pair<List<Photo>, String>?>(null) }
    var previewPhoto by remember { mutableStateOf<Photo?>(null) }
    var loading by remember { mutableStateOf(false) }
    var hasAccess by remember { mutableStateOf(false) }
    var accessBlocked by remember { mutableStateOf(false) }
    var pendingDeleteIds by remember { mutableStateOf<Set<String>>(emptySet()) }

    // Favorites state with persistent storage
    var favoriteIds by remember {
        mutableStateOf(
            prefs.getStringSet("favorites", emptySet())?.mapNotNull { it.toLongOrNull() }?.toSet() ?: emptySet()
        )
    }
    fun toggleFavorite(id: Long) {
        val newSet = if (id in favoriteIds) favoriteIds - id else favoriteIds + id
        favoriteIds = newSet
        prefs.edit().putStringSet("favorites", newSet.map { it.toString() }.toSet()).apply()
    }

    // Trash state with order tracking (reverse order: newest trashed item is on top)
    var trashIds by remember { mutableStateOf(prefs.getStringSet("trash", emptySet())?.toSet() ?: emptySet()) }
    var trashOrder by remember {
        mutableStateOf(
            prefs.getString("trash_order", "")?.split(",")?.filter { it.isNotEmpty() } ?: emptyList()
        )
    }

    fun addToTrash(newIds: Collection<String>) {
        val updatedOrder = newIds.toList() + (trashOrder - newIds.toSet())
        trashOrder = updatedOrder
        trashIds = trashIds + newIds.toSet()
        prefs.edit()
            .putStringSet("trash", trashIds)
            .putString("trash_order", updatedOrder.joinToString(","))
            .apply()
    }

    fun removeFromTrash(removedIds: Collection<String>) {
        val updatedOrder = trashOrder.filter { it !in removedIds }
        trashOrder = updatedOrder
        trashIds = trashIds - removedIds.toSet()
        prefs.edit()
            .putStringSet("trash", trashIds)
            .putString("trash_order", updatedOrder.joinToString(","))
            .apply()
    }

    suspend fun loadPhotos() {
        loading = true
        photos = withContext(Dispatchers.IO) {
            (queryMedia(context, isVideo = false) + queryMedia(context, isVideo = true)).sortedByDescending { it.date }
        }
        loading = false
    }

    fun onAccessGranted() {
        hasAccess = true
        accessBlocked = false
        scope.launch { loadPhotos() }
    }

    val permissions = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { granted ->
        when {
            granted.values.any { it } || hasMediaAccess(context) -> onAccessGranted()
            !hasAccess -> {
                val activity = context as? Activity
                accessBlocked = activity != null && granted.keys.none { ActivityCompat.shouldShowRequestPermissionRationale(activity, it) }
            }
        }
    }

    val deleteLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
        val deletedIds = pendingDeleteIds
        pendingDeleteIds = emptySet()
        if (result.resultCode == Activity.RESULT_OK && deletedIds.isNotEmpty()) {
            removeFromTrash(deletedIds)
            scope.launch { loadPhotos() }
        }
    }

    fun permanentlyDelete(items: List<Photo>) {
        if (items.isEmpty()) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            pendingDeleteIds = items.map { it.id.toString() }.toSet()
            val request = MediaStore.createDeleteRequest(context.contentResolver, items.map { it.uri })
            deleteLauncher.launch(IntentSenderRequest.Builder(request).build())
        } else {
            val removed = mutableSetOf<String>()
            items.forEach { photo ->
                try {
                    context.contentResolver.delete(photo.uri, null, null)
                    removed += photo.id.toString()
                } catch (_: SecurityException) {}
            }
            removeFromTrash(removed)
            scope.launch { loadPhotos() }
        }
    }

    fun applyMove(items: List<Photo>, targetPath: String) {
        scope.launch {
            val failed = withContext(Dispatchers.IO) {
                items.count { item ->
                    try {
                        val values = ContentValues().apply { put(MediaStore.MediaColumns.RELATIVE_PATH, targetPath) }
                        context.contentResolver.update(item.uri, values, null, null) <= 0
                    } catch (_: Exception) {
                        true
                    }
                }
            }
            val moved = items.size - failed
            val message = when {
                failed == 0 -> "Moved $moved ${if (moved == 1) "item" else "items"}"
                moved == 0 -> "Couldn't move these items to that folder"
                else -> "Moved $moved, couldn't move $failed"
            }
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            selectedIds = emptySet()
            loadPhotos()
        }
    }

    val writeLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
        val move = pendingMove
        pendingMove = null
        if (result.resultCode == Activity.RESULT_OK && move != null) applyMove(move.first, move.second)
    }

    fun requestMove(items: List<Photo>, targetPath: String) {
        val toMove = items.filter { it.relativePath != targetPath }
        if (toMove.isEmpty()) { selectedIds = emptySet(); return }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            pendingMove = toMove to targetPath
            val request = MediaStore.createWriteRequest(context.contentResolver, toMove.map { it.uri })
            writeLauncher.launch(IntentSenderRequest.Builder(request).build())
        }
    }

    fun share(items: List<Photo>) {
        if (items.isEmpty()) return
        val uris = ArrayList(items.map { it.uri })
        val type = when {
            items.all { it.isVideo } -> "video/*"
            items.none { it.isVideo } -> "image/*"
            else -> "*/*"
        }
        val send = (
            if (uris.size == 1) Intent(Intent.ACTION_SEND).putExtra(Intent.EXTRA_STREAM, uris.first())
            else Intent(Intent.ACTION_SEND_MULTIPLE).putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris)
        ).setType(type).addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        context.startActivity(Intent.createChooser(send, null))
    }

    fun requestAccess() {
        val all = requiredPermissions()
        if (hasMediaAccess(context)) {
            onAccessGranted()
            val missingVideo = Build.VERSION.SDK_INT >= 33 && !isGranted(context, Manifest.permission.READ_MEDIA_VIDEO) &&
                !isGranted(context, Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED)
            if (missingVideo && !prefs.getBoolean("video_access_asked", false)) {
                prefs.edit().putBoolean("video_access_asked", true).apply()
                permissions.launch(all)
            }
        } else permissions.launch(all)
    }

    LaunchedEffect(Unit) { requestAccess() }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        if (!hasAccess && hasMediaAccess(context)) onAccessGranted()
    }

    val visiblePhotos = photos.filter { it.id.toString() !in trashIds }
    // In trash bin, sort by reverse order: newest trashed item is on top!
    val trashed = photos.filter { it.id.toString() in trashIds }.sortedBy {
        val idx = trashOrder.indexOf(it.id.toString())
        if (idx >= 0) idx else Int.MAX_VALUE
    }

    val albumPhotos = visiblePhotos.filter { selectedAlbum == null || it.album == selectedAlbum }
    val filteredByMedia = albumPhotos.filter {
        when (mediaFilter) {
            MediaFilter.All -> true
            MediaFilter.Photos -> !it.isVideo
            MediaFilter.Videos -> it.isVideo
            MediaFilter.Favorites -> it.id in favoriteIds
        }
    }
    val gridItems = if (searchQuery.isBlank()) {
        filteredByMedia
    } else {
        val q = searchQuery.trim()
        filteredByMedia.filter { it.name.contains(q, ignoreCase = true) }
    }

    val selectedItems = gridItems.filter { it.id in selectedIds }
    val selecting = selectedIds.isNotEmpty() && page == Page.Photos && selectedPhoto == null
    fun clearSelection() { selectedIds = emptySet() }
    val canNavigateBack = menuOpen || viewingCleanUp || viewingTrash || isSearching || searchQuery.isNotEmpty() || videoDetailsPhoto != null || playingVideo != null || previewPhoto != null || selecting || showSettings || selectedPhoto != null || selectedAlbum != null || page != Page.Photos

    BackHandler(enabled = canNavigateBack) {
        when {
            menuOpen -> menuOpen = false
            videoDetailsPhoto != null -> videoDetailsPhoto = null
            playingVideo != null -> playingVideo = null
            previewPhoto != null -> previewPhoto = null
            selecting -> clearSelection()
            showSettings -> showSettings = false
            viewingTrash -> viewingTrash = false
            viewingCleanUp -> viewingCleanUp = false
            selectedPhoto != null -> selectedPhoto = null
            isSearching || searchQuery.isNotEmpty() -> {
                isSearching = false
                searchQuery = ""
            }
            selectedAlbum != null -> { selectedAlbum = null; page = Page.Albums }
            page != Page.Photos -> page = Page.Photos
        }
    }

    GalleryTheme(themeMode = themeMode) {
        val colors = LocalAppColors.current

        Surface(Modifier.fillMaxSize(), color = colors.canvas) {
            when {
                showSettings -> {
                    SettingsPage(
                        hasAccess = hasAccess,
                        currentTheme = themeMode,
                        onThemeChange = { updateTheme(it) },
                        onBack = { showSettings = false }
                    )
                }
                viewingCleanUp -> {
                    CleanUpPage(
                        photos = visiblePhotos,
                        onBack = { viewingCleanUp = false },
                        onPreviewPhoto = { photo ->
                            if (photo.isVideo) playingVideo = photo else previewPhoto = photo
                        },
                        onDeleteItems = { items -> permanentlyDelete(items) }
                    )
                }
                viewingTrash -> {
                    Column(Modifier.fillMaxSize().background(colors.canvas)) {
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .statusBarsPadding()
                                .padding(horizontal = 22.dp, vertical = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = { viewingTrash = false }) {
                                Icon(
                                    Icons.AutoMirrored.Rounded.ArrowBack,
                                    contentDescription = "Back",
                                    tint = colors.ink
                                )
                            }
                            Column(Modifier.weight(1f)) {
                                Text(
                                    "Trash",
                                    fontSize = 26.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.ink
                                )
                                Text(
                                    "${trashed.size} items · recoverable bin",
                                    fontSize = 10.sp,
                                    letterSpacing = 1.2.sp,
                                    color = colors.muted,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                        TrashPage(
                            photos = trashed,
                            onRestore = { photo -> removeFromTrash(listOf(photo.id.toString())) },
                            onDelete = { photo -> permanentlyDelete(listOf(photo)) },
                            onEmpty = { ids -> permanentlyDelete(trashed.filter { it.id.toString() in ids }) }
                        )
                    }
                }
                else -> {
                    Box(Modifier.fillMaxSize()) {
                        Column(Modifier.fillMaxSize()) {
                            if (selecting) {
                                SelectionTopBar(
                                    count = selectedItems.size,
                                    allSelected = selectedItems.size == gridItems.size,
                                    onClose = { clearSelection() },
                                    onToggleAll = {
                                        selectedIds = if (selectedItems.size == gridItems.size) emptySet() else gridItems.map { it.id }.toSet()
                                    }
                                )
                            } else if (selectedPhoto == null) {
                            Header(
                                page = page,
                                count = gridItems.size,
                                album = selectedAlbum,
                                isSearching = isSearching,
                                onToggleSearch = {
                                    isSearching = !isSearching
                                    if (!isSearching) searchQuery = ""
                                }
                            ) {
                                clearSelection()
                                if (isSearching) {
                                    isSearching = false
                                    searchQuery = ""
                                }
                                if (selectedAlbum != null) { selectedAlbum = null; page = Page.Albums }
                                else page = Page.Photos
                            }

                            AnimatedVisibility(
                                visible = isSearching && (page == Page.Photos || selectedAlbum != null),
                                enter = fadeIn(tween(180)) + expandVertically(),
                                exit = fadeOut(tween(140)) + shrinkVertically()
                            ) {
                                SearchBar(
                                    query = searchQuery,
                                    onQueryChange = { searchQuery = it }
                                )
                            }
                        }

                        Box(Modifier.weight(1f).fillMaxWidth()) {
                            when {
                                loading && photos.isEmpty() -> AppSplashScreen(Modifier.align(Alignment.Center))
                                !hasAccess -> PermissionEmpty(
                                    blocked = accessBlocked,
                                    onGrant = { requestAccess() },
                                    onOpenSettings = { openAppSettings(context) }
                                )
                                else -> AnimatedContent(
                                    targetState = page,
                                    transitionSpec = {
                                        (fadeIn(tween(180)) + slideInHorizontally(tween(220, easing = FastOutSlowInEasing)) { it / 18 }) togetherWith
                                            (fadeOut(tween(140)) + slideOutHorizontally(tween(220, easing = FastOutSlowInEasing)) { -it / 18 })
                                    },
                                    label = "section navigation"
                                ) { visiblePage ->
                                    when (visiblePage) {
                                        Page.Photos -> AnimatedContent(
                                            targetState = selectedPhoto,
                                            contentKey = { it != null },
                                            transitionSpec = {
                                                if (targetState == null) {
                                                    (fadeIn(tween(220)) + slideInVertically(tween(260, easing = FastOutSlowInEasing)) { -it / 8 }) togetherWith
                                                        (fadeOut(tween(180)) + slideOutVertically(tween(240)) { it / 5 })
                                                } else {
                                                    (fadeIn(tween(220)) + slideInVertically(tween(280, easing = FastOutSlowInEasing)) { it / 5 }) togetherWith
                                                        (fadeOut(tween(180)) + slideOutVertically(tween(220)) { -it / 6 })
                                                }
                                            },
                                            label = "photo viewer navigation"
                                        ) { openingPhoto ->
                                            if (openingPhoto == null) {
                                                PhotoGrid(
                                                    photos = gridItems,
                                                    filter = mediaFilter,
                                                    onFilter = { mediaFilter = it },
                                                    sort = mediaSort,
                                                    group = mediaGroup,
                                                    onGroupChange = { updateSortAndGroup(mediaSort, it) },
                                                    onOpenSortGroup = { sortGroupDialogOpen = true },
                                                    selectedIds = if (selecting) selectedIds else emptySet(),
                                                    favoriteIds = favoriteIds,
                                                    onPhoto = { photo ->
                                                        if (selecting) {
                                                            selectedIds = if (photo.id in selectedIds) selectedIds - photo.id else selectedIds + photo.id
                                                        } else {
                                                            selectedPhoto = photo
                                                        }
                                                    },
                                                    onLongPress = { photo -> selectedIds = selectedIds + photo.id },
                                                    empty = !loading,
                                                    searchQuery = searchQuery
                                                )
                                            } else {
                                                PhotoDetail(
                                                    initialPhoto = openingPhoto,
                                                    photos = gridItems,
                                                    favoriteIds = favoriteIds,
                                                    onToggleFavorite = { toggleFavorite(it) },
                                                    onClose = { selectedPhoto = null },
                                                    onPlay = { playingVideo = it },
                                                    onTrash = { photo ->
                                                        val index = gridItems.indexOfFirst { it.id == photo.id }
                                                        val remaining = gridItems.filter { it.id != photo.id }
                                                        addToTrash(listOf(photo.id.toString()))
                                                        selectedPhoto = remaining.getOrNull(index) ?: remaining.lastOrNull()
                                                    },
                                                    onRenamed = { _, _ -> scope.launch { loadPhotos() } },
                                                    onMetadataStripped = { _ -> scope.launch { loadPhotos() } },
                                                    onShare = { share(listOf(it)) }
                                                )
                                            }
                                        }
                                        Page.Albums -> Albums(
                                            photos = visiblePhotos,
                                            favoritePhotos = visiblePhotos.filter { it.id in favoriteIds },
                                            onAlbum = { clearSelection(); selectedAlbum = it; page = Page.Photos },
                                            onOpenFavorites = { clearSelection(); page = Page.Photos; mediaFilter = MediaFilter.Favorites }
                                        )
                                        Page.Swipe -> SwipePage(
                                            photos = visiblePhotos,
                                            onKeep = {},
                                            onTrash = { photo -> addToTrash(listOf(photo.id.toString())) },
                                            onOpen = { photo -> if (photo.isVideo) playingVideo = photo else previewPhoto = photo }
                                        )
                                    }
                                }
                            }
                        }

                        if (selecting) {
                            SelectionActionBar(
                                enabled = selectedItems.isNotEmpty(),
                                canMove = Build.VERSION.SDK_INT >= Build.VERSION_CODES.R,
                                onShare = { share(selectedItems) },
                                onMove = { moveDialogOpen = true },
                                onDelete = {
                                    addToTrash(selectedItems.map { it.id.toString() })
                                    val count = selectedItems.size
                                    clearSelection()
                                    Toast.makeText(context, "Moved $count ${if (count == 1) "item" else "items"} to the bin", Toast.LENGTH_SHORT).show()
                                }
                            )
                        } else if (selectedPhoto == null) {
                            BottomBar(
                                page = page,
                                menuOpen = menuOpen,
                                onPage = {
                                    clearSelection()
                                    menuOpen = false
                                    if (it != Page.Photos) {
                                        isSearching = false
                                        searchQuery = ""
                                    }
                                    page = it
                                    selectedAlbum = null
                                },
                                onToggleMenu = {
                                    menuOpen = !menuOpen
                                }
                            )
                        }
                    }

                    if (menuOpen) {
                        val duplicatesCount = remember(visiblePhotos) { detectDuplicateClusters(visiblePhotos).size }
                        MenuPopover(
                            trashedCount = trashed.size,
                            favoritesCount = favoriteIds.size,
                            duplicatesCount = duplicatesCount,
                            onDismiss = { menuOpen = false },
                            onOpenCleanUp = {
                                menuOpen = false
                                viewingCleanUp = true
                            },
                            onOpenTrash = {
                                menuOpen = false
                                viewingTrash = true
                            },
                            onOpenFavorites = {
                                menuOpen = false
                                clearSelection()
                                page = Page.Photos
                                mediaFilter = MediaFilter.Favorites
                            },
                            onOpenSettings = {
                                menuOpen = false
                                showSettings = true
                            }
                        )
                    }
                }
            }
        }

            if (sortGroupDialogOpen) {
                SortAndGroupDialog(
                    sort = mediaSort,
                    group = mediaGroup,
                    onApply = { newSort, newGroup ->
                        sortGroupDialogOpen = false
                        updateSortAndGroup(newSort, newGroup)
                    },
                    onDismiss = { sortGroupDialogOpen = false }
                )
            }

            if (moveDialogOpen) {
                val albums = visiblePhotos.filter { it.relativePath != null }
                    .groupBy { it.relativePath!! }
                    .map { (path, items) -> AlbumTarget(items.first().album, path, items.size) }
                    .sortedByDescending { it.count }
                MoveToAlbumDialog(
                    count = selectedItems.size,
                    albums = albums,
                    onDismiss = { moveDialogOpen = false },
                    onMove = { path -> moveDialogOpen = false; requestMove(selectedItems, path) }
                )
            }

            previewPhoto?.let { item ->
                MediaPreview(
                    item = item,
                    isFavorite = item.id in favoriteIds,
                    onToggleFavorite = { toggleFavorite(item.id) },
                    onClose = { previewPhoto = null },
                    onPlay = { playingVideo = it }
                )
            }

            playingVideo?.let { video ->
                VideoPlayer(
                    video = video,
                    isFavorite = video.id in favoriteIds,
                    onToggleFavorite = { toggleFavorite(video.id) },
                    onClose = { playingVideo = null },
                    onShowDetails = { videoDetailsPhoto = it }
                )
            }

            videoDetailsPhoto?.let { videoPhoto ->
                MediaDetailsSheet(
                    photo = videoPhoto,
                    onDismiss = { videoDetailsPhoto = null },
                    onRenamed = { _, _ -> scope.launch { loadPhotos() } },
                    onMetadataStripped = { _ -> scope.launch { loadPhotos() } }
                )
            }
        }
    }
}
