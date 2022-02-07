package com.yikexiya.lazykit.ui.autoclick

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.yikexiya.lazykit.app.MainApplication
import com.yikexiya.lazykit.theme.TimeInput
import com.yikexiya.lazykit.util.log
import com.yikexiya.lazykit.util.toast
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class AutoClickFragment : Fragment() {
    private val viewModel by viewModels<AutoClickViewModel>()
    private val mainScope = MainScope()
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return AutoClickView(requireContext())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val attendView = view as AutoClickView
        attendView.newItemEvent = viewModel::newGestureGroup
        attendView.deleteItemEvent = viewModel::deleteGestureGroup
        attendView.showItemEvent = viewModel::showGestureGroup
        attendView.playItemEvent = viewModel::playGestureGroup
        attendView.cancelItemEvent = viewModel::cancelGestureGroup
        attendView.clickTimeEvent = { relation ->
            mainScope.launch {
                val currentSecondInDay = relation.gestureGroup.runTimeS
                val secondInDay = TimeInput.getTimeSecondInDay(requireContext(), currentSecondInDay)
                if (secondInDay != null)
                    viewModel.saveGroupTime(relation, secondInDay)
            }
        }
        attendView.saveItemEvent = {
            val allPoints = MainApplication.instance().getAllGestures()
            if (allPoints.isEmpty()) {
                toast("当前点击数量为0")
            } else {
                val dialog = GestureDialog(requireContext())
                dialog.sureEvent = { groupName, runningTime, delayTimes, gesturesPoints ->
                    viewModel.saveGestureGroup(groupName, runningTime) { groupId ->
                        delayTimes.mapIndexed { index, l ->
                            val pair = gesturesPoints[index]
                            Gesture(pair.first, pair.second, l, 50, groupId)
                        }
                    }
                }
                dialog.show()
            }
        }
        viewModel.gestureGroups.observe(viewLifecycleOwner) {
            attendView.setGestures(it)
        }
    }
}