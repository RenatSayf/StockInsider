package com.renatsayf.stockinsider.firebase.push

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MessagingService: FirebaseMessagingService() {

    override fun onNewToken(token: String) {

    }

    override fun onMessageReceived(message: RemoteMessage) {
        val data = message.data
        data
    }
}