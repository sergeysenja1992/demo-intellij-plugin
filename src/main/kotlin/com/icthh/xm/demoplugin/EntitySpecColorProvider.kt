package com.icthh.xm.demoplugin

import com.intellij.openapi.editor.ElementColorProvider
import com.intellij.psi.PsiElement
import org.jetbrains.yaml.YAMLElementGenerator
import org.jetbrains.yaml.psi.YAMLKeyValue
import java.awt.Color

class EntitySpecColorProvider: ElementColorProvider {
    override fun getColorFrom(element: PsiElement): Color? {
        if (element is YAMLKeyValue && element.keyText.equals("color")) {
            try {
                return Color.decode(element.valueText.toUpperCase())
            } catch (e: NumberFormatException) {
                return null
            }
        }
        return null
    }

    override fun setColorTo(element: PsiElement, color: Color) {
        if (element is YAMLKeyValue && element.keyText.equals("color")) {
            val hex = color.let { String.format("#%02x%02x%02x", it.red, it.green, it.blue) }

            val elementGenerator = YAMLElementGenerator.getInstance(element.project)
            val colorKeyValue = elementGenerator.createYamlKeyValue("color", "\"${hex}\"")
            val value = colorKeyValue.value ?: return
            element.setValue(value)
        }
    }
}
