package com.swipegallery.app

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/**
 * Solar Icons - Linear Style (1.5px stroke, round caps, 24x24 viewBox).
 * Reference: https://solar-icons.dev/icons?style=linear
 */
object SolarIcons {

    /** Solar Linear Gallery / Pictures */
    val Gallery: ImageVector by lazy {
        ImageVector.Builder(
            name = "SolarGalleryLinear",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            // Outer rounded square frame
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.5f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(2f, 12f)
                curveTo(2f, 7.286f, 2f, 4.929f, 3.464f, 3.464f)
                curveTo(4.929f, 2f, 7.286f, 2f, 12f, 2f)
                curveTo(16.714f, 2f, 19.071f, 2f, 20.536f, 3.464f)
                curveTo(22f, 4.929f, 22f, 7.286f, 22f, 12f)
                curveTo(22f, 16.714f, 22f, 19.071f, 20.536f, 20.536f)
                curveTo(19.071f, 22f, 16.714f, 22f, 12f, 22f)
                curveTo(7.286f, 22f, 4.929f, 22f, 3.464f, 20.536f)
                curveTo(2f, 19.071f, 2f, 16.714f, 2f, 12f)
                close()
            }
            // Sun circle
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.5f,
                strokeLineCap = StrokeCap.Round
            ) {
                moveTo(8.5f, 10.5f)
                curveTo(9.605f, 10.5f, 10.5f, 9.605f, 10.5f, 8.5f)
                curveTo(10.5f, 7.395f, 9.605f, 6.5f, 8.5f, 6.5f)
                curveTo(7.395f, 6.5f, 6.5f, 7.395f, 6.5f, 8.5f)
                curveTo(6.5f, 9.605f, 7.395f, 10.5f, 8.5f, 10.5f)
                close()
            }
            // Mountains path
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.5f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(2.68f, 17.5f)
                lineTo(7.34f, 13.08f)
                curveTo(8.07f, 12.38f, 9.2f, 12.38f, 9.93f, 13.08f)
                lineTo(13.5f, 16.5f)
            }
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.5f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(12.5f, 15.5f)
                lineTo(14.84f, 13.25f)
                curveTo(15.57f, 12.55f, 16.7f, 12.55f, 17.43f, 13.25f)
                lineTo(21.32f, 17f)
            }
        }.build()
    }

    /** Solar Linear Folder / Album */
    val Folder: ImageVector by lazy {
        ImageVector.Builder(
            name = "SolarFolderLinear",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.5f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(2f, 11f)
                curveTo(2f, 7.23f, 2f, 5.34f, 3.17f, 4.17f)
                curveTo(4.34f, 3f, 6.23f, 3f, 10f, 3f)
                lineTo(11.17f, 3f)
                curveTo(11.83f, 3f, 12.46f, 3.26f, 12.93f, 3.73f)
                lineTo(14.27f, 5.07f)
                curveTo(14.74f, 5.54f, 15.37f, 5.8f, 16.03f, 5.8f)
                lineTo(18f, 5.8f)
                curveTo(20.36f, 5.8f, 21.54f, 5.8f, 22.27f, 6.53f)
                curveTo(23f, 7.26f, 23f, 8.44f, 23f, 10.8f)
                lineTo(23f, 13.2f)
                curveTo(23f, 17.37f, 23f, 19.46f, 21.7f, 20.73f)
                curveTo(20.4f, 22f, 18.31f, 22f, 14.14f, 22f)
                lineTo(10.86f, 22f)
                curveTo(6.69f, 22f, 4.6f, 22f, 3.3f, 20.73f)
                curveTo(2.07f, 19.52f, 2.01f, 17.57f, 2f, 14f)
                close()
            }
        }.build()
    }

    /** Solar Linear Magnifier / Search */
    val Magnifier: ImageVector by lazy {
        ImageVector.Builder(
            name = "SolarMagnifierLinear",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.5f,
                strokeLineCap = StrokeCap.Round
            ) {
                moveTo(11.5f, 21f)
                curveTo(16.747f, 21f, 21f, 16.747f, 21f, 11.5f)
                curveTo(21f, 6.253f, 16.747f, 2f, 11.5f, 2f)
                curveTo(6.253f, 2f, 2f, 6.253f, 2f, 11.5f)
                curveTo(2f, 16.747f, 6.253f, 21f, 11.5f, 21f)
                close()
            }
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.5f,
                strokeLineCap = StrokeCap.Round
            ) {
                moveTo(18.5f, 18.5f)
                lineTo(22f, 22f)
            }
        }.build()
    }

    /** Solar Linear Stars / Magic Stick (Clean Up) */
    val Stars: ImageVector by lazy {
        ImageVector.Builder(
            name = "SolarStarsLinear",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            // Main 4-point star
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.5f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(14f, 2f)
                curveTo(14f, 6.42f, 17.58f, 10f, 22f, 10f)
                curveTo(17.58f, 10f, 14f, 13.58f, 14f, 18f)
                curveTo(14f, 13.58f, 10.42f, 10f, 6f, 10f)
                curveTo(10.42f, 10f, 14f, 6.42f, 14f, 2f)
                close()
            }
            // Secondary small star
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.5f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(5f, 16f)
                curveTo(5f, 17.66f, 6.34f, 19f, 8f, 19f)
                curveTo(6.34f, 19f, 5f, 20.34f, 5f, 22f)
                curveTo(5f, 20.34f, 3.66f, 19f, 2f, 19f)
                curveTo(3.66f, 19f, 5f, 17.66f, 5f, 16f)
                close()
            }
        }.build()
    }

    /** Solar Linear Menu Dots / Settings / More */
    val MenuDots: ImageVector by lazy {
        ImageVector.Builder(
            name = "SolarMenuDotsLinear",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.5f,
                strokeLineCap = StrokeCap.Round
            ) {
                moveTo(5f, 12f)
                curveTo(5f, 12.55f, 4.55f, 13f, 4f, 13f)
                curveTo(3.45f, 13f, 3f, 12.55f, 3f, 12f)
                curveTo(3f, 11.45f, 3.45f, 11f, 4f, 11f)
                curveTo(4.55f, 11f, 5f, 11.45f, 5f, 12f)
                close()
            }
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.5f,
                strokeLineCap = StrokeCap.Round
            ) {
                moveTo(13f, 12f)
                curveTo(13f, 12.55f, 12.55f, 13f, 12f, 13f)
                curveTo(11.45f, 13f, 11f, 12.55f, 11f, 12f)
                curveTo(11f, 11.45f, 11.45f, 11f, 12f, 11f)
                curveTo(12.55f, 11f, 13f, 11.45f, 13f, 12f)
                close()
            }
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.5f,
                strokeLineCap = StrokeCap.Round
            ) {
                moveTo(21f, 12f)
                curveTo(21f, 12.55f, 20.55f, 13f, 20f, 13f)
                curveTo(19.45f, 13f, 19f, 12.55f, 19f, 12f)
                curveTo(19f, 11.45f, 19.45f, 11f, 20f, 11f)
                curveTo(20.55f, 11f, 21f, 11.45f, 21f, 12f)
                close()
            }
        }.build()
    }

    /** Solar Linear Heart / Favorite */
    val Heart: ImageVector by lazy {
        ImageVector.Builder(
            name = "SolarHeartLinear",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.5f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(12f, 21.35f)
                lineTo(10.55f, 20.03f)
                curveTo(5.4f, 15.36f, 2f, 12.28f, 2f, 8.5f)
                curveTo(2f, 5.42f, 4.42f, 3f, 7.5f, 3f)
                curveTo(9.24f, 3f, 10.91f, 3.81f, 12f, 5.09f)
                curveTo(13.09f, 3.81f, 14.76f, 3f, 16.5f, 3f)
                curveTo(19.58f, 3f, 22f, 5.42f, 22f, 8.5f)
                curveTo(22f, 12.28f, 18.6f, 15.36f, 13.45f, 20.04f)
                lineTo(12f, 21.35f)
                close()
            }
        }.build()
    }

    /** Solar Linear Video Camera */
    val VideoCamera: ImageVector by lazy {
        ImageVector.Builder(
            name = "SolarVideoCameraLinear",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.5f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(2f, 12f)
                curveTo(2f, 8.23f, 2f, 6.34f, 3.17f, 5.17f)
                curveTo(4.34f, 4f, 6.23f, 4f, 10f, 4f)
                lineTo(11f, 4f)
                curveTo(14.77f, 4f, 16.66f, 4f, 17.83f, 5.17f)
                curveTo(19f, 6.34f, 19f, 8.23f, 19f, 12f)
                curveTo(19f, 15.77f, 19f, 17.66f, 17.83f, 18.83f)
                curveTo(16.66f, 20f, 14.77f, 20f, 11f, 20f)
                lineTo(10f, 20f)
                curveTo(6.23f, 20f, 4.34f, 20f, 3.17f, 18.83f)
                curveTo(2f, 17.66f, 2f, 15.77f, 2f, 12f)
                close()
            }
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.5f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(19f, 10.5f)
                lineTo(21.4f, 8.7f)
                curveTo(21.8f, 8.4f, 22.4f, 8.7f, 22.4f, 9.2f)
                lineTo(22.4f, 14.8f)
                curveTo(22.4f, 15.3f, 21.8f, 15.6f, 21.4f, 15.3f)
                lineTo(19f, 13.5f)
            }
        }.build()
    }

    /** Solar Linear Grid / All */
    val Widget: ImageVector by lazy {
        ImageVector.Builder(
            name = "SolarWidgetLinear",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.5f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(3f, 7.5f)
                curveTo(3f, 5.38f, 3f, 4.32f, 3.66f, 3.66f)
                curveTo(4.32f, 3f, 5.38f, 3f, 7.5f, 3f)
                curveTo(9.62f, 3f, 10.68f, 3f, 11.34f, 3.66f)
                curveTo(12f, 4.32f, 12f, 5.38f, 12f, 7.5f)
                curveTo(12f, 9.62f, 12f, 10.68f, 11.34f, 11.34f)
                curveTo(10.68f, 12f, 9.62f, 12f, 7.5f, 12f)
                curveTo(5.38f, 12f, 4.32f, 12f, 3.66f, 11.34f)
                curveTo(3f, 10.68f, 3f, 9.62f, 3f, 7.5f)
                close()
            }
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.5f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(12f, 16.5f)
                curveTo(12f, 14.38f, 12f, 13.32f, 12.66f, 12.66f)
                curveTo(13.32f, 12f, 14.38f, 12f, 16.5f, 12f)
                curveTo(18.62f, 12f, 19.68f, 12f, 20.34f, 12.66f)
                curveTo(21f, 13.32f, 21f, 14.38f, 21f, 16.5f)
                curveTo(21f, 18.62f, 21f, 19.68f, 20.34f, 20.34f)
                curveTo(19.68f, 21f, 18.62f, 21f, 16.5f, 21f)
                curveTo(14.38f, 21f, 13.32f, 21f, 12.66f, 20.34f)
                curveTo(12f, 19.68f, 12f, 18.62f, 12f, 16.5f)
                close()
            }
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.5f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(3f, 16.5f)
                curveTo(3f, 14.38f, 3f, 13.32f, 3.66f, 12.66f)
                curveTo(4.32f, 12f, 5.38f, 12f, 7.5f, 12f)
                curveTo(9.62f, 12f, 10.68f, 12f, 11.34f, 12.66f)
                curveTo(12f, 13.32f, 12f, 14.38f, 12f, 16.5f)
                curveTo(12f, 18.62f, 12f, 19.68f, 11.34f, 20.34f)
                curveTo(10.68f, 21f, 9.62f, 21f, 7.5f, 21f)
                curveTo(5.38f, 21f, 4.32f, 21f, 3.66f, 20.34f)
                curveTo(3f, 19.68f, 3f, 18.62f, 3f, 16.5f)
                close()
            }
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.5f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(12f, 7.5f)
                curveTo(12f, 5.38f, 12f, 4.32f, 12.66f, 3.66f)
                curveTo(13.32f, 3f, 14.38f, 3f, 16.5f, 3f)
                curveTo(18.62f, 3f, 19.68f, 3f, 20.34f, 3.66f)
                curveTo(21f, 4.32f, 21f, 5.38f, 21f, 7.5f)
                curveTo(21f, 9.62f, 21f, 10.68f, 20.34f, 11.34f)
                curveTo(19.68f, 12f, 18.62f, 12f, 16.5f, 12f)
                curveTo(14.38f, 12f, 13.32f, 12f, 12.66f, 11.34f)
                curveTo(12f, 10.68f, 12f, 9.62f, 12f, 7.5f)
                close()
            }
        }.build()
    }

    /** Solar Linear Trash Bin */
    val TrashBin: ImageVector by lazy {
        ImageVector.Builder(
            name = "SolarTrashBinLinear",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.5f,
                strokeLineCap = StrokeCap.Round
            ) {
                moveTo(20.5f, 6f)
                lineTo(3.5f, 6f)
            }
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.5f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(9.5f, 6f)
                curveTo(9.5f, 4.5f, 10.5f, 3f, 12f, 3f)
                curveTo(13.5f, 3f, 14.5f, 4.5f, 14.5f, 6f)
            }
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.5f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(18.85f, 8.5f)
                lineTo(18.2f, 17.8f)
                curveTo(18.09f, 19.32f, 18.03f, 20.08f, 17.53f, 20.54f)
                curveTo(17.03f, 21f, 16.27f, 21f, 14.75f, 21f)
                lineTo(9.25f, 21f)
                curveTo(7.73f, 21f, 6.97f, 21f, 6.47f, 20.54f)
                curveTo(5.97f, 20.08f, 5.91f, 19.32f, 5.8f, 17.8f)
                lineTo(5.15f, 8.5f)
            }
        }.build()
    }

    /** Solar Linear Share */
    val Share: ImageVector by lazy {
        ImageVector.Builder(
            name = "SolarShareLinear",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.5f,
                strokeLineCap = StrokeCap.Round
            ) {
                moveTo(18f, 8f)
                curveTo(19.657f, 8f, 21f, 6.657f, 21f, 5f)
                curveTo(21f, 3.343f, 19.657f, 2f, 18f, 2f)
                curveTo(16.343f, 2f, 15f, 3.343f, 15f, 5f)
                curveTo(15f, 6.657f, 16.343f, 8f, 18f, 8f)
                close()
            }
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.5f,
                strokeLineCap = StrokeCap.Round
            ) {
                moveTo(6f, 15f)
                curveTo(7.657f, 15f, 9f, 13.657f, 9f, 12f)
                curveTo(9f, 10.343f, 7.657f, 9f, 6f, 9f)
                curveTo(4.343f, 9f, 3f, 10.343f, 3f, 12f)
                curveTo(3f, 13.657f, 4.343f, 15f, 6f, 15f)
                close()
            }
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.5f,
                strokeLineCap = StrokeCap.Round
            ) {
                moveTo(18f, 22f)
                curveTo(19.657f, 22f, 21f, 20.657f, 21f, 19f)
                curveTo(21f, 17.343f, 19.657f, 16f, 18f, 16f)
                curveTo(16.343f, 16f, 15f, 17.343f, 15f, 19f)
                curveTo(15f, 20.657f, 16.343f, 22f, 18f, 22f)
                close()
            }
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.5f,
                strokeLineCap = StrokeCap.Round
            ) {
                moveTo(8.59f, 13.51f)
                lineTo(15.42f, 17.49f)
            }
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.5f,
                strokeLineCap = StrokeCap.Round
            ) {
                moveTo(15.41f, 6.51f)
                lineTo(8.59f, 10.49f)
            }
        }.build()
    }

    /** Solar Linear Lock / Security */
    val Lock: ImageVector by lazy {
        ImageVector.Builder(
            name = "SolarLockLinear",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.5f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(2f, 16f)
                curveTo(2f, 12.229f, 2f, 10.343f, 3.172f, 9.172f)
                curveTo(4.343f, 8f, 6.229f, 8f, 10f, 8f)
                lineTo(14f, 8f)
                curveTo(17.771f, 8f, 19.657f, 8f, 20.828f, 9.172f)
                curveTo(22f, 10.343f, 22f, 12.229f, 22f, 16f)
                curveTo(22f, 19.771f, 22f, 21.657f, 20.828f, 22.828f)
                curveTo(19.657f, 24f, 17.771f, 24f, 14f, 24f)
                lineTo(10f, 24f)
                curveTo(6.229f, 24f, 4.343f, 24f, 3.172f, 22.828f)
                curveTo(2f, 21.657f, 2f, 19.771f, 2f, 16f)
                close()
            }
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.5f,
                strokeLineCap = StrokeCap.Round
            ) {
                moveTo(6f, 8f)
                verticalLineTo(7f)
                curveTo(6f, 3.686f, 8.686f, 1f, 12f, 1f)
                curveTo(15.314f, 1f, 18f, 3.686f, 18f, 7f)
                verticalLineTo(8f)
            }
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.5f,
                strokeLineCap = StrokeCap.Round
            ) {
                moveTo(12f, 15f)
                verticalLineTo(17f)
            }
        }.build()
    }

    /** Solar Linear Shield / Access */
    val ShieldCheck: ImageVector by lazy {
        ImageVector.Builder(
            name = "SolarShieldCheckLinear",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.5f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(12f, 2f)
                lineTo(4f, 5f)
                verticalLineTo(11f)
                curveTo(4f, 16.5f, 7.5f, 21.5f, 12f, 23f)
                curveTo(16.5f, 21.5f, 20f, 16.5f, 20f, 11f)
                verticalLineTo(5f)
                lineTo(12f, 2f)
                close()
            }
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.5f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(9f, 12f)
                lineTo(11f, 14f)
                lineTo(15f, 10f)
            }
        }.build()
    }

    /** Solar Linear Chat / Feedback / Bug */
    val ChatRoundDots: ImageVector by lazy {
        ImageVector.Builder(
            name = "SolarChatRoundDotsLinear",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.5f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(12f, 22f)
                curveTo(17.523f, 22f, 22f, 17.523f, 22f, 12f)
                curveTo(22f, 6.477f, 17.523f, 2f, 12f, 2f)
                curveTo(6.477f, 2f, 2f, 6.477f, 2f, 12f)
                curveTo(2f, 14.153f, 2.68f, 16.147f, 3.841f, 17.781f)
                lineTo(2.553f, 21.447f)
                lineTo(6.219f, 20.159f)
                curveTo(7.853f, 21.32f, 9.847f, 22f, 12f, 22f)
                close()
            }
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.5f,
                strokeLineCap = StrokeCap.Round
            ) {
                moveTo(8f, 12f)
                horizontalLineTo(8.01f)
                moveTo(12f, 12f)
                horizontalLineTo(12.01f)
                moveTo(16f, 12f)
                horizontalLineTo(16.01f)
            }
        }.build()
    }

    /** Solar Linear Sun / Light */
    val Sun: ImageVector by lazy {
        ImageVector.Builder(
            name = "SolarSunLinear",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.5f,
                strokeLineCap = StrokeCap.Round
            ) {
                moveTo(12f, 18f)
                curveTo(15.314f, 18f, 18f, 15.314f, 18f, 12f)
                curveTo(18f, 8.686f, 15.314f, 6f, 12f, 6f)
                curveTo(8.686f, 6f, 6f, 8.686f, 6f, 12f)
                curveTo(6f, 15.314f, 8.686f, 18f, 12f, 18f)
                close()
            }
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.5f,
                strokeLineCap = StrokeCap.Round
            ) {
                moveTo(12f, 2f)
                lineTo(12f, 4f)
                moveTo(12f, 20f)
                lineTo(12f, 22f)
                moveTo(2f, 12f)
                lineTo(4f, 12f)
                moveTo(20f, 12f)
                lineTo(22f, 12f)
                moveTo(4.93f, 4.93f)
                lineTo(6.34f, 6.34f)
                moveTo(17.66f, 17.66f)
                lineTo(19.07f, 19.07f)
                moveTo(4.93f, 19.07f)
                lineTo(6.34f, 17.66f)
                moveTo(17.66f, 6.34f)
                lineTo(19.07f, 4.93f)
            }
        }.build()
    }

    /** Solar Linear Moon / Dark */
    val Moon: ImageVector by lazy {
        ImageVector.Builder(
            name = "SolarMoonLinear",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.5f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(21.53f, 15.93f)
                curveTo(20.35f, 16.61f, 18.98f, 17f, 17.5f, 17f)
                curveTo(12.81f, 17f, 9f, 13.19f, 9f, 8.5f)
                curveTo(9f, 7.02f, 9.39f, 5.65f, 10.07f, 4.47f)
                curveTo(5.53f, 5.37f, 2.1f, 9.42f, 2.1f, 14.3f)
                curveTo(2.1f, 19.65f, 6.45f, 24f, 11.8f, 24f)
                curveTo(16.68f, 24f, 20.73f, 20.57f, 21.53f, 15.93f)
                close()
            }
        }.build()
    }
}
