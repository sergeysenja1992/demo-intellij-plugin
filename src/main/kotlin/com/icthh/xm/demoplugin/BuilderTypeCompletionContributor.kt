package com.icthh.xm.demoplugin

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.patterns.PlatformPatterns
import com.intellij.patterns.PlatformPatterns.psiElement
import com.intellij.patterns.PsiElementPattern
import com.intellij.psi.PsiElement
import com.intellij.util.ProcessingContext
import org.jetbrains.yaml.psi.*

class BuilderTypeCompletionContributor : CompletionContributor() {

    init {
        extend(CompletionType.BASIC, getPattern("builderType"), toResult(listOf("NEW", "SEARCH")))
    }


    private fun toResult(variants: List<String>) = object : CompletionProvider<CompletionParameters>() {
        override fun addCompletions(
            params: CompletionParameters,
            context: ProcessingContext,
            result: CompletionResultSet
        ) {
            result.addAllElements(variants.map { LookupElementBuilder.create(it) })
            result.stopHere()
        }
    }

}

fun getPattern(linksFieldName: String): PsiElementPattern.Capture<PsiElement> {
    return PlatformPatterns.psiElement().withParent(
        scalarPattern(linksFieldName)
    )
}

fun scalarPattern(linksFieldName: String) = psiElement<YAMLScalar>().withParent(
    psiElement<YAMLKeyValue>().withName(linksFieldName).withParent(
        psiElement<YAMLMapping>().withParent(
            psiElement<YAMLSequenceItem>().withParent(
                psiElement<YAMLSequence>().withParent(
                    psiElement<YAMLKeyValue>().withName("links")
                )
            )
        )
    )
)

inline fun <reified T : PsiElement> psiElement() = psiElement(T::class.java)
