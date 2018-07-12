package com.molarmak.eventpick.model.main.createNotification

import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.Circle
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.molarmak.eventpick.R
import com.molarmak.eventpick.app.ViewType
import com.molarmak.eventpick.app.errorHandleToast
import com.molarmak.eventpick.model.main.MainActivityView
import kotlinx.android.synthetic.main.fragment_create_notification.*

interface CreateNotificationView: ViewType {
    fun onNotificationCreated()
}

class CreateNotificationFragment: Fragment(), OnMapReadyCallback, GoogleMap.OnMapClickListener, CreateNotificationView {

    private lateinit var mMap: GoogleMap
    private var marker: LatLng = LatLng(0.0,0.0)
    private var circle: Circle? = null
    private val presenter: CreateNotificationPresenterInterface = CreateNotificationPresenterImpl(this)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v: View = inflater.inflate(R.layout.fragment_create_notification, null)
        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        radiusSelect.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                radiusText.text = "Selected radius: ${progress * 10} meters"
                circle?.radius = progress * 10.0
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {

            }

        })
        radiusSelect.progress = 20
        createNew.setOnClickListener {
            presenter.startCreateNotification(marker.latitude, marker.longitude, radiusSelect.progress * 10)
        }
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
        val circleOptions = CircleOptions()
                .center(pos)
                .radius(radiusSelect.progress * 10.0)
                .strokeColor(Color.argb(35, 54, 120, 237))
                .fillColor(Color.argb(35, 54, 120, 237))
        circle = mMap.addCircle(circleOptions)
    }

    override fun onNotificationCreated() {
        if(activity != null && isVisible) {
            (activity as MainActivityView).let {
                it.backFromChild()
            }
        }
    }

    override fun onError(errors: ArrayList<String>) {
        errorHandleToast(activity, isVisible, errors)
    }

}