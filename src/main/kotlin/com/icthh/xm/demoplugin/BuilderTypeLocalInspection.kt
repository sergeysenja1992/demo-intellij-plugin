package com.icthh.xm.demoplugin

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemHighlightType.ERROR
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import org.jetbrains.yaml.psi.YamlPsiElementVisitor

class BuilderTypeLocalInspection : LocalInspectionTool() {

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : YamlPsiElementVisitor() {
            val acceptableValue = setOf("NEW", "SEARCH")

            override fun visitElement(element: PsiElement) {
                if (getPattern("builderType").accepts(element) && !acceptableValue.contains(element.text)) {
                    val message = "${element.text} is invalid builder type"
                    holder.registerProblem(element, message, ERROR, ReplaceYamlValueFix(acceptableValue, element))
                }
            }
        }
    }
}

