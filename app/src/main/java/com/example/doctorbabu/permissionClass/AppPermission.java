package com.example.doctorbabu.permissionClass;

import android.content.Context;
import android.os.PowerManager;
import android.provider.Settings;


public class AppPermission {
    Context context;
    private AppPermission(Context context){
        this.context = context;
    }
    private static AppPermission instance = null;

    public static AppPermission createInstance(Context context){
        if(instance == null){
            return new AppPermission(context);
        }
        return instance;
    }

    public boolean isBatteryOptimizationsEnabled() {
        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        if (!powerManager.isIgnoringBatteryOptimizations(context.getPackageName())) {

            return true;
        }
        return false;
    }
    public boolean canDrawOverlays(){
        if(!Settings.canDrawOverlays(context)){
            return false;
        }

        return true;
    }

}
