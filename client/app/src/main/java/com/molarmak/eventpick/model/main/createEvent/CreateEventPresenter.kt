package com.molarmak.eventpick.model.main.createEvent

import com.google.gson.Gson
import com.molarmak.eventpick.app.*
import com.molarmak.eventpick.model.main.events.CategoryModel
import java.text.SimpleDateFormat

interface CreateEventPresenterInterface: PresenterType, CategoryLoadInterface {
    fun startCreateEvent(form: CreateEventForm)
    fun onCreated()
}

class CreateEventPresenterImpl(private val view: CreateEventView): CreateEventPresenterInterface {

    private val model = CreateEventModel(this)
    private val categoryModel = CategoryModel(this)

    override fun onError(errors: ArrayList<String>) {
        view.onError(errors)
    }

    override fun startLoadCategories() {
        categoryModel.getCategories()
    }

    override fun endLoadCategories(categories: ArrayList<String>) {
        categories.add(0, "None")
        view.onLoadCategories(categories)
    }

    override fun startCreateEvent(form: CreateEventForm) {
        if(!validate(form)) { return }
        val json = Gson().toJson(form)
        model.createEvent(json)
    }

    override fun onCreated() {
        view.onCreated()
    }

    private fun validate(form: CreateEventForm): Boolean {
        if(form.name.isEmpty()) {
            view.onError(ERROR_FILL_FIELDS)
            return false
        }

        if(form.categoryId == 0) {
            view.onError(ERROR_SELECT_CATEGORY)
            return false
        }

        if(!timeCheck(form.startTime) || !timeCheck(form.endTime)) {
            view.onError(ERROR_SELECT_TIME_RANGE)
            return false
        }

        if(form.latitude == 0.0 && form.longitude == 0.0) {
            view.onError(ERROR_SELECT_POSITION)
            return false
        }

        return true
    }

    private fun timeCheck(time: String): Boolean {
        try {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm")
            dateFormat.parse(time)
            return true
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

}