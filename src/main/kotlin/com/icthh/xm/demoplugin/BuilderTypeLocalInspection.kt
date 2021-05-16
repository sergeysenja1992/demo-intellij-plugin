package com.icthh.xm.demoplugin

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.jetbrains.rd.util.first
import org.apache.commons.text.similarity.LevenshteinDistance
import org.jetbrains.yaml.psi.YamlPsiElementVisitor

class BuilderTypeLocalInspection : LocalInspectionTool() {

    override fun buildVisitor(
        holder: ProblemsHolder,
        isOnTheFly: Boolean
    ): PsiElementVisitor {
        return object : YamlPsiElementVisitor() {
            val acceptableValue = setOf<String>("NEW", "SEARCH")

            override fun visitElement(element: PsiElement) {
                if (getPattern().accepts(element) && !acceptableValue.contains(element.text)) {

                    val levenshtein = LevenshteinDistance()
                    val variants = acceptableValue.map { it to levenshtein.apply(it, element.text) }.toMap()
                    val key = variants.minByOrNull { it.value }?.key ?: variants.first().key

                    holder.registerProblem(element, "${element.text} is invalid builder type")
                }
            }
        }
    }
}
