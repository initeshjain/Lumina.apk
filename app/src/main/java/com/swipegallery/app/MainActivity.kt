package com.swipegallery.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import coil.Coil
import coil.ImageLoader
import coil.decode.VideoFrameDecoder

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        // Lets GalleryImage render a frame of a video as its thumbnail.
        Coil.setImageLoader(ImageLoader.Builder(applicationContext).components { add(VideoFrameDecoder.Factory()) }.build())
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent { GalleryApp() }
    }
}
