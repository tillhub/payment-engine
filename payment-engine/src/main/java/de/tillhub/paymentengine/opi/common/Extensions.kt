package de.tillhub.paymentengine.opi.common

import android.app.Service
import android.content.pm.ServiceInfo
import android.os.Build
import androidx.core.app.ServiceCompat
import de.tillhub.paymentengine.data.ISOAlphaCurrency
import java.math.BigDecimal
import java.util.Currency

fun BigDecimal.modifyAmountForOpi(currency: ISOAlphaCurrency): BigDecimal =
    scaleByPowerOfTen(
        Currency.getInstance(currency.value).defaultFractionDigits.unaryMinus()
    )

fun Service.startAsForegroundService() {
    // create the notification channel
    NotificationsHelper.createNotificationChannel(this)

    // promote service to foreground service
    ServiceCompat.startForeground(
        this,
        1,
        NotificationsHelper.buildNotification(this),
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            ServiceInfo.FOREGROUND_SERVICE_TYPE_SHORT_SERVICE
        } else {
            0
        }
    )
}