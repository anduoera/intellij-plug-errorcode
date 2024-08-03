package com.github.anduoera.intellijplug.myAnAction.ModifyErrorCodeActionService.Impl

import com.fasterxml.jackson.jr.ob.JSON
import com.github.anduoera.intellijplug.constants.ErrorCodeTreeSetConstants
import com.github.anduoera.intellijplug.myAnAction.ModifyErrorCodeActionService.ModifyErrorCodeAction
import com.github.anduoera.intellijplug.utils.ReplaceErrorCodeUtils
import com.goide.inspections.vet.GoErrorsAsInspection
import com.goide.psi.GoFile
import com.goide.psi.GoImportSpec
import com.goide.psi.GoResolveCache
import com.intellij.codeHighlighting.HighlightDisplayLevel
import com.intellij.codeInsight.codeVision.lensContext
import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer
import com.intellij.codeInsight.daemon.impl.HighlightInfo
import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.ex.LocalInspectionToolWrapper
import com.intellij.ide.impl.ProjectUtil
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.*
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.ui.components.JBList
import com.jetbrains.rd.util.string.println
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashSet
import kotlin.io.path.name

/**
 * @Description: 描述
 * @author fanshq
 * @Date: 2024/07/17/23:15
 *
 */
class ExException : ModifyErrorCodeAction {

    private val errorCodeTreeSet = ErrorCodeTreeSetConstants.instance.getErrorCodeTreeSet()
    private val errorCodeBaseDir: String = "/model/exception/"
    override fun toDo(project: Project, file: GoFile, e: AnActionEvent) {
        val errorCodeList:HashSet<String> = hashSetOf()
        val errorMessageStr:HashSet<String> = hashSetOf()
        PsiTreeUtil.processElements(file) { element ->
            if (element.text.startsWith("exception.ErrorCode")&& element.reference?.resolve()==null) {
                errorCodeList.add(element.text.split(".")[1])
            }else if(element.text.startsWith("exception.ErrorMessage")&& element.reference?.resolve()==null){
                errorMessageStr.add(element.text.split(".")[1])
            }
            true
        }
        var toList: List<String> = errorCodeTreeSet[project.name]?.keys?.toList() ?: return
        val list = JBList(toList.reversed())
        val popup = JBPopupFactory.getInstance()
                .createListPopupBuilder(list)
                .setTitle("Choose an ErrorCode File")
                .setItemChoosenCallback {
                    val chooseFile=project.baseDir.findFileByRelativePath(errorCodeBaseDir+list.selectedValue)
                    val chooseGoFile= chooseFile?.let { PsiManager.getInstance(project).findFile(it) }
                    if (chooseGoFile != null && chooseGoFile is GoFile){
                        ReplaceErrorCodeUtils(chooseGoFile,project).ExException(errorCodeList,errorMessageStr)
                    }
                }
                .createPopup()
        popup.showInBestPositionFor(e.dataContext)
    }
}
