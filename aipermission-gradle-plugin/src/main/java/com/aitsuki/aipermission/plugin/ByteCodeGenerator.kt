package com.aitsuki.aipermission.plugin

import org.objectweb.asm.*
import org.objectweb.asm.Opcodes.*
import java.io.InputStream

/**
 * Create by AItsuki on 2019/4/2.
 */
class ByteCodeGenerator(private val inputStream: InputStream, private val classInfo: ClassInfo) {

    private val result = mutableMapOf<String, ByteArray>()

    companion object {
        const val INVOKE_SUFFIX = "_Invoke"
        const val DISPATCHER_FIELD_NAME = "permissionDispatcher"

        fun generate(inputStream: InputStream, classInfo: ClassInfo): Map<String, ByteArray> {
            return ByteCodeGenerator(inputStream, classInfo).generateByteCode()
        }

        private fun loadInt(mv: MethodVisitor, i: Int) {
            when (i) {
                -1 -> mv.visitInsn(ICONST_M1)
                0 -> mv.visitInsn(ICONST_0)
                1 -> mv.visitInsn(ICONST_1)
                2 -> mv.visitInsn(ICONST_2)
                3 -> mv.visitInsn(ICONST_3)
                4 -> mv.visitInsn(ICONST_4)
                5 -> mv.visitInsn(ICONST_5)
                in 6..127 -> mv.visitVarInsn(BIPUSH, i)
                in 128..32767 -> mv.visitVarInsn(SIPUSH, i)
                else -> mv.visitLdcInsn(i)
            }
        }

        private fun bindDispatcherCodes(mv: MethodVisitor, classOwner: String, isFragment: Boolean) {
            mv.visitVarInsn(ALOAD, 0)
            mv.visitFieldInsn(GETFIELD, classOwner, DISPATCHER_FIELD_NAME, Api.DISPATCHER_DESC)
            mv.visitVarInsn(ALOAD, 0)
            val bindDesc = if (isFragment) "(${Api.FRAGMENT_DESC})V" else "(${Api.ACTIVITY_DESC})V"
            mv.visitMethodInsn(INVOKEVIRTUAL, Api.DISPATCHER_NAME, Api.BIND_METHOD, bindDesc, false)
        }

        private fun dispatchPermissionResultCodes(mv: MethodVisitor, classOwner: String) {
            mv.visitVarInsn(ALOAD, 0)
            mv.visitFieldInsn(GETFIELD, classOwner, DISPATCHER_FIELD_NAME, Api.DISPATCHER_DESC)
            mv.visitVarInsn(ILOAD, 1)
            mv.visitVarInsn(ALOAD, 2)
            mv.visitVarInsn(ALOAD, 3)
            mv.visitMethodInsn(INVOKEVIRTUAL, Api.DISPATCHER_NAME, Api.DISPATCH_RESULT_METHOD, Api.DISPATCH_RESULT_METHOD_DESC, false)
        }
    }

    /**
     * 返回map的原因是因为会生成一个额外的内部类。
     */
    private fun generateByteCode(): Map<String, ByteArray> {
        val cr = ClassReader(inputStream)
        val cw = ClassWriter(cr, 0)
        val ca = ClassAdapter(cr.className, cr.superName, classInfo, result, cw)
        cr.accept(ca, ClassReader.EXPAND_FRAMES)
        result[classInfo.path] = cw.toByteArray()
        return result
    }

