package com.molarmak.eventpick.app

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.support.v4.app.NotificationCompat
import android.util.Log

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.molarmak.eventpick.R
import com.molarmak.eventpick.model.auth.AuthActivity

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage?) {
        super.onMessageReceived(remoteMessage)
        sendNotification(remoteMessage)
    }

    private fun sendNotification(remoteMessage: RemoteMessage?) {
        try {
            val intent = Intent(this, AuthActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            val pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                    PendingIntent.FLAG_ONE_SHOT)
            val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

            val notificationBuilder: NotificationCompat.Builder
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP){
                notificationBuilder = NotificationCompat.Builder(this)
                        .setContentText(remoteMessage!!.notification!!.body)
                        .setAutoCancel(true)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setSound(defaultSoundUri)
                        .setContentIntent(pendingIntent)
            } else{
                notificationBuilder = NotificationCompat.Builder(this, "")
                        .setContentText(remoteMessage!!.notification!!.body)
                        .setAutoCancel(true)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setSound(defaultSoundUri)
                        .setContentIntent(pendingIntent)
            }
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(0 /* ID of notification */, notificationBuilder.build())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
