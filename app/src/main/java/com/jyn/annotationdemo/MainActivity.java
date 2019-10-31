package com.jyn.annotationdemo;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.jyn.annotationdemo.ioc.ViewInjector;
import com.jyn.ioc_annotation.BindView;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.text)
    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ViewInjector.inject(this);
        if (textView==null){
            Toast.makeText(this, "textView为空", Toast.LENGTH_SHORT).show();
        }else {
            textView.setText("我成功被findviewbyid了");
        }
    }
}
