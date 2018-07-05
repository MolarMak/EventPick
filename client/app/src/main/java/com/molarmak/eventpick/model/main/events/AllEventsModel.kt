package com.molarmak.eventpick.model.main.events

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.molarmak.eventpick.app.*
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class Event (
        @SerializedName("id") @Expose val id: Int,
        @SerializedName("name") @Expose val name: String,
        @SerializedName("categoryId") @Expose val categoryId: Int,
        @SerializedName("startTime") @Expose val startTime: String,
        @SerializedName("endTime") @Expose val endTime: String,
        @SerializedName("latitude") @Expose val latitude: Double,
        @SerializedName("longitude") @Expose val longitude: Double,
        @SerializedName("userId") @Expose val userId: Int
)

data class DeleteEvent (
        @SerializedName("eventId") @Expose val eventId: Int,
        @SerializedName("token") @Expose val token: String?
)

data class UserInfo (
        @SerializedName("latitude") @Expose val latitude: Double,
        @SerializedName("longitude") @Expose val longitude: Double,
        @SerializedName("userFirstName") @Expose val userFirstName: String,
        @SerializedName("userLastName") @Expose val userLastName: String?,
        @SerializedName("email") @Expose val email: String)

class MapAllModel(private val view: MapAllPresenterInterface) : Callback {

    private val getEventsWith = {other:String -> getRequest(base_url + getEvents, other, this)}
    private val TAG = "GetEventsModel"

    override fun onFailure(call: Call?, e: IOException?) {
        apiFailure(TAG, e, view)
    }

    override fun onResponse(call: Call?, response: Response?) {
        val action = { res: JSONObject ->
            if(res.has("events")) {
                val type = object : TypeToken<ArrayList<Event>>() {}.type
                val events: ArrayList<Event> = Gson().fromJson(res.getString("events"), type)
                view.onEventsLoad(events)
            } else view.onError(ERROR_BAD_RESPONSE)
        }
        apiResponse(TAG, response, view, action)
    }

    fun getEvents(other: String) {
        getEventsWith(other)
    }
}

class CategoryModel(private val view: CategoryLoadInterface) : Callback {

    private val getCategoriesRequest = {other:String -> getRequest(base_url + getCategories, other, this)}
    private val TAG = "GetEventsModel"

    override fun onFailure(call: Call?, e: IOException?) {
        apiFailure(TAG, e, view)
    }

    override fun onResponse(call: Call?, response: Response?) {
        val action = { res: JSONObject ->
            if(res.has("categories")) {
                val type = object : TypeToken<ArrayList<String>>() {}.type
                val categories: ArrayList<String> = Gson().fromJson(res.getString("categories"), type)
                view.endLoadCategories(categories)
            } else view.onError(ERROR_BAD_RESPONSE)
        }
        apiResponse(TAG, response, view, action)
    }

    fun getCategories() {
        getCategoriesRequest("")
    }

}


class DeleteEventModel(private val view: MapAllPresenterInterface) : Callback {

    private val deleteEventRequest = {json:String -> deleteRequest(base_url + deleteEvent, json, this)}
    private val TAG = "DelEventsModel"

    override fun onFailure(call: Call?, e: IOException?) {
        apiFailure(TAG, e, view)
    }

    override fun onResponse(call: Call?, response: Response?) {
        val action = { res: JSONObject ->
            if(res.has("eventId")) {
                view.endDeleteEvent(res.getInt("eventId"))
            } else view.onError(ERROR_BAD_RESPONSE)
        }
        apiResponse(TAG, response, view, action)
    }

    fun deleteEvent(json: String) {
        deleteEventRequest(json)
    }

}

class GetUserInfoModel(private val view: MapAllPresenterInterface) : Callback {

    private val getUserInfoRequest = {other:String -> getRequest(base_url + getUserInfo, other, this)}
    private val TAG = "UserInfoModel"

    override fun onFailure(call: Call?, e: IOException?) {
        apiFailure(TAG, e, view)
    }

    override fun onResponse(call: Call?, response: Response?) {
        val action = { res: JSONObject ->
            if(res.has("info")) {
                val info = Gson().fromJson(res.getString("info"), UserInfo::class.java)
                view.endGetUserInfo(info)
            } else view.onError(ERROR_BAD_RESPONSE)
        }
        apiResponse(TAG, response, view, action)
    }

    fun getUserInfo(other: String) {
        getUserInfoRequest(other)
    }

}

class LogoutModel(private val view: MapAllPresenterInterface) : Callback {

    private val logoutRequest = {other:String -> getRequest(base_url + logout_url, other, this)}
    private val TAG = "LogoutModel"

    override fun onFailure(call: Call?, e: IOException?) {
        apiFailure(TAG, e, view)
    }

    override fun onResponse(call: Call?, response: Response?) {
        val action = { _: JSONObject ->
            view.endLogout()
        }
        apiResponse(TAG, response, view, action)
    }

    fun logout(other: String) {
        logoutRequest(other)
    }

}