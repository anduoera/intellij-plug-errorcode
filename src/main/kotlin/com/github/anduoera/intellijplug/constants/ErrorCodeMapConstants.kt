package com.github.anduoera.intellijplug.constants

import com.github.anduoera.intellijplug.dto.ErrorCodeMapListDto

class ErrorCodeMapConstants private constructor() {
    companion object {
        val instance: ErrorCodeMapConstants by lazy { ErrorCodeMapConstants() }
    }

    private val errorCodeMap: MutableMap<String, MutableMap<String, MutableList<ErrorCodeMapListDto>>> = mutableMapOf()

    fun getErrorCodeMap(): MutableMap<String, MutableMap<String, MutableList<ErrorCodeMapListDto>>> {
        return errorCodeMap
    }
}

