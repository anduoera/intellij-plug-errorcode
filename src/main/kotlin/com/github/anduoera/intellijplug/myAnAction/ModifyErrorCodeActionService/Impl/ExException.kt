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
import com.intellij.ui.SimpleListCellRenderer
import com.intellij.ui.components.JBList
import com.jetbrains.rd.util.string.println
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import java.util.*
import java.util.regex.Pattern
import javax.swing.DefaultListModel
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
            if (element.text.startsWith("exception.ErrorCode")&& element.reference?.resolve()==null&&element.text.split("+").size<2) {
                val errorCode=element.text.split(".")[1]
                errorCodeList.add( errorCode.replace(Regex("[^a-zA-Z].*"), ""))
            }else if(element.text.startsWith("exception.ErrorMessage")&& element.reference?.resolve()==null&&element.text.split("+").size<2){
                val errorMessage=element.text.split(".")[1]
                errorMessageStr.add( errorMessage.replace(Regex("[^a-zA-Z].*"), ""))
            }
            true
        }
        var toList: List<String> = errorCodeTreeSet[project.name]?.keys?.toList() ?: return
        val list = JBList(toList.reversed())
        val listModel = DefaultListModel<String>()
        toList.forEach { listModel.addElement(it) }
        list.model = listModel
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
                .setRenderer(SimpleListCellRenderer.create<String> { label, value, _ ->
                    label.text = value
                })
                .setAutoselectOnMouseMove(true)
                .setFilterAlwaysVisible(true)
                .createPopup()
        // 监听键盘事件实现过滤逻辑
        list.addKeyListener(object : KeyAdapter() {
            private var currentFilter = ""

            override fun keyTyped(e: KeyEvent) {
                // 获取输入的字符
                val inputChar = e.keyChar.toString()

                // 如果按下的是退格键（用于删除字符），则清空 currentFilter
                if (inputChar == KeyEvent.VK_BACK_SPACE.toChar().toString() && currentFilter.isNotEmpty()) {
                    currentFilter = currentFilter.dropLast(1) // 移除最后一个字符
                } else {
                    currentFilter += inputChar // 添加新输入的字符
                }

                // 根据 currentFilter 进行模糊匹配
                val filteredItems = if (currentFilter.isNotEmpty()) {
                    toList.filter { item ->
                        val regex = currentFilter.fold("", { acc, char -> "$acc.*?$char" }).toRegex(RegexOption.IGNORE_CASE)
                        regex.containsMatchIn(item)
                    }
                } else {
                    toList // 如果没有过滤条件，则返回所有项目
                }

                // 更新 listModel
                listModel.clear()
                filteredItems.forEach { listModel.addElement(it) }
            }
        })

        popup.showInBestPositionFor(e.dataContext)
    }
}
