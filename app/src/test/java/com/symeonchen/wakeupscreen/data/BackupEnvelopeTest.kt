package com.symeonchen.wakeupscreen.data

import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BackupEnvelopeTest {

    @Test
    fun `wrap and unwrap round trip`() {
        val settings = JSONObject().put("custom_status", true).put("app_white_list_switch", 1)

        val raw = BackupEnvelope.wrap(settings, exportedAtMs = 123L).toString()
        val result = BackupEnvelope.unwrap(raw)

        assertTrue(result is BackupEnvelope.UnwrapResult.Success)
        val unwrapped = (result as BackupEnvelope.UnwrapResult.Success).settings
        assertEquals(true, unwrapped.getBoolean("custom_status"))
        assertEquals(1, unwrapped.getInt("app_white_list_switch"))
    }

    @Test
    fun `not json is corrupt`() {
        assertTrue(BackupEnvelope.unwrap("hello") is BackupEnvelope.UnwrapResult.Corrupt)
        assertTrue(BackupEnvelope.unwrap("") is BackupEnvelope.UnwrapResult.Corrupt)
    }

    @Test
    fun `another app's json is corrupt`() {
        val alien = JSONObject()
            .put("app", "SomeOtherApp")
            .put("format_version", 1)
            .put("settings", JSONObject())
        assertTrue(BackupEnvelope.unwrap(alien.toString()) is BackupEnvelope.UnwrapResult.Corrupt)
    }

    @Test
    fun `a missing settings object is corrupt`() {
        val truncated = JSONObject()
            .put("app", "WakeUpScreen")
            .put("format_version", 1)
        assertTrue(BackupEnvelope.unwrap(truncated.toString()) is BackupEnvelope.UnwrapResult.Corrupt)
    }

    @Test
    fun `a newer format is refused with its version`() {
        val future = JSONObject()
            .put("app", "WakeUpScreen")
            .put("format_version", BackupEnvelope.FORMAT_VERSION + 5)
            .put("settings", JSONObject())

        val result = BackupEnvelope.unwrap(future.toString())

        assertTrue(result is BackupEnvelope.UnwrapResult.NewerVersion)
        assertEquals(
            BackupEnvelope.FORMAT_VERSION + 5,
            (result as BackupEnvelope.UnwrapResult.NewerVersion).version,
        )
    }

    @Test
    fun `a missing or absurd version is corrupt rather than guessed`() {
        val versionless = JSONObject()
            .put("app", "WakeUpScreen")
            .put("settings", JSONObject())
        assertTrue(BackupEnvelope.unwrap(versionless.toString()) is BackupEnvelope.UnwrapResult.Corrupt)

        val zero = JSONObject()
            .put("app", "WakeUpScreen")
            .put("format_version", 0)
            .put("settings", JSONObject())
        assertTrue(BackupEnvelope.unwrap(zero.toString()) is BackupEnvelope.UnwrapResult.Corrupt)
    }

    @Test
    fun `unknown keys inside settings survive unwrap for forward tolerance`() {
        val settings = JSONObject().put("a_setting_from_the_future", 42)
        val raw = BackupEnvelope.wrap(settings, exportedAtMs = 0L).toString()

        val result = BackupEnvelope.unwrap(raw) as BackupEnvelope.UnwrapResult.Success

        assertEquals(42, result.settings.getInt("a_setting_from_the_future"))
    }
}
