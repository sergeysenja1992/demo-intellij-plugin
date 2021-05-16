package com.icthh.xm.demoplugin

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.project.Project
import com.intellij.psi.ElementManipulators
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
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
                    holder.registerProblem(element, "${element.text} is invalid builder type",
                        ReplaceYamlValueFix(acceptableValue, element))
                }
            }
        }
    }
}


class ReplaceYamlValueFix(val acceptableValues: Collection<String>, val element: PsiElement): LocalQuickFix {

    private val closesVariant = closesVariant()
    private fun closesVariant(): String? {
        val levenshtein = LevenshteinDistance()
        return acceptableValues.minByOrNull { levenshtein.apply(it, element.text) }

    }

    override fun getFamilyName() = "Change to ${closesVariant}"

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
        ElementManipulators.handleContentChange(element, closesVariant)
    }
}
