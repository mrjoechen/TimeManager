package com.tm.timemanager.Activity;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.jeremyfeinstein.slidingmenu.lib.app.SlidingFragmentActivity;
import com.tm.timemanager.R;
import com.tm.timemanager.Service.Lookservice;
import com.tm.timemanager.Utils.DateUtil;
import com.tm.timemanager.application.MyApplication;
import com.tm.timemanager.dao.DBOpenHelperdao;
import com.tm.timemanager.fragment.ContentFragment;
import com.tm.timemanager.fragment.LeftMenuFragment;
import com.tm.timemanager.fragment.ManagementFragment;
import com.tm.timemanager.fragment.SettingFragment;
import com.tm.timemanager.fragment.TrendFragment;

import java.util.Timer;
import java.util.TimerTask;

public class HomeActivity extends SlidingFragmentActivity {

    private FragmentManager fragmentManager;
    private boolean isChecked;
    private NotificationManager manager;


    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        isChecked = MyApplication.gettime("isChecked", true);
        if (isChecked) {
        }
            initTzl();//创建通知栏


        int phoneWidth = MyApplication.getPhoneWidth(this);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_home);
        SlidingMenu slidingMenu = getSlidingMenu();
        setBehindContentView(R.layout.layout_leftmenu);
        slidingMenu.setMode(SlidingMenu.LEFT);
        slidingMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);
        slidingMenu.setBehindOffset(phoneWidth / 2);
        initFragment();

        //开启收集数据的服务
        Intent intent = new Intent(this, Lookservice.class);
        startService(intent);
        //
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        manager.cancel(1);
       /* DBOpenHelperdao dbOpenHelperdao = new DBOpenHelperdao(this);
        for (int i=0;i<10;i++){
            dbOpenHelperdao.insertBlackNumber("haha",1111,1111,i);ooo
        }*/

        //注册广播接受者
        refreshreceiver receiver = new refreshreceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.tm.timemanager.refresh");
        registerReceiver(receiver, filter);

    }


    public void initTzl() {
        String date = DateUtil.getDate();
        int phoneDailyUsageCount = 0;

        DBOpenHelperdao dbOpenHelperdao=new DBOpenHelperdao(this);


        Cursor cursor = dbOpenHelperdao.getappevent(date);
        while(cursor.moveToNext()){
            phoneDailyUsageCount++;
        }

        long getappeventtotalday = dbOpenHelperdao.getappeventtotalday(date);
        Cursor getappdaily = dbOpenHelperdao.getappdaily(date);
        int todaycount = getappdaily.getCount();
        Cursor getapptotal = dbOpenHelperdao.getapptotal();
        int todayappc = getappdaily.getCount();
        manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//构建通知
        Notification notification = new Notification();

        notification.icon = android.R.drawable.stat_notify_call_mute;
        notification.tickerText = "nice";
//加载自定义布局
        RemoteViews contentView = new RemoteViews(getPackageName(),R.layout.notification);
//通知显示的布局
        notification.contentView = contentView;
//设置值
        //remoteviews在RemoteViews这种调用方式中，你只能使用以下几种界面组件：Layout:FrameLayout, LinearLayout, RelativeLayout Component:AnalogClock, Button, Chronometer, ImageButton, ImageView, ProgressBar, TextView, ViewFlipper, ListView, GridView, StackView, AdapterViewFlipper


        contentView.setTextViewText(R.id.tv_notification_time,getappeventtotalday/60000+"分钟");
        contentView.setTextViewText(R.id.tv_notification_sypc, todaycount+"次");
        contentView.setTextViewText(R.id.tv_notification_sygs, todayappc+"个");
        contentView.setTextViewText(R.id.tv_notification_unlocks,phoneDailyUsageCount/2+ "次");



//点击跳转
        Intent intent = new Intent(this,HomeActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 100, intent, 0);
//点击的事件
        notification.contentIntent = contentIntent;
//点击通知之后不消失
        notification.flags = Notification.FLAG_NO_CLEAR;
//发送通知
        manager.notify(0, notification);
    }



    public void getimage(View view) {
        startActivity(new Intent(this, MytestActivity.class));
    }

    private void initFragment() {

        //layout_leftmenu和activity_home都是空的FrameLayout，用fragment去替换
        fragmentManager = getFragmentManager(); // 改为全局
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fl_main_content, new ContentFragment(), "contentfragment");
        fragmentTransaction.replace(R.id.fl_left_menu, new LeftMenuFragment(), "leftmenufragment");
        fragmentTransaction.commit();

//        Fragment fragmentByTag = fragmentManager.findFragmentByTag();
    }

    //通过tag获取主界面activity的fragment，方便后面调用
    public Fragment getFragment(String tag) {

        FragmentManager fragmentManager = getFragmentManager();
        return fragmentManager.findFragmentByTag(tag); // 直接返回
    }

    // 替换新的fragment，供外界调用（i对应左侧边栏按钮，比如i=2时替换为ManagementFragment）
    public void replaceFragment(int i) {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        switch (i) {
            case 0:
                fragmentTransaction.replace(R.id.fl_main_content, new ContentFragment());
                fragmentTransaction.commit(); // 提交事务
                break;
            case 1:
                Fragment trendFragment = new TrendFragment();
                fragmentTransaction.replace(R.id.fl_main_content, trendFragment);
                fragmentTransaction.commit(); // 提交事务
                break;
            case 2:
                fragmentTransaction.replace(R.id.fl_main_content, new ManagementFragment());
                fragmentTransaction.commit(); // 提交事务
                break;
            case 3:
                fragmentTransaction.replace(R.id.fl_main_content, new SettingFragment());
                fragmentTransaction.commit(); // 提交事务
                break;
            default:
                // ...
                break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == android.view.KeyEvent.KEYCODE_BACK) {
            exitBy2Click(); //调用双击退出函数
        }
        return false;
    }

    /**
     * 双击退出函数
     */
    private static Boolean isExit = false;

    private void exitBy2Click() {
        Timer tExit = null;
        if (isExit == false) {
            isExit = true; // 准备退出
            Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
            tExit = new Timer();
            tExit.schedule(new TimerTask() {
                @Override
                public void run() {
                    isExit = false; // 取消退出
                }
            }, 2000); // 如果2秒钟内没有按下返回键，则启动定时器取消掉刚才执行的任务
        } else {
            finish();
            System.exit(0);
        }
    }


    //主界面从新获取焦点是重新加载
    @Override
    protected void onResume() {
        super.onResume();
        initFragment();
    }
    public void skip(View view){

        startActivity(new Intent(HomeActivity.this,SimpleAdapterActivity.class));

    }
   public   class refreshreceiver extends BroadcastReceiver {
       public refreshreceiver() {
       }

       @Override
        public void onReceive(Context context, Intent intent) {
           isChecked = MyApplication.gettime("isChecked", true);
               initTzl();//创建通知栏
           if (isChecked) {
//               Log.i("哈哈","接收到广播了"+isChecked);
               initTzl();//创建通知栏

           }
           if (isChecked) {
//               Log.i("哈哈","接收到广播了"+isChecked);
//               initTzl();//创建通知栏
               manager.cancelAll();
           }
        }
    }
}
