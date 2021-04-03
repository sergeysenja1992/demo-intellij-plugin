package com.icthh.xm.demoplugin

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.util.ProcessingContext

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

