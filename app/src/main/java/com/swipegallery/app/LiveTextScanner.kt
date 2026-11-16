package com.swipegallery.app

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

data class RecognizedLine(
    val text: String,
    val boundingBox: Rect?
)

data class RecognizedBlock(
    val text: String,
    val boundingBox: Rect?,
    val lines: List<RecognizedLine> = emptyList()
)

data class VisionTextResult(
    val fullText: String,
    val blocks: List<RecognizedBlock> = emptyList(),
    val error: String? = null
)

/**
 * On-Device Text Recognition using Google Play Services ML Kit.
 * Operates 100% offline with zero network requests.
 */
suspend fun extractTextFromPhoto(context: Context, uri: Uri): VisionTextResult = suspendCancellableCoroutine { continuation ->
    try {
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        val image = InputImage.fromFilePath(context, uri)
        recognizer.process(image)
            .addOnSuccessListener { visionText ->
                val blocks = visionText.textBlocks.map { block ->
                    RecognizedBlock(
                        text = block.text,
                        boundingBox = block.boundingBox,
                        lines = block.lines.map { line ->
                            RecognizedLine(
                                text = line.text,
                                boundingBox = line.boundingBox
                            )
                        }
                    )
                }
                continuation.resume(
                    VisionTextResult(
                        fullText = visionText.text.trim(),
                        blocks = blocks
                    )
                )
            }
            .addOnFailureListener { error ->
                continuation.resume(
                    VisionTextResult(
                        fullText = "",
                        blocks = emptyList(),
                        error = error.localizedMessage ?: "OCR processing failed"
                    )
                )
            }
    } catch (e: Exception) {
        continuation.resume(
            VisionTextResult(
                fullText = "",
                blocks = emptyList(),
                error = e.localizedMessage ?: "Could not open image for OCR"
            )
        )
    }
}

/**
 * Floating "T" Live Text Button styled like Samsung Gallery / iOS Live Text.
 */
@Composable
fun LiveTextScanButton(
    isScanning: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier
            .size(44.dp)
            .clip(CircleShape),
        shape = CircleShape,
        color = Color(0xFF1E1E1E).copy(alpha = 0.90f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.25f)),
        shadowElevation = 8.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (isScanning) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = Color(0xFF4FD8A2),
                    strokeWidth = 2.5.dp
                )
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "T",
                        fontWeight = FontWeight.Black,
                        fontSize = 17.sp,
                        color = Color(0xFF4FD8A2),
                        fontFamily = FontFamily.SansSerif
                    )
                }
            }
        }
    }
}

/**
 * Modern Live Text Bottom Sheet / Dialog:
 * Allows user to tap, select, copy, and share recognized text directly from the photo.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveTextResultDialog(
    result: VisionTextResult,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF181B1E),
        dragHandle = {
            BottomSheetDefaults.DragHandle(color = Color.White.copy(alpha = 0.3f))
        },
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 20.dp)
        ) {
            // Header
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFF4FD8A2).copy(alpha = 0.15f),
                        modifier = Modifier.size(34.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                "T",
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp,
                                color = Color(0xFF4FD8A2)
                            )
                        }
                    }
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text(
                            "Recognized Text",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            "${result.blocks.sumOf { it.lines.size }} lines detected on-device",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                    }
                }

                IconButton(onClick = onDismiss) {
                    Icon(Icons.Rounded.Close, "Close", tint = Color.White.copy(alpha = 0.7f))
                }
            }

            Spacer(Modifier.height(14.dp))

            // Selectable Text Container
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 120.dp, max = 280.dp),
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF22262A),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
            ) {
                Column(
                    Modifier
                        .fillMaxSize()
                        .padding(14.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    SelectionContainer {
                        Text(
                            text = result.fullText,
                            fontSize = 14.sp,
                            lineHeight = 22.sp,
                            color = Color(0xFFECEFF1),
                            fontFamily = FontFamily.SansSerif
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Actions: Copy All & Share
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Copy All Button
                Button(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("Recognized Text", result.fullText)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "Copied text to clipboard", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4FD8A2)),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Rounded.ContentCopy, null, modifier = Modifier.size(16.dp), tint = Color.Black)
                    Spacer(Modifier.width(6.dp))
                    Text("Copy All", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                // Share Button
                OutlinedButton(
                    onClick = {
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, result.fullText)
                            type = "text/plain"
                        }
                        val shareIntent = Intent.createChooser(sendIntent, "Share text")
                        context.startActivity(shareIntent)
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.25f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                ) {
                    Icon(Icons.Rounded.Share, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Share", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                }
            }
        }
    }
}
