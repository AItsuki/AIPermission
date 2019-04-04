package com.aitsuki.aipermission.plugin

import com.android.build.api.transform.Format
import com.android.build.api.transform.QualifiedContent
import com.android.build.api.transform.Transform
import com.android.build.api.transform.TransformInvocation
import com.android.build.gradle.internal.pipeline.TransformManager
import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.jar.JarFile
import java.util.jar.JarOutputStream
import java.util.zip.ZipEntry

/**
 * Create by AItsuki on 2019/3/29.
 */
class AIPermissionTransform : Transform() {

    override fun getName(): String = "aipermission"

    override fun getInputTypes(): MutableSet<QualifiedContent.ContentType> =
            TransformManager.CONTENT_CLASS

    override fun isIncremental(): Boolean = false

    override fun getScopes(): MutableSet<QualifiedContent.Scope> =
            TransformManager.SCOPE_FULL_PROJECT

    override fun transform(transformInvocation: TransformInvocation) {
        super.transform(transformInvocation)
        val startTime = System.currentTimeMillis()
        transformInvocation.inputs.forEach { input ->
            input.directoryInputs.forEach { dirInput ->
                val dest = transformInvocation.outputProvider.getContentLocation(
                        dirInput.name + "_" + dirInput.file.absolutePath.hashCode(),
                        dirInput.contentTypes,
                        dirInput.scopes,
                        Format.DIRECTORY)
                FileUtils.copyDirectory(dirInput.file, dest)
                Scanner.scanDir(dest)
            }

            input.jarInputs.forEach { jarInput ->
                val src = jarInput.file
                val dest = transformInvocation.outputProvider.getContentLocation(
                        src.name + "_" + src.absolutePath.hashCode(),
                        jarInput.contentTypes,
                        jarInput.scopes,
                        Format.JAR)
                FileUtils.copyFile(src, dest)
                Scanner.scanJar(dest)
            }
        }

        if (Scanner.scanResult.isEmpty()) {
            Log.lifeCycle("No class require permission.")
        } else {
            Log.lifeCycle("----------------- Scan result ---------------------")
            Scanner.scanResult.forEach { clazz ->
                Log.lifeCycle("${clazz.className} has require permissions.")
                clazz.methodsInfo.forEach { info ->
                    Log.lifeCycle("Require info =  $info")
                }
            }
            Log.lifeCycle("----------------- Scan result ---------------------")
        }

        Log.lifeCycle("Start injectCode...")
        injectCode(Scanner.scanResult)
        Log.lifeCycle("AIPermissionTransform completed! Cost ${System.currentTimeMillis() - startTime}ms.")
    }

    private fun injectCode(results: List<ClassInfo>) {
        val jarMap = mutableMapOf<String, MutableList<ClassInfo>>()
        results.forEach { result ->
            if (result.isJarEntry) {
                jarMap[result.parentPath] ?: mutableListOf<ClassInfo>().add(result)
            } else {
                injectClass(result)
            }
        }

        if (jarMap.isNotEmpty()) {
            jarMap.forEach { path, infoList ->
                injectJar(File(path), infoList)
            }
        }
    }

    private fun injectClass(classInfo: ClassInfo) {
        val inputStream = FileInputStream(classInfo.path)
        val resultMap = ByteCodeGenerator.generate(inputStream, classInfo)
        inputStream.close()
        resultMap.forEach { path, bytes ->
            val outputStream = FileOutputStream(path)
            outputStream.write(bytes)
            outputStream.close()
        }
    }

    private fun injectJar(file: File, infoList: List<ClassInfo>) {
        val optJar = File(file.parent, file.name + ".opt")
        if (optJar.exists()) {
            optJar.delete()
        }
        val jarOutputStream = JarOutputStream(FileOutputStream(optJar))
        val jarFile = JarFile(file)
        val entries = jarFile.entries()
        while (entries.hasMoreElements()) {
            val jarEntry = entries.nextElement()
            val inputStream = jarFile.getInputStream(jarEntry)
            val info = infoList.find { it.path == jarEntry.name }
            if (info != null) {
                val resultMap = ByteCodeGenerator.generate(inputStream, info)
                resultMap.forEach { entry, bytes ->
                    jarOutputStream.putNextEntry(ZipEntry(entry))
                    jarOutputStream.write(bytes)
                    jarOutputStream.closeEntry()
                }
            } else {
                jarOutputStream.write(IOUtils.toByteArray(inputStream))
                jarOutputStream.closeEntry()
            }
            inputStream.close()
        }
        jarOutputStream.close()
        jarFile.close()
        if (file.exists()) {
            file.delete()
        }
        optJar.renameTo(file)
    }
}