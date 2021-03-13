package com.icthh.xm.demoplugin

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.patterns.PlatformPatterns
import com.intellij.patterns.PlatformPatterns.psiElement
import com.intellij.psi.PsiElement
import com.intellij.util.ProcessingContext
import org.jetbrains.yaml.psi.*
import org.jetbrains.yaml.psi.impl.YAMLSequenceImpl

class BuilderTypeCompletionContributor: CompletionContributor() {

    init {
        extend(
            CompletionType.BASIC,
            PlatformPatterns.psiElement().withParent(
                psiElement<YAMLScalar>().withParent(
                    psiElement<YAMLKeyValue>().withName("builderType").withParent(
                        psiElement<YAMLMapping>().withParent(
                            psiElement<YAMLSequenceItem>().withParent(
                                psiElement<YAMLSequence>().withParent(
                                    psiElement<YAMLKeyValue>().withName("links")
                                )
                            )
                        )
                    )
                )
            ),
            toResult(listOf("NEW", "SEARCH"))
        )
    }

    private fun toResult(variants: List<String>) = object : CompletionProvider<CompletionParameters>() {
        override fun addCompletions(
            parameters: CompletionParameters,
            context: ProcessingContext,
            result: CompletionResultSet
        ) {
            result.addAllElements(variants.map { LookupElementBuilder.create(it) })
            result.stopHere()
        }
    }

    inline fun <reified T: PsiElement> psiElement() = psiElement(T::class.java)

}
