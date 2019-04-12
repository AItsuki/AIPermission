package com.aitsuki.processor

import com.aitsuki.aipermission.annotation.ScanPermission
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement

/**
 * Create by AItsuki on 2019/4/8.
 */
class AIPermissionProcessor : AbstractProcessor() {

    override fun init(processingEnv: ProcessingEnvironment?) {
        super.init(processingEnv)
    }

    override fun process(annotations: Set<TypeElement>, roundEnv: RoundEnvironment): Boolean {
        val requestCodeProvider = RequestCodeProvider()
        roundEnv.getElementsAnnotatedWith(ScanPermission::class.java).forEach {

        }
        return true
    }

    override fun getSupportedAnnotationTypes(): Set<String> {
        return hashSetOf(ScanPermission::class.java.canonicalName)
    }

    override fun getSupportedSourceVersion(): SourceVersion {
        return SourceVersion.latestSupported()
    }

    private fun processJava(requestCodeProvider: RequestCodeProvider, element: Element) {

    }
}