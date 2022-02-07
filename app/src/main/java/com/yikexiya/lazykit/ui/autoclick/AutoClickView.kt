package com.yikexiya.lazykit.ui.autoclick

import android.content.Context
import android.util.TypedValue
import android.view.*
import androidx.core.view.children
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.yikexiya.lazykit.R
import com.yikexiya.lazykit.app.MainApplication
import com.yikexiya.lazykit.theme.*
import com.yikexiya.lazykit.util.*
import java.util.*

class AutoClickView(context: Context) : ViewGroup(context) {
    private val listAdapter = object : ListAdapter<GestureGroupRelation, RecyclerView.ViewHolder>(
        object : DiffUtil.ItemCallback<GestureGroupRelation>() {
            override fun areItemsTheSame(oldItem: GestureGroupRelation, newItem: GestureGroupRelation): Boolean {
                return oldItem.gestureGroup.id == newItem.gestureGroup.id
            }

            override fun areContentsTheSame(oldItem: GestureGroupRelation, newItem: GestureGroupRelation): Boolean {
                val oldGroup = oldItem.gestureGroup
                val newGroup = newItem.gestureGroup
                return oldGroup.name == newGroup.name && oldGroup.isRunning == newGroup.isRunning && oldGroup.runTimeS == newGroup.runTimeS
            }
        }
    ) {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val itemView = ItemView(parent.context)
            itemView.layoutParams = itemLayoutParam
            return object : RecyclerView.ViewHolder(itemView) {}
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            (holder.itemView as ItemView).bindItem(getItem(position))
        }
    }

    private val recyclerView = RecyclerView(context).apply {
        layoutParams = SizeKit.ignoreLp
        layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        adapter = listAdapter
        this@AutoClickView.addView(this)
    }

    private val add = button(this, "切换显示", {
        newItemEvent?.invoke()
    })
    private val save = button(this, "保存", {
        saveItemEvent?.invoke()
    })
    private val reset = button(this, "重置", {
        MainApplication.instance().clearGestures()
    })

    override fun shouldDelayChildPressedState() = false

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        add.autoMeasure(this)
        save.autoMeasure(this)
        reset.autoMeasure(this)
        val heightSpec = MeasureSpec.makeMeasureSpec(measuredHeight - add.measuredHeight - itemPadding * 2, MeasureSpec.EXACTLY)
        recyclerView.measure(widthMeasureSpec, heightSpec)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        recyclerView.layout(0, 0)
        save.layout(40, recyclerView.bottom + itemPadding)
        reset.centerHorizontal(this, save.top)
        add.layout(measuredWidth - add.measuredWidth - 40, save.top)
    }

    var showItemEvent: ((item: GestureGroupRelation) -> Unit)? = null
    var playItemEvent: ((item: GestureGroupRelation) -> Unit)? = null
    var cancelItemEvent: ((item: GestureGroupRelation) -> Unit)? = null
    var newItemEvent: (() -> Unit)? = null
    var deleteItemEvent: ((item: GestureGroupRelation) -> Unit)? = null
    var saveItemEvent: (() -> Unit)? = null
    var clickTimeEvent: ((item: GestureGroupRelation) -> Unit)? = null
    fun setGestures(list: List<GestureGroupRelation>) {
        listAdapter.submitList(list)
    }

    private inner class ItemView(context: Context) : ViewGroup(context) {
        private val name = text(this, "").apply {
            setTextSize(TypedValue.COMPLEX_UNIT_PX, 32.sp)
            isSingleLine = true
        }
        private val runTime = text(this, "").apply {
            setTextSize(TypedValue.COMPLEX_UNIT_PX, 32.sp)
            isSingleLine = true
            setOnClickListener {
                clickTimeEvent?.invoke(this@ItemView.tag as GestureGroupRelation)
            }
        }
        private val deleteIcon = icon(this, R.drawable.ic_delete).apply {
            setOnClickListener {
                deleteItemEvent?.invoke(this@ItemView.tag as GestureGroupRelation)
            }
        }
        private val playIcon = icon(this, R.drawable.ic_play).apply {
            setOnClickListener {
                val relation = this@ItemView.tag as GestureGroupRelation
                if (relation.gestureGroup.isRunning)
                    cancelItemEvent?.invoke(relation)
                else
                    playItemEvent?.invoke(relation)
            }
        }

        init {
            layoutParams = itemLayoutParam
            setOnClickListener {
                showItemEvent?.invoke(it.tag as GestureGroupRelation)
            }
        }

        override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
            children.forEach { it.autoMeasure(this) }
        }
        override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
            name.centerVertical(this, itemPadding)
            deleteIcon.centerVertical(this, measuredWidth - deleteIcon.measuredWidth - itemPadding)
            playIcon.centerVertical(this, deleteIcon.left - playIcon.measuredWidth - itemPadding)
            runTime.centerVertical(this, playIcon.left - runTime.measuredWidth - itemPadding)
        }

        fun bindItem(model: GestureGroupRelation) {
            tag = model
            name.text = model.gestureGroup.name
            val runTimeS = model.gestureGroup.runTimeS
            val hour = runTimeS / 3600
            val minute = runTimeS % 3600 / 60
            val second = runTimeS % 60
            val hourStr = if (hour < 10) "0$hour" else "$hour"
            val minuteStr = if (minute < 10) "0$minute" else "$minute"
            val secondStr = if (second < 10) "0$second" else "$second"
            val timeStr = "$hourStr:$minuteStr:$secondStr"
            runTime.text = timeStr
            val running = model.gestureGroup.isRunning
            val icon = if (running)
                R.drawable.ic_pause
            else
                R.drawable.ic_play
            playIcon.setImageResource(icon)
        }
    }

    companion object {
        private val itemHeight = 48.dp.toInt()
        private val itemPadding = 8.dp.toInt()
        private val itemLayoutParam = LayoutParams(LayoutParams.MATCH_PARENT, itemHeight)
    }
}