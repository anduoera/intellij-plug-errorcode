package com.github.anduoera.intellijplug.mySettings

import com.intellij.openapi.options.Configurable
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import javax.swing.*

class MySettingsConfigurable : Configurable, ActionListener {
    private var settingsPanel: JPanel? = null
    private val textFields: MutableList<JTextField> = mutableListOf()
    val settings = MySettings.getInstance().state
    override fun createComponent(): JComponent? {
        settingsPanel = JPanel()


        settingsPanel!!.layout = BoxLayout(settingsPanel, BoxLayout.Y_AXIS)

        for (setting in settings.exampleSettings) {
            addSettingComponent(setting)
        }

        // 添加一个按钮用于添加新的配置项
        val addButton = JButton("addSetting")
        addButton.actionCommand = "addSetting"
        addButton.addActionListener(this)
        
        val buttonPanel = JPanel()
        buttonPanel.layout = BorderLayout()
        buttonPanel.add(addButton, BorderLayout.PAGE_START)
        settingsPanel!!.add(buttonPanel)

        return settingsPanel
    }

    private fun addSettingComponent(value: String = "ErrorCode") {
        //$method fun Name
        //$file file Name
        //Don't enter "ErrorCode" prefix ,Enter the suffix of "ErrorCode".

        val itemPanel = JPanel()
        itemPanel.layout = BoxLayout(itemPanel, BoxLayout.X_AXIS)



        val textField = JTextField(value, 10).apply {

            preferredSize = Dimension(200, 30) // 例如，宽度200，高度30
            toolTipText = "Enter the ErrorCode here."
        }
        textFields.add(textField)
        
        itemPanel.add(JLabel("item:"))
        itemPanel.add(Box.createHorizontalStrut(10))
        itemPanel.add(textField)
        
        settingsPanel!!.add(itemPanel, settingsPanel!!.componentCount - 1)
        settingsPanel!!.revalidate()
        settingsPanel!!.repaint()
    }

    override fun isModified(): Boolean {
        if (settings.exampleSettings.size != textFields.size) {
            return true
        }

        for (i in textFields.indices) {
            if (textFields[i].text != settings.exampleSettings[i]) {
                return true
            }
        }

        return false
    }
    override fun apply() {
        settings.exampleSettings = textFields.map { it.text }.toMutableList()
    }

    override fun getDisplayName(): String {
        return "Error Code Settings"
    }

    override fun actionPerformed(e: ActionEvent) {
        if (e.actionCommand == "addSetting") {
            addSettingComponent()
        }
    }
}