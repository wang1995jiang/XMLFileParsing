package com.example.xmlfileparsing;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    public static String findStr="";
    EditText editText;
    Button button;
    TextView textView,getApp;
    ImageView appIcon;

    String resultFind="";

    RootCmd rootCmd;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        rootCmd=new RootCmd(this);
        editText=(EditText) findViewById(R.id.find_string);
        button=(Button) findViewById(R.id.start_find);
        textView=(TextView) findViewById(R.id.show_file);
        getApp=(TextView) findViewById(R.id.get_app);
        appIcon=(ImageView) findViewById(R.id.app_icon);

        appIcon.setVisibility(View.GONE);

        button.setOnClickListener(this);
        getApp.setOnClickListener(this);
    }



    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.start_find:
                findStr=editText.getText().toString();
                resultFind=rootCmd.startFind(resultFind,getApp);
                textView.setText(resultFind);
                break;
            case R.id.get_app:
                rootCmd.toastDialog(appIcon,getApp);
            default:break;
        }
    }
}
