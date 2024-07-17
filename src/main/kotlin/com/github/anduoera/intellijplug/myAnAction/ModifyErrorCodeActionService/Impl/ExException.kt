package com.github.anduoera.intellijplug.myAnAction.ModifyErrorCodeActionService.Impl

import com.github.anduoera.intellijplug.myAnAction.ModifyErrorCodeActionService.ModifyErrorCodeAction
import com.goide.inspections.vet.GoErrorsAsInspection
import com.goide.psi.GoFile
import com.intellij.codeHighlighting.HighlightDisplayLevel
import com.intellij.codeInsight.codeVision.lensContext
import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer
import com.intellij.codeInsight.daemon.impl.HighlightInfo
import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.ex.LocalInspectionToolWrapper
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiErrorElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil

/**
 * @Description: 描述
 * @author fanshq
 * @Date: 2024/07/17/23:15
 *
 */
class ExException:ModifyErrorCodeAction {
    override fun toDo(project: Project, file: GoFile,e: AnActionEvent) {
        val project = e.project ?: return
        val editor = e.getData(CommonDataKeys.EDITOR) ?: return
        val file = e.getData(CommonDataKeys.PSI_FILE) ?: return

        var size = file.references.size
        println(size)

    }
}