package com.github.anduoera.intellijplugtest .padding

import com.github.anduoera.intellijplugtest.Constants.ErrorCodeMapConstants
import com.goide.psi.GoConstDeclaration
import com.goide.psi.GoFile
import com.goide.psi.GoPackageClause
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiFile
import com.intellij.codeInsight.editorActions.TypedHandlerDelegate
import com.intellij.codeInsight.lookup.LookupManager
import com.intellij.psi.util.PsiTreeUtil
import java.util.TreeSet


class ErrorCodePadding : TypedHandlerDelegate() {
    val map = ErrorCodeMapConstants.instance.getErrorCodeMap()
    override fun charTyped(c: Char, project: Project, editor: Editor, file: PsiFile): Result {
        if (file !is GoFile) return Result.STOP
        val packageClause: GoPackageClause? = file.getPackage()
        if (packageClause?.name != "exception") return Result.STOP
        val document = editor.document
        val caretModel = editor.caretModel
        val offset = caretModel.offset
        val currentLineNumber = document.getLineNumber(offset)
        val projectMapLists = map[file.project.name]
        if (currentLineNumber > 0) {
            val previousLineNumber = currentLineNumber - 1
            val previousLineStartOffset = document.getLineStartOffset(previousLineNumber)
            val previousLineEndOffset = document.getLineEndOffset(previousLineNumber)
            val previousLineText = document.getText().substring(previousLineStartOffset, previousLineEndOffset)
            if (previousLineText.contains("ErrorCode") || (currentLineNumber<10&&previousLineText.contains("const") )) {
                val lineStartOffset = document.getLineStartOffset(document.getLineNumber(offset))
                val lineEndOffset = document.getLineEndOffset(document.getLineNumber(offset))
                val lineText = document.getText().substring(lineStartOffset, lineEndOffset)
                var errorCode = getFileLastErrorCode(file)
                var num=errorCode.first()
                if (lineText.length < 10) {
                    do {
                        num++
                    }while (projectMapLists?.containsKey(num.toString()) == true)
                    val keyword = "\tErrorCode = \"${num}\""
                    document.replaceString(lineStartOffset, lineEndOffset, keyword)
                    caretModel.moveToOffset(lineStartOffset + keyword.split("=")[0].trim().length + 1)
                    return Result.CONTINUE
                }
            }
        }
        return Result.STOP
    }


    private fun getFileLastErrorCode(file: PsiFile): TreeSet<Long> {
        var customSet = TreeSet<Long>(compareByDescending { it });
        val constDeclarations = PsiTreeUtil.findChildrenOfType(file, GoConstDeclaration::class.java)
        constDeclarations.forEach { constDecl ->
            constDecl.children.forEach {
                if (it.children.size > 1 && it.firstChild.text.startsWith("ErrorCode")) {
                    val num = it.lastChild.text.replace("\"", "")
                    var number = num.toLongOrNull()
                    if (number != null) {
                        customSet.add(number)
                    }
                }
            }
        }
        return customSet
    }
}