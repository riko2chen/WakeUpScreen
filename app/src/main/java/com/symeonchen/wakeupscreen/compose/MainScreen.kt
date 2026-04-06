package com.symeonchen.wakeupscreen.compose

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.symeonchen.wakeupscreen.compose.components.HeroSection
import com.symeonchen.wakeupscreen.compose.components.StatusCard
import com.symeonchen.wakeupscreen.compose.theme.Amber
import com.symeonchen.wakeupscreen.compose.theme.AmberSoft

data class StatusDisplayState(
    val statusText: String,
    val isError: Boolean,
    val isToggleVisible: Boolean,
    val isNoticeVisible: Boolean,
)

@Composable
fun MainScreen(
    statusDisplay: StatusDisplayState,
    tipsText: String,
    isToggleChecked: Boolean,
    onToggleClick: () -> Unit,
    // Status items
    serviceName: String,
    serviceOk: Boolean,
    serviceBtn: String,
    onServiceItemClick: () -> Unit,
    onServiceBtnClick: () -> Unit,
    permissionName: String,
    permissionOk: Boolean,
    permissionBtn: String,
    onPermissionItemClick: () -> Unit,
    onPermissionBtnClick: () -> Unit,
    batteryName: String,
    batteryOk: Boolean,
    batteryBtn: String,
    onBatteryItemClick: () -> Unit,
    onBatteryBtnClick: () -> Unit,
    notifPermName: String,
    notifPermOk: Boolean,
    notifPermBtn: String,
    onNotifPermItemClick: () -> Unit,
    onNotifPermBtnClick: () -> Unit,
    // Notice
    noticeText: String,
    onNoticeClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Hero
        HeroSection(
            isError = statusDisplay.isError,
            statusText = statusDisplay.statusText,
            tipsText = tipsText,
            isToggleChecked = isToggleChecked,
            isToggleVisible = statusDisplay.isToggleVisible,
            onToggleClick = onToggleClick,
        )

        // Status cards
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            StatusCard(
                name = serviceName,
                isOk = serviceOk,
                buttonText = serviceBtn,
                onItemClick = onServiceItemClick,
                onButtonClick = onServiceBtnClick,
            )
            StatusCard(
                name = permissionName,
                isOk = permissionOk,
                buttonText = permissionBtn,
                onItemClick = onPermissionItemClick,
                onButtonClick = onPermissionBtnClick,
            )
            StatusCard(
                name = batteryName,
                isOk = batteryOk,
                buttonText = batteryBtn,
                onItemClick = onBatteryItemClick,
                onButtonClick = onBatteryBtnClick,
            )
            StatusCard(
                name = notifPermName,
                isOk = notifPermOk,
                buttonText = notifPermBtn,
                onItemClick = onNotifPermItemClick,
                onButtonClick = onNotifPermBtnClick,
            )
        }

        // Notice card
        if (statusDisplay.isNoticeVisible) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 24.dp),
                shape = RoundedCornerShape(12.dp),
                color = AmberSoft,
                tonalElevation = 0.dp,
                onClick = onNoticeClick,
            ) {
                Text(
                    text = noticeText,
                    color = Amber,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(14.dp),
                )
            }
        }
    }
}
