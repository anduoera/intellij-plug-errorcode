package com.github.anduoera.intellijplugtest.Constants

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import java.io.File

class ErrorCodeMapConstants private constructor() {
companion object {
val instance: ErrorCodeMapConstants by lazy { ErrorCodeMapConstants() }
}

    private var errorCodeMap: MutableMap<String, MutableList<PsiElement>> = mutableMapOf()

    fun getErrorCodeMap(): MutableMap<String, MutableList<PsiElement>> {
        return errorCodeMap
    }

    fun refreshErrorCodeMap() = runBlocking {
        val newMap = mutableMapOf<String, MutableList<PsiElement>>()
        val projects = ProjectManager.getInstance().openProjects
        val deferredResults = projects.map { project ->
            async(Dispatchers.IO) { processProject(project) }
        }
        deferredResults.awaitAll().forEach { newMap.putAll(it) }
        errorCodeMap = newMap
    }

    private fun processProject(project: Project): MutableMap<String, MutableList<PsiElement>> {
        val map = mutableMapOf<String, MutableList<PsiElement>>()
        ApplicationManager.getApplication().runReadAction {
            val baseDir = project.basePath ?: return@runReadAction
            val exceptionDir = File("$baseDir/model/exception")
            if (exceptionDir.exists() && exceptionDir.isDirectory) {
                exceptionDir.walkTopDown().filter { it.isFile && it.extension == "go" }.forEach { file ->
                    val psiFile = convertFileToPsiFile(project, file)
                    psiFile?.let {
                        it.children.filter(::isVarSpec).forEach { element ->
                            element.children.forEach { child ->
                                val varName = getVarName(child)
                                if (varName != null && varName.startsWith("ErrorCode")) {
                                    val value = getLiteralValue(varName)
                                    value?.let { map.computeIfAbsent(it) { mutableListOf() }.add(child) }
                                }
                            }
                        }
                    }
                }
            }
        }
        return map
    }

    fun convertFileToPsiFile(project: Project?, file: File): PsiFile? {
        val virtualFile = LocalFileSystem.getInstance().findFileByIoFile(file)
        return virtualFile?.let { PsiManager.getInstance(project!!).findFile(it) }
    }

    private fun isVarSpec(element: PsiElement): Boolean {
        return element.node?.elementType?.toString() == "CONST_DECLARATION"
    }

    private fun getVarName(element: PsiElement): String? {
        return element.text
    }

    private fun getLiteralValue(varName: String): String? {
        return varName.split("=").getOrNull(1)?.replace("\"", "")
    }
}











// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

package com.github.anduoera.intellijplugtest.codeInspection


import com.github.anduoera.intellijplugtest.Constants.ErrorCodeMapConstants
import com.goide.psi.properties.GoFileProperties
import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer
import com.intellij.codeInspection.*
import com.intellij.openapi.project.ProjectManager
import com.intellij.psi.*
import com.intellij.psi.util.PsiTreeUtil

class CheckDuplicateErrorCodesInspection : LocalInspectionTool() {
override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
return object : PsiElementVisitor() {
var errorCodeMap = ErrorCodeMapConstants.instance.getErrorCodeMap()

            override fun visitFile(file: PsiFile) {

                if (file.fileType.name != "Go") return // 检查文件类型
                var fileName=file.name
                ErrorCodeMapConstants.instance.refreshErrorCodeMap()
                // 检查并报告重复的错误代码
                for ((value, elements) in errorCodeMap) {
                    if (elements.size > 1) {
                        for (element in elements) {
                            if(element.containingFile.name==fileName){
                                holder.registerProblem(
                                        element,
                                        "Duplicate ErrorCode: $value",
                                        ProblemHighlightType.GENERIC_ERROR_OR_WARNING
                                )
                            }
                        }
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
