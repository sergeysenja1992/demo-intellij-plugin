package com.icthh.xm.demoplugin

import com.intellij.codeInsight.daemon.EmptyResolveMessageProvider
import com.intellij.psi.*
import com.intellij.psi.ElementManipulators.getValueTextRange
import com.intellij.psi.PsiReferenceRegistrar.HIGHER_PRIORITY
import com.intellij.psi.util.collectDescendantsOfType
import com.intellij.psi.util.parentOfType
import com.intellij.util.ProcessingContext
import org.jetbrains.yaml.psi.YAMLDocument
import org.jetbrains.yaml.psi.YAMLKeyValue
import org.jetbrains.yaml.psi.YAMLSequence
import org.jetbrains.yaml.psi.YAMLValue

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

    override fun getVariants(): Array<Any> {
        return getTypeKeys(element).map { it.valueText }.toTypedArray()
    }
}

private fun findReferenceTarget(element: PsiElement): YAMLValue? {
    val target = getTypeKeys(element).firstOrNull { it.valueText == element.text }?.value
    return target
}

private fun getTypeKeys(element: PsiElement): List<YAMLKeyValue> {
    val yamlDocument = element.parentOfType<YAMLDocument>() ?: return emptyList()
    return yamlDocument.getChildrenOfType<YAMLSequence>().flatMap {
        it.getChildrenOfType<YAMLKeyValue>().filter { it.keyText == "key" }
    }
}

inline fun <reified T: PsiElement> PsiElement.getChildrenOfType() = this.collectDescendantsOfType<T>(canGoInside = {
    it !is T
})
