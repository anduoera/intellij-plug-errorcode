package com.github.anduoera.intellijplug.myAnAction

import com.github.anduoera.intellijplug.constants.ErrorCodeMapConstants
import com.github.anduoera.intellijplug.constants.ErrorCodeTreeSetConstants
import com.github.anduoera.intellijplug.dto.ErrorCodeMapListDto
import com.github.anduoera.intellijplug.utils.RefreshErrorCodeMap
import com.goide.psi.GoConstDeclaration
import com.goide.psi.GoFile
import com.goide.psi.GoPackageClause
import com.goide.psi.GoVarDefinition
import com.goide.psi.impl.GoConstSpecImpl
import com.goide.psi.impl.GoElementFactory
import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.refactoring.suggested.endOffset
import com.jetbrains.rd.util.addUnique
import com.jetbrains.rd.util.string.println

/**
 * @Description: 描述
 * @author fanshq
 * @Date: 2024/07/13/19:56
 *
 */
class ModifyErrorCodeAction : AnAction() {

    val errorCodeMap = ErrorCodeMapConstants.instance.getErrorCodeMap()
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val file = e.getData(com.intellij.openapi.actionSystem.CommonDataKeys.PSI_FILE) ?: return
        if (file !is GoFile) return
        val packageClause: GoPackageClause? = file.getPackage()
        if (packageClause?.name != "exception") return
        replaceDuplicateErrorCode(file, project)
        addErrorMessage(file, project)
    }

    private fun addErrorMessage(file: GoFile, project: Project) {
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

    private fun replaceDuplicateErrorCode(file: GoFile, project: Project) {
        val mutableMap = errorCodeMap[file.project.name]
        val errorCodeTreeSet = ErrorCodeTreeSetConstants.instance.getErrorCodeTreeSet()
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