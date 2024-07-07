package com.github.anduoera.intellijplugtest.Constants

import ErrorCodeMapList
import com.goide.psi.GoConstDeclaration
import com.goide.psi.GoFile
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.psi.util.PsiTreeUtil
import kotlinx.coroutines.*

class ErrorCodeMapConstants private constructor() {
    companion object {
        val instance: ErrorCodeMapConstants by lazy { ErrorCodeMapConstants() }
    }

    private val errorCodeBaseDir: String = "/model/exception"
    private val errorCodeMap: MutableMap<String, MutableMap<String, MutableList<ErrorCodeMapList>>> = mutableMapOf()



    fun getErrorCodeMap(): MutableMap<String, MutableMap<String, MutableList<ErrorCodeMapList>>> {
        errorCodeMap.clear()
        processProject()
        return errorCodeMap
    }


    fun refreshErrorCodeMap(file: PsiFile) {
        val projectMap = errorCodeMap[file.project.name] ?: mutableMapOf()
        errorCodeMap[file.project.name] = projectMap

        // 使用 Iterator 安全地遍历和修改 projectMap
        val iterator = projectMap.entries.iterator()
        while (iterator.hasNext()) {
            val entry = iterator.next()
            entry.value.removeIf { it.file == file.name }
            if (entry.value.isEmpty()) {
                iterator.remove()
            }
        }

        // Update map with new data from the file
        updateErrorCodeMapForFile(file, projectMap)
    }

    private fun updateErrorCodeMapForFile(file: PsiFile, projectMap: MutableMap<String, MutableList<ErrorCodeMapList>>) {
        val constDeclarations = PsiTreeUtil.findChildrenOfType(file, GoConstDeclaration::class.java)
        constDeclarations.forEach { constDecl ->
            constDecl.children.forEach {
                if (it.children.size > 1 && it.firstChild.text.startsWith("ErrorCode")) {
                    val num = it.lastChild.text.replace("\"", "")
                    val list = ErrorCodeMapList().apply {
                        this.project = file.project.name
                        this.file = file.name
                        this.psiElement = it
                        this.isText = num.toLongOrNull() == null
                    }
                    projectMap.computeIfAbsent(num) { mutableListOf() }.add(list)
                }
            }
        }
    }

    private fun processProject() {
        val openProjects = ProjectManager.getInstance().openProjects
        openProjects.map {
            runBlocking {
                launch {
                    errorCodeMap[it.name] = getCodeMap(it)
                }
            }
        }
    }

    private fun getCodeMap(project: Project): MutableMap<String, MutableList<ErrorCodeMapList>> {
        var map: MutableMap<String, MutableList<ErrorCodeMapList>> = mutableMapOf()
        ApplicationManager.getApplication().runReadAction {
            val psiFiles = findPsiFilesInDirectory(project, errorCodeBaseDir)
            map = getErrorCodeMapListMap(psiFiles)
        }
        return map;
    }

    private fun getErrorCodeMapListMap(psiFiles: List<PsiFile>): MutableMap<String, MutableList<ErrorCodeMapList>> {
        val map: MutableMap<String, MutableList<ErrorCodeMapList>> = mutableMapOf()
        psiFiles.forEach { file ->
            var const = PsiTreeUtil.getChildOfType(file, GoConstDeclaration::class.java)
            const?.children?.iterator()?.forEach {
                if (it.children.size > 1 && it.firstChild.text.startsWith("ErrorCode")) {
                    val num = it.lastChild.text.replace("\"", "")
                    val list = ErrorCodeMapList()
                    list.project = file.project.name
                    list.file = file.name
                    list.psiElement = it
                    list.isText = num.toLongOrNull() == null
                    num.let { value->
                        map.computeIfAbsent(value) { mutableListOf() }.add(list)
                    }
                }
            }
        }
        return map
    }


    private fun findPsiFilesInDirectory(project: Project, relativePath: String): List<PsiFile> {
        val baseDir = project.baseDir
        val targetDir = baseDir.findFileByRelativePath(relativePath) ?: return emptyList()

        val psiManager = PsiManager.getInstance(project)
        val psiFileList = mutableListOf<PsiFile>()
        findPsiFilesRecursively(psiManager, targetDir, psiFileList)
        return psiFileList
    }

    private fun findPsiFilesRecursively(psiManager: PsiManager, dir: VirtualFile, psiFileList: MutableList<PsiFile>) {
        if (dir.isDirectory) {
            dir.children.forEach { child ->
                if (child.isDirectory) {
                    findPsiFilesRecursively(psiManager, child, psiFileList)
                } else {
                    val psiFile = psiManager.findFile(child)
                    if (psiFile != null && psiFile is GoFile) {
                        psiFileList.add(psiFile)
                    }
                }
            }
        }
    }
}

