package com.icthh.xm.demoplugin

import com.intellij.openapi.vfs.VirtualFile

fun VirtualFile.isEntitySpec() = path.endsWith("/config/tenants/DEMO/entity/xmentityspec.yml")
