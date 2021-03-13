package com.icthh.xm.demoplugin

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.jetbrains.jsonSchema.extension.JsonSchemaFileProvider
import com.jetbrains.jsonSchema.extension.JsonSchemaProviderFactory
import com.jetbrains.jsonSchema.extension.SchemaType.userSchema

class EntitySchemaProviderFactory: JsonSchemaProviderFactory {
    override fun getProviders(project: Project): List<JsonSchemaFileProvider> {
        return listOf(object: JsonSchemaFileProvider {
            override fun isAvailable(file: VirtualFile) = file.isEntitySpec()
            override fun getSchemaFile(): VirtualFile? {
                val classLoader = EntitySchemaProviderFactory::class.java.classLoader
                val url = classLoader.getResource("specs/entityspecschema.json") ?: return null
                return VfsUtil.findFileByURL(url)
            }

            override fun getSchemaType() = userSchema
            override fun getName(): String = "Entity spec validation"
        })
    }
}
