package com.molarmak.eventpick.model.main.createNotification

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.molarmak.eventpick.app.*
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

class CreateNotificationForm(
                @SerializedName("token") @Expose val token: String,
                @SerializedName("latitude") @Expose val latitude: Double,
                @SerializedName("longitude") @Expose val longitude: Double,
                @SerializedName("radius") @Expose val radius: Int,
                @SerializedName("pushId") @Expose val pushId: String)

class CreateNotificationModel(private val view: CreateNotificationPresenterInterface) : Callback {

    private val createNotificationRequest = {json:String -> postRequest(json, base_url + createNotification, this) }
    private val TAG = "CreateNotModel"

    override fun onFailure(call: Call?, e: IOException?) {
        apiFailure(TAG, e, view)
    }

    override fun onResponse(call: Call?, response: Response?) {
        val action = { _: JSONObject ->
            view.endCreateNotification()
        }
        apiResponse(TAG, response, view, action)
    }

    fun createNotification(json: String) {
        createNotificationRequest(json)
    }

}