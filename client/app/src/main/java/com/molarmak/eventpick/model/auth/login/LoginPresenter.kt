package com.molarmak.eventpick.model.auth.login

import com.google.gson.Gson
import com.molarmak.eventpick.app.PresenterType
import com.molarmak.eventpick.app.Cache
import com.molarmak.eventpick.app.ERROR_FILL_FIELDS

interface LoginPresenterInterface: PresenterType {
    fun startLoginWith(form: LoginForm)
    fun onLogin()
}

class LoginPresenterImpl(val view: LoginView): LoginPresenterInterface {

    private val model = LoginModel(this)

    override fun startLoginWith(form: LoginForm) {
        try {
            if(!validateInput(form)) { return }
            Cache.instance.email = form.email
            Cache.instance.password = form.password
            val json = Gson().toJson(form)
            model.loginWith(json)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onLogin() {
        view.onLogin()
    }

    override fun onError(errors: ArrayList<String>) {
        view.onError(errors)
    }

    private fun validateInput(form: LoginForm): Boolean {
        if(form.email.isEmpty() || form.password.isEmpty()) {
            view.onError(ERROR_FILL_FIELDS)
            return false
        }
        return true
    }

}