package com.icthh.xm.demoplugin

import com.intellij.codeInsight.daemon.EmptyResolveMessageProvider
import com.intellij.find.actions.ShowUsagesAction
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.psi.*
import com.intellij.psi.ElementManipulators.getValueTextRange
import com.intellij.psi.PsiReferenceRegistrar.HIGHER_PRIORITY
import com.intellij.psi.impl.RenameableFakePsiElement
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
    PsiReferenceBase<PsiElement>(parent, getValueTextRange(parent), false),
    EmptyResolveMessageProvider {
    override fun resolve() = target?.let{ YAMLNamedPsiScalar(it) }
    override fun getUnresolvedMessagePattern(): String = "Entity with typeKey ${parent.text} not found"

    override fun getVariants(): Array<Any> {
        return getTypeKeys(element).map { it.valueText }.toTypedArray()
    }

    override fun getElement(): PsiElement {
        return parent
    }

    override fun isReferenceTo(element: PsiElement): Boolean {
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
    override fun getName(): String {
        return ElementManipulators.getValueText(parent)
    }

    override fun getTypeName() = "Target entity type key"

    override fun getIcon() = null

    override fun getNavigationElement(): PsiElement {
        return source
    }

    override fun canNavigate(): Boolean {
        return true
    }

    override fun isEquivalentTo(another: PsiElement?): Boolean {
        return equals(another) ||
                another != null && another is YAMLScalar && another.textValue == source.textValue
    }

    override fun setName(name: String): PsiElement {
        ElementManipulators.handleContentChange(source, name)
        return this
    }

    override fun navigate(requestFocus: Boolean) {
        val editor = FileEditorManager.getInstance(project).selectedTextEditor ?: return
        val popupPosition = JBPopupFactory.getInstance().guessBestPopupLocation(editor)

        ShowUsagesAction.startFindUsages(this, popupPosition, editor)
    }

}
