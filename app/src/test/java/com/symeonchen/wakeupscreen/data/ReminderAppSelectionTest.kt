package com.symeonchen.wakeupscreen.data

import org.junit.Assert.*
import org.junit.Test

class ReminderAppSelectionTest {
    @Test fun `default inherits initial filter without requiring selection`() {
        val selection = ReminderAppSelection()
        assertTrue(selection.allows("a", true))
        assertFalse(selection.allows("a", false))
    }

    @Test fun `custom selection narrows initial A B C to A`() {
        val selection = ReminderAppSelection(true, setOf("a"))
        assertTrue(selection.allows("a", true))
        assertFalse(selection.allows("b", true))
        assertFalse(selection.allows("c", true))
    }

    @Test fun `selected app never bypasses changed initial filter`() {
        val selection = ReminderAppSelection(true, setOf("a"))
        assertFalse(selection.allows("a", false))
        assertTrue(selection.allows("a", true))
    }

    @Test fun `empty custom list intentionally allows nothing`() {
        assertFalse(ReminderAppSelection(true).allows("a", true))
    }

    @Test fun `switching to inherited mode retains dormant choices`() {
        val selected = ReminderAppSelection(true, setOf("a"))
        val inherited = selected.copy(custom = false)
        assertTrue(inherited.allows("b", true))
        assertFalse(inherited.copy(custom = true).allows("b", true))
    }

    @Test fun `package codec preserves unavailable packages and empty selection`() {
        val packages = setOf("com.example.uninstalled", "com.example.app")
        assertEquals(packages, ReminderAppSelection.decode(ReminderAppSelection.encode(packages)))
        assertEquals(emptySet<String>(), ReminderAppSelection.decode(" , , "))
        assertEquals(setOf("a"), ReminderAppSelection.decode("a,a"))
    }
}
