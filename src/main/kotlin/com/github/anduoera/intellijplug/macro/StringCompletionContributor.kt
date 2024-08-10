package com.github.anduoera.intellijplug.macro

import com.github.anduoera.intellijplug.mySettings.MySettings
import com.goide.psi.GoFile
import com.goide.psi.GoMethodDeclaration
import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.*
import com.intellij.openapi.project.Project
import com.intellij.psi.filters.ElementFilter
import com.intellij.util.ProcessingContext
import com.intellij.util.Processor
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiNamedElement
import com.intellij.psi.util.PsiTreeUtil

class StringCompletionContributor : CompletionContributor() {
    val settings = MySettings.getInstance().state
    init {
        extend(CompletionType.BASIC, // 根据需要选择合适的 CompletionType
            PlatformPatterns.psiElement(PsiElement::class.java), // 根据上下文选择合适的 PsiElement
            object : CompletionProvider<CompletionParameters>() {
                    override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
                        val position = parameters.position
                        val psiFile: PsiFile = position.containingFile
                        val structName=getStructNameFromMethod(psiFile)
                        if ((psiFile !is GoFile) || (psiFile.getPackage()?.name == "exception")|| structName?.isEmpty() != false) {
                            return
                        }
                        val strings = getStringList(parameters) // 获取你要提示的字符串集合
                        for (string in strings) {

                            val errorMessage=string.replace("ErrorCode","ErrorMessage")
                            val str="\t\t$structName.throw.ErrorCode = exception.$string\n" +
                                    "\t\t$structName.throw.ErrorMessage = exception.$errorMessage\n"

                            val lookupElement = LookupElementBuilder.create(str)
                                    .withPresentableText(string)
                                    .withLookupString(string)
                                    .withInsertHandler(object : InsertHandler<LookupElement>{
                                        override fun handleInsert(context: InsertionContext, item: LookupElement) {
                                            val editor = context.editor
                                            val document = editor.document
                                            val offset = context.tailOffset

                                            // 替换之前输入的内容
                                            val replacement = item.lookupString
                                            document.replaceString(context.startOffset, context.tailOffset, replacement)

                                            // 移动光标到替换内容的末尾
                                            editor.caretModel.moveToOffset(context.startOffset + replacement.length)
                                        }
                                    }) // 使用自定义插入处理器
                                    .withTypeText("error code")
                                    .bold()
                            result.addElement(lookupElement)
                            result.withRelevanceSorter(object : CompletionSorter() {
                                override fun weighBefore(beforeId: String, vararg weighers: LookupElementWeigher?): CompletionSorter {
                                    TODO("Not yet implemented")
                                }

                                override fun weighAfter(afterId: String, vararg weighers: LookupElementWeigher?): CompletionSorter {
                                    TODO("Not yet implemented")
                                }

                                override fun weigh(weigher: LookupElementWeigher?): CompletionSorter {
                                    val myWeigher = object : LookupElementWeigher("myWeigher") {
                                        override fun weigh(element: LookupElement): Comparable<*> {
                                            // 自定义权重计算逻辑
                                            return if (element.lookupString.startsWith("ErrorCode",true)) 1 else 0
                                        }
                                    }
                                    return weigh(myWeigher)
                                }

                            })
                        }
                    }
            })

    }

    private fun getStringList(parameters: CompletionParameters): List<String> {
        val position = parameters.position
        val psiFile: PsiFile = position.containingFile
        val elementAtCursor = psiFile.findElementAt(position.textOffset)
        val methodName = elementAtCursor?.let { getCurrentMethodName(it) }
        val fileName=psiFile.name
       val errorCode: MutableList<String> = mutableListOf()
        for (it in settings.exampleSettings) {
            var str=it
            if (str.contains("`method`")) {
                if (methodName==null)continue
                str=str.replace("`method`",convertToCamelCase(methodName))
            }
            if (str.contains("`file`")){
                if (fileName=="")continue
                str=str.replace("`file`",convertToCamelCase(fileName))
            }
            errorCode.add(str)
        }

        return errorCode
    }

    private fun getCurrentMethodName(element: PsiElement): String {
        val parent = PsiTreeUtil.getParentOfType(element, PsiNamedElement::class.java)
        return parent?.name?:""
    }

    fun convertToCamelCase(fileName: String): String {
        // 去掉文件扩展名
        val baseName = fileName.substringBeforeLast(".")

        // 将下划线分隔的部分转成驼峰命名
        val parts = baseName.split('_')
        val camelCaseString = parts.joinToString("") { part ->
            part.capitalize() // 首字母大写
        }

        return camelCaseString
    }


    fun getStructNameFromMethod(psiFile: PsiElement): String? {
        if (psiFile is GoFile) {
            val methods = PsiTreeUtil.findChildrenOfType(psiFile, GoMethodDeclaration::class.java)
            methods.forEach { method ->
                val receiver = method.receiver
                if (receiver != null) {
                    // 结构体类型的简写在 receiver 的类型中
                    return receiver.name?.trim() ?: "Unknown"
                }
            }
        }
        return null
    }
}
