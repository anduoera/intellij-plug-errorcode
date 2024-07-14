package com.github.anduoera.intellijplug.utils

import com.github.anduoera.intellijplug.dto.LineObjectDto
import com.intellij.openapi.editor.CaretModel
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile

/**
 * @Description: 描述
 * @author fanshq
 * @Date: 2024/07/13/16:40
 *
 */
class DocumentUtils(var editor: Editor) {
    private  var document: Document = this.editor.document
    private var caretModel: CaretModel = this.editor.caretModel
    private var offset: Int = this.caretModel.offset

    fun getPreviousLine(): LineObjectDto? {
        val currentLineNumber = document.getLineNumber(offset)
        if(currentLineNumber>0){
            val previousLineNumber = currentLineNumber - 1
            val lineObjectDto = getLineObjectDto(previousLineNumber)
            return lineObjectDto
        }
        return null
    }

    fun getCurrentLine(): LineObjectDto {
        val currentLineNumber = document.getLineNumber(offset)
        return getLineObjectDto(currentLineNumber)
    }

    private fun getLineObjectDto(lineNumber: Int): LineObjectDto {
        val lineStartOffset = document.getLineStartOffset(lineNumber)
        val lineEndOffset = document.getLineEndOffset(lineNumber)
        val lineText = document.getText().substring(lineStartOffset, lineEndOffset)
        val lineObjectDto = LineObjectDto()
        lineObjectDto.lineNumber = lineNumber
        lineObjectDto.lineStartOffset = lineStartOffset
        lineObjectDto.lineEndOffset = lineEndOffset
        lineObjectDto.lineText = lineText
        return lineObjectDto
    }

}