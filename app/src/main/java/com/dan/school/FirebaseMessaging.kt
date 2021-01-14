package com.dan.school

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.net.Uri
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlin.random.Random

const val TAG = "FirebaseMessaging"

const val ACTION_CLICK = "action_click"

const val OPEN_LINK = "open_link"

const val LINK = "link"
const val TEXT = "text"
const val TITLE = "title"

@SuppressLint("MissingFirebaseInstanceTokenRefresh")
class FirebaseMessaging : FirebaseMessagingService() {
    override fun onMessageReceived(p0: RemoteMessage) {
        super.onMessageReceived(p0)

        val data = p0.data

        val intent: Intent?
        val pendingIntent: PendingIntent?

        if (data[ACTION_CLICK] != null && data[ACTION_CLICK] == OPEN_LINK) {
            intent = Intent(Intent.ACTION_VIEW)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.data = Uri.parse(data[LINK])

            pendingIntent =
                PendingIntent.getActivity(this, 0, intent, 0)
        } else {
            intent = Intent(this, SplashScreenActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT)
        }

        val channelId = getString(R.string.updates_notification_channel_id)
        val builder = NotificationCompat.Builder(this, channelId)
            .setStyle(NotificationCompat.BigTextStyle().bigText(data[TEXT]))
            .setSmallIcon(R.drawable.ic_notification_logo)
            .setColor(ContextCompat.getColor(this, R.color.colorPrimary))
            .setContentTitle(data[TITLE])
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel =
                NotificationChannel(channelId, School.CHANNEL_UPDATES_NAME, NotificationManager.IMPORTANCE_DEFAULT)
            manager.createNotificationChannel(channel)
        }
        manager.notify(Random.nextInt(), builder.build())
    }
}