package com.github.anduoera.intellijplug.myAnAction.ModifyErrorCodeActionService

import com.goide.psi.GoFile
import com.intellij.codeInsight.editorActions.TypedHandlerDelegate
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile

/**
 * @Description: 描述
 * @author fanshq
 * @Date: 2024/07/17/23:14
 *
 */
interface ModifyErrorCodeAction {
    fun toDo( project: Project, file: GoFile,e: AnActionEvent)
}