package com.github.anduoera.intellijplug.utils

import com.github.anduoera.intellijplug.constants.ErrorCodeMapConstants
import com.github.anduoera.intellijplug.constants.ErrorCodeTreeSetConstants
import com.github.anduoera.intellijplug.dto.ErrorCodeMapListDto
import com.goide.psi.GoConstDeclaration
import com.goide.psi.GoFile
import com.goide.psi.impl.GoElementFactory
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.impl.cache.CacheManager
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.refactoring.suggested.endOffset
import com.intellij.webSymbols.webTypes.WebTypesSymbol
import com.jetbrains.rd.util.first

/**
 * @Description: 描述
 * @author fanshq
 * @Date: 2024/08/03/15:21
 *
 */
class ReplaceErrorCodeUtils(var file: GoFile, var project: Project) {
    val errorCodeMap = ErrorCodeMapConstants.instance.getErrorCodeMap()
    val errorCodeTreeSet = ErrorCodeTreeSetConstants.instance.getErrorCodeTreeSet()

    fun InException(){
        replaceDuplicateErrorCode()
        addErrorMessage()
    }

    fun ExException(errorCodeStr:HashSet<String>,errorMessageStr:HashSet<String>){
        addErrorMessage(errorMessageStr)
        addErrorCode(errorCodeStr)
    }

    fun addErrorMessage(errorMessageStr:HashSet<String>){
        val const = file.constants
        var lastErrorMessagePsiElement: PsiElement = const.last()
        const.forEach {
            val text = it.firstChild.text
            if (text.startsWith("ErrorMessage")){
                lastErrorMessagePsiElement = it
            }
        }
        val document = PsiDocumentManager.getInstance(project).getDocument(file)
        var insertStringText=""
        errorMessageStr.forEach {
            var value = it.replace("ErrorMessage","").replace(Regex("([A-Z])"), " $1").lowercase().trim()
            insertStringText += "\n\t$it = \"$value\""
        }

        WriteCommandAction.runWriteCommandAction(project) {
            val errormessageOffset = lastErrorMessagePsiElement.parent.endOffset
            document?.insertString(errormessageOffset, insertStringText)
        }
    }


    fun addErrorCode(errorCodeStr:HashSet<String> ){
        val const = file.constants
        var lastErrorCodePsiElement: PsiElement = const.last()
        const.forEach {
            val text = it.firstChild.text
            if (text.startsWith("ErrorCode")){
                lastErrorCodePsiElement = it
            }
        }
        val document = PsiDocumentManager.getInstance(project).getDocument(file)
        var insertStringText=""
        errorCodeStr.forEach {
            val errorCode = RefreshErrorCodeMap().getFirstAvailableErrorCode(file)
            insertStringText+="\n\t$it = \"$errorCode\""
            errorCode.toLongOrNull()?.let { it1 -> errorCodeTreeSet[project.name]?.get(file.name)?.add(it1) }
        }

        WriteCommandAction.runWriteCommandAction(project) {
            val errorCodeOffset = lastErrorCodePsiElement.parent.endOffset
            document?.insertString(errorCodeOffset, insertStringText)
        }
    }

     fun addErrorMessage() {
        val const = file.constants
        var lastPsiElement: PsiElement = const.last()
        var errorCodeText: MutableList<String> = mutableListOf()
        var errorMessageText: MutableList<String> = mutableListOf()
        const.forEach {
            val text = it.firstChild.text
            if (text.startsWith("ErrorCode")) {
                errorCodeText.add(text.replace("ErrorCode", ""))
            } else if ((text.startsWith("ErrorMessage"))) {
                errorMessageText.add(text.replace("ErrorMessage", ""))
                lastPsiElement = it
            }
        }

        if (errorCodeText.size == errorMessageText.size) return
        val document = PsiDocumentManager.getInstance(project).getDocument(file)
        errorCodeText.toSet().subtract(errorMessageText.toSet()).forEach {
            val value = it.replace(Regex("([A-Z])"), " $1").lowercase().trim()
            WriteCommandAction.runWriteCommandAction(project) {
                if (document != null) {
                    val offset = lastPsiElement.parent.endOffset
                    document.insertString(offset, "\n\tErrorMessage$it = \"$value\"")
                }
            }
        }

        if (document != null) {
            PsiDocumentManager.getInstance(project).commitDocument(document)
        }
    }

     fun replaceDuplicateErrorCode() {
        val mutableMap = errorCodeMap[file.project.name]
        val fileName = file.name
        val const = PsiTreeUtil.getChildOfType(file, GoConstDeclaration::class.java)
        const?.children?.iterator()?.forEach {
            if (it.children.size > 1 && it.firstChild.text.startsWith("ErrorCode")) {
                val num = it.lastChild.text.replace("\"", "")
                val errorCodeMapList = mutableMap?.get(num)
                val tagList:MutableList<ErrorCodeMapListDto> = mutableListOf()
                if (errorCodeMapList != null && errorCodeMapList.size > 1) {
                    var tag = 0
                    errorCodeMapList.forEach { value ->
                        if (value.file == fileName && value.psiElement.hashCode() == it.hashCode() && tag < errorCodeMapList.size - 1) {
                            tag++
                            val errorCode = RefreshErrorCodeMap().getFirstAvailableErrorCode(file)
                            try {
                                WriteCommandAction.runWriteCommandAction(project) {
                                    val newElement = GoElementFactory.createConstSpec(project, it.firstChild.text, "", "\"${errorCode}\"\n")
                                    it.replace(newElement)
                                }
                                errorCode.toLongOrNull()?.let { it1 -> errorCodeTreeSet[file.project.name]?.get(file.name)?.add(it1) }
                                tagList.add(value)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                    errorCodeMapList.removeAll(tagList)
                }else if(num.toLongOrNull()==null){
                    val errorCode = RefreshErrorCodeMap().getFirstAvailableErrorCode(file)
                    try {
                        WriteCommandAction.runWriteCommandAction(project) {
                            val newElement = GoElementFactory.createConstSpec(project, it.firstChild.text, "", "\"${errorCode}\"\n")
                            it.replace(newElement)
                        }
                        errorCode.toLongOrNull()?.let { it1 -> errorCodeTreeSet[file.project.name]?.get(file.name)?.add(it1) }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }else if(it.children.size==1){
                try {
                    val errorCode = RefreshErrorCodeMap().getFirstAvailableErrorCode(file)
                    WriteCommandAction.runWriteCommandAction(project) {
                        val newElement = GoElementFactory.createConstSpec(project, it.firstChild.text, "", "\"${errorCode}\"\n")
                        it.replace(newElement)
                    }
                    errorCode.toLongOrNull()?.let { it1 -> errorCodeTreeSet[file.project.name]?.get(file.name)?.add(it1) }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
}