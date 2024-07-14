package com.github.anduoera.intellijplug.listenCodeChange.sercvice.Impl

import com.github.anduoera.intellijplug.constants.ErrorCodeMapConstants
import com.github.anduoera.intellijplug.constants.ErrorCodeTreeSetConstants
import com.github.anduoera.intellijplug.listenCodeChange.sercvice.CodeChange
import com.github.anduoera.intellijplug.utils.DocumentUtils
import com.github.anduoera.intellijplug.utils.RefreshErrorCodeMap
import com.goide.psi.GoConstDeclaration
import com.intellij.codeInsight.editorActions.TypedHandlerDelegate.*
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import java.util.*

/**
 * @Description: 描述
 * @author fanshq
 * @Date: 2024/07/13/14:01
 *
 */
class ErrorCodePadding:CodeChange {
    private val errorCodeTreeSet = ErrorCodeTreeSetConstants.instance.getErrorCodeTreeSet()
    override fun toDo(c: Char, project: Project, editor: Editor, file: PsiFile): Result {
//        val documentUtils = DocumentUtils(editor)
//        val currentLine = documentUtils.getCurrentLine()
//        var previousLine = documentUtils.getPreviousLine()
//        val firstErrorCode = RefreshErrorCodeMap().getFirstAvailableErrorCode(file)
        return Result.STOP
    }
}