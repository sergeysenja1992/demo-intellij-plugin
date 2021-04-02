package com.icthh.xm.demoplugin

import com.intellij.codeInsight.daemon.EmptyResolveMessageProvider
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.lang.Language
import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.lang.injection.MultiHostInjector
import com.intellij.lang.injection.MultiHostRegistrar
import com.intellij.model.SymbolResolveResult
import com.intellij.model.psi.ImplicitReferenceProvider
import com.intellij.openapi.util.TextRange
import com.intellij.psi.*
import com.intellij.psi.ElementManipulators.getValueTextRange
import com.intellij.psi.PsiLanguageInjectionHost.Shred
import com.intellij.psi.PsiReferenceRegistrar.HIGHER_PRIORITY
import com.intellij.psi.impl.source.tree.injected.InjectedReferenceVisitor
import com.intellij.psi.injection.ReferenceInjector
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.collectDescendantsOfType
import com.intellij.psi.util.parentOfType
import com.intellij.util.ArrayUtil
import com.intellij.util.ProcessingContext
import org.jetbrains.annotations.Nullable
import org.jetbrains.yaml.psi.*
import org.jetbrains.yaml.psi.impl.YAMLScalarImpl
import java.lang.Boolean
import java.util.*

class XmEntitySpecPsiReferenceContributor: PsiReferenceContributor() {
    override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
        registrar.registerReferenceProvider(
            scalarPattern("typeKey"),
            getReferenceProvider(),
            HIGHER_PRIORITY
        )
    }

    private fun getReferenceProvider() = object : PsiReferenceProvider() {
        override fun getReferencesByElement(element: PsiElement, context: ProcessingContext): Array<PsiReference> {
            val target = findReferenceTarget(element)
            return arrayOf(LinkTypeKeyReference(target, element, context))
        }
    }
}

class LinkTypeKeyReference(val target: PsiElement?, val parent: PsiElement, val context: ProcessingContext):
    PsiReferenceBase<PsiElement>(parent, getValueTextRange(parent), false),
    EmptyResolveMessageProvider {
    override fun resolve() = target
    override fun getUnresolvedMessagePattern(): String = "Entity with typeKey ${parent.text} not found"
}

private fun findReferenceTarget(element: PsiElement): YAMLValue? {
    val yamlDocument = element.parentOfType<YAMLDocument>() ?: return null
    val target = yamlDocument.getChildrenOfType<YAMLSequence>().flatMap {
        it.getChildrenOfType<YAMLKeyValue>().filter { it.keyText == "key" }
    }.firstOrNull { it.valueText == element.text }?.value
    return target
}

inline fun <reified T: PsiElement> PsiElement.getChildrenOfType() = this.collectDescendantsOfType<T>(canGoInside = {
    it !is T
})
