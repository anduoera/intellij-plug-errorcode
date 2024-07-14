package com.github.anduoera.intellijplug.listenFileChange.service.Impl

import com.github.anduoera.intellijplug.constants.ErrorCodeMapConstants
import com.github.anduoera.intellijplug.constants.FileChangeMapConstants
import com.github.anduoera.intellijplug.listenFileChange.service.FileChange
import com.github.anduoera.intellijplug.utils.RefreshErrorCodeMap
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiFile
import com.jetbrains.exported.JBRApi.Service

/**
 * @Description: 描述
 * @author fanshq
 * @Date: 2024/07/13/6:05
 *
 */
class FileChangeErrorCodeInsp:FileChange {

    val errorCodeMap = ErrorCodeMapConstants.instance.getErrorCodeMap()
    override fun toDo(file: PsiFile,holder: ProblemsHolder) {
        try {
            RefreshErrorCodeMap().refreshErrorCodeMap(file)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val projectMapLists = errorCodeMap[file.project.name]
        projectMapLists?.forEach { key, value ->
            val errorCodeList = value
            if (errorCodeList.size > 1) {
                errorCodeList.forEach { errorCode ->
                    if (errorCode.file == file.name) {
                        holder.registerProblem(
                                errorCode.psiElement,
                                "Duplicate ErrorCode: ${key}",
                                ProblemHighlightType.GENERIC_ERROR_OR_WARNING
                        )
                    }
                }
            } else if (value[0].isText) {
                holder.registerProblem(
                        value[0].psiElement,
                        "Non-numeric ErrorCode: ${key}",
                        ProblemHighlightType.GENERIC_ERROR_OR_WARNING
                )
            }
        }
    }

}