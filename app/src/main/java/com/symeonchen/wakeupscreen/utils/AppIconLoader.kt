package com.symeonchen.wakeupscreen.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.util.LruCache
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import java.io.Closeable

/** Decode Drawable resources (including adaptive icons) into display-sized pixels. */
internal fun loadAppIconBitmap(context: Context, packageName: String, sizePx: Int): Bitmap {
    require(sizePx > 0)
    val pm = context.packageManager
    val drawable = try {
        pm.getApplicationIcon(packageName)
    } catch (_: Exception) {
        // An app may be removed between listing its package and loading its icon.
        pm.defaultActivityIcon
    }
    return Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888).also { bitmap ->
        val canvas = Canvas(bitmap)
        try {
            drawable.mutate().apply {
                setBounds(0, 0, sizePx, sizePx)
                draw(canvas)
            }
        } catch (_: RuntimeException) {
            // Broken third-party Drawable resources must not crash the list.
            bitmap.eraseColor(android.graphics.Color.TRANSPARENT)
            pm.defaultActivityIcon.apply {
                setBounds(0, 0, sizePx, sizePx)
                draw(canvas)
            }
        }
    }
}

/** One screen owns this cache; the application list itself never holds Drawables. */
internal class AppIconLoader(context: Context, cacheBytes: Int = 2 * 1024 * 1024) : Closeable {
    private val appContext = context.applicationContext
    private val permits = Semaphore(2)
    private val lock = Any()
    private var closed = false
    private val cache = object : LruCache<String, Bitmap>(cacheBytes) {
        override fun sizeOf(key: String, value: Bitmap) = value.allocationByteCount
    }

    suspend fun load(packageName: String, sizePx: Int): Bitmap = withContext(Dispatchers.IO) {
        val key = "$packageName:$sizePx"
        permits.withPermit {
            synchronized(lock) {
                if (closed) throw CancellationException("Icon loader is closed")
                cache.get(key)
            }?.let { return@withPermit it }

            val bitmap = loadAppIconBitmap(appContext, packageName, sizePx)
            currentCoroutineContext().ensureActive()
            synchronized(lock) {
                if (!closed) cache.put(key, bitmap)
            }
            bitmap
        }
    }

    override fun close() {
        synchronized(lock) {
            closed = true
            // Views may still display evicted bitmaps; never recycle them manually.
            cache.evictAll()
        }
    }
}
