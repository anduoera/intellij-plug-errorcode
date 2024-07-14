package com.github.anduoera.intellijplug.constants

import com.github.anduoera.intellijplug.listenCodeChange.sercvice.CodeChange
import com.github.anduoera.intellijplug.listenCodeChange.sercvice.Impl.ErrorCodePadding

/**
 * @Description: 描述
 * @author fanshq
 * @Date: 2024/07/13/13:57
 *
 */
class CodeChangeMapConstants {
    companion object {
        val instance: CodeChangeMapConstants by lazy { CodeChangeMapConstants() }
    }

    private val codeChangeMap: HashMap<Boolean, CodeChange> = hashMapOf()

    fun getCodeChangeMap(): HashMap<Boolean, CodeChange> {
        initCodeChangeMap()
        return codeChangeMap
    }

    private fun initCodeChangeMap(){
        codeChangeMap[true] = ErrorCodePadding()
    }
}