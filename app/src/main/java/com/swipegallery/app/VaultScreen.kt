package com.swipegallery.app

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Backspace
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.LockOpen
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import java.security.MessageDigest

internal fun hashPin(pin: String): String {
    val md = MessageDigest.getInstance("SHA-256")
    val digest = md.digest(pin.toByteArray(Charsets.UTF_8))
    return digest.fold("") { str, it -> str + "%02x".format(it) }
}

@Composable
internal fun VaultScreen(
    savedPinHash: String?,
    vaultPhotos: List<Photo>,
    onSaveNewPin: (String) -> Unit,
    onBack: () -> Unit,
    onPreviewPhoto: (Photo) -> Unit,
    onUnhidePhoto: (Photo) -> Unit,
    onDeletePhoto: (Photo) -> Unit
) {
    val colors = LocalAppColors.current
    var isUnlocked by remember { mutableStateOf(false) }

    if (!isUnlocked) {
        VaultPinScreen(
            savedPinHash = savedPinHash,
            onPinSuccess = { isUnlocked = true },
            onSaveNewPin = { newHash ->
                onSaveNewPin(newHash)
                isUnlocked = true
            },
            onBack = onBack
        )
    } else {
        VaultContentScreen(
            photos = vaultPhotos,
            onLock = { isUnlocked = false },
            onBack = onBack,
            onPreviewPhoto = onPreviewPhoto,
            onUnhidePhoto = onUnhidePhoto,
            onDeletePhoto = onDeletePhoto
        )
    }
}

