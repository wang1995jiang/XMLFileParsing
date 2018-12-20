package com.example.xmlfileparsing;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by 王将 on 2018/12/12.
 */


public class RootCmd {
    private String findPackage="";
    private List<AppModel> appModels=new ArrayList<>();
    Context context;

    public RootCmd(Context mContext){
        context=mContext;
    }

    //翻译并执行相应的adb命令
    public static boolean exusecmd(String command) {
        Process process = null;
        DataOutputStream os = null;
        try {
            process = Runtime.getRuntime().exec("su");
            os = new DataOutputStream(process.getOutputStream());
            os.writeBytes(command + "\n");
            os.writeBytes("exit\n");
            os.flush();
            Log.e("updateFile", "======000==writeSuccess======");
            process.waitFor();
        } catch (Exception e) {
            Log.e("updateFile", "======111=writeError======" + e.toString());
            return false;
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
                if (process != null) {
                    process.destroy();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    //移动文件
    public void moveFileToSD(String filePath, String cpFilePath,String name) {
        exusecmd("mount -o rw,remount "+cpFilePath);
        exusecmd("chmod 777 "+cpFilePath);
        exusecmd("cp -r "+filePath+" "+cpFilePath);

        writeTarget(cpFilePath,name);
    }

    public void deleFile(){
        exusecmd("chmod 777 /storage/emulated/0/xmlTransit");
        exusecmd("rm -rf /storage/emulated/0/xmlTransit");
    }

    private void writeTarget(String path,String name){
        File file = new File(path);
        File[] subFile = file.listFiles();
        if (subFile!=null&&subFile.length>0){
            for (int iFileLength = 0; iFileLength < subFile.length; iFileLength++) {
                if (!subFile[iFileLength].isDirectory()) {
                    String filePath = subFile[iFileLength].getAbsolutePath();

                    List<String> strList=readText(filePath);
                    writeTxtToFile(strList,getSDPath()+"/xmlTarget/"+name+"/"+subFile[iFileLength].getName());
                }
            }
            deleFile();

            showToast("XML文件获取成功！");
        }else {
            showToast("没有找到该应用的XML文件！");
        }

    }

    public List<AppModel> getAppInfo(){
        if (appModels.size()==0){
            PackageManager pm = context.getPackageManager();
            List<PackageInfo> list = pm.getInstalledPackages(PackageManager.GET_UNINSTALLED_PACKAGES);

            int i=0;
            for (PackageInfo packageInfo:list) {
                AppModel appModel = new AppModel();
                appModel.setId(i);
                appModel.setAppName(packageInfo.applicationInfo.loadLabel(context.getPackageManager()).toString());
                appModel.setPackageName(packageInfo.packageName);
                appModel.setIcon(packageInfo.applicationInfo.loadIcon(context.getPackageManager()));
                appModels.add(appModel);
                i++;
            }
        }

        return appModels;
    }

    public void toastDialog(final ImageView imageView, final TextView textView){
        final List<String> strList=getFolderName();
        if (strList.size()==0){
            appShowDialog(true,imageView,textView,getAppInfo());
        }else {
            AlertDialog.Builder dialog=new AlertDialog.Builder(context);
            dialog.setTitle("发现已经存在的应用XML文件");
            dialog.setMessage("你想再重新获取新的应用XML文件吗？");
            dialog.setCancelable(false);
            dialog.setPositiveButton("是的", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    appShowDialog(true,imageView,textView,getAppInfo());
                    dialog.dismiss();
                }
            });
            dialog.setNegativeButton("不需要", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    List<AppModel> appModels=new ArrayList<>();
                    for (String str:strList){
                        for (AppModel appModel:getAppInfo()){
                            if (appModel.getPackageName().equals(str)){
                                appModels.add(appModel);
                            }
                        }
                    }
                    appShowDialog(false,imageView,textView,appModels);
                }
            });
            dialog.show();
        }
    }

    public void appShowDialog(final boolean isNew, final ImageView imageView, final TextView textView, final List<AppModel> appModels){
        final Dialog dialog=new Dialog(context, R.style.app_style_dialog);

        View view= LayoutInflater.from(context).inflate(R.layout.show_package,null,false);
        dialog.setContentView(view);

        LinearLayout linearLayout=(LinearLayout) view.findViewById(R.id.scroll_appInfo);
        TextView title=(TextView) view.findViewById(R.id.title_show);

        title.setText("请选择你需要解析的应用：");
        for (final AppModel appModel:appModels){
            final View app= LayoutInflater.from(context).inflate(R.layout.appinfo_layout,linearLayout,false);

            ImageView icon=(ImageView) app.findViewById(R.id.icon_app);
            TextView name=(TextView) app.findViewById(R.id.name_app);
            TextView packageName=(TextView) app.findViewById(R.id.package_app);

            icon.setImageDrawable(appModel.getIcon());
            name.setText(appModel.getAppName());
            packageName.setText(appModel.getPackageName());

            app.setTag(appModel);
            app.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AppModel model=(AppModel) v.getTag();
                        if (isNew){
                            String dataFilePath="/data/data/"+model.getPackageName()+"/shared_prefs";
                            moveFileToSD(dataFilePath,getSDPath()+"/xmlTransit",model.getPackageName());
                        }

                        imageView.setVisibility(View.VISIBLE);
                        imageView.setImageDrawable(model.getIcon());
                        textView.setText(model.getAppName());
                        findPackage=model.getPackageName();
                        dialog.dismiss();
                    }
            });
            linearLayout.addView(app);
        }


        Window window = dialog.getWindow();
        window.setGravity(Gravity.BOTTOM);
        window.getDecorView().setPadding(0, 0, 0, 0);

        WindowManager.LayoutParams lp = window.getAttributes();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.MATCH_PARENT;
        window.setAttributes(lp);

        dialog.show();
    }

    public String getSDPath(){
        File sdDir = null;
        boolean sdCardExist = Environment.getExternalStorageState()
                .equals(android.os.Environment.MEDIA_MOUNTED); //判断sd卡是否存在
        if (sdCardExist)
        {
            sdDir = Environment.getExternalStorageDirectory();//获取跟目录
        }else {
            Log.i("++++++++","SD卡不存在！");
        }
        return sdDir.toString();
    }

    public String startFind(String resultFind,TextView textView){
        if (resultFind.length()>0){
            resultFind="";
        }

        boolean isHaveFileName=false;

        if (findPackage.length()>0){

            File file = new File(getSDPath()+"/xmlTarget/"+findPackage);
            File[] subFile = file.listFiles();
            for (int iFileLength = 0; iFileLength < subFile.length; iFileLength++) {
                // 判断是否为文件夹
                if (!subFile[iFileLength].isDirectory()) {
                    String filePath = subFile[iFileLength].getAbsolutePath();

                    List<String> strList=readText(filePath);
                    for (String str:strList){
                        String allString=str.toLowerCase();
                        String findString=MainActivity.findStr.toLowerCase();

                        if (allString.contains(findString)){
                            if (!isHaveFileName){
                                resultFind=resultFind+"文件名称："+subFile[iFileLength].getName()+"\r\n";
                                isHaveFileName=true;
                            }
                            resultFind=resultFind+str+"\r\n";
                        }
                    }

                    isHaveFileName=false;
                }
            }

            if (resultFind.length()==0){
                resultFind="没有找到相关字段信息！";
            }
            showToast("查找完成！");
        }else {
            showToast("你还没有选择一个应用");
        }

        return resultFind;
    }

    public List<String> readText(String filePath1) {
        List<String> txtStr=new ArrayList<>();
        Log.i("++++++",filePath1);
        try {
            File file1 = new File(filePath1);

            int count = 0;
            if (file1.isFile() && file1.exists()) {
                InputStreamReader isr = new InputStreamReader(new FileInputStream(file1));
                BufferedReader br = new BufferedReader(isr);
                String lineTxt = null;
                while ((lineTxt = br.readLine()) != null) {
                    if (!"".equals(lineTxt)) {
                        String reds = lineTxt.split("\\+")[0];  //java 正则表达式
                        txtStr.add(count, reds);
                        count++;
                    }
                }
                isr.close();
                br.close();
            }else {
                Log.i("++++++++","文件不存在！");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return txtStr;
    }

    public void writeTxtToFile(List<String> txtStr,String filePath) {
        String strContent ="";
        for (String str:txtStr){
            strContent=strContent+str+"\r\n";
        }

        try {
            File file = makeFilePath(filePath);
            RandomAccessFile raf = new RandomAccessFile(file, "rwd");
            raf.seek(file.length());
            raf.write(strContent.getBytes());
            raf.close();
        } catch (Exception e) {
            Log.e("TestFile", "Error on write File:" + e);
        }
    }

    public File makeFilePath(String filePath) {
        File file = new File(filePath);
        if (file.isFile() && file.exists()){
            file.delete();
        }

        try {
            File fileParent = file.getParentFile();
            if(!fileParent.exists()){
                fileParent.mkdirs();
            }
            file.createNewFile();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return file;
    }

    private void showToast(String str){
        Toast.makeText(context,str,Toast.LENGTH_SHORT).show();
    }

    private List<String> getFolderName(){
        File file = new File(getSDPath()+"/xmlTarget");
        File[] subFile = file.listFiles();

        List<String> strs=new ArrayList<>();
        if (subFile!=null&&subFile.length>0){
            for (File f:subFile){
                strs.add(f.getName());
            }
        }
        return strs;
    }
}
