package com.mo.signedwork;

import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.util.Log;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/** hook定位
 *  Created by motw on 2016/11/16.
 */

public class LocalHook implements IXposedHookLoadPackage {

    private final String TAG = "LocalHook";

    //不带参数的方法拦截
    private void hook_method(String className, ClassLoader classLoader, String methodName,
                             Object... parameterTypesAndCallback){
        try {
            XposedHelpers.findAndHookMethod(className, classLoader, methodName, parameterTypesAndCallback);
        } catch (Exception e) {
            XposedBridge.log(e);
        }
    }

    //带参数的方法拦截
    private void hook_methods(String className, String methodName, XC_MethodHook xmh){
        try {
            Class<?> clazz = Class.forName(className);
            for (Method method : clazz.getDeclaredMethods())
                if (method.getName().equals(methodName)
                        && !Modifier.isAbstract(method.getModifiers())
                        && Modifier.isPublic(method.getModifiers())) {
                    XposedBridge.hookMethod(method, xmh);
                }
        } catch (Exception e) {
            XposedBridge.log(e);
        }
    }


    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
        Log.d("debug", "pkg: " + loadPackageParam.packageName);

        hook_method("android.net.wifi.WifiManager", loadPackageParam.classLoader, "getScanResults",
                new XC_MethodHook(){
                    /**
                     * Android提供了基于网络的定位服务和基于卫星的定位服务两种
                     * android.net.wifi.WifiManager的getScanResults方法
                     * Return the results of the latest access point scan.
                     * @return the list of access points found in the most recent scan.
                     */
                    @Override
                    protected void afterHookedMethod(MethodHookParam param)
                            throws Throwable {
                        //返回空，就强制让apps使用gps定位信息
                        param.setResult(null);
                    }
                });

        hook_method("android.telephony.TelephonyManager", loadPackageParam.classLoader, "getCellLocation",
                new XC_MethodHook(){
                    /**
                     * android.telephony.TelephonyManager的getCellLocation方法
                     * Returns the current location of the device.
                     * Return null if current location is not available.
                     */
                    @Override
                    protected void afterHookedMethod(XC_MethodHook.MethodHookParam param)
                            throws Throwable {
                        param.setResult(null);
                    }
                });

        hook_method("android.telephony.TelephonyManager", loadPackageParam.classLoader, "getNeighboringCellInfo",
                new XC_MethodHook(){
                    /**
                     * android.telephony.TelephonyManager类的getNeighboringCellInfo方法
                     * Returns the neighboring cell information of the device.
                     */
                    @Override
                    protected void afterHookedMethod(MethodHookParam param)
                            throws Throwable {
                        param.setResult(null);
                    }
                });


        hook_methods("android.location.LocationManager", "requestLocationUpdates",
                new XC_MethodHook() {
                    /**
                     * android.location.LocationManager类的requestLocationUpdates方法
                     * 其参数有4个：
                     * String provider, long minTime, float minDistance,LocationListener listener
                     * Register for location updates using the named provider, and a pending intent
                     */
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {

                        if (param.args.length == 4 && (param.args[0] instanceof String)) {
                            //位置监听器,当位置改变时会触发onLocationChanged方法
                            LocationListener ll = (LocationListener)param.args[3];

                            Class<?> clazz = LocationListener.class;
                            Method m = null;
                            for (Method method : clazz.getDeclaredMethods()) {
                                if (method.getName().equals("onLocationChanged")) {
                                    m = method;
                                    break;
                                }
                            }

                            try {
                                if (m != null) {
                                    Object[] args = new Object[1];
                                    Location l = new Location(LocationManager.GPS_PROVIDER);
                                    //台北经纬度:121.53407,25.077796
                                    double la=113.334773;
                                    double lo=22.992792;
                                    l.setLatitude(la);
                                    l.setLongitude(lo);
                                    args[0] = l;
                                    m.invoke(ll, args);
                                    XposedBridge.log("fake location: " + la + ", " + lo);
                                }
                            } catch (Exception e) {
                                XposedBridge.log(e);
                            }
                        }
                    }
                });


        hook_methods("android.location.LocationManager", "getGpsStatus",
                new XC_MethodHook(){
                    /**
                     * android.location.LocationManager类的getGpsStatus方法
                     * 其参数只有1个：GpsStatus status
                     * Retrieves information about the current status of the GPS engine.
                     * This should only be called from the {@link GpsStatus.Listener#onGpsStatusChanged}
                     * callback to ensure that the data is copied atomically.
                     *
                     */
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        GpsStatus gss = (GpsStatus)param.getResult();
                        if (gss == null)
                            return;

                        Class<?> clazz = GpsStatus.class;
                        Method m = null;
                        for (Method method : clazz.getDeclaredMethods()) {
                            if (method.getName().equals("setStatus")) {
                                if (method.getParameterTypes().length > 1) {
                                    m = method;
                                    break;
                                }
                            }
                        }
                        m.setAccessible(true);
                        //make the apps belive GPS works fine now
                        int svCount = 5;
                        int[] prns = {1, 2, 3, 4, 5};
                        float[] snrs = {0, 0, 0, 0, 0};
                        float[] elevations = {0, 0, 0, 0, 0};
                        float[] azimuths = {0, 0, 0, 0, 0};
                        int ephemerisMask = 0x1f;
                        int almanacMask = 0x1f;
                        //5 satellites are fixed
                        int usedInFixMask = 0x1f;
                        try {
                            if (m != null) {
                                m.invoke(gss,svCount, prns, snrs, elevations, azimuths, ephemerisMask, almanacMask, usedInFixMask);
                                param.setResult(gss);
                            }
                        } catch (Exception e) {
                            XposedBridge.log(e);
                        }
                    }
                });

    }
}
