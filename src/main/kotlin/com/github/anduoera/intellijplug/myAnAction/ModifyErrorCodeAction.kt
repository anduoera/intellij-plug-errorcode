package com.github.anduoera.intellijplug.myAnAction

import com.github.anduoera.intellijplug.constants.ErrorCodeMapConstants
import com.github.anduoera.intellijplug.constants.ErrorCodeTreeSetConstants
import com.github.anduoera.intellijplug.constants.ModifyErrorCodeActionMapConstants
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


    val modifyErrorCodeActionMap = ModifyErrorCodeActionMapConstants.instance.getModifyErrorCodeActionMap()
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        if (project.name.endsWith("foundation")) return
        val file = e.getData(com.intellij.openapi.actionSystem.CommonDataKeys.PSI_FILE) ?: return
        if (file !is GoFile) return
        val packageClause: GoPackageClause? = file.getPackage()
        modifyErrorCodeActionMap[packageClause?.name == "exception"]?.toDo(project,file,e)
    }


}