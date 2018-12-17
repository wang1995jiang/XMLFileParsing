package com.example.xmlfileparsing;

import android.graphics.drawable.Drawable;

/**
 * Created by 王将 on 2018/10/29.
 */

public class AppModel {
    private int id;
    private Drawable icon;
    private String appName;
    private String packageName;

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setIcon(Drawable icon) {
        this.icon = icon;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public Drawable getIcon() {
        return icon;
    }

    public String getPackageName() {
        return packageName;
    }

    public String getAppName() {
        return appName;
    }
}
