package com.symeonchen.wakeupscreen.compose.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.symeonchen.wakeupscreen.compose.theme.WakeUpTheme
import com.symeonchen.wakeupscreen.data.SleepSegment
import kotlin.math.cos
import kotlin.math.sin

/** Every hour of the day, so any arc can be read off directly. */
private val HOUR_TICKS = (0..23).toList()

/**
 * The day as a ring: midnight at the top, clockwise through 24 hours. Sleep
 * windows are drawn in a muted colour over the lit track, so the shape of the
 * day is readable at a glance and a window running over midnight simply crosses
 * the top of the circle instead of splitting into two rows.
 *
 * A window is drawn from its own arc; the ring never merges neighbours, because
 * two windows that meet at one minute are still two separate settings and the
 * hairline between them is the honest picture.
 */
@Composable
fun SleepDayRing(
    segments: List<SleepSegment>,
    modifier: Modifier = Modifier,
    diameter: Dp = 220.dp,
    thickness: Dp = 22.dp,
    content: @Composable () -> Unit = {},
) {
    val awakeColor = SleepRingColors.awake
    val sleepColor = SleepRingColors.asleep
    val tickColor = MaterialTheme.colorScheme.onSurfaceVariant

    val textMeasurer = rememberTextMeasurer()
    // Small enough that 24 labels sit side by side without touching.
    val tickStyle = MaterialTheme.typography.labelSmall.copy(
        color = tickColor,
        fontSize = 9.sp,
    )

    Box(modifier = modifier.size(diameter), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(diameter)) {
            val strokeWidth = thickness.toPx()
            val inset = strokeWidth / 2f
            val arcSize = Size(size.width - strokeWidth, size.height - strokeWidth)
            val topLeft = Offset(inset, inset)

            // -90 puts minute zero at the top; a full day is 360 degrees.
            fun minuteToDegrees(minute: Int) = minute * 360f / SleepSegment.MINUTES_PER_DAY

            drawArc(
                color = awakeColor,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth),
            )

            segments.forEach { segment ->
                drawArc(
                    color = sleepColor,
                    startAngle = minuteToDegrees(segment.startMinute) - 90f,
                    sweepAngle = minuteToDegrees(segment.durationMinutes),
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth),
                )
            }

            // Hour labels inside the ring, so an arc can be read as a time
            // rather than just a proportion of the day.
            val labelRadius = size.minDimension / 2f - strokeWidth - 11.dp.toPx()
            HOUR_TICKS.forEach { hour ->
                val radians = Math.toRadians(hour * 15.0 - 90.0)
                val measured = textMeasurer.measure(hour.toString(), tickStyle)
                drawText(
                    textLayoutResult = measured,
                    topLeft = Offset(
                        center.x + (cos(radians) * labelRadius).toFloat() - measured.size.width / 2f,
                        center.y + (sin(radians) * labelRadius).toFloat() - measured.size.height / 2f,
                    ),
                )
            }
        }

        content()
    }
}

/**
 * Colours the ring uses, so a legend can match it without guessing.
 *
 * Green for the hours that wake the screen, grey for the ones that do not.
 */
object SleepRingColors {
    // Bound to the success role, not to tertiary: tertiary is a brand slot and
    // is currently pink, whereas "the screen wakes" has to read as green.
    val awake: Color
        @Composable get() = WakeUpTheme.colors.success.copy(alpha = 0.55f)
    // Opaque on purpose: sleep arcs are painted over the full green track, and
    // a translucent grey would blend into it as a muddy green instead of
    // replacing it.
    val asleep: Color
        @Composable get() = MaterialTheme.colorScheme.onSurfaceVariant
}
