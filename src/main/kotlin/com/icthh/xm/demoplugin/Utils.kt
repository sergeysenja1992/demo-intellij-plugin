package com.icthh.xm.demoplugin

import com.intellij.openapi.vfs.VirtualFile
import com.intellij.patterns.PlatformPatterns
import com.intellij.patterns.PsiElementPattern
import com.intellij.psi.PsiElement
import org.jetbrains.yaml.psi.*

fun VirtualFile.isEntitySpec() = path.endsWith("/config/tenants/DEMO/entity/xmentityspec.yml")

fun getPattern(linksFieldName: String): PsiElementPattern.Capture<PsiElement> {
    return PlatformPatterns.psiElement().withParent(
        scalarPattern(linksFieldName)
    )
}

fun scalarPattern(linksFieldName: String) = psiElement<YAMLScalar>().withParent(
    psiElement<YAMLKeyValue>().withName(linksFieldName).withParent(
        psiElement<YAMLMapping>().withParent(
            psiElement<YAMLSequenceItem>().withParent(
                psiElement<YAMLSequence>().withParent(
                    psiElement<YAMLKeyValue>().withName("links")
                )
            )
        )
    )
)

inline fun <reified T : PsiElement> psiElement() = PlatformPatterns.psiElement(T::class.java)
