package com.molarmak.eventpick.model.auth

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.google.firebase.iid.FirebaseInstanceId
import com.molarmak.eventpick.R
import com.molarmak.eventpick.app.Cache
import com.molarmak.eventpick.app.backPressedActivity
import com.molarmak.eventpick.app.navigateWithBackStack
import com.molarmak.eventpick.model.auth.login.LoginFragment


interface AuthView {
    fun navigate(fragment: Fragment)
    fun backFromChild()
}

class AuthActivity : AppCompatActivity(), AuthView {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_layer)
        navigate(LoginFragment())
        FirebaseInstanceId.getInstance().instanceId.addOnSuccessListener(AuthActivity@this) { instanceIdResult ->
            val deviceToken = instanceIdResult.token
            Cache.instance.fbToken = deviceToken
        }
    }

    override fun navigate(fragment: Fragment) {
        navigateWithBackStack(supportFragmentManager, fragment)
    }

    override fun backFromChild() {
        onBackPressed()
    }

    override fun onBackPressed() {
        backPressedActivity(supportFragmentManager, AuthActivity@this)
    }

}
