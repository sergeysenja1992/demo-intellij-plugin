package com.icthh.xm.demoplugin

import com.intellij.codeInsight.daemon.EmptyResolveMessageProvider
import com.intellij.psi.*
import com.intellij.psi.PsiReferenceRegistrar.HIGHER_PRIORITY
import com.intellij.psi.impl.RenameableFakePsiElement
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.GlobalSearchScope.FilesScope
import com.intellij.psi.util.descendants
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
        registrar.registerReferenceProvider(
            keyPattern(),
            object : PsiReferenceProvider() {
                override fun getReferencesByElement(element: PsiElement, context: ProcessingContext): Array<PsiReference> {
                    return arrayOf(LinkTypeKeyReference(element, context))
                }
            },
            HIGHER_PRIORITY
        )
    }

    private fun getReferenceProvider() = object : PsiReferenceProvider() {
        override fun getReferencesByElement(element: PsiElement, context: ProcessingContext): Array<PsiReference> {
            return arrayOf(LinkTypeKeyReference(element, context))
        }
    }
}

class LinkTypeKeyReference(val parent: PsiElement, val context: ProcessingContext):
    PsiReferenceBase<PsiElement>(parent, false),
    EmptyResolveMessageProvider {

    private fun findReferenceTarget(element: PsiElement): YAMLValue? {
        return getTypeKeys(element).firstOrNull { it.valueText == element.text }?.value
    }

    override fun resolve() = findReferenceTarget(parent)?.let{ YAMLNamedPsiScalar(it) }
    override fun getUnresolvedMessagePattern(): String = "Entity with typeKey ${parent.text} not found"

    override fun getVariants(): Array<Any> {
        return getTypeKeys(element).map { it.valueText }.toTypedArray()
    }

    override fun isReferenceTo(element: PsiElement): Boolean {
        val target = findReferenceTarget(parent)
        if (element is YAMLNamedPsiScalar) {
            return element.source == target
        }
        return element == target
    }
}


private fun getTypeKeys(element: PsiElement): List<YAMLKeyValue> {
    val yamlDocument = element.parentOfType<YAMLDocument>() ?: return emptyList()
    return yamlDocument.getChildrenOfType<YAMLSequence>().flatMap {
        it.getChildrenOfType<YAMLKeyValue>().filter { it.keyText == "key" }
    }.toList()
}

inline fun <reified T: PsiElement> PsiElement.getChildrenOfType() = this.descendants(canGoInside = {
    it !is T
}).filterIsInstance<T>()

class YAMLNamedPsiScalar(val source: YAMLValue): RenameableFakePsiElement(source) {

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

