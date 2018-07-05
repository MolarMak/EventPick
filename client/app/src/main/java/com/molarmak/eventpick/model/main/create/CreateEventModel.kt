package com.molarmak.eventpick.model.main.create

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.molarmak.eventpick.app.*
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

class CreateEventForm(
                      @SerializedName("name") @Expose val name: String,
                      @SerializedName("categoryId") @Expose val categoryId: Int,
                      @SerializedName("startTime") @Expose val startTime: String,
                      @SerializedName("endTime") @Expose val endTime: String,
                      @SerializedName("latitude") @Expose val latitude: Double,
                      @SerializedName("longitude") @Expose val longitude: Double,
                      @SerializedName("token") @Expose val token: String?)

class CreateEventModel(private val view: CreateEventPresenterInterface) : Callback {

    private val createEventRequest = {json:String -> postRequest(json, base_url + createEvent, this) }
    private val TAG = "CreateEventModel"

    override fun onFailure(call: Call?, e: IOException?) {
        apiFailure(TAG, e, view)
    }

    override fun onResponse(call: Call?, response: Response?) {
        val action = { _: JSONObject ->
            view.onCreated()
        }
        apiResponse(TAG, response, view, action)
    }

    fun createEvent(json: String) {
        createEventRequest(json)
    }

}