package com.github.anduoera.intellijplug.constants

import java.util.*

/**
 * @Description: 描述
 * @author fanshq
 * @Date: 2024/07/13/17:41
 *
 */
class ErrorCodeTreeSetConstants {
    companion object {
        val instance: ErrorCodeTreeSetConstants by lazy { ErrorCodeTreeSetConstants() }
    }

    private val errorCodeTreeSet: MutableMap<String, MutableMap<String, TreeSet<Long>>> = mutableMapOf()

    fun getErrorCodeTreeSet(): MutableMap<String, MutableMap<String,  TreeSet<Long>>> {
        return errorCodeTreeSet
    }
}