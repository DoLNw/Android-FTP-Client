package com.jiacheng.ftptry;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import it.sauronsoftware.ftp4j.FTPClient;

public class InitialActivity extends AppCompatActivity {
    public static FTPClient client;

    private Toolbar toolbar;

    private ListView homeListView;
    private List<HashMap<String,Object>> homeSimpleAdaptList;;
    private SimpleAdapter homeSimpleAdapter;

    private com.baoyz.swipemenulistview.SwipeMenuListView ftpListView;
    private List<HashMap<String,Object>> ftpSimpleAdaptList;;
    private SimpleAdapter ftpSimpleAdapter;

    private Button newFTPButton;

    private com.wang.avi.AVLoadingIndicatorView loadingView;
    private TextView loadText;

    private ArrayList<String> hostNamesCopy;
    private ArrayList<String> userNamesCopy;
    private ArrayList<String> userPasswordsCopy;
    private ArrayList<String> canLoginCopy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_initial);

        initViews();
        setupData();
        setListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();

        loadingView.hide();
        loadText.setVisibility(View.INVISIBLE);

        if (InitialActivity.client != null) {
            try {
                InitialActivity.client.disconnect(false);
                InitialActivity.client = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void setupData() {
        hostNamesCopy = new ArrayList<>(Arrays.asList(getSharedPreference("host_name")));
        userNamesCopy = new ArrayList<>(Arrays.asList(getSharedPreference("user_name")));
        userPasswordsCopy = new ArrayList<>(Arrays.asList(getSharedPreference("user_password")));
        canLoginCopy = new ArrayList<>(Arrays.asList(getSharedPreference("can_login")));

        if (hostNamesCopy.size() == 1 && hostNamesCopy.get(0) == "") {
            return;
        }

        for (int i=0; i<hostNamesCopy.size(); i++) {
            HashMap<String, Object> hashMap
                    = new HashMap<>();
            hashMap.put("icon",R.drawable.ftp2);
            hashMap.put("name", userNamesCopy.get(i) + "@" + hostNamesCopy.get(i));
            //注意String相比较双等号比较是地址比较
            if (canLoginCopy.get(i).contentEquals("false")) {
                hashMap.put("name", userNamesCopy.get(i) + "@" + hostNamesCopy.get(i) + "(失败)");
            }
            ftpSimpleAdaptList.add(hashMap);
        }

        ftpSimpleAdapter.notifyDataSetChanged();
    }

    private void initViews() {
        toolbar = findViewById(R.id.my_toolbar);
        toolbar.setTitle("FTP");
        setSupportActionBar(toolbar);

        homeListView = findViewById(R.id.home_List_view);
        homeSimpleAdaptList = new ArrayList<>();
        String[] from1 = {"icon", "name"};
        int[] to1 = {R.id.cell_image, R.id.cell_name};
        HashMap<String, Object> hashMap
                = new HashMap<>();
        hashMap.put("icon",R.drawable.home);
        hashMap.put("name","Local Home");
        homeSimpleAdaptList.add(hashMap);
        homeSimpleAdapter = new SimpleAdapter(getApplicationContext(), homeSimpleAdaptList, R.layout.cell, from1, to1);
        homeListView.setAdapter(homeSimpleAdapter);

        ftpListView = findViewById(R.id.ftp_list_view);
        ftpSimpleAdaptList = new ArrayList<>();
        String[] from2 = {"icon", "name"};
        int[] to2 = {R.id.cell_image, R.id.cell_name};
        ftpSimpleAdapter = new SimpleAdapter(getApplicationContext(), ftpSimpleAdaptList, R.layout.cell, from2, to2);
        ftpListView.setAdapter(ftpSimpleAdapter);

        newFTPButton = findViewById(R.id.new_ftp);

        loadingView = findViewById(R.id.avi);

        loadText = findViewById(R.id.loadText);
    }

    private void setListeners() {
        homeListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(InitialActivity.this, LocalHomeActivity.class);
                startActivity(intent);
            }
        });

        ftpListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                loadingView.smoothToShow();
                loadText.setVisibility(View.VISIBLE);

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            InitialActivity.client = new FTPClient();
                            InitialActivity.client.connect(hostNamesCopy.get(position), 21);
                            InitialActivity.client.login(userNamesCopy.get(position), userPasswordsCopy.get(position));

                            if (InitialActivity.client.isConnected()) {
                                if (canLoginCopy.get(position).contentEquals("false")) {
                                    canLoginCopy.set(position, "true");
                                    setSharedPreferenceValues("can_login", canLoginCopy);

                                    HashMap<String, Object> hashMap
                                            = new HashMap<>();
                                    hashMap.put("icon",R.drawable.ftp2);
                                    hashMap.put("name", userNamesCopy.get(position) + "@" + hostNamesCopy.get(position));
                                    ftpSimpleAdaptList.remove(position);
                                    ftpSimpleAdaptList.add(position, hashMap);

                                    notifyDataChanged();
                                }

                                showToast("登录成功");

                                Intent intent = new Intent(InitialActivity.this, FolderActivity.class);
                                intent.putExtra("filename", File.separator);
                                intent.putExtra("username", userNamesCopy.get(position));
                                startActivity(intent);
                            } else {
                                showToast("登录失败");
                            }
                        } catch (Exception e) {
                            e.printStackTrace();

                            if (canLoginCopy.get(position).contentEquals("true")) {
                                canLoginCopy.set(position, "false");
                                setSharedPreferenceValues("can_login", canLoginCopy);

                                HashMap<String, Object> hashMap
                                        = new HashMap<>();
                                hashMap.put("icon",R.drawable.ftp2);
                                hashMap.put("name", userNamesCopy.get(position) + "@" + hostNamesCopy.get(position) + "(失败)");
                                ftpSimpleAdaptList.remove(position);
                                ftpSimpleAdaptList.add(position, hashMap);

                                notifyDataChanged();
                            }

                            showToast("登录失败");
                            hideLoadView();
                        }
                    }
                }).start();
            }
        });

        SwipeMenuCreator creator = new SwipeMenuCreator() {
            @Override
            public void create(SwipeMenu menu) {
                SwipeMenuItem deleteItem = new SwipeMenuItem(getApplicationContext());
                deleteItem.setBackground(new ColorDrawable(Color.rgb(0xF9,
                        0x3F, 0x25)));
                deleteItem.setWidth(200);
                deleteItem.setIcon(R.drawable.delete);
                menu.addMenuItem(deleteItem);
            }
        };
        ftpListView.setMenuCreator(creator);
        ftpListView.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
                switch (index) {
                    case 0:
                        hostNamesCopy.remove(position);
                        userNamesCopy.remove(position);
                        userPasswordsCopy.remove(position);
                        canLoginCopy.remove(position);
                        setSharedPreferenceValues("can_login", canLoginCopy);
                        setSharedPreferenceValues("host_name", hostNamesCopy);
                        setSharedPreferenceValues("user_name", userNamesCopy);
                        setSharedPreferenceValues("user_password", userPasswordsCopy);

                        ftpSimpleAdaptList.remove(position);
                        ftpSimpleAdapter.notifyDataSetChanged();

                    default:
                        break;
                }

                // false : close the menu; true : not close the menu
                return true;
            }
        });
        ftpListView.setSwipeDirection(SwipeMenuListView.DIRECTION_LEFT);

        newFTPButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final View cusView = LayoutInflater.from(InitialActivity.this).inflate(R.layout.login, null);
                final AlertDialog.Builder cusDia = new AlertDialog.Builder(InitialActivity.this);
                cusDia.setTitle("添加FTP地址");
                cusDia.setView(cusView);

                cusDia.setPositiveButton("保存", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        loadingView.smoothToShow();
                        loadText.setVisibility(View.VISIBLE);

                        EditText hostName = cusView.findViewById(R.id.host_name);
                        EditText userName = cusView.findViewById(R.id.user_name);
                        EditText userPassword = cusView.findViewById(R.id.user_password);

                        final String hostNameString = hostName.getText().toString().trim();
                        final String userNameString = userName.getText().toString().trim();
                        final String userPasswordString = userPassword.getText().toString().trim();

                        hostNamesCopy.add(hostNameString);
                        userNamesCopy.add(userNameString);
                        userPasswordsCopy.add(userPasswordString);
                        canLoginCopy.add("false");

                        final HashMap<String, Object> hashMap
                                = new HashMap<>();
                        hashMap.put("icon",R.drawable.ftp2);
                        hashMap.put("name", userNameString + "@" + hostNameString);
                        ftpSimpleAdaptList.add(hashMap);
                        ftpSimpleAdapter.notifyDataSetChanged();

                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    InitialActivity.client = new FTPClient();
                                    InitialActivity.client.connect(hostNameString, 21);
                                    InitialActivity.client.login(userNameString, userPasswordString);

                                    if (InitialActivity.client.isConnected()) {
                                        canLoginCopy.remove(canLoginCopy.size()-1);
                                        canLoginCopy.add("true");
                                        setSharedPreference("can_login", "true");
                                        setSharedPreference("host_name", hostNameString);
                                        setSharedPreference("user_name", userNameString);
                                        setSharedPreference("user_password", userPasswordString);

                                        showToast("登录成功");
                                        hideLoadView();

                                        Intent intent = new Intent(InitialActivity.this, FolderActivity.class);
                                        intent.putExtra("filename", File.separator);
                                        intent.putExtra("username", userNameString);
                                        startActivity(intent);
                                    } else {
                                        showToast("登录失败");
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();

                                    setSharedPreference("can_login", "false");
                                    setSharedPreference("host_name", hostNameString);
                                    setSharedPreference("user_name", userNameString);
                                    setSharedPreference("user_password", userPasswordString);

                                    hashMap.put("name", userNameString + "@" + hostNameString + "(失败)");
                                    ftpSimpleAdaptList.remove(ftpSimpleAdaptList.size()-1);
                                    ftpSimpleAdaptList.add(hashMap);


                                    notifyDataChanged();
                                    showToast("登录失败");
                                    hideLoadView();
                                }
                            }
                        }).start();
                    }
                });

                cusDia.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

                cusDia.create().show();
            }
        });
    }





    public String[] getSharedPreference(String key) {
        String regularEx = "#";
        String[] str;
        SharedPreferences sp = getSharedPreferences("FTPHost", Context.MODE_PRIVATE);
        String values;
        values = sp.getString(key, "");
        str = values.split(regularEx);

        return str;
    }
    public void setSharedPreferenceValues(String key, ArrayList<String> values) {
        String regularEx = "#";
        String str = "";
        SharedPreferences sp = getSharedPreferences("FTPHost", Context.MODE_PRIVATE);
        if (values != null && values.size() > 0) {
            for (String value : values) {
                str += value;
                str += regularEx;
            }
            SharedPreferences.Editor et = sp.edit();
            et.putString(key, str);
            et.commit();
        }
    }
    public void setSharedPreference(String key, String value) {
        String regularEx = "#";
        SharedPreferences sp = getSharedPreferences("FTPHost", Context.MODE_PRIVATE);
        String values;
        values = sp.getString(key, "");
        SharedPreferences.Editor et = sp.edit();
        et.putString(key, values+value+regularEx);
        et.commit();
    }




    //交互消息处理
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Helper.SHOW_TOAST:
                    Toast.makeText(InitialActivity.this, (String)msg.obj, Toast.LENGTH_SHORT).show();
                    break;
                case Helper.NOTIFY_DATA_CHANGED:
                    ftpSimpleAdapter.notifyDataSetChanged();
                    break;
                case Helper.SHOW_LOAD_VIEW:
                    loadingView.smoothToShow();
                    break;
                case Helper.HIDE_LOAD_VIEW:
                    loadingView.smoothToHide();
                    loadText.setVisibility(View.INVISIBLE);
                    break;
                default:
                    break;
            }
        }
    };
    private void showToast(String message) {
        Message msg = new Message();
        msg.what = Helper.SHOW_TOAST;
        msg.obj = message;
        mHandler.sendMessage(msg);
    }

    private void hideLoadView() {
        Message msg = new Message();
        msg.what = Helper.HIDE_LOAD_VIEW;
        mHandler.sendMessage(msg);
    }

    private void notifyDataChanged() {
        Message msg = new Message();
        msg.what = Helper.NOTIFY_DATA_CHANGED;
        mHandler.sendMessage(msg);
    }
}
