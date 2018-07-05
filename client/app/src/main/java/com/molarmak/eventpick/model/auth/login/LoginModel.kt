package com.molarmak.eventpick.model.auth.login

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.molarmak.eventpick.app.*
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

class LoginForm(@SerializedName("email") @Expose val email: String,
               @SerializedName("password") @Expose val password: String)

class LoginModel(private val view: LoginPresenterInterface) : Callback {

    private val loginRequest = {json:String -> postRequest(json, base_url + login_url, this)}
    private val TAG = "LoginModel"

    override fun onFailure(call: Call?, e: IOException?) {
        apiFailure(TAG, e, view)
    }

    override fun onResponse(call: Call?, response: Response?) {
        val action = { res: JSONObject ->
            if(res.has("token")) {
                Cache.instance.token = res.getString("token")
                view.onLogin()
            } else view.onError(ERROR_BAD_RESPONSE)
        }
        apiResponse(TAG, response, view, action)
    }

    fun loginWith(json: String) {
        loginRequest(json)
    }
}