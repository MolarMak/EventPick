package com.molarmak.eventpick.model.auth.register

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.molarmak.eventpick.app.*
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

class RegisterForm(@SerializedName("firstName") @Expose val firstName: String,
                   @SerializedName("lastName") @Expose var lastName: String?,
                   @SerializedName("age") @Expose val age: Int,
                   @SerializedName("gender") @Expose val gender: Int,
                   @SerializedName("email") @Expose val email: String,
                   @SerializedName("password") @Expose val password: String)

class RegisterModel(private val view: RegisterPresenterInterface) : Callback {

    private val loginRequest = {json:String -> postRequest(json, base_url + register_url, this)}
    private val TAG = "RegisterModel"

    override fun onFailure(call: Call?, e: IOException?) {
        apiFailure(TAG, e, view)
    }

    override fun onResponse(call: Call?, response: Response?) {
        val action = { res: JSONObject ->
            if(res.has("token")) {
                Cache.instance.token = res.getString("token")
                view.onRegister()
            } else view.onError(ERROR_BAD_RESPONSE)
        }
        apiResponse(TAG, response, view, action)
    }

    fun registerWith(json: String) {
        loginRequest(json)
    }
}