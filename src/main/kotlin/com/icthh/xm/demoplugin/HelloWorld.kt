package com.icthh.xm.demoplugin

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import java.awt.BorderLayout.CENTER
import javax.swing.JFrame
import javax.swing.JLabel

class HelloWorld : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val frame = JFrame("Hello world")
        frame.contentPane.add(JLabel("Hello world"), CENTER)
        frame.isVisible = true
        frame.pack()
    }
}
