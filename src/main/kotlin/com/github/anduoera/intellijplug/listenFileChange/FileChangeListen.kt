// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

package com.github.anduoera.intellijplug.listenFileChange


import com.github.anduoera.intellijplug.constants.FileChangeMapConstants
import com.goide.psi.GoFile
import com.goide.psi.GoPackageClause
import com.intellij.codeInspection.*
import com.intellij.psi.*

class FileChangeListen : LocalInspectionTool() {
    val fileChangeMap = FileChangeMapConstants.instance.getFileChangeMap()
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : PsiElementVisitor() {
            override fun visitFile(file: PsiFile) {
                if (file !is GoFile) return
                val packageClause: GoPackageClause? = file.getPackage()
                fileChangeMap[packageClause?.name == "exception"]?.toDo(file, holder)
            }
        }
    }

    override fun runForWholeFile(): Boolean {
        return true // 检查器应该应用于整个文件
    }

    override fun getDisplayName(): String {
        return "Duplicate ErrorCode Inspection"
    }

    override fun getShortName(): String {
        return "FileChangeListen"
    }
}
