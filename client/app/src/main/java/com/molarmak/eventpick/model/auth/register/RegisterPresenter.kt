package com.molarmak.eventpick.model.auth.register

import com.google.gson.Gson
import com.molarmak.eventpick.app.Cache
import com.molarmak.eventpick.app.ERROR_FILL_FIELDS
import com.molarmak.eventpick.app.PresenterType

interface RegisterPresenterInterface: PresenterType {
    fun startRegisterWith(form: RegisterForm)
    fun onRegister()
}

class RegisterPresenterImpl(val view: RegisterView): RegisterPresenterInterface {

    private val model = RegisterModel(this)

    override fun startRegisterWith(form: RegisterForm) {
        try {
            if(!validateInput(form)) { return }
            Cache.instance.email = form.email
            Cache.instance.password = form.password
            val json = Gson().toJson(form)
            model.registerWith(json)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onRegister() {
        view.onRegister()
    }

    override fun onError(errors: ArrayList<String>) {
        view.onError(errors)
    }

    private fun validateInput(form: RegisterForm): Boolean {
        if(form.email.isEmpty() || form.password.isEmpty() || form.firstName.isEmpty()) {
            view.onError(ERROR_FILL_FIELDS)
            return false
        }
        return true
    }

}