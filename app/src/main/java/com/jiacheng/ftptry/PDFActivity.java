package com.jiacheng.ftptry;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener;

import java.io.File;

public class PDFActivity extends AppCompatActivity {
    private String filePath;
    private String filename;
    private Toolbar toolbar;

    private com.github.barteksc.pdfviewer.PDFView pdfView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf);

        filePath = getIntent().getStringExtra("filepath");
        filename = getIntent().getStringExtra("filename");

        initViews();

        showTxt();
    }

    private void initViews() {
        pdfView = findViewById(R.id.pdfView);

        toolbar = findViewById(R.id.my_toolbar);
        toolbar.setTitle(filename);
        setSupportActionBar(toolbar);
    }


    private void showTxt() {
        File file = new File(filePath);

        pdfView.fromFile(file).enableSwipe(true).swipeHorizontal(false).enableDoubletap(false).defaultPage(0)
        .onLoad(new OnLoadCompleteListener() {
            @Override
            public void loadComplete(int nbPages) {

            }
        }).onPageChange(new OnPageChangeListener() {
            @Override
            public void onPageChanged(int page, int pageCount) {

            }
        }).enableAnnotationRendering(false).password(null).scrollHandle(null).enableAntialiasing(true).spacing(0).load();
    }
}
