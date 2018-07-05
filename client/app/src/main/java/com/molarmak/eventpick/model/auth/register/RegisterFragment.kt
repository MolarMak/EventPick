package com.molarmak.eventpick.model.auth.register

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.molarmak.eventpick.R
import com.molarmak.eventpick.app.ERROR_PASSWORD_REPEATE
import com.molarmak.eventpick.app.ViewType
import com.molarmak.eventpick.app.authToMain
import com.molarmak.eventpick.app.errorHandleView
import com.molarmak.eventpick.model.auth.AuthView
import kotlinx.android.synthetic.main.fragment_register.*

interface RegisterView: ViewType {
    fun onRegister()
}

class RegisterFragment: Fragment(), RegisterView {

    private var errorView: TextView? = null
    private val presenter: RegisterPresenterInterface = RegisterPresenterImpl(this)
    private val genders = arrayOf("male", "female")
    private val ages = (16..99).map { it }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v: View = inflater.inflate(R.layout.fragment_register, null)
        errorView = v.findViewById(R.id.errorView)
        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val genderAdapter = ArrayAdapter(activity, android.R.layout.simple_spinner_item, genders)
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        genderSelect?.adapter = genderAdapter

        val ageAdapter = ArrayAdapter(activity, android.R.layout.simple_spinner_item, ages.map { it.toString() })
        ageAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        ageSelect?.adapter = ageAdapter

        registerButton?.setOnClickListener {
            errorView?.visibility = View.GONE
            if(passwordInput?.text.toString() == passwordRepeat?.text.toString()) {
                val form = RegisterForm(
                        fnameInput?.text.toString(),
                        if (lnameInput?.text.toString() == "") null else lnameInput?.text.toString(),
                        ages[ageSelect.selectedItemPosition],
                        genderSelect.selectedItemPosition+1,
                        emailInput?.text.toString(),
                        passwordInput?.text.toString()
                )
                presenter.startRegisterWith(form)
            } else onError(ERROR_PASSWORD_REPEATE)
        }
        backButton?.setOnClickListener {
            if(activity != null && isVisible) {
                (activity as AuthView)?.let {
                    it.backFromChild()
                }
            }
        }
    }

    override fun onRegister() {
        authToMain(activity, isVisible)
    }

    override fun onError(errors: ArrayList<String>) {
        errorHandleView(activity, isVisible, errorView, errors)
    }

}
