package com.github.anduoera.intellijplug.dto

import com.intellij.psi.PsiElement

class ErrorCodeMapListDto{
     lateinit var psiElement: PsiElement
     var file:String=""
     var project:String=""
     var isText:Boolean=false
     var errorCodeName=""
}