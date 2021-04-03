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
import com.jetbrains.rd.util.first
import org.apache.commons.text.similarity.LevenshteinDistance
import org.jetbrains.yaml.YAMLElementGenerator
import org.jetbrains.yaml.psi.YAMLKeyValue

class LinkReferencesInspection : LocalInspectionTool() {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : PsiElementVisitor() {
            override fun visitElement(element: PsiElement) {
                if (scalarPattern("typeKey").accepts(element)) {
                    val reference = element.reference ?: return
                    if (reference.resolve() == null) {

                        val levenshtein = LevenshteinDistance()
                        val variants = reference.variants.map { it.toString() to levenshtein.apply(it.toString(), element.text) }.toMap()
                        val key = variants.minByOrNull { it.value }?.key ?: variants.first().key

                        holder.registerProblemForReference(reference, LIKE_UNKNOWN_SYMBOL,
                            unresolvedReferenceMessage(reference),

                        object: LocalQuickFix {
                            override fun getFamilyName() = key

                            override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
                                val elementGenerator = YAMLElementGenerator.getInstance(element.project)
                                val value = elementGenerator.createYamlKeyValue("key", key).value ?: return
                                val parent = element.parentOfType<YAMLKeyValue>() ?: return
                                parent.setValue(value)
                            }
                        })
                    }
                }
            }
        }
    }
}