@Composable
private fun VaultPinScreen(
    savedPinHash: String?,
    onPinSuccess: () -> Unit,
    onSaveNewPin: (String) -> Unit,
    onBack: () -> Unit
) {
    val colors = LocalAppColors.current
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    val isSetup = savedPinHash.isNullOrEmpty()

    var enteredPin by remember { mutableStateOf("") }
    var confirmPinStage by remember { mutableStateOf(false) }
    var firstEnteredPin by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    val shakeOffset = remember { Animatable(0f) }

    fun triggerShake() {
        scope.launch {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            repeat(3) {
                shakeOffset.animateTo(24f, spring(dampingRatio = Spring.DampingRatioHighBouncy, stiffness = Spring.StiffnessHigh))
                shakeOffset.animateTo(-24f, spring(dampingRatio = Spring.DampingRatioHighBouncy, stiffness = Spring.StiffnessHigh))
            }
            shakeOffset.animateTo(0f)
        }
    }

    fun onDigit(d: String) {
        if (enteredPin.length < 4) {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            val newPin = enteredPin + d
            enteredPin = newPin
            errorMessage = ""

            if (newPin.length == 4) {
                if (isSetup) {
                    if (!confirmPinStage) {
                        firstEnteredPin = newPin
                        confirmPinStage = true
                        enteredPin = ""
                    } else {
                        if (newPin == firstEnteredPin) {
                            onSaveNewPin(hashPin(newPin))
                        } else {
                            errorMessage = "PINs do not match. Try again."
                            triggerShake()
                            enteredPin = ""
                            confirmPinStage = false
                            firstEnteredPin = ""
                        }
                    }
                } else {
                    if (hashPin(newPin) == savedPinHash) {
                        onPinSuccess()
                    } else {
                        errorMessage = "Incorrect PIN"
                        triggerShake()
                        enteredPin = ""
                    }
                }
            }
        }
    }

    fun onBackspace() {
        if (enteredPin.isNotEmpty()) {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            enteredPin = enteredPin.dropLast(1)
            errorMessage = ""
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.canvas)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = colors.ink)
            }
            Spacer(Modifier.weight(1f))
        }

        Spacer(Modifier.height(24.dp))

        // Lock icon
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(colors.green.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Rounded.Lock,
                contentDescription = null,
                tint = colors.green,
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(Modifier.height(18.dp))

        Text(
            text = when {
                !isSetup -> "Enter Vault PIN"
                confirmPinStage -> "Confirm your 4-digit PIN"
                else -> "Set up Vault PIN"
            },
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = colors.ink
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = when {
                !isSetup -> "Your private photos are encrypted & kept strictly offline on your device"
                confirmPinStage -> "Re-enter the same 4 digits to confirm"
                else -> "Choose a 4-digit PIN to protect your private photos"
            },
            fontSize = 13.sp,
            color = colors.muted,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 20.dp)
        )

        Spacer(Modifier.height(32.dp))

        // 4 PIN indicator dots with shake animation on error
        Row(
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            modifier = Modifier.graphicsLayer { translationX = shakeOffset.value }
        ) {
            for (i in 0 until 4) {
                val filled = i < enteredPin.length
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(if (filled) colors.green else colors.chipBackground)
                        .border(1.dp, if (filled) colors.green else colors.border, CircleShape)
                )
            }
        }

        if (errorMessage.isNotEmpty()) {
            Spacer(Modifier.height(14.dp))
            Text(errorMessage, color = Color(0xFFE53935), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        }

        Spacer(Modifier.weight(1f))

        // Keypad (1 to 9, then empty, 0, backspace)
        Column(
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            listOf(
                listOf("1", "2", "3"),
                listOf("4", "5", "6"),
                listOf("7", "8", "9"),
                listOf("", "0", "DEL")
            ).forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    row.forEach { key ->
                        if (key.isEmpty()) {
                            Spacer(Modifier.size(68.dp))
                        } else if (key == "DEL") {
                            Box(
                                modifier = Modifier
                                    .size(68.dp)
                                    .clip(CircleShape)
                                    .clickable { onBackspace() },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.AutoMirrored.Rounded.Backspace, contentDescription = "Delete", tint = colors.ink, modifier = Modifier.size(24.dp))
                            }
                        } else {
                            Surface(
                                modifier = Modifier
                                    .size(68.dp)
                                    .clip(CircleShape)
                                    .clickable { onDigit(key) },
                                shape = CircleShape,
                                color = colors.card,
                                border = BorderStroke(1.dp, colors.border),
                                shadowElevation = 1.dp
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = key,
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = colors.ink
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun VaultContentScreen(
    photos: List<Photo>,
    onLock: () -> Unit,
    onBack: () -> Unit,
    onPreviewPhoto: (Photo) -> Unit,
    onUnhidePhoto: (Photo) -> Unit,
    onDeletePhoto: (Photo) -> Unit
) {
    val colors = LocalAppColors.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.canvas)
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = colors.ink)
            }
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Hidden Vault",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.ink
                    )
                    Spacer(Modifier.width(8.dp))
                    Icon(Icons.Rounded.Lock, contentDescription = null, tint = colors.green, modifier = Modifier.size(18.dp))
                }
                Text(
                    text = "${photos.size} private items · 100% offline & local",
                    fontSize = 11.sp,
                    color = colors.muted
                )
            }
            FilledTonalIconButton(
                onClick = onLock,
                shape = CircleShape,
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = colors.chipBackground,
                    contentColor = colors.ink
                )
            ) {
                Icon(Icons.Rounded.LockOpen, contentDescription = "Lock now", modifier = Modifier.size(20.dp))
            }
        }

        if (photos.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(colors.chipBackground),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Rounded.Lock, contentDescription = null, tint = colors.muted, modifier = Modifier.size(32.dp))
                    }
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "Vault is empty",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = colors.ink
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Tap any photo in your gallery and choose 'Hide to Vault' to keep it completely private and protected by your PIN.",
                        fontSize = 13.sp,
                        color = colors.muted,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(photos, key = { it.id }) { photo ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = colors.card),
                        border = BorderStroke(1.dp, colors.border)
                    ) {
                        Column {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(1f)
                                    .clickable { onPreviewPhoto(photo) }
                            ) {
                                GalleryImage(
                                    model = photo.uri,
                                    description = photo.name,
                                    modifier = Modifier.fillMaxSize()
                                )
                                if (photo.isVideo) {
                                    VideoBadge(photo.durationMs, Modifier.align(Alignment.BottomEnd).padding(4.dp))
                                }
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 4.dp, vertical = 2.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Unhide button
                                IconButton(
                                    onClick = { onUnhidePhoto(photo) },
                                    modifier = Modifier.size(34.dp)
                                ) {
                                    Icon(
                                        Icons.Rounded.Visibility,
                                        contentDescription = "Unhide",
                                        tint = colors.green,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                // Delete button
                                IconButton(
                                    onClick = { onDeletePhoto(photo) },
                                    modifier = Modifier.size(34.dp)
                                ) {
                                    Icon(
                                        Icons.Rounded.DeleteOutline,
                                        contentDescription = "Delete permanently",
                                        tint = Color(0xFFE53935),
                                        modifier = Modifier.size(18.dp)
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
