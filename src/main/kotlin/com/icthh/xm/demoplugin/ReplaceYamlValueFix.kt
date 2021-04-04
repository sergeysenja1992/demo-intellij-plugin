package com.icthh.xm.demoplugin

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.project.Project
import com.intellij.psi.ElementManipulators
import com.intellij.psi.PsiElement
import org.apache.commons.text.similarity.LevenshteinDistance

class ReplaceYamlValueFix(val acceptableValues: Collection<String>, val element: PsiElement) : LocalQuickFix {

    private val closestVariant = closestVariant()

    private fun closestVariant(): String {
        val levenshtein = LevenshteinDistance()
        return acceptableValues.minByOrNull { levenshtein.apply(it, element.text) } ?: acceptableValues.first()
    }

    override fun getFamilyName() = "Change to ${closestVariant}"

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
        ElementManipulators.handleContentChange(element, closestVariant)
    }
}
