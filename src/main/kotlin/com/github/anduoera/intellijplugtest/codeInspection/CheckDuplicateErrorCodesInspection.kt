// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

package com.github.anduoera.intellijplugtest.codeInspection


import com.github.anduoera.intellijplugtest.Constants.ErrorCodeMapConstants
import com.goide.psi.GoFile
import com.goide.psi.GoPackageClause
import com.goide.psi.properties.GoFileProperties
import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer
import com.intellij.codeInspection.*
import com.intellij.openapi.project.ProjectManager
import com.intellij.psi.*
import com.intellij.psi.util.PsiTreeUtil

class CheckDuplicateErrorCodesInspection : LocalInspectionTool() {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        val map = ErrorCodeMapConstants.instance.getErrorCodeMap()
        return object : PsiElementVisitor() {
            override fun visitFile(file: PsiFile) {
                if (file !is GoFile) return
                val packageClause: GoPackageClause? = file.getPackage()
                if (packageClause?.name != "exception") return

                try {
                    ErrorCodeMapConstants.instance.refreshErrorCodeMap(file)
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                val projectMapLists = map[file.project.name]
                projectMapLists?.forEach { key, value ->
                    val errorCodeList = value
                    if (errorCodeList.size > 1) {
                        errorCodeList.forEach { errorCode ->
                            if (errorCode.file == file.name) {
                                holder.registerProblem(
                                        errorCode.psiElement,
                                        "Duplicate ErrorCode: ${key}",
                                        ProblemHighlightType.GENERIC_ERROR_OR_WARNING
                                )
                            }
                        }
                    } else if (value[0].isText) {
                        holder.registerProblem(
                                value[0].psiElement,
                                "Non-numeric ErrorCode: ${key}",
                                ProblemHighlightType.GENERIC_ERROR_OR_WARNING
                        )
                    }
                }
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
        return "CheckDuplicateErrorCodes"
    }
}
