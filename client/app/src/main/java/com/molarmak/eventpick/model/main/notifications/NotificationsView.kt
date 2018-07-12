package com.molarmak.eventpick.model.main.notifications

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import com.molarmak.eventpick.R
import com.molarmak.eventpick.app.ViewType
import com.molarmak.eventpick.app.errorHandleToast
import com.molarmak.eventpick.app.runOnMain
import com.molarmak.eventpick.model.main.MainActivityView
import com.molarmak.eventpick.model.main.createNotification.CreateNotificationFragment
import com.molarmak.eventpick.model.main.events.OnSwipeTouchListener
import kotlinx.android.synthetic.main.fragment_my_events.*

interface NotificationsView: ViewType {
    fun endDeleteNotifications(id: Int)
    fun endLoadNotifications(list: ArrayList<Notification>)
}

class NotificationsFragment: Fragment(), NotificationsView {

    private lateinit var adapter: RecycleViewAdapter
    private val presenter: NotificationsPresenterInterface = NotificationsPresenterImpl(this)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v: View = inflater.inflate(R.layout.fragment_my_notifications, null)
        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        createNew.setOnClickListener {
            if(activity != null && isVisible) {
                (activity as MainActivityView).let {
                    it.navigate(CreateNotificationFragment())
                }
            }
        }

        val r = context?.resources
        val pxToMove = (-TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 60f, r?.displayMetrics)).toInt()
        adapter = RecycleViewAdapter(pxToMove, presenter)
        recycler?.layoutManager = LinearLayoutManager(context)
        recycler?.adapter = adapter
        presenter.startLoadNotifications()
    }

    override fun endDeleteNotifications(id: Int) {
        runOnMain(activity, isVisible) {
            adapter.deleteNotification(id)
        }
    }

    override fun endLoadNotifications(list: ArrayList<Notification>) {
        runOnMain(activity, isVisible) {
            adapter.setNotifications(list)
        }
    }

    override fun onError(errors: ArrayList<String>) {
        errorHandleToast(activity, isVisible, errors)
    }

}

class RecycleViewAdapter(private val pxToMove: Int,
                         private val presenter: NotificationsPresenterInterface): RecyclerView.Adapter<RecycleViewAdapter.ViewHolder>() {

    private val repoList = ArrayList<Notification>()
    private var context: Context? = null

    fun setNotifications(list: ArrayList<Notification>) {
        repoList.clear()
        repoList.addAll(list)
        notifyDataSetChanged()
    }

    fun deleteNotification(id: Int) {
        repoList.find { trigger -> trigger.id == id }
                ?.let { repoList.remove(it) }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ViewHolder {
        val v = LayoutInflater.from(viewGroup.context).inflate(R.layout.item_notification, viewGroup, false)
        context = viewGroup.context
        return ViewHolder(v)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, i: Int) {
        try {
            val item = repoList[i]
            viewHolder.place.text = "${item.latitude} latitude, ${item.longitude} longitude"
            viewHolder.radius.text = "Radius: ${item.radius} meters"

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
                presenter.startDeleteNotification(item.id)
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
        val place: TextView = itemView.findViewById(R.id.place)
        val radius: TextView = itemView.findViewById(R.id.radius)

    }
}