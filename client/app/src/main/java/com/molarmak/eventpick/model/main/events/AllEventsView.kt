package com.molarmak.eventpick.model.main.events

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.TypedValue
import android.view.*
import android.widget.*
import com.github.javiersantos.bottomdialogs.BottomDialog
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.molarmak.eventpick.R
import com.molarmak.eventpick.app.Cache
import com.molarmak.eventpick.app.ViewType
import com.molarmak.eventpick.app.errorHandleToast
import com.molarmak.eventpick.app.runOnMain
import com.molarmak.eventpick.model.auth.AuthActivity
import com.molarmak.eventpick.model.main.MainActivityView
import com.molarmak.eventpick.model.main.create.CreateEventFragment
import kotlinx.android.synthetic.main.fragment_map_all.*
import kotlinx.android.synthetic.main.fragment_my_events.*


interface AllEventsView: ViewType {
    fun onCategoriesLoad(categories: ArrayList<String>)
    fun onEventsLoad(events: ArrayList<Event>)
    fun endDeleteEvent(eventId: Int)
    fun endGetUserInfo(userInfo: UserInfo)
    fun endLogout()
}
class MapAllFragment: Fragment(), OnMapReadyCallback, AllEventsView {

    private lateinit var mMap: GoogleMap
    private val presenter: MapAllPresenterInterface = MapAllPresenterImpl(this)
    private val events: ArrayList<Event> = ArrayList()
    private val categories: ArrayList<String> = ArrayList()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v: View = inflater.inflate(R.layout.fragment_map_all, null)
        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        myEventsButton.setOnClickListener {
            if(activity != null && isVisible) {
                (activity as MainActivityView).let {
                    it.navigate(MyEventsFragment())
                }
            }
        }
        exitButton.setOnClickListener {
            presenter.startLogout()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        presenter.startLoadCategories()
        presenter.startLoadEvents(null, null)
        refreshButton.setOnClickListener {
            if(categorySelect.selectedItemPosition != 0) {
                presenter.startLoadEvents(categorySelect.selectedItemId.toInt(), null)
            } else {
                presenter.startLoadEvents(null, null)
            }
        }
        mMap.setOnInfoWindowClickListener {
            onMarkerClick(it)
        }
    }

    override fun onEventsLoad(events: ArrayList<Event>) {
        runOnMain(activity, isVisible) {
            mMap.clear()
            events.forEach {
                val pos = LatLng(it.latitude, it.longitude)
                mMap.addMarker(MarkerOptions().position(pos).title(it.name))
            }
            events.lastOrNull()?.let {
                val lastPos = LatLng(it.latitude, it.longitude)
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastPos, 15f))
            }
            this.events.clear()
            this.events.addAll(events)
        }
    }

    override fun onCategoriesLoad(categories: ArrayList<String>) {
        runOnMain(activity, isVisible) {
            val categoryAdapter = ArrayAdapter(activity, android.R.layout.simple_spinner_item, categories)
            categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            categorySelect?.adapter = categoryAdapter
        }
        categorySelect.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                refreshButton.callOnClick()
            }
        }
        this.categories.clear()
        this.categories.addAll(categories)
    }

    private fun onMarkerClick(pos: Marker) {
        val event = events.findLast { event -> event.latitude == pos.position.latitude && event.longitude == pos.position.longitude }
        event?.let {
            presenter.startGetUserInfo(it.latitude, it.longitude)
        }
    }

    override fun endGetUserInfo(userInfo: UserInfo) {
        val event = events.findLast { event -> event.latitude == userInfo.latitude && event.longitude == userInfo.longitude }
        event?.let {
            runOnMain(activity, isVisible) {
                try {
                    BottomDialog.Builder(context!!)
                            .setTitle(event.name)
                            .setContent("Category: ${categories[event.categoryId]}\nTime: ${event.startTime}  -  ${event.endTime}\nUser: ${userInfo.userFirstName} ${userInfo.userLastName ?: ""}\nContact: ${userInfo.email}")
                            .setPositiveText("OK")
                            .onPositive {
                                it.dismiss()
                            }
                            .show()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun endDeleteEvent(eventId: Int) {
        //Not implement
    }

    override fun endLogout() {
        if(activity != null && isVisible) {
            activity!!.finish()
            startActivity(Intent(activity!!, AuthActivity::class.java))
        }
    }

    override fun onError(errors: ArrayList<String>) {
        errorHandleToast(activity, isVisible, errors)
    }

}


class MyEventsFragment: Fragment(), AllEventsView {

    private val presenter: MapAllPresenterInterface = MapAllPresenterImpl(this)
    private lateinit var adapter: RecycleViewAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v: View = inflater.inflate(R.layout.fragment_my_events, null)
        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        createNew.setOnClickListener {
            if(activity != null && isVisible) {
                (activity as MainActivityView).let {
                    it.navigate(CreateEventFragment())
                }
            }
        }
        presenter.startLoadCategories()
        logoutButton.setOnClickListener {
            presenter.startLogout()
        }
    }


    override fun onEventsLoad(events: ArrayList<Event>) {
        runOnMain(activity, isVisible) {
            adapter.setEvents(events)
        }
    }

    override fun onCategoriesLoad(categories: ArrayList<String>) {
        runOnMain(activity, isVisible) {
            val categoryAdapter = ArrayAdapter(activity, android.R.layout.simple_spinner_item, categories)
            categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            categorySelected.adapter = categoryAdapter

            val r = context?.resources
            val pxToMove = (-TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 60f, r?.displayMetrics)).toInt()
            adapter = RecycleViewAdapter(categories, pxToMove, presenter)
            recycler?.layoutManager = LinearLayoutManager(context)
            recycler?.adapter = adapter
        }
        categorySelected.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if(position == 0) {
                    presenter.startLoadEvents(null, Cache.instance.token)
                } else {
                    presenter.startLoadEvents(position, Cache.instance.token)
                }
            }

        }

    }

    override fun endDeleteEvent(eventId: Int) {
        runOnMain(activity, isVisible) {
            adapter.deleteEvent(eventId)
        }
    }

    override fun endGetUserInfo(userInfo: UserInfo) {
        //Not implement
    }

    override fun endLogout() {
        if(activity != null && isVisible) {
            activity!!.finish()
            startActivity(Intent(activity!!, AuthActivity::class.java))
        }
    }

    override fun onError(errors: ArrayList<String>) {
        errorHandleToast(activity, isVisible, errors)
    }

}


