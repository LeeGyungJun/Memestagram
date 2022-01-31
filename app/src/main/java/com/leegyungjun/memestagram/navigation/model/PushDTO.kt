package com.leegyungjun.memestagram.navigation.model

import android.app.Notification

data class PushDTO (
    var to : String? = null,
    var notifcation : Notification = Notification()
){
    data class Notification(
        var body : String? = null,
        var title : String? = null
    )
}