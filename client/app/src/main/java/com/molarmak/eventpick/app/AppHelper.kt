package com.molarmak.eventpick.app

import android.app.Activity
import android.content.Intent
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.molarmak.eventpick.R
import com.molarmak.eventpick.model.main.MainActivity
import com.molarmak.eventpick.model.main.events.Event
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

val JSON = MediaType.parse("application/json; charset=utf-8")

interface PresenterType {
    fun onError(errors: ArrayList<String>)
}

interface ViewType {
    fun onError(errors: ArrayList<String>)
}

interface CategoryLoadInterface : PresenterType {
    fun startLoadCategories()
    fun endLoadCategories(categories: ArrayList<String>)
}

fun postRequest(json: String, url: String, callback: Callback) {
    val client = OkHttpClient()
    val endUrl = "$url"

    val body = RequestBody.create(JSON, json)
    val request = Request.Builder()
            .url(endUrl)
            .post(body)
            .build()

    client.newCall(request).enqueue(callback)
}

fun getRequest(url: String, other: String, callback: Callback) {
    val client = OkHttpClient()
    val endUrl = "$url$other"

    val request = Request.Builder()
            .url(endUrl)
            .build()

    client.newCall(request).enqueue(callback)
}

fun deleteRequest(url: String, json: String, callback: Callback) {
    val client = OkHttpClient()
    val endUrl = "$url"

    val body = RequestBody.create(JSON, json)
    val request = Request.Builder()
            .url(endUrl)
            .delete(body)
            .build()

    client.newCall(request).enqueue(callback)
}

fun buildParams(base: StringBuilder, param: String, self: Any) {
    if(base.isEmpty()) {
        base.append("?$param=$self")
    } else {
        base.append("&$param=$self")
    }
}

fun apiFailure(TAG: String, e: IOException?, view: PresenterType) {
    Log.d(TAG, "something went wrong")
    Log.d(TAG, e?.toString())
    view.onError(ERROR_SERVER)
}

fun apiResponse(TAG: String, response: Response?, view: PresenterType, action: (JSONObject) -> Unit) {
    try {
        Log.d(TAG, "response get")
        val responseString = response?.body()?.string()
        Log.d(TAG, responseString)
        if(response?.code() == 200) {
            val res = JSONObject(responseString)
            if(res.has("result")) {
                if(res.getBoolean("result")) {
                    action(res)
                } else {
                    if(res.has("errors")) {
                        val type = object : TypeToken<ArrayList<String>>() {}.type
                        val errors: ArrayList<String> = Gson().fromJson(res.getString("errors"), type)
                        view.onError(errors)
                    } else view.onError(ERROR_BAD_RESPONSE)
                }
            } else view.onError(ERROR_BAD_RESPONSE)
        } else view.onError(ERROR_SERVER)
    } catch (e: Exception) {
        e.printStackTrace()
        view.onError(ERROR_UNHANDLED)
    }
}

val errorHandleView = {activity: Activity?, isVisible: Boolean, errorView: TextView?, errors: ArrayList<String> ->
    errorHandle(activity, isVisible) {
        errorView?.visibility = View.VISIBLE
        errorView?.text = errors.joinToString(separator = "\n")
    }
}

val errorHandleToast = {activity: Activity?, isVisible: Boolean, errors: ArrayList<String> ->
    errorHandle(activity, isVisible) {
        errors.forEach {
            Toast.makeText(activity!!, it, Toast.LENGTH_SHORT).show()
        }
    }
}

fun errorHandle(activity: Activity?, isVisible: Boolean, functionality: () -> Unit) {
    if(activity != null && isVisible) {
        activity.runOnUiThread {
            functionality()
        }
    }
}

fun authToMain(activity: Activity?, isVisible: Boolean) {
    if(activity != null && isVisible) {
        activity.finish()
        activity.startActivity(Intent(activity, MainActivity::class.java))
    }
}

fun runOnMain(activity: Activity?, isVisible: Boolean, action: () -> Unit) {
    if(activity != null && isVisible) {
        activity.runOnUiThread {
            action()
        }
    }
}

fun backPressedActivity(supportFragmentManager: FragmentManager, activity: Activity?) {
    try {
        if(supportFragmentManager.backStackEntryCount > 1) {
            supportFragmentManager.popBackStack()
        } else {
            activity?.finish()
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun navigateWithBackStack(supportFragmentManager: FragmentManager, fragment: Fragment) {
    val fragmentTransaction = supportFragmentManager.beginTransaction()
    fragmentTransaction.replace(R.id.mainFrame, fragment).addToBackStack(fragment::class.java.name)
    fragmentTransaction.commit()
}

