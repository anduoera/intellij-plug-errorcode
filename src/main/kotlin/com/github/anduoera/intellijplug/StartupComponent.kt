package com.github.anduoera.intellijplug

import com.github.anduoera.intellijplug.constants.ErrorCodeTreeSetConstants
import com.github.anduoera.intellijplug.constants.ErrorCodeMapConstants
import com.github.anduoera.intellijplug.dto.ErrorCodeMapListDto
import com.goide.psi.GoConstDeclaration
import com.goide.psi.GoFile
import com.intellij.ide.AppLifecycleListener
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.project.ProjectManagerListener
import com.intellij.openapi.startup.ProjectActivity
import com.intellij.openapi.vcs.changes.shelf.ShelveChangesManager
import com.intellij.openapi.vcs.changes.shelf.ShelveChangesManager.PostStartupActivity
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.projectImport.ProjectOpenProcessor
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.psi.util.PsiTreeUtil
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * @Description: 描述
 * @author fanshq
 * @Date: 2024/07/13/5:37
 *
 */
class StartupComponent : ProjectActivity {

    private val errorCodeMap = ErrorCodeMapConstants.instance.getErrorCodeMap()
    private val errorCodeTreeSet = ErrorCodeTreeSetConstants.instance.getErrorCodeTreeSet()
    private val errorCodeBaseDir: String = "/model/exception"
    override suspend fun execute(project: Project) {
        errorCodeMap[project.name]?.clear()
        errorCodeTreeSet[project.name]?.clear()
        processProject(project.name)
    }

    private fun processProject(projectName:String) {
        val openProjects = ProjectManager.getInstance().openProjects
        for (openProject in openProjects) {
            if (openProject.name!=projectName||openProject.name.endsWith("foundation"))continue
            runBlocking{
                launch {
                    errorCodeMap[openProject.name] = ConcurrentHashMap(getCodeMap(openProject))
                }
            }
        }
    }

    private fun getCodeMap(project: Project): MutableMap<String, MutableList<ErrorCodeMapListDto>> {
        var map: MutableMap<String, MutableList<ErrorCodeMapListDto>> = mutableMapOf()
        ApplicationManager.getApplication().runReadAction {
            val psiFiles = findPsiFilesInDirectory(project, errorCodeBaseDir)
            map = getErrorCodeMapListMap(psiFiles)
        }
        return map;
    }

    private fun getErrorCodeMapListMap(psiFiles: List<PsiFile>): MutableMap<String, MutableList<ErrorCodeMapListDto>> {
        val map: MutableMap<String, MutableList<ErrorCodeMapListDto>> = mutableMapOf()
        val treeSetMap: MutableMap<String, TreeSet<Long>> = mutableMapOf()
        psiFiles.forEach { file ->
            val customSet = TreeSet<Long>(compareByDescending { it })
            val const = PsiTreeUtil.getChildOfType(file, GoConstDeclaration::class.java)
            const?.children?.iterator()?.forEach {
                if (it.children.size > 1 && it.firstChild.text.startsWith("ErrorCode")) {
                    val num = it.lastChild.text.replace("\"", "")
                    val list = ErrorCodeMapListDto()
                    list.project = file.project.name
                    list.file = file.name
                    list.psiElement = it
                    list.isText = num.toLongOrNull() == null
                    list.errorCodeName = it.firstChild.text
                    val numLong = num.toLongOrNull()
                    if (numLong != null) {
                        customSet.add(numLong)
                    }
                    num.let { value ->
                        map.computeIfAbsent(value) { mutableListOf() }.add(list)
                    }
                }
            }
            treeSetMap[file.name] = customSet
            errorCodeTreeSet[file.project.name] = treeSetMap
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