package com.molarmak.eventpick.model.main.createNotification

import com.google.gson.Gson
import com.molarmak.eventpick.app.Cache
import com.molarmak.eventpick.app.ERROR_UNHANDLED
import com.molarmak.eventpick.app.PresenterType

interface CreateNotificationPresenterInterface: PresenterType {
    fun startCreateNotification(ltg: Double, long: Double, radius: Int)
    fun endCreateNotification()
}

class CreateNotificationPresenterImpl(private val view: CreateNotificationView): CreateNotificationPresenterInterface {

    private val model = CreateNotificationModel(this)

    override fun startCreateNotification(ltd: Double, long: Double, radius: Int) {
        try {
            val form = CreateNotificationForm(
                    Cache.instance.token!!,
                    ltd,
                    long,
                    radius,
                    Cache.instance.fbToken!!
            )
            val json = Gson().toJson(form)
            model.createNotification(json)
        } catch (e: Exception) {
            e.printStackTrace()
            onError(ERROR_UNHANDLED)
        }
    }

    override fun endCreateNotification() {
        view.onNotificationCreated()
    }

    override fun onError(errors: ArrayList<String>) {
        view.onError(errors)
    }

}