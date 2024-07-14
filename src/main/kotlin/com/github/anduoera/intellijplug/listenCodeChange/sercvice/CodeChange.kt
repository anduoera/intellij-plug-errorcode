package com.github.anduoera.intellijplug.listenCodeChange.sercvice

import com.intellij.codeInsight.editorActions.TypedHandlerDelegate.*
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile

/**
 * @Description: 描述
 * @author fanshq
 * @Date: 2024/07/13/13:51
 *
 */
interface CodeChange {
    fun toDo(c: Char, project: Project, editor: Editor, file: PsiFile): Result
}