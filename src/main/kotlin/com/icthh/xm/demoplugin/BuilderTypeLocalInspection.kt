package com.icthh.xm.demoplugin

import com.intellij.codeInspection.*
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.util.parentOfType
import com.jetbrains.rd.util.first
import org.apache.commons.text.similarity.LevenshteinDistance
import org.jetbrains.yaml.YAMLElementGenerator
import org.jetbrains.yaml.psi.*

class BuilderTypeLocalInspection : LocalInspectionTool() {

    override fun buildVisitor(
        holder: ProblemsHolder,
        isOnTheFly: Boolean
    ): PsiElementVisitor {
        return object : YamlPsiElementVisitor() {
            val acceptableValue = setOf<String>("NEW", "BUILDER")

            override fun visitElement(element: PsiElement) {
                if (getPattern("builderType").accepts(element) && !acceptableValue.contains(element.text)) {

                    val levenshtein = LevenshteinDistance()
                    val variants = acceptableValue.map { it to levenshtein.apply(it, element.text) }.toMap()
                    val key = variants.minByOrNull { it.value }?.key ?: variants.first().key

                    holder.registerProblem(element, "${element.text} is invalid builder type",
                        ProblemHighlightType.ERROR, object : LocalQuickFix {

                        override fun getFamilyName() = "Change to ${key}"

                        override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
                            val elementGenerator = YAMLElementGenerator.getInstance(element.project)
                            val colorKeyValue = elementGenerator.createYamlKeyValue("key", key)
                            val value = colorKeyValue.value ?: return
                            val parent = element.parentOfType<YAMLKeyValue>() ?: return
                            parent.setValue(value)
                        }
                    })
                }
            }
        }
    }
}
