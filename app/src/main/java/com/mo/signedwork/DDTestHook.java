package com.mo.signedwork;

import android.os.Bundle;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

/** hook
 *  Created by motw on 2016/11/16.
 */

public class DDTestHook implements IXposedHookLoadPackage {

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
        XposedBridge.log("pkg name : " + loadPackageParam.packageName);
        if (loadPackageParam.packageName.equals("com.itseye.phantom")) {
            XposedBridge.log("Loaded app: " + loadPackageParam.packageName);

            findAndHookMethod("com.itseye.phantom.ui.WelcomeActivity", loadPackageParam.classLoader, "onCreate", Bundle.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    super.beforeHookedMethod(param);
                    XposedBridge.log("成功_onCreate");
                }

                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                }
            });


            findAndHookMethod("com.itseye.phantom.ui.WelcomeActivity", loadPackageParam.classLoader, "test", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    super.beforeHookedMethod(param);
                    XposedBridge.log("成功_test");
                }

                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                    XposedBridge.log("成功_test");

                }
            });
        }

    }
}
