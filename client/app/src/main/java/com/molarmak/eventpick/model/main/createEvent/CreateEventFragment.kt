package com.molarmak.eventpick.model.main.createEvent

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.DatePicker
import android.widget.TimePicker
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.molarmak.eventpick.R
import com.molarmak.eventpick.app.Cache
import com.molarmak.eventpick.app.ViewType
import com.molarmak.eventpick.app.errorHandleToast
import com.molarmak.eventpick.app.runOnMain
import com.molarmak.eventpick.model.main.MainActivityView
import kotlinx.android.synthetic.main.fragment_create_event.*
import java.util.*
import kotlin.collections.ArrayList

interface CreateEventView: ViewType,OnMapReadyCallback, GoogleMap.OnMapClickListener, DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {
    fun onLoadCategories(categories: ArrayList<String>)
    fun onCreated()
}

class CreateEventFragment: Fragment(), CreateEventView {

    private lateinit var mMap: GoogleMap
    private var isNowStartTimeSelect = false
    private var marker: LatLng = LatLng(0.0,0.0)
    private val presenter: CreateEventPresenterInterface = CreateEventPresenterImpl(this)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v: View = inflater.inflate(R.layout.fragment_create_event, null)
        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        startTimeSelectBox.setOnClickListener {
            isNowStartTimeSelect = true
            getDate()
        }
        endTimeSelectBox.setOnClickListener {
            isNowStartTimeSelect = false
            getDate()
        }
        presenter.startLoadCategories()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(50.448544, 30.453086), 15f))
        mMap.setOnMapClickListener(this)
    }

    override fun onMapClick(pos: LatLng) {
        marker = pos
        mMap.clear()
        mMap.addMarker(MarkerOptions().position(pos).title("Event position"))
    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        val monthString = String.format("%02d", month)
        val dayString = String.format("%02d", dayOfMonth)
        if(isNowStartTimeSelect) {
            startTimeSelect.text = "$year-$monthString-$dayString"
        } else {
            endTimeSelect.text = "$year-$monthString-$dayString"
        }
        getTime()
    }

    override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
        val hoursString = String.format("%02d", hourOfDay)
        val minutesString = String.format("%02d", minute)
        if(isNowStartTimeSelect) {
            startTimeSelect.append(" $hoursString:$minutesString")
        } else {
            endTimeSelect.append(" $hoursString:$minutesString")
        }
    }

    override fun onLoadCategories(categories: ArrayList<String>) {
        runOnMain(activity, isVisible) {
            val categoryAdapter = ArrayAdapter(activity, android.R.layout.simple_spinner_item, categories)
            categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            categorySelect?.adapter = categoryAdapter
        }
        createNew.setOnClickListener {
            val form = CreateEventForm(
                    editName.text.toString(),
                    categorySelect.selectedItemPosition,
                    startTimeSelect.text.toString(),
                    endTimeSelect.text.toString(),
                    marker.latitude,
                    marker.longitude,
                    Cache.instance.token
            )
            presenter.startCreateEvent(form)
        }
    }

    override fun onCreated() {
        if(activity != null && isVisible) {
            (activity as MainActivityView).let {
                it.backFromChild()
                it.backFromChild()
            }
        }
    }

    override fun onError(errors: ArrayList<String>) {
        errorHandleToast(activity, isVisible, errors)
    }

    private fun getDate() {
        val now = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(
                context,
                this,
                now.get(Calendar.YEAR),
                now.get(Calendar.MONTH),
                now.get(Calendar.DAY_OF_MONTH))
        datePickerDialog.show()
    }

    private fun getTime() {
        val now = Calendar.getInstance()
        val timePickerDialog = TimePickerDialog(
                context,
                this,
                now.get(Calendar.HOUR_OF_DAY),
                now.get(Calendar.MINUTE),
                true)
        timePickerDialog.show()
    }

}