package com.github.anduoera.intellijplug.myAnAction.ModifyErrorCodeActionService.Impl

import com.github.anduoera.intellijplug.constants.ErrorCodeMapConstants
import com.github.anduoera.intellijplug.constants.ErrorCodeTreeSetConstants
import com.github.anduoera.intellijplug.dto.ErrorCodeMapListDto
import com.github.anduoera.intellijplug.myAnAction.ModifyErrorCodeActionService.ModifyErrorCodeAction
import com.github.anduoera.intellijplug.utils.RefreshErrorCodeMap
import com.github.anduoera.intellijplug.utils.ReplaceErrorCodeUtils
import com.goide.psi.GoConstDeclaration
import com.goide.psi.GoFile
import com.goide.psi.impl.GoElementFactory
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.refactoring.suggested.endOffset

/**
 * @Description: 描述
 * @author fanshq
 * @Date: 2024/07/17/23:15
 *
 */
class InException:ModifyErrorCodeAction {
    val errorCodeMap = ErrorCodeMapConstants.instance.getErrorCodeMap()
    override fun toDo(project: Project, file: GoFile,e: AnActionEvent) {
        ReplaceErrorCodeUtils(file,project).InException()
    }
}