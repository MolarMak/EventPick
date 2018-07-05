package com.molarmak.eventpick.model.main

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import com.molarmak.eventpick.R
import com.molarmak.eventpick.app.backPressedActivity
import com.molarmak.eventpick.app.navigateWithBackStack
import com.molarmak.eventpick.model.main.events.MapAllFragment

interface MainActivityView {
    fun navigate(fragment: Fragment)
    fun backFromChild()
}

class MainActivity : AppCompatActivity(), MainActivityView {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_layer)
        navigate(MapAllFragment())
    }

    override fun navigate(fragment: Fragment) {
        navigateWithBackStack(supportFragmentManager, fragment)
    }

    override fun backFromChild() {
        onBackPressed()
    }

    override fun onBackPressed() {
        backPressedActivity(supportFragmentManager, MainActivity@this)
    }
}
