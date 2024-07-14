package com.github.anduoera.intellijplug.constants

import com.github.anduoera.intellijplug.listenFileChange.service.FileChange
import com.github.anduoera.intellijplug.listenFileChange.service.Impl.FileChangeErrorCodeInsp

/**
 * @Description: 描述
 * @author fanshq
 * @Date: 2024/07/13/5:59
 *
 */
class FileChangeMapConstants {
    companion object {
        val instance: FileChangeMapConstants by lazy { FileChangeMapConstants() }
    }


    private val fileChangeMap: HashMap<Boolean, FileChange> = hashMapOf()

    fun getFileChangeMap(): HashMap<Boolean, FileChange> {
        initFileChangeMap()
        return fileChangeMap
    }

    private fun initFileChangeMap(){
        fileChangeMap[true]=FileChangeErrorCodeInsp()
    }
}