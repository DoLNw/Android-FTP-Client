package com.jiacheng.ftptry;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

public class TxtActivity extends AppCompatActivity {
    private String filePath;
    private String filename;
    private Toolbar toolbar;

    private TextView scrollTxtView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_txt);

        filePath = getIntent().getStringExtra("filepath");
        filename = getIntent().getStringExtra("filename");

        initViews();

        showTxt();
    }

    private void initViews() {
        scrollTxtView = findViewById(R.id.scrollTxtView);

        toolbar = findViewById(R.id.my_toolbar);
        toolbar.setTitle(filename);
        setSupportActionBar(toolbar);
    }


    private void showTxt() {
        StringBuffer sb = new StringBuffer();
        File file = new File(filePath);
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            while((line = br.readLine())!=null){
                sb.append(line + "\n");
            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        scrollTxtView.setText(sb.toString());

        if (sb.toString() == "") {
            scrollTxtView.setText("Nothing here...");
        }
    }
}
