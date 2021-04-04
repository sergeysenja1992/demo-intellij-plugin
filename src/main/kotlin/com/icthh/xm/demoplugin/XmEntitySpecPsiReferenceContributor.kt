package com.icthh.xm.demoplugin

import com.intellij.codeInsight.daemon.EmptyResolveMessageProvider
import com.intellij.psi.*
import com.intellij.psi.PsiReferenceRegistrar.HIGHER_PRIORITY
import com.intellij.psi.impl.RenameableFakePsiElement
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.GlobalSearchScope.FilesScope
import com.intellij.psi.util.collectDescendantsOfType
import com.intellij.psi.util.parentOfType
import com.intellij.util.ProcessingContext
import org.jetbrains.yaml.psi.*


class XmEntitySpecPsiReferenceContributor: PsiReferenceContributor() {
    override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
        registrar.registerReferenceProvider(
            scalarPattern("typeKey"),
            getReferenceProvider(),
            HIGHER_PRIORITY
        )
        registrar.registerReferenceProvider(
            keyPattern(),
            object : PsiReferenceProvider() {
                override fun getReferencesByElement(element: PsiElement, context: ProcessingContext): Array<PsiReference> {
                    if (element !is YAMLScalar) {
                        return emptyArray()
                    }
                    return arrayOf(LinkTypeKeyReference(element, element, context))
                }
            },
            HIGHER_PRIORITY
        )
    }

    private fun getReferenceProvider() = object : PsiReferenceProvider() {
        override fun getReferencesByElement(element: PsiElement, context: ProcessingContext): Array<PsiReference> {
            val target = findReferenceTarget(element)
            if (target !is YAMLScalar? || element !is YAMLScalar) {
                return emptyArray()
            }
            return arrayOf(LinkTypeKeyReference(target, element, context))
        }
    }
}

class LinkTypeKeyReference(val target: YAMLScalar?, val parent: YAMLScalar, val context: ProcessingContext):
    PsiReferenceBase<PsiElement>(parent, false),
    EmptyResolveMessageProvider {
    override fun resolve() = target?.let{ YAMLNamedPsiScalar(it) }
    override fun getUnresolvedMessagePattern(): String = "Entity with typeKey ${parent.text} not found"

    override fun getVariants(): Array<Any> {
        return getTypeKeys(element).map { it.valueText }.toTypedArray()
    }

    override fun isReferenceTo(element: PsiElement): Boolean {
        if (element is YAMLNamedPsiScalar) {
            return element.source == target
        }
        return element == target
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

class YAMLNamedPsiScalar(val source: YAMLScalar): RenameableFakePsiElement(source) {

    val file = source.containingFile.virtualFile

    override fun getName(): String {
        return ElementManipulators.getValueText(source)
    }

    override fun getTypeName() = "Target entity type key"

    override fun getIcon() = null

    override fun getNavigationElement(): PsiElement {
        return source
    }

    override fun canNavigate(): Boolean {
        return true
    }

    override fun setName(name: String): PsiElement {
        ElementManipulators.handleContentChange(source, name)
        return this
    }

    override fun getResolveScope(): GlobalSearchScope {
        return FilesScope.filesScope(project, listOf(file))
    }

    override fun getUseScope(): GlobalSearchScope {
        return FilesScope.filesScope(project, listOf(file))
    }

}

