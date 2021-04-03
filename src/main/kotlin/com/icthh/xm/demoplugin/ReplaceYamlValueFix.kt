package com.icthh.xm.demoplugin

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.util.parentOfType
import com.jetbrains.rd.util.first
import org.apache.commons.text.similarity.LevenshteinDistance
import org.jetbrains.yaml.psi.YAMLKeyValue

class ReplaceYamlValueFix(val acceptableValues: Collection<String>, val element: PsiElement) : LocalQuickFix {

    private val closestVariant = closestVariant()

    private fun closestVariant(): String {
        val levenshtein = LevenshteinDistance()
        val variants = acceptableValues.map { it to levenshtein.apply(it, element.text) }.toMap()
        return variants.minByOrNull { it.value }?.key ?: variants.first().key
    }

    override fun getFamilyName() = "Change to ${closestVariant}"

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
        val parent = element.parentOfType<YAMLKeyValue>() ?: return
        replaceValue(parent, closestVariant)
    }
}
