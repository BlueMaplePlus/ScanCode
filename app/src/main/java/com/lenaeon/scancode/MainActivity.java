package com.lenaeon.scancode;

import android.app.ActionBar;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.app.Instrumentation;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.lenaeon.scancode.zxing.utils.Constant;

import java.lang.reflect.Method;

import butterknife.ButterKnife;
import butterknife.OnClick;


public class MainActivity extends Activity {

    private ViewPager mViewPager;
    private DrawerLayout slideMenu;
    private ActionBar actionBar;

    ImageView home_footer_main_btn;
    TextView home_footer_main_txt;
    ImageView home_footer_message_btn;
    TextView home_footer_message_txt;
    ImageView home_footer_report_btn;
    TextView home_footer_report_txt;
    ImageView home_footer_user_btn;
    TextView home_footer_user_txt;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        int mode = getIntent().getIntExtra(Constant.REQUEST_SCAN_MODE, Constant.REQUEST_SCAN_MODE_ALL_MODE);
        initView();

        mViewPager = (ViewPager) findViewById(R.id.view_pager);

        actionBar = getActionBar();
        slideMenu = (DrawerLayout) findViewById(R.id.slide_menu);


        slideMenu.setScrimColor(Color.argb(50, 0, 0, 0));

        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        ActionBar.TabListener tabListener = new ActionBar.TabListener() {
            @Override
            public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
                mViewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {

            }

            @Override
            public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {

            }
        };

        TypedArray tabIconIds = getResources().obtainTypedArray(R.array.actionbar_icons);
        for (int i = 0; i < 3; i++) {
            View view = getLayoutInflater().inflate(R.layout.actionbar_tab, null);
            ImageView tabIcon = (ImageView) view.findViewById(R.id.actionbar_tab_icon);
            tabIcon.setImageResource(tabIconIds.getResourceId(i, -1));

            actionBar.addTab(actionBar.newTab().setCustomView(view).setTabListener(tabListener));
        }

