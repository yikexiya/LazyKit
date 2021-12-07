package com.yikexiya.lazykit.ui.autoclick

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels

class AutoClickFragment : Fragment() {
    private val viewModel by viewModels<AttendViewModel>()
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return AttendView(requireContext())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val attendView = view as AttendView
        attendView.newItemEvent = viewModel::newGestureGroup
        attendView.deleteItemEvent = viewModel::deleteGestureGroup
        attendView.editItemEvent = viewModel::editGestureGroup
        attendView.showItemEvent = viewModel::showGestureGroup
        attendView.playItemEvent = viewModel::playGestureGroup
        viewModel.gestureGroups.observe(viewLifecycleOwner, {
            attendView.setGestures(it)
        })
        AttendManager.xyArrays.observe(viewLifecycleOwner, {
        })
    }
}