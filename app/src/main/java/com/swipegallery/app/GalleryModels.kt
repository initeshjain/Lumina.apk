package com.swipegallery.app

import android.net.Uri
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.log10
import kotlin.math.pow

data class Photo(
    val id: Long,
    val uri: Uri,
    val name: String,
    val album: String,
    val date: Long,
    val isVideo: Boolean = false,
    val durationMs: Long = 0,
    /** MediaStore RELATIVE_PATH (e.g. "DCIM/Camera/"); null before Android 10. */
    val relativePath: String? = null,
    val sizeBytes: Long = 0,
    /** Camera shutter timestamp in milliseconds (from EXIF/MediaStore DATE_TAKEN). */
    val dateTakenMs: Long = 0L,
)

enum class Page { Photos, Albums, Search, CleanUp, More }

enum class MediaFilter(val label: String) { All("All"), Photos("Photos"), Videos("Videos"), Favorites("Favorites") }

enum class ThemeMode(val label: String) {
    System("System default"),
    Light("Light mode"),
    Dark("Dark mode")
}

enum class SortField(val label: String) {
    Date("Date"),
    Name("Name")
}

enum class SortOrder(val label: String) {
    Descending("Descending"),
    Ascending("Ascending")
}

data class MediaSort(
    val field: SortField = SortField.Date,
    val order: SortOrder = SortOrder.Descending
)

enum class GroupBy(val label: String) {
    None("None"),
    Date("Day"),
    Month("Month"),
    Year("Year"),
    Name("Name")
}

data class MediaGroup(
    val groupBy: GroupBy = GroupBy.Date,
    val order: SortOrder = SortOrder.Descending
)

internal const val SUPPORT_EMAIL = "jay@noobgeek.in"

internal fun formatDuration(ms: Long): String {
    val totalSeconds = ms / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) "%d:%02d:%02d".format(hours, minutes, seconds) else "%d:%02d".format(minutes, seconds)
}

internal fun formatFileSize(bytes: Long): String {
    if (bytes <= 0) return "0 B"
    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    val digitGroups = (log10(bytes.toDouble()) / log10(1024.0)).toInt().coerceIn(0, units.size - 1)
    val value = bytes / 1024.0.pow(digitGroups.toDouble())
    return "${DecimalFormat("#,##0.#").format(value)} ${units[digitGroups]}"
}

enum class FolderViewMode {
    Grouped, // Shows subfolders (Months in Year, Dates in Month)
    All      // Shows all images in this folder in a flat grid
}

enum class AlbumTab(val label: String) {
    Device("Device Albums"),
    Years("Year Folders")
}

data class TimeFolder(
    val id: String,
    val title: String,
    val subtitle: String,
    val coverPhoto: Photo?,
    val items: List<Photo>,
    val year: Int? = null,
    val month: Int? = null,
    val day: Int? = null
)

internal val Photo.calendar: Calendar
    get() = Calendar.getInstance().apply { timeInMillis = date * 1000L }

internal val Photo.year: Int
    get() = calendar.get(Calendar.YEAR)

internal val Photo.month: Int
    get() = calendar.get(Calendar.MONTH) + 1

internal val Photo.day: Int
    get() = calendar.get(Calendar.DAY_OF_MONTH)

internal val Photo.monthName: String
    get() = SimpleDateFormat("MMMM", Locale.getDefault()).format(Date(date * 1000L))

internal val Photo.dayLabel: String
    get() = SimpleDateFormat("d MMMM", Locale.getDefault()).format(Date(date * 1000L))