        enableEmbeddedTabs(actionBar);
    }

    /**
     * 在actionbar中内嵌Tab
     *
     * @param actionBar actionbar
     */
    private void enableEmbeddedTabs(android.app.ActionBar actionBar) {
        try {
            Method setHasEmbeddedTabsMethod = actionBar.getClass().getDeclaredMethod("setHasEmbeddedTabs", boolean.class);
            setHasEmbeddedTabsMethod.setAccessible(true);
            setHasEmbeddedTabsMethod.invoke(actionBar, true);
        } catch (Exception e) {
            Log.v("enableEmbeddedTabs", e.getMessage().toString());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    void initView() {
        Intent intent = getIntent();
        boolean clickble = intent.getBooleanExtra("clickble", true);

        home_footer_main_btn = (ImageView) findViewById(R.id.home_footer_main_btn);
        home_footer_main_txt = (TextView) findViewById(R.id.home_footer_main_txt);
        home_footer_message_btn = (ImageView) findViewById(R.id.home_footer_message_btn);
        home_footer_message_txt = (TextView) findViewById(R.id.home_footer_message_txt);
        home_footer_report_btn = (ImageView) findViewById(R.id.home_footer_report_btn);
        home_footer_report_txt = (TextView) findViewById(R.id.home_footer_report_txt);
        home_footer_user_btn = (ImageView) findViewById(R.id.home_footer_user_btn);
        home_footer_user_txt = (TextView) findViewById(R.id.home_footer_user_txt);

        home_footer_main_btn.setSelected(clickble);
    }

    void initHomeFooterBtn() {
        int imageSource;
        imageSource = (home_footer_main_btn.isSelected() ? R.drawable.home_footbar_main_2 : R.drawable.home_footbar_main_btn);
        home_footer_main_btn.setBackgroundResource(imageSource);
        imageSource = (home_footer_message_btn.isSelected() ? R.drawable.home_footbar_message_2 : R.drawable.home_footbar_message_btn);
        home_footer_message_btn.setBackgroundResource(imageSource);
        imageSource = (home_footer_report_btn.isSelected() ? R.drawable.home_footbar_report_2 : R.drawable.home_footbar_report_btn);
        home_footer_report_btn.setBackgroundResource(imageSource);
        imageSource = (home_footer_user_btn.isSelected() ? R.drawable.home_footbar_user_2 : R.drawable.home_footbar_user_btn);
        home_footer_user_btn.setBackgroundResource(imageSource);
    }

    /**
     * 按钮监听事件，这里我使用Butterknife，不喜欢的也可以直接写监听
     *
     * @param view
     */
    @OnClick({R.id.create_code, R.id.scan_2code, R.id.scan_bar_code, R.id.scan_code, R.id.home_footer_scan_btn
            , R.id.home_footer_main_btn, R.id.home_footer_main_txt, R.id.home_footer_message_btn, R.id.home_footer_message_txt,
            R.id.home_footer_report_btn, R.id.home_footer_report_txt, R.id.home_footer_user_btn, R.id.home_footer_user_txt})
    public void clickListener(View view) {
        Intent intent;
        switch (view.getId()) {
            case R.id.create_code: //生成码
                intent = new Intent(this, CreateCodeActivity.class);
                startActivity(intent);
                break;
            case R.id.scan_2code: //扫描二维码
                intent = new Intent(this, CommonScanActivity.class);
                intent.putExtra(Constant.REQUEST_SCAN_MODE, Constant.REQUEST_SCAN_MODE_QRCODE_MODE);
                startActivity(intent);
                break;
            case R.id.scan_bar_code://扫描条形码
                intent = new Intent(this, CommonScanActivity.class);
                intent.putExtra(Constant.REQUEST_SCAN_MODE, Constant.REQUEST_SCAN_MODE_BARCODE_MODE);
                startActivity(intent);
                break;


            case R.id.home_footer_main_btn:
            case R.id.home_footer_main_txt:
                home_footer_main_btn.setSelected(true);
                home_footer_main_txt.setSelected(true);
                home_footer_message_btn.setSelected(false);
                home_footer_message_txt.setSelected(false);
                home_footer_report_btn.setSelected(false);
                home_footer_report_txt.setSelected(false);
                home_footer_user_btn.setSelected(false);
                home_footer_user_txt.setSelected(false);
                initHomeFooterBtn();
                break;
            case R.id.home_footer_message_btn:
            case R.id.home_footer_message_txt:
                home_footer_main_btn.setSelected(false);
                home_footer_main_txt.setSelected(false);
                home_footer_message_btn.setSelected(true);
                home_footer_message_txt.setSelected(true);
                home_footer_report_btn.setSelected(false);
                home_footer_report_txt.setSelected(false);
                home_footer_user_btn.setSelected(false);
                home_footer_user_txt.setSelected(false);
                initHomeFooterBtn();
                break;
            case R.id.home_footer_report_btn:
            case R.id.home_footer_report_txt:
                home_footer_main_btn.setSelected(false);
                home_footer_main_txt.setSelected(false);
                home_footer_message_btn.setSelected(false);
                home_footer_message_txt.setSelected(false);
                home_footer_report_btn.setSelected(true);
                home_footer_report_txt.setSelected(true);
                home_footer_user_btn.setSelected(false);
                home_footer_user_txt.setSelected(false);
                initHomeFooterBtn();
                break;
            case R.id.home_footer_user_btn:
            case R.id.home_footer_user_txt:
                home_footer_main_btn.setSelected(false);
                home_footer_main_txt.setSelected(false);
                home_footer_message_btn.setSelected(false);
                home_footer_message_txt.setSelected(false);
                home_footer_report_btn.setSelected(false);
                home_footer_report_txt.setSelected(false);
                home_footer_user_btn.setSelected(true);
                home_footer_user_txt.setSelected(true);
                initHomeFooterBtn();
                //弹出菜单项目
                //openOptionsMenu();
                //simulateKey(KeyEvent.KEYCODE_MENU);
                break;
            // 扫描条形码或者二维码
            case R.id.home_footer_scan_btn:
            case R.id.scan_code:
                intent = new Intent(this, CommonScanActivity.class);
                intent.putExtra(Constant.REQUEST_SCAN_MODE, Constant.REQUEST_SCAN_MODE_ALL_MODE);
                startActivity(intent);
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_search) {
            // TODO: 2015-07-25 搜索界面
        } else if (id == R.id.action_menu) {
            // TODO: 2015-07-25 菜单选择
            if (slideMenu.isDrawerOpen(Gravity.LEFT)) {
                slideMenu.closeDrawer(Gravity.LEFT);
            } else {
                slideMenu.openDrawer(Gravity.LEFT);
            }
        }

        return true;
    }

    public static void simulateKey(final int KeyCode) {
        new Thread() {
            public void run() {
                try {
                    Instrumentation inst = new Instrumentation();
                    inst.sendKeyDownUpSync(KeyCode);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }
}
