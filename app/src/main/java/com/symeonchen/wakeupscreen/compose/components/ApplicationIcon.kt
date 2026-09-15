package com.symeonchen.wakeupscreen.compose.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import com.symeonchen.wakeupscreen.R
import com.symeonchen.wakeupscreen.utils.loadAppIconBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun ApplicationIcon(size: Dp) {
    val context = LocalContext.current
    val sizePx = with(LocalDensity.current) { size.roundToPx().coerceAtLeast(1) }
    val configuration = LocalConfiguration.current
    val bitmap by produceState<ImageBitmap?>(null, context, sizePx, configuration) {
        value = withContext(Dispatchers.IO) {
            loadAppIconBitmap(context, context.packageName, sizePx).asImageBitmap()
        }
    }
    val loaded = bitmap
    if (loaded == null) {
        Box(Modifier.size(size))
    } else {
        Image(
            bitmap = loaded,
            contentDescription = stringResource(R.string.app_name),
            modifier = Modifier.size(size),
        )
    }
}
