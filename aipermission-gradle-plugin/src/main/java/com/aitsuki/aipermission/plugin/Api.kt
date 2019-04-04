package com.aitsuki.aipermission.plugin

/**
 * Create by AItsuki on 2019/4/2.
 */
object Api {

    // android class
    const val ACTIVITY_DESC = "Landroidx/fragment/app/FragmentActivity;"
    const val FRAGMENT_DESC = "Landroidx/fragment/app/Fragment;"

    // annotation
    const val ACTIVITY_ANNO_DESC = "Lcom/aitsuki/aipermission/annotation/ScanActivity;"
    const val FRAGMENT_ANNO_DESC = "Lcom/aitsuki/aipermission/annotation/ScanFragment;"
    const val REQUIRE_ANNO_DESC = "Lcom/aitsuki/aipermission/annotation/RequirePermissions;"

    // annotation argument name
    const val ARG_PERMISSIONS = "permissions"
    const val ARG_RATIONALE = "rationale"
    const val ARG_RATIONALE_ID = "rationaleId"
    const val ARG_STRATEGY = "strategy"

    // class
    const val DEFAULT_STRATEGY_NAME = "com/aitsuki/aipermission/strategy/DefaultStrategy"
    const val DISPATCHER_NAME = "com/aitsuki/aipermission/PermissionDispatcher"
    const val DISPATCHER_DESC = "Lcom/aitsuki/aipermission/PermissionDispatcher;"

    // method
    const val BIND_METHOD = "bind"
    const val DISPATCH_RESULT_METHOD = "dispatchPermissionResult"
    const val DISPATCH_RESULT_METHOD_DESC = "(I[Ljava/lang/String;[I)V"
    const val REQUEST_METHOD = "request"
    const val REQUEST_METHOD_DESC = "(I[Ljava/lang/String;Ljava/lang/String;Lcom/aitsuki/aipermission/strategy/Strategy;Ljava/lang/Runnable;)V"
}