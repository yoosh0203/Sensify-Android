package com.seung.sensormaster.ui.screens.category

import androidx.lifecycle.ViewModel
import com.seung.sensormaster.data.model.SensorTool
import com.seung.sensormaster.data.model.SensorTools
import com.seung.sensormaster.data.model.ToolCategory
import com.seung.sensormaster.data.sensor.SensorAvailabilityManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CategoryViewModel @Inject constructor(
    private val sensorAvailabilityManager: SensorAvailabilityManager
) : ViewModel() {

    /**
     * 카테고리에 해당하는 도구 목록 중, 기기에서 지원하지 않는 센서를 사용하는 도구는 제외하여 반환.
     */
    fun getFilteredTools(category: ToolCategory): List<SensorTool> {
        return SensorTools.byCategory(category).filter { tool ->
            sensorAvailabilityManager.isSensorAvailable(tool.requiredSensorType)
        }
    }
}
