package com.molarmak.eventpick.model.main.events

import com.google.gson.Gson
import com.molarmak.eventpick.app.*

interface MapAllPresenterInterface: PresenterType, CategoryLoadInterface {
    fun startLoadEvents(category: Int?, token: String?)
    fun onEventsLoad(events: ArrayList<Event>)
    fun startDeleteEvent(eventId: Int)
    fun endDeleteEvent(eventId: Int)
    fun startGetUserInfo(latitude: Double, longitude: Double)
    fun endGetUserInfo(userInfo: UserInfo)
    fun startLogout()
    fun endLogout()
}

class MapAllPresenterImpl(val view: AllEventsView): MapAllPresenterInterface {

    private val model = MapAllModel(this)
    private val categoryModel = CategoryModel(this)
    private val deleteEventModel = DeleteEventModel(this)
    private val getUserInfoModel = GetUserInfoModel(this)
    private val logoutModel = LogoutModel(this)

    override fun startLoadEvents(category: Int?, token: String?) {
        val otherUrl = StringBuilder()
        category?.let {
            buildParams(otherUrl, "category", it)
        }
        token?.let {
            buildParams(otherUrl, "token", it)
        }
        model.getEvents(otherUrl.toString())
    }

    override fun onEventsLoad(events: ArrayList<Event>) {
        view.onEventsLoad(events)
    }

    override fun startLoadCategories() {
        categoryModel.getCategories()
    }

    override fun endLoadCategories(categories: ArrayList<String>) {
        categories.add(0, "None")
        view.onCategoriesLoad(categories)
    }

    override fun startDeleteEvent(eventId: Int) {
        val form = DeleteEvent(eventId, Cache.instance.token)
        val json = Gson().toJson(form)
        deleteEventModel.deleteEvent(json)
    }

    override fun endDeleteEvent(eventId: Int) {
        view.endDeleteEvent(eventId)
    }

    override fun startGetUserInfo(latitude: Double, longitude: Double) {
        val otherUrl = StringBuilder()
        buildParams(otherUrl, "latitude", latitude)
        buildParams(otherUrl, "longitude", longitude)
        getUserInfoModel.getUserInfo(otherUrl.toString())
    }

    override fun endGetUserInfo(userInfo: UserInfo) {
        view.endGetUserInfo(userInfo)
    }

    override fun startLogout() {
        if(Cache.instance.token != null) {
            val otherUrl = StringBuilder()
            buildParams(otherUrl, "token", Cache.instance.token!!)
            logoutModel.logout(otherUrl.toString())
        } else onError(ERROR_UNHANDLED)
    }

    override fun endLogout() {
        view.endLogout()
    }

    override fun onError(errors: ArrayList<String>) {
        view.onError(errors)
    }
}