package com.symeonchen.wakeupscreen

import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.runner.AndroidJUnit4
import com.symeonchen.wakeupscreen.model.FilterListViewModel
import com.symeonchen.wakeupscreen.pages.AppInfoPageActivity
import com.symeonchen.wakeupscreen.pages.FilterListActivity
import com.symeonchen.wakeupscreen.pages.NightGlowActivity
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.math.roundToInt

@RunWith(AndroidJUnit4::class)
class EdgeToEdgeInstrumentedTest {
    @Test
    fun nightGlowHidesSystemBars() {
        ActivityScenario.launch(NightGlowActivity::class.java).use { scenario ->
            var hidden = false
            val deadline = android.os.SystemClock.uptimeMillis() + 2_000
            while (!hidden && android.os.SystemClock.uptimeMillis() < deadline) {
                InstrumentationRegistry.getInstrumentation().waitForIdleSync()
                scenario.onActivity { activity ->
                    hidden = ViewCompat.getRootWindowInsets(activity.window.decorView)
                        ?.isVisible(WindowInsetsCompat.Type.systemBars()) == false
                }
                if (!hidden) android.os.SystemClock.sleep(50)
            }
            assertTrue("Night glow should not expose system bars", hidden)
        }
    }

    @Test
    fun appInfoPageOpensWithAdaptiveIcon() {
        ActivityScenario.launch(AppInfoPageActivity::class.java).use { scenario ->
            Espresso.onIdle()
            scenario.onActivity { assertFalse(it.isFinishing) }
        }
    }

    @Test
    fun filterListAccountsForSideCutoutAndKeyboardWithoutAccumulatingInsets() {
        ActivityScenario.launch(FilterListActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                val root = activity.findViewById<ViewGroup>(android.R.id.content).getChildAt(0)
                val header = activity.findViewById<View>(R.id.ll_header)
                val list = activity.findViewById<View>(R.id.rv_app_list)
                val density = activity.resources.displayMetrics.density
                val insets = WindowInsetsCompat.Builder()
                    .setInsets(WindowInsetsCompat.Type.systemBars(), Insets.of(19, 31, 23, 47))
                    .setInsets(WindowInsetsCompat.Type.displayCutout(), Insets.of(61, 0, 0, 0))
                    .setInsets(WindowInsetsCompat.Type.ime(), Insets.of(0, 0, 0, 211))
                    .build()
                repeat(2) {
                    ViewCompat.dispatchApplyWindowInsets(root, insets)
                    assertEquals(61, root.paddingLeft)
                    assertEquals(23, root.paddingRight)
                    assertEquals((56 * density).roundToInt() + 31, header.layoutParams.height)
                    assertEquals(31, header.paddingTop)
                    assertEquals((16 * density).roundToInt() + 211, list.paddingBottom)
                }
            }
        }
    }

    @Test
    fun emptySearchClearsRowsAndSelectionSurvivesFiltering() {
        ActivityScenario.launch(FilterListActivity::class.java).use { scenario ->
            var loaded = false
            val deadline = android.os.SystemClock.uptimeMillis() + 10_000
            while (!loaded && android.os.SystemClock.uptimeMillis() < deadline) {
                InstrumentationRegistry.getInstrumentation().waitForIdleSync()
                scenario.onActivity { activity ->
                    loaded = !ViewModelProvider(activity)[FilterListViewModel::class.java]
                        .visibleList.value.isNullOrEmpty()
                }
                if (!loaded) android.os.SystemClock.sleep(50)
            }
            assertTrue("Application metadata should load", loaded)
            scenario.onActivity { activity ->
                val model = ViewModelProvider(activity)[FilterListViewModel::class.java]
                val item = model.visibleList.value!!.first()
                item.selected = true
                model.searchKey = "no-match-430-instrumented-test"
                assertEquals(0, activity.findViewById<RecyclerView>(R.id.rv_app_list).adapter!!.itemCount)
                model.searchKey = ""
                assertTrue(model.visibleList.value!!.first { it.packageName == item.packageName }.selected)
                item.selected = false
                model.searchKey = "no-match-430-instrumented-test"
                model.searchKey = ""
                assertFalse(model.visibleList.value!!.first { it.packageName == item.packageName }.selected)
            }
        }
    }
}
