package com.aitsuki.aipermission.plugin

import org.objectweb.asm.*
import org.objectweb.asm.Opcodes.*
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.util.jar.JarFile

/**
 * Create by AItsuki on 2019/3/29.
 */
object Scanner {

    var scanResult: MutableList<ClassInfo> = mutableListOf()

    fun initialize() {
        scanResult.clear()
    }

    /**
     * startsWith：过滤jar包中的entryName
     * endsWith和contains：过滤jar包中的entryName和文件的absolutePath
     */
    private fun needScanClass(path: String): Boolean {
        val p = path.toSlash()
        return p.endsWith(".class")
                && !p.startsWith("androidx/")
                && !p.startsWith("com/google/")
                && !p.startsWith("android/arch/")
                && !p.endsWith("/BuildConfig.class")
                && !p.endsWith("/R.class")
                && !p.contains("/R\$")
                && !p.contains("android/support/")
    }

    /**
     * 过滤不需要扫描的jar包
     */
    private fun needScanJar(path: String): Boolean {
        val p = path.toSlash()
        return p.endsWith(".jar")
                && !p.contains(".gradle/caches")
                && !p.contains("/android/m2repository")
                && !p.contains("com.android.support")
    }

    /**
     * 扫描目录， 对应transform的dirInput
     */
    fun scanDir(dir: File) {
        dir.walk().filter { it.isFile }
                .filter { needScanClass(it.absolutePath) }
                .forEach {
                    Log.info("scanDir: ${it.absolutePath}")
                    val inputStream = FileInputStream(it)
                    scanClass(inputStream, ClassInfo(dir.absolutePath, it.absolutePath, false))
                    inputStream.close()
                }
    }

    /**
     * 扫描jar包，对应transform的jarInput
     */
    fun scanJar(file: File) {
        if (needScanJar(file.absolutePath)) {
            Log.info("scanJar: ${file.absolutePath}")
            val jarFile = JarFile(file)
            val entries = jarFile.entries()
            while (entries.hasMoreElements()) {
                val entry = entries.nextElement()
                if (!entry.isDirectory && needScanClass(entry.name)) {
                    Log.info("scanJarEntry: ${entry.name}")
                    val inputStream = jarFile.getInputStream(entry)
                    scanClass(inputStream, ClassInfo(file.absolutePath, entry.name, true))
                    inputStream.close()
                }
            }
            jarFile.close()
        }
    }

    private fun scanClass(inputStream: InputStream, classInfo: ClassInfo) {
        val reader = ClassReader(inputStream)
        classInfo.className = reader.className // reader获取的className是斜杠分隔
        val cv = ScanClassVisitor(classInfo)
        reader.accept(cv, ClassReader.SKIP_CODE or ClassReader.SKIP_DEBUG or ClassReader.SKIP_FRAMES) // 扫描不需要读取方法代码
    }

    private class ScanClassVisitor(val classInfo: ClassInfo) : ClassVisitor(ASM5) {

        private var hashScanAnnotation = false

        override fun visitAnnotation(desc: String, visible: Boolean): AnnotationVisitor? {
            if (desc == Api.ACTIVITY_ANNO_DESC || desc == Api.FRAGMENT_ANNO_DESC) {
                hashScanAnnotation = true
                classInfo.isFragment = desc == Api.FRAGMENT_ANNO_DESC
            }
            return super.visitAnnotation(desc, visible)
        }

        override fun visitMethod(access: Int, name: String, desc: String, signature: String?,
                                 exceptions: Array<out String>?): MethodVisitor? {
            return if (hashScanAnnotation) {
                ScanMethodVisitor(access,classInfo, MethodInfo(name, desc))
            } else {
                super.visitMethod(access, name, desc, signature, exceptions)
            }
        }

        override fun visitEnd() {
            super.visitEnd()
            if (classInfo.methodsInfo.isNotEmpty()) {
                scanResult.add(classInfo)
            }
        }
    }

    private class ScanMethodVisitor(val access: Int, val classInfo: ClassInfo, val methodInfo: MethodInfo)
        : MethodVisitor(ASM5) {

        override fun visitAnnotation(desc: String, visible: Boolean): AnnotationVisitor? {
            return if (desc == Api.REQUIRE_ANNO_DESC) {
                if (access.and(ACC_PRIVATE) != 0 || access.and(ACC_PROTECTED) != 0) {
                    throw IllegalStateException("At ${classInfo.className}.${methodInfo.name}. " +
                            "Target method access cannot be private protected or static.")
                }
                if (methodInfo.desc != "()V") {
                    throw IllegalStateException("At ${classInfo.className}.${methodInfo.name}. " +
                            "Target method cannot have parameters or return value.")
                }
                ScanAnnotationVisitor(classInfo, methodInfo)
            } else {
                super.visitAnnotation(desc, visible)
            }
        }
    }

    private class ScanAnnotationVisitor(val classInfo: ClassInfo, val methodInfo: MethodInfo)
        : AnnotationVisitor(ASM5) {

        override fun visit(name: String?, value: Any?) {
            when (name) {
                Api.ARG_RATIONALE -> methodInfo.rationale = value as? String
                Api.ARG_RATIONALE_ID -> methodInfo.rationaleId = value as? Int
                Api.ARG_STRATEGY -> methodInfo.strategyType = value as? Type
            }
            super.visit(name, value)
        }

        override fun visitArray(name: String): AnnotationVisitor? {
            return if (name == Api.ARG_PERMISSIONS) {
                object : AnnotationVisitor(ASM5) {
                    override fun visit(name: String?, value: Any?) {
                        methodInfo.permissions.add(value as String)
                        super.visit(name, value)
                    }
                }
            } else {
                super.visitArray(name)
            }
        }

        override fun visitEnd() {
            classInfo.methodsInfo.add(methodInfo)
            super.visitEnd()
        }
    }
}