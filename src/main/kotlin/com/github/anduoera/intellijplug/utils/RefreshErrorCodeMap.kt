package com.github.anduoera.intellijplug.utils

import com.github.anduoera.intellijplug.constants.ErrorCodeMapConstants
import com.github.anduoera.intellijplug.constants.ErrorCodeTreeSetConstants
import com.github.anduoera.intellijplug.dto.ErrorCodeMapListDto
import com.goide.psi.GoConstDeclaration
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * @Description: 描述
 * @author fanshq
 * @Date: 2024/07/13/5:54
 *
 */
class RefreshErrorCodeMap {

    val errorCodeMap = ErrorCodeMapConstants.instance.getErrorCodeMap()
    val errorCodeTreeSet = ErrorCodeTreeSetConstants.instance.getErrorCodeTreeSet()
    fun refreshErrorCodeMap(file: PsiFile) {
        val projectMap = errorCodeMap.getOrPut(file.project.name) { ConcurrentHashMap() }
        errorCodeMap[file.project.name] = projectMap

        // 使用 Iterator 安全地遍历和修改 projectMap
        val iterator = projectMap.entries.iterator()
        while (iterator.hasNext()) {
            val entry = iterator.next()
            entry.value.removeIf { it.file == file.name }
            if (entry.value.isEmpty()) {
                iterator.remove()
            }
        }

        // Update map with new data from the file
        updateErrorCodeMapForFile(file, projectMap)
    }

    fun getFirstAvailableErrorCode(file: PsiFile):String{
        var first = errorCodeTreeSet[file.project.name]?.get(file.name)?.first().toString()
       do{
           first=first.toLongOrNull()?.inc().toString()
            var i=first.length
            while (i<4){
                first= "0$first"
                i=first.length
            }

        } while (errorCodeMap[file.project.name]?.containsKey(first) == true)
        return first
    }

    private fun updateErrorCodeMapForFile(file: PsiFile, projectMap: MutableMap<String, MutableList<ErrorCodeMapListDto>>) {
        var mutableMap = errorCodeTreeSet[file.project.name]
        mutableMap?.remove(file.name)
        var customSet = TreeSet<Long>(compareByDescending { it });
        val constDeclarations = PsiTreeUtil.findChildrenOfType(file, GoConstDeclaration::class.java)
        constDeclarations.forEach { constDecl ->
            constDecl.children.forEach {
                if (it.children.size > 1 && it.firstChild.text.startsWith("ErrorCode")) {
                    val num = it.lastChild.text.replace("\"", "")
                    val list = ErrorCodeMapListDto().apply {
                        this.project = file.project.name
                        this.file = file.name
                        this.psiElement = it
                        this.isText = num.toLongOrNull() == null
                        val numLong=num.toLongOrNull()
                        if(numLong!=null){
                            customSet.add(numLong)
                        }
                    }
                    projectMap.computeIfAbsent(num) { mutableListOf() }.add(list)
                }
            }
        }
        mutableMap?.set(file.name, customSet)
    }
}