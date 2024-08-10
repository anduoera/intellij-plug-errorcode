package com.github.anduoera.intellijplug.mySettings

import com.github.anduoera.intellijplug.constants.CodeChangeMapConstants
import com.intellij.openapi.components.*

/**
 * @Description: 描述
 * @author fanshq
 * @Date: 2024/08/07/22:02
 *
 */
@State(
        name = "com.github.anduoera.intellijplug.mySettings",
        storages = [Storage("MyPluginSettings.xml")]
)
@Service(Service.Level.APP)
class MySettings: PersistentStateComponent<MySettingsState> {
    private var myState = MySettingsState()

    companion object {
        fun getInstance(): MySettings {
            return service()
        }
    }
    override fun getState(): MySettingsState {
        return myState
    }

    override fun loadState(state: MySettingsState) {
        myState=state
    }
}