package com.github.anduoera.intellijplug.listenCodeChange

import com.github.anduoera.intellijplug.constants.CodeChangeMapConstants
import com.goide.psi.GoFile
import com.goide.psi.GoPackageClause
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.intellij.codeInsight.editorActions.TypedHandlerDelegate


class CodeChangeListen : TypedHandlerDelegate() {
    val codeChangeMap = CodeChangeMapConstants.instance.getCodeChangeMap()
    override fun charTyped(c: Char, project: Project, editor: Editor, file: PsiFile): Result {
        if (file !is GoFile) return Result.STOP
        val packageClause: GoPackageClause? = file.getPackage()
        return  codeChangeMap[packageClause?.name == "exception"]?.toDo(c,project,editor,file)?:Result.STOP
    }
}