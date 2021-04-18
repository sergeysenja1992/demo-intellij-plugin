package com.icthh.xm.demoplugin

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemHighlightType.LIKE_UNKNOWN_SYMBOL
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiReference

class LinkReferencesInspection : LocalInspectionTool() {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : PsiElementVisitor() {
            override fun visitElement(element: PsiElement) {
                if (scalarPattern("typeKey").accepts(element)) {
                    val reference = element.reference ?: return
                    if (reference.resolve() == null) {
                        addQuickFix(element, reference)
                    }
                }
            }

            private fun addQuickFix(element: PsiElement, reference: PsiReference) {
                val message = ProblemsHolder.unresolvedReferenceMessage(reference)
                val acceptableValues = reference.variants.map { it.toString() }
                holder.registerProblemForReference(reference, LIKE_UNKNOWN_SYMBOL, message,
                    ReplaceYamlValueFix(acceptableValues, element))
            }
        }
    }
}
