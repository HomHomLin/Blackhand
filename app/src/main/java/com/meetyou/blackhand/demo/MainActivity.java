package com.meetyou.blackhand.demo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public static void test1(){

    }

    public static int test2(){
        return 1;
    }

    private String onActivity2(String[] var1) {
        return (String)null;
    }


    private String onActivity2(int var1) {
        return (String)null;
    }

}
