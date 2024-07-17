package com.github.anduoera.intellijplug.constants

import com.github.anduoera.intellijplug.listenCodeChange.sercvice.CodeChange
import com.github.anduoera.intellijplug.listenCodeChange.sercvice.Impl.ErrorCodePadding
import com.github.anduoera.intellijplug.myAnAction.ModifyErrorCodeActionService.Impl.ExException
import com.github.anduoera.intellijplug.myAnAction.ModifyErrorCodeActionService.Impl.InException
import com.github.anduoera.intellijplug.myAnAction.ModifyErrorCodeActionService.ModifyErrorCodeAction

/**
 * @Description: 描述
 * @author fanshq
 * @Date: 2024/07/13/13:57
 *
 */
class ModifyErrorCodeActionMapConstants {
    companion object {
        val instance: ModifyErrorCodeActionMapConstants by lazy { ModifyErrorCodeActionMapConstants() }
    }

    private val modifyErrorCodeActionMap: HashMap<Boolean, ModifyErrorCodeAction> = hashMapOf()

    fun getModifyErrorCodeActionMap(): HashMap<Boolean, ModifyErrorCodeAction> {
        initCodeChangeMap()
        return modifyErrorCodeActionMap
    }

    private fun initCodeChangeMap() {
        modifyErrorCodeActionMap[true] = InException()
        modifyErrorCodeActionMap[false] = ExException()
    }
}