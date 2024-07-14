package com.github.anduoera.intellijplug.listenFileChange.service

import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiFile

/**
 * @Description: 描述
 * @author fanshq
 * @Date: 2024/07/13/5:57
 *
 */
interface FileChange {
    fun toDo(file: PsiFile,holder: ProblemsHolder)
}