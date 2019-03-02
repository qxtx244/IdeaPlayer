package org.qxtx.idea.player.demo;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

public class TestActivity extends AppCompatActivity {

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

//        ((Button)findViewById(R.id.btn)).setOnClickListener(v -> {
//            Intent intent = new Intent();
//            intent.setAction("com.vatata.player.hvod");
//            intent.putExtra("URLS", "http://47.244.3.169:9003/output/1-0-005-0213754-2018-06353839/1-0-005-0213754-2018-06353839.mpd");
//            intent.putExtra("drmServerUrl", "http://47.244.3.169:8084/proxy/visioncrypt");
//            intent.putExtra("deviceID", "");
//            intent.putExtra("contentID", "");
//            startActivityForResult(intent, 0);
//        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data != null && resultCode == Activity.RESULT_OK) {
            Toast.makeText(this, data.getStringExtra("result"), Toast.LENGTH_LONG).show();
        }
    }
}