class RecycleViewAdapter(private val categories: ArrayList<String>,
                         private val pxToMove: Int,
                         private val presenter: MapAllPresenterInterface): RecyclerView.Adapter<RecycleViewAdapter.ViewHolder>() {

    private val repoList = ArrayList<Event>()
    private var context: Context? = null

    fun setEvents(list: ArrayList<Event>) {
        repoList.clear()
        repoList.addAll(list)
        notifyDataSetChanged()
    }
    
    fun deleteEvent(id: Int) {
        repoList.find { event -> event.id == id }
                ?.let { repoList.remove(it) }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ViewHolder {
        val v = LayoutInflater.from(viewGroup.context).inflate(R.layout.item_event, viewGroup, false)
        context = viewGroup.context
        return ViewHolder(v)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, i: Int) {
        try {
            val item = repoList[i]
            viewHolder.name.text = item.name
            viewHolder.category.text = "Category: ${categories[item.categoryId]}"
            viewHolder.startTime.text = "Start time: ${item.startTime}"
            viewHolder.endTime.text = "End time: ${item.endTime}"

            val swipeTouchListener = object : OnSwipeTouchListener(context) {

                override fun onSwipeLeft() {
                    if (viewHolder.infoLayout.translationX == 0f) {
                        viewHolder.binLayout.visibility = View.VISIBLE
                        viewHolder.binLayout.alpha = 0.0f
                        viewHolder.binLayout.animate().alpha(1.0f).setDuration(300).start()
                        viewHolder.infoLayout.animate().translationX(pxToMove.toFloat()).setDuration(250).start()
                    }
                }

                override fun onSwipeRight() {
                    if (viewHolder.infoLayout.translationX != 0f) {
                        viewHolder.binLayout.visibility = View.GONE
                        viewHolder.infoLayout.animate().translationX(0f).setDuration(250).start()
                    }
                }

                override fun onSingleTapUp() {
                    if (viewHolder.infoLayout.translationX != 0f) {
                        onSwipeRight()
                    }
                }
            }

            viewHolder.itemView.setOnTouchListener(swipeTouchListener)

            viewHolder.binLayout.setOnClickListener {
                presenter.startDeleteEvent(item.id)
                swipeTouchListener.onSwipeRight()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    override fun getItemCount(): Int {
        return repoList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val infoLayout: LinearLayout = itemView.findViewById(R.id.infoLayout)
        val binLayout: RelativeLayout = itemView.findViewById(R.id.binLayout)
        val name: TextView = itemView.findViewById(R.id.name)
        val category: TextView = itemView.findViewById(R.id.category)
        val startTime: TextView = itemView.findViewById(R.id.startTime)
        val endTime: TextView = itemView.findViewById(R.id.endTime)

    }
}

open class OnSwipeTouchListener(ctx: Context?) : View.OnTouchListener {

    private val gestureDetector: GestureDetector

    init {
        gestureDetector = GestureDetector(ctx, GestureListener())
    }

    override fun onTouch(v: View, event: MotionEvent): Boolean {
        return gestureDetector.onTouchEvent(event)
    }

    private inner class GestureListener : GestureDetector.SimpleOnGestureListener() {

        override fun onDown(e: MotionEvent): Boolean {
            return true
        }

        override fun onSingleTapUp(e: MotionEvent): Boolean {
            this@OnSwipeTouchListener.onSingleTapUp()
            return true
        }

        override fun onScroll(e1: MotionEvent, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
            var result = false
            val diffY = e2.y - e1.y
            val diffX = e2.x - e1.x
            try {
                if (Math.abs(diffX) > Math.abs(diffY)) {
                    if (diffX > 0) {
                        onSwipeRight()
                    } else {
                        onSwipeLeft()
                    }
                }
                result = true
            } catch (exception: Exception) {
                exception.printStackTrace()
            }

            return result
        }
    }

    open fun onSwipeRight() {}

    open fun onSwipeLeft() {}

    open fun onSingleTapUp() {}
}