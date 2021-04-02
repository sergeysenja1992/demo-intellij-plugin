package com.icthh.xm.demoplugin

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType.LIKE_UNKNOWN_SYMBOL
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.codeInspection.ProblemsHolder.unresolvedReferenceMessage
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.util.parentOfType
import org.jetbrains.yaml.YAMLElementGenerator
import org.jetbrains.yaml.psi.YAMLKeyValue

class LinkReferencesInspection : LocalInspectionTool() {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : PsiElementVisitor() {
            override fun visitElement(element: PsiElement) {
                if (scalarPattern("typeKey").accepts(element)) {
                    val reference = element.reference ?: return
                    if (reference.resolve() == null) {
                        holder.registerProblemForReference(reference, LIKE_UNKNOWN_SYMBOL,
                            unresolvedReferenceMessage(reference))
                    }
                }
            }
        }
    }
}
