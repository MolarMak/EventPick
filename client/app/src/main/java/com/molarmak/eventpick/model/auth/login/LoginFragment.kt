package com.molarmak.eventpick.model.auth.login

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.molarmak.eventpick.R
import com.molarmak.eventpick.app.ViewType
import com.molarmak.eventpick.app.authToMain
import com.molarmak.eventpick.app.errorHandleView
import com.molarmak.eventpick.model.auth.AuthView
import com.molarmak.eventpick.model.auth.register.RegisterFragment
import kotlinx.android.synthetic.main.fragment_login.*

interface LoginView: ViewType {
    fun onLogin()
}

class LoginFragment: Fragment(), LoginView {

    private var errorView: TextView? = null
    private val presenter: LoginPresenterInterface = LoginPresenterImpl(this)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v: View = inflater.inflate(R.layout.fragment_login, null)
        errorView = v.findViewById(R.id.errorView)
        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        loginButton?.setOnClickListener {
            errorView?.visibility = View.GONE
            val form = LoginForm(
                    emailInput?.text.toString(),
                    passwordInput?.text.toString()
            )
            presenter.startLoginWith(form)
        }
        registerClick?.setOnClickListener {
            if(activity != null && isVisible) {
                (activity as AuthView).let {
                    it.navigate(RegisterFragment())
                }
            }
        }
    }

    override fun onLogin() {
        authToMain(activity, isVisible)
    }

    override fun onError(errors: ArrayList<String>) {
        errorHandleView(activity, isVisible, errorView, errors)
    }

}