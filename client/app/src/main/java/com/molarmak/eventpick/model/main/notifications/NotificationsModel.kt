package com.molarmak.eventpick.model.main.notifications

import com.google.gson.Gson
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import com.molarmak.eventpick.app.*
import com.molarmak.eventpick.model.main.events.MapAllPresenterInterface
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

data class Notification(
        @SerializedName("id") @Expose val id: Int,
        @SerializedName("latitude") @Expose val latitude: Double,
        @SerializedName("longitude") @Expose val longitude: Double,
        @SerializedName("radius") @Expose val radius: Int
)

data class DeleteNotificationForm(
        @SerializedName("eventTriggerId") @Expose val eventTriggerId: Int,
        @SerializedName("token") @Expose val token: String
)

class GetAllNotifications(private val view: NotificationsPresenterInterface) : Callback {

    private val getNotificationsWith = {other:String -> getRequest(base_url + getNotifications, other, this) }
    private val TAG = "GetNotModel"

    override fun onFailure(call: Call?, e: IOException?) {
        apiFailure(TAG, e, view)
    }

    override fun onResponse(call: Call?, response: Response?) {
        val action = { res: JSONObject ->
            if(res.has("eventTriggers")) {
                val type = object : TypeToken<ArrayList<Notification>>() {}.type
                val events: ArrayList<Notification> = Gson().fromJson(res.getString("eventTriggers"), type)
                view.endLoadNotifications(events)
            } else view.onError(ERROR_BAD_RESPONSE)
        }
        apiResponse(TAG, response, view, action)
    }

    fun getNotifications(other: String) {
        getNotificationsWith(other)
    }

}

class DeleteNotificationModel(private val view: NotificationsPresenterInterface) : Callback {

    private val deleteNotificationRequest = {json:String -> deleteRequest(base_url + deleteNotification, json, this)}
    private val TAG = "DelNotModel"

    override fun onFailure(call: Call?, e: IOException?) {
        apiFailure(TAG, e, view)
    }

    override fun onResponse(call: Call?, response: Response?) {
        val action = { res: JSONObject ->
            if(res.has("eventTriggerId")) {
                view.endDeleteNotifications(res.getInt("eventTriggerId"))
            } else view.onError(ERROR_BAD_RESPONSE)
        }
        apiResponse(TAG, response, view, action)
    }

    fun deleteNotification(json: String) {
        deleteNotificationRequest(json)
    }

}