package com.icthh.xm.demoplugin

import com.intellij.psi.*
import com.intellij.psi.impl.RenameableFakePsiElement
import com.intellij.psi.util.descendants
import com.intellij.psi.util.parentOfType
import com.intellij.util.ProcessingContext
import org.jetbrains.yaml.psi.YAMLDocument
import org.jetbrains.yaml.psi.YAMLKeyValue
import org.jetbrains.yaml.psi.YAMLSequence
import org.jetbrains.yaml.psi.YAMLValue

class XmEntitySpecPsiReferenceContributor: PsiReferenceContributor() {
    override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
        registrar.registerReferenceProvider(scalarPattern("typeKey"), getReferenceProvider())
    }//1

    private fun getReferenceProvider() = object : PsiReferenceProvider() {
        override fun getReferencesByElement(element: PsiElement, context: ProcessingContext) =
            arrayOf(LinkTypeKeyReference(element))
    }//2
}//1.1

class LinkTypeKeyReference(val parent: PsiElement): PsiReferenceBase<PsiElement>(parent, false) {

    override fun resolve() = findReferenceTarget(parent)?.let{ YAMLNamedPsiScalar(it) }

    private fun findReferenceTarget(element: PsiElement) =
        getTypeKeys(element).firstOrNull { it.valueText == element.text }?.value

    private fun getTypeKeys(element: PsiElement): List<YAMLKeyValue> {
        val yamlDocument = element.parentOfType<YAMLDocument>() ?: return emptyList()
        return yamlDocument.getChildrenOfType<YAMLSequence>().flatMap {
            it.getChildrenOfType<YAMLKeyValue>().filter { it.keyText == "key" }
        }.toList()
    }//3

    private inline fun <reified T: PsiElement> PsiElement.getChildrenOfType() =
        this.descendants(canGoInside = { it !is T }).filterIsInstance<T>()

    override fun getVariants() = getTypeKeys(element).map { it.valueText }.toTypedArray()

}//1.2

class YAMLNamedPsiScalar(val source: YAMLValue): RenameableFakePsiElement(source) {

    val file = source.containingFile.virtualFile

    override fun getName() = ElementManipulators.getValueText(source)

    override fun setName(name: String) = ElementManipulators.handleContentChange(source, name)

    override fun getNavigationElement() = source

    override fun getTypeName() = "Target entity type key"

    override fun getIcon() = null

}//1.3
