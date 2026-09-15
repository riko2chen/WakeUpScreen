package com.symeonchen.wakeupscreen.utils

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.runner.AndroidJUnit4
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppIconLoaderInstrumentedTest {
    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun adaptiveLauncherIconRendersAtRequestedSize() {
        // On API 26+ this resolves to adaptive-icon XML, which BitmapFactory cannot decode.
        val bitmap = loadAppIconBitmap(context, context.packageName, 96)
        assertEquals(96, bitmap.width)
        assertEquals(96, bitmap.height)
        val pixels = IntArray(96 * 96)
        bitmap.getPixels(pixels, 0, 96, 0, 0, 96, 96)
        assertTrue(pixels.any { it ushr 24 != 0 })
    }

    @Test
    fun uninstalledPackageUsesFallbackIcon() {
        val bitmap = loadAppIconBitmap(context, "missing.package.wakeupscreen.test", 42)
        assertEquals(42, bitmap.width)
        assertEquals(42, bitmap.height)
    }

    @Test
    fun cacheEvictsByBytesWithoutRecyclingDisplayedIcons() = runBlocking {
        AppIconLoader(context, cacheBytes = 42 * 42 * 4).use { loader ->
            val first = loader.load(context.packageName, 42)
            assertSame(first, loader.load(context.packageName, 42))
            loader.load("missing.package.wakeupscreen.test", 42)
            assertNotSame(first, loader.load(context.packageName, 42))
            assertFalse(first.isRecycled)
        }
    }

    @Test
    fun differentTargetSizesDoNotReuseWrongResolution() = runBlocking {
        AppIconLoader(context).use { loader ->
            val small = loader.load(context.packageName, 42)
            val large = loader.load(context.packageName, 96)
            assertNotSame(small, large)
            assertEquals(96, large.width)
        }
    }

    @Test
    fun closingTheScreenCancelsLateRequestsInsteadOfThrowingAnAppError() = runBlocking {
        val loader = AppIconLoader(context)
        loader.close()
        try {
            loader.load(context.packageName, 42)
            fail("A closed loader must cancel the request")
        } catch (_: CancellationException) {
            // Cancellation is safe for lifecycleScope.launch, including a request
            // entering the cache just as the screen is being destroyed.
        }
    }
}
