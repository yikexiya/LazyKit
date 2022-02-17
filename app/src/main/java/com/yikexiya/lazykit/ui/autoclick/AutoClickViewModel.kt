package com.yikexiya.lazykit.ui.autoclick

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.room.withTransaction
import androidx.work.*
import com.yikexiya.lazykit.app.MainApplication
import com.yikexiya.lazykit.util.PermissionExt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import java.util.concurrent.TimeUnit

class AutoClickViewModel(application: Application) : AndroidViewModel(application) {
    private val database = getApplication<MainApplication>().database
    private val autoClickDao = database.autoClickDao()
    private val workQuery = WorkQuery.Builder.fromTags(listOf(AutoClickWorker::class.java.name))
        .addStates(listOf(WorkInfo.State.ENQUEUED, WorkInfo.State.FAILED))
        .build()
    val works = WorkManager.getInstance(application).getWorkInfosLiveData(workQuery)
    val gestureGroups: LiveData<List<GestureGroupRelation>> = autoClickDao.getGestures().asLiveData()

    fun newGestureGroup() {
        val context = getApplication<Application>()
        if (!PermissionExt.hasPermission(context, PermissionExt.PERMISSION_ACCESSIBILITY)) {
            PermissionExt.requestPermission(context, PermissionExt.PERMISSION_ACCESSIBILITY)
            return
        }
        if (!PermissionExt.hasPermission(context, PermissionExt.PERMISSION_FLOAT_WINDOW)) {
            PermissionExt.requestPermission(context, PermissionExt.PERMISSION_FLOAT_WINDOW)
            return
        }
        AutoClickService.showView(context)
    }
    fun deleteGestureGroup(relation: GestureGroupRelation) {
        viewModelScope.launch(Dispatchers.IO) {
            val workRequestId = relation.gestureGroup.workRequestId ?: return@launch
            WorkManager.getInstance(getApplication()).cancelWorkById(UUID.fromString(workRequestId))
            autoClickDao.deleteGestureGroup(relation.gestureGroup)
        }
    }
    fun showGestureGroup(relation: GestureGroupRelation) {

    }
    fun saveGroupTime(relation: GestureGroupRelation, secondInDay: Long) = viewModelScope.launch(Dispatchers.IO) {
        autoClickDao.changeGestureGroupTime(relation.gestureGroup.id, secondInDay)
    }
    fun cancelGestureGroup(relation: GestureGroupRelation) {
        viewModelScope.launch(Dispatchers.IO) {
            val workRequestId = relation.gestureGroup.workRequestId ?: return@launch
            WorkManager.getInstance(getApplication()).cancelWorkById(UUID.fromString(workRequestId))
            autoClickDao.runGestureGroupWith(relation.gestureGroup.id, null)
        }
    }
    fun playGestureGroup(relation: GestureGroupRelation) {
        val context = getApplication<Application>()
        if (!PermissionExt.hasPermission(context, PermissionExt.PERMISSION_ACCESSIBILITY)) {
            PermissionExt.requestPermission(context, PermissionExt.PERMISSION_ACCESSIBILITY)
            return
        }
        val gestureGroup = relation.gestureGroup
        val gestures = relation.gestures
        val xArray = FloatArray(gestures.size)
        val yArray = FloatArray(gestures.size)
        val durations = LongArray(gestures.size)
        val delayTimes = LongArray(gestures.size)
        gestures.forEachIndexed { index, gesture ->
            xArray[index] = gesture.x
            yArray[index] = gesture.y
            durations[index] = gesture.durationTimeMs
            delayTimes[index] = gesture.delayTimeS
        }
        val data = Data.Builder()
            .putLong("groupId", gestureGroup.id)
            .putFloatArray("xArray", xArray)
            .putFloatArray("yArray", yArray)
            .putLongArray("durations", durations)
            .putLongArray("delayTimes", delayTimes)
            .build()
        val workRequest = OneTimeWorkRequestBuilder<AutoClickWorker>()
//            .setInitialDelay(5, TimeUnit.SECONDS)
            .setInitialDelay(calcTimeOffset(gestureGroup.runTimeS), TimeUnit.SECONDS)
            .setInputData(data)
            .build()
        val workManager = WorkManager.getInstance(getApplication())
        workManager.enqueue(workRequest)
        viewModelScope.launch(Dispatchers.IO) {
            autoClickDao.runGestureGroupWith(gestureGroup.id, workRequest.id.toString())
        }
    }

    fun saveGestureGroup(groupName: String, runningTime: Long, gesturesGenerate: (groupId: Long) -> List<Gesture>) {
        viewModelScope.launch(Dispatchers.IO) {
            database.withTransaction {
                val groupId = autoClickDao.saveGestureGroup(GestureGroup(groupName, runningTime))
                val gestures = gesturesGenerate(groupId)
                autoClickDao.saveGestures(gestures)
                getApplication<MainApplication>().clearGestures()
            }
        }
    }

    private fun calcTimeOffset(runAt: Long): Long {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        val second = calendar.get(Calendar.SECOND)
        val currentTime = (hour * 3600 + minute * 60 + second)
        val realTime =  if (runAt >= currentTime) {
            runAt - currentTime
        } else {
            runAt - currentTime + 86400L
        }
        return realTime
    }
}