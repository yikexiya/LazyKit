package com.yikexiya.lazykit.ui.autoclick

import android.content.Context
import android.view.*
import androidx.core.view.children
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.yikexiya.lazykit.R
import com.yikexiya.lazykit.theme.*
import com.yikexiya.lazykit.util.autoMeasure
import com.yikexiya.lazykit.util.centerVertical
import com.yikexiya.lazykit.util.dp
import com.yikexiya.lazykit.util.layout
import java.util.*

class AttendView(context: Context) : ViewGroup(context) {
    private val itemHeight = 36.dp.toInt()
    private val itemLayoutParam = LayoutParams(LayoutParams.MATCH_PARENT, itemHeight)
    private val listAdapter = object : ListAdapter<GestureGroup, RecyclerView.ViewHolder>(
        object : DiffUtil.ItemCallback<GestureGroup>() {
            override fun areItemsTheSame(oldItem: GestureGroup, newItem: GestureGroup): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: GestureGroup, newItem: GestureGroup): Boolean {
                return oldItem == newItem
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
        layoutParams = SizeKit.matchParent
        layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        adapter = listAdapter
    }

    private val add = button(this, "新增", {
        newItemEvent?.invoke()
    })
//    private val watch = AppCompatButton(context).apply {
//        layoutParams = SizeKit.wrapContent
//        this@AttendView.addView(this)
//        setOnClickListener {
//            val data = Data.Builder()
//                .putString("key", UUID.randomUUID().toString())
//                .build()
//            val workRequest = OneTimeWorkRequestBuilder<PrintWorker>()
//                .setInitialDelay(5, TimeUnit.SECONDS)
//                .setInputData(data)
//                .build()
//            WorkManager.getInstance(context).enqueue(workRequest)
//        }
//    }

    override fun shouldDelayChildPressedState() = false

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        children.forEach { it.autoMeasure(this) }
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        recyclerView.layout(0, 0)
        add.layout(measuredWidth - add.measuredWidth - 40, measuredHeight - add.measuredHeight - 40)
    }

    var showItemEvent: ((item: GestureGroup) -> Unit)? = null
    var editItemEvent: ((item: GestureGroup) -> Unit)? = null
    var playItemEvent: ((item: GestureGroup) -> Unit)? = null
    var newItemEvent: (() -> Unit)? = null
    var deleteItemEvent: ((item: GestureGroup) -> Unit)? = null
    fun setGestures(list: List<GestureGroup>) {
        listAdapter.submitList(list)
    }

    private inner class ItemView(context: Context) : ViewGroup(context) {
        private val name = text(this, "").apply {
            gravity = Gravity.CENTER_VERTICAL
        }
        private val editIcon = icon(this, R.drawable.ic_edit).apply {
            setOnClickListener {
                editItemEvent?.invoke(this@ItemView.tag as GestureGroup)
            }
        }
        private val playIcon = icon(this, R.drawable.ic_play).apply {
            setOnClickListener {
                playItemEvent?.invoke(this@ItemView.tag as GestureGroup)
            }
        }

        init {
            setOnClickListener {
                showItemEvent?.invoke(it.tag as GestureGroup)
            }
            setOnLongClickListener {
                deleteItemEvent?.invoke(it.tag as GestureGroup)
                return@setOnLongClickListener true
            }
        }

        override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
            editIcon.autoMeasure(this)
            playIcon.autoMeasure(this)
            val nameWidth = MeasureSpec.makeMeasureSpec(measuredWidth - editIcon.measuredWidth - playIcon.measuredWidth, MeasureSpec.EXACTLY)
            name.measure(nameWidth, heightMeasureSpec)
        }
        override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
            name.centerVertical(this, 0)
            editIcon.centerVertical(name, name.right)
            playIcon.centerVertical(editIcon, editIcon.right)
        }

        fun bindItem(model: GestureGroup) {
            tag = model
            name.text = model.name
        }
    }
}