    /**
     * 需要做的工作
     * 1. 生成Field：  PermissionDispatcher dispatcher， 初始化Field
     * 2. 在onCreate方法调用super.onCreate后插入dispatcher的绑定逻辑
     * 3. 如果是Fragment，需要重写onRequestPermissionsResult方法，插入分发权限请求结果的代码。
     * 4. 将被注解的方法的方法名改成invoke （e.g. openCameraInvoke）
     * 5. 生成请求权限的方法，方法名为原方法名 (e.g. openCamera)，此方法包含一个Runnable内部类，用于权限
     * 请求成功后回调invoke方法。
     */
    private class ClassAdapter(
            val classOwner: String,
            val superOwner: String,
            val classInfo: ClassInfo,
            val result: MutableMap<String, ByteArray>,
            cw: ClassWriter
    ) : ClassVisitor(ASM5, cw), Opcodes {


        var byteCodeVersion = V1_7
        var overrideOnCreate = false
        var overrideResult = false

        override fun visit(version: Int, access: Int, name: String?, signature: String?, superName: String?, interfaces: Array<out String>?) {
            byteCodeVersion = version
            val fv = cv.visitField(
                    ACC_PRIVATE,
                    DISPATCHER_FIELD_NAME,
                    Api.DISPATCHER_DESC,
                    null,
                    null)
            fv.visitEnd()
            super.visit(version, access, name, signature, superName, interfaces)
        }

        override fun visitMethod(access: Int, name: String?, desc: String?, signature: String?, exceptions: Array<out String>?): MethodVisitor {
            if (name == "<init>" && desc == "()V") {
                // 初始化dispatcher filed
                return InitDispatcherAdapter(classOwner, super.visitMethod(access, name, desc, signature, exceptions))
            } else if (name == "onCreate" && desc == "(Landroid/os/Bundle;)V") {
                // 绑定dispatcher
                overrideOnCreate = true
                return BindDispatcherAdapter(classOwner, classInfo.isFragment, super.visitMethod(access, name, desc, signature, exceptions))
            } else if (classInfo.isFragment && name == "onRequestPermissionsResult" && desc == "(I[Ljava/lang/String;[I)V") {
                // 分发权限请求结果
                overrideResult = true
                return DispatchPermissionResultAdapter(classOwner, super.visitMethod(access, name, desc, signature, exceptions))
            } else {
                classInfo.methodsInfo.forEach { methodInfo ->
                    if (methodInfo.name == name && methodInfo.desc == desc) {
                        // 生成原方法，开始权限请求
                        generateSrcMethod(access, name, desc, signature, exceptions, methodInfo)
                        // 修改原方法名为invoke
                        return super.visitMethod(access, name + INVOKE_SUFFIX, desc, signature, exceptions)
                    }
                }
            }
            return super.visitMethod(access, name, desc, signature, exceptions)
        }

        /**
         * 生成请求权限的方法
         */
        private fun generateSrcMethod(access: Int, name: String, desc: String, signature: String?, exceptions: Array<out String>?, methodInfo: MethodInfo) {
            // 创建一个用于回调invoke方法的Runnable内部类
            val innerName = name.capitalize() + "Runnable"
            val innerClassName = classOwner + "\$" + innerName
            generateInvokeRunnable(innerName, innerClassName, name)
            cv.visitInnerClass(innerClassName, classOwner, innerName, ACC_PRIVATE)

            val requestCode = RequestCodeProvider.get()
            val mv = cv.visitMethod(access, name, desc, signature, exceptions)
            mv.visitCode()
            loadInt(mv, requestCode)
            mv.visitVarInsn(ISTORE, 1) // requestCode 存到1

            loadInt(mv, methodInfo.permissions.size)
            mv.visitTypeInsn(ANEWARRAY, "java/lang/String")
            mv.visitInsn(DUP)
            methodInfo.permissions.forEachIndexed { index, permission ->
                loadInt(mv, index)
                mv.visitLdcInsn(permission)
                mv.visitInsn(AASTORE)
                if (index < methodInfo.permissions.size - 1) {
                    mv.visitInsn(DUP)
                }
            }
            mv.visitVarInsn(ASTORE, 2) // String[] 存到2

            when {
                methodInfo.rationaleId != null -> {
                    mv.visitVarInsn(ALOAD, 0)
                    loadInt(mv, methodInfo.rationaleId ?: -1)
                    mv.visitMethodInsn(INVOKEVIRTUAL, classOwner, "getString", "(I)Ljava/lang/String;", false)
                }
                methodInfo.rationale.isNullOrEmpty() -> mv.visitLdcInsn(methodInfo.rationale)
                else -> mv.visitInsn(ACONST_NULL)
            }
            mv.visitVarInsn(ASTORE, 3) // rationale 存到3

            val strategy = methodInfo.strategyType?.internalName ?: Api.DEFAULT_STRATEGY_NAME
            mv.visitTypeInsn(NEW, strategy)
            mv.visitInsn(DUP)
            mv.visitMethodInsn(INVOKESPECIAL, strategy, "<init>", "()V", false)
            mv.visitVarInsn(ASTORE, 4) // strategy 存到 4

            mv.visitTypeInsn(NEW, innerClassName)
            mv.visitInsn(DUP)
            mv.visitVarInsn(ALOAD, 0)
            mv.visitMethodInsn(INVOKESPECIAL, innerClassName, "<init>", "(L$classOwner;)V", false)
            mv.visitVarInsn(ASTORE, 5) // runnable 存到5

            mv.visitVarInsn(ALOAD, 0)
            mv.visitFieldInsn(GETFIELD, classOwner, DISPATCHER_FIELD_NAME, Api.DISPATCHER_DESC)
            mv.visitVarInsn(ILOAD, 1)
            mv.visitVarInsn(ALOAD, 2)
            mv.visitVarInsn(ALOAD, 3)
            mv.visitVarInsn(ALOAD, 4)
            mv.visitVarInsn(ALOAD, 5)
            mv.visitMethodInsn(INVOKEVIRTUAL, Api.DISPATCHER_NAME, Api.REQUEST_METHOD, Api.REQUEST_METHOD_DESC, false)
            mv.visitMaxs(6, 6)
            mv.visitInsn(RETURN)
            mv.visitEnd()
        }

        /**
         * 生成调用invoke方法的Runnable内部类
         * @param innerName 内部类名字（e.g. OpenCameraRunnable）
         * @param innerClassName 内部类全类名（e.g. com/example/MainActivity$OpenCameraRunnable）
         * @param relationMethod 关联的原方法名 （e.g. openCamera)
         */
        private fun generateInvokeRunnable(innerName: String, innerClassName: String, relationMethod: String) {
            val cw = ClassWriter(0)
            cw.visit(byteCodeVersion, ACC_SUPER, innerClassName, null, "java/lang/Object", arrayOf("java/lang/Runnable"))
            cw.visitInnerClass(innerClassName, classOwner, innerName, 0)

            // 构造函数初始化，初始化this$0，也就是外部类的引用。
            val fv = cw.visitField(ACC_FINAL + ACC_SYNTHETIC, "this\$0", "L$classOwner;", null, null)
            fv.visitEnd()
            var mv = cw.visitMethod(0, "<init>", "(L$classOwner;)V", null, null)
            mv.visitCode()
            mv.visitVarInsn(ALOAD, 0)
            mv.visitVarInsn(ALOAD, 1)
            mv.visitFieldInsn(PUTFIELD, innerClassName, "this\$0", "L$classOwner;")
            mv.visitVarInsn(ALOAD, 0)
            mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false)
            mv.visitInsn(RETURN)
            mv.visitMaxs(2, 2)
            mv.visitEnd()

            // 生成run方法
            mv = cw.visitMethod(ACC_PUBLIC, "run", "()V", null, null)
            mv.visitCode()
            mv.visitVarInsn(ALOAD, 0)
            mv.visitFieldInsn(GETFIELD, innerClassName, "this\$0", "L$classOwner;")
            mv.visitMethodInsn(INVOKEVIRTUAL, classOwner, relationMethod + INVOKE_SUFFIX, "()V", false)
            mv.visitInsn(RETURN)
            mv.visitMaxs(1, 1)
            mv.visitEnd()
            cw.visitEnd()

            // 生成号的内部类写入结果集合，等待写入class字节码文件（实际上内部类编译后是单独的class文件）
            result[classInfo.path.replace(".class", "\$$innerName.class")] = cw.toByteArray()
        }

