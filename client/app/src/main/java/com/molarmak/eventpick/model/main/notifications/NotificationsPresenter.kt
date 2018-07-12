package com.molarmak.eventpick.model.main.notifications

import com.google.gson.Gson
import com.molarmak.eventpick.app.Cache
import com.molarmak.eventpick.app.ERROR_UNHANDLED
import com.molarmak.eventpick.app.PresenterType
import com.molarmak.eventpick.app.buildParams

interface NotificationsPresenterInterface: PresenterType {
    fun startLoadNotifications()
    fun endLoadNotifications(list: ArrayList<Notification>)
    fun startDeleteNotification(id: Int)
    fun endDeleteNotifications(id: Int)
}

class NotificationsPresenterImpl(val view: NotificationsView): NotificationsPresenterInterface {

    private val model = GetAllNotifications(this)
    private val deleteNotificationModel = DeleteNotificationModel(this)

    override fun startLoadNotifications() {
        try {
            val otherUrl = StringBuilder()
            buildParams(otherUrl, "token", Cache.instance.token!!)
            model.getNotifications(otherUrl.toString())
        } catch (e: Exception) {
            onError(ERROR_UNHANDLED)
            e.printStackTrace()
        }
    }

    override fun endLoadNotifications(list: ArrayList<Notification>) {
        view.endLoadNotifications(list)
    }

    override fun startDeleteNotification(id: Int) {
        try {
            val form = DeleteNotificationForm(id, Cache.instance.token!!)
            val json = Gson().toJson(form)
            deleteNotificationModel.deleteNotification(json)
        } catch (e: Exception) {
            e.printStackTrace()
            onError(ERROR_UNHANDLED)
        }
    }

    override fun endDeleteNotifications(id: Int) {
        view.endDeleteNotifications(id)
    }

    override fun onError(errors: ArrayList<String>) {
        view.onError(errors)
    }

}