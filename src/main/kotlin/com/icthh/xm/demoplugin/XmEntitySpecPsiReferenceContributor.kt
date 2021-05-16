import com.icthh.xm.demoplugin.scalarPattern
import com.intellij.psi.*
import com.intellij.psi.util.collectDescendantsOfType
import com.intellij.psi.util.parentOfType
import com.intellij.util.ProcessingContext
import org.jetbrains.yaml.psi.YAMLDocument
import org.jetbrains.yaml.psi.YAMLKeyValue
import org.jetbrains.yaml.psi.YAMLSequence


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

    override fun resolve() = findReferenceTarget(parent)

    private fun findReferenceTarget(element: PsiElement) =
        getTypeKeys(element).firstOrNull { it.valueText == element.text }?.value

    private fun getTypeKeys(element: PsiElement): List<YAMLKeyValue> {
        val yamlDocument = element.parentOfType<YAMLDocument>() ?: return emptyList()
        return yamlDocument.getChildrenOfType<YAMLSequence>().flatMap {
            it.getChildrenOfType<YAMLKeyValue>().filter { it.keyText == "key" }
        }.toList()
    }//3

    inline fun <reified T: PsiElement> PsiElement.getChildrenOfType() = this.collectDescendantsOfType<T>(canGoInside = {
        it !is T
    })

    override fun getVariants() = getTypeKeys(element).map { it.valueText }.toTypedArray()

}//1.2