        override fun visitEnd() {
            // 没有复写onCreate方法时
            if (!overrideOnCreate) {
                val mv = cv.visitMethod(ACC_PUBLIC, "onCreate", "(Landroid/os/Bundle;)V", null, null)
                mv.visitVarInsn(ALOAD, 0)
                mv.visitVarInsn(ALOAD, 1)
                mv.visitMethodInsn(INVOKESPECIAL, superOwner, "onCreate", "(Landroid/os/Bundle;)V", false)
                bindDispatcherCodes(mv, classOwner, classInfo.isFragment)
                mv.visitInsn(RETURN)
                mv.visitMaxs(2, 2)
                mv.visitEnd()
            }

            // 没有复写onRequestPermissionsResult时
            if (classInfo.isFragment && !overrideResult) {
                val mv = cv.visitMethod(ACC_PUBLIC, "onRequestPermissionsResult", "(I[Ljava/lang/String;[I)V", null, null)
                dispatchPermissionResultCodes(mv, classOwner)
                mv.visitInsn(RETURN)
                mv.visitMaxs(4, 4)
                mv.visitEnd()
            }

            super.visitEnd()
        }
    }

    /**
     * 初始化dispatcher field
     * 在空构造函数Return之前插入初始化代码。
     */
    private class InitDispatcherAdapter(val classOwner: String, mv: MethodVisitor)
        : MethodVisitor(ASM5, mv), Opcodes {

        override fun visitInsn(opcode: Int) {
            if (opcode == RETURN) {
                mv.visitVarInsn(ALOAD, 0)
                mv.visitTypeInsn(NEW, Api.DISPATCHER_NAME)
                visitInsn(DUP)
                visitMethodInsn(INVOKESPECIAL, Api.DISPATCHER_NAME, "<init>",
                        "()V", false)
                visitFieldInsn(PUTFIELD, classOwner, DISPATCHER_FIELD_NAME, Api.DISPATCHER_DESC)
            }
            super.visitInsn(opcode)
        }

        override fun visitMaxs(maxStack: Int, maxLocals: Int) {
            super.visitMaxs(maxStack + 2, maxLocals)
        }
    }

    /**
     * 当前fragment或activity绑定dispatcher
     * 在onCreate(Bundle bundle)方法的super之后插入代码
     */
    private class BindDispatcherAdapter(
            val classOwner: String,
            val isFragment: Boolean, mv: MethodVisitor
    ) : MethodVisitor(ASM5, mv), Opcodes {

        override fun visitMethodInsn(opcode: Int, owner: String?, name: String?, desc: String?, itf: Boolean) {
            super.visitMethodInsn(opcode, owner, name, desc, itf)
            if (opcode == INVOKESPECIAL && name == "onCreate" && desc == "(Landroid/os/Bundle;)V") {
                bindDispatcherCodes(mv, classOwner, isFragment)
            }
        }

        override fun visitMaxs(maxStack: Int, maxLocals: Int) {
            super.visitMaxs(maxStack + 2, maxLocals)
        }
    }

    /**
     * dispatcher派发权限请求结果。
     * 在当前onRequestPermissionsResult方法第一行插入分发result的逻辑。
     */
    private class DispatchPermissionResultAdapter(
            val classOwner: String,
            mv: MethodVisitor
    ) : MethodVisitor(ASM5, mv) {

        override fun visitCode() {
            super.visitCode()
            dispatchPermissionResultCodes(mv, classOwner)
        }

        override fun visitMaxs(maxStack: Int, maxLocals: Int) {
            super.visitMaxs(maxStack + 4, maxLocals)
        }
    }
}