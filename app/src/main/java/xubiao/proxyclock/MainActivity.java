package xubiao.proxyclock;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.*;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;

import xubiao.proxyclock.utils.NetUtil;

import android.webkit.WebView;

import org.json.*;


public class MainActivity extends AppCompatActivity {
    public final static int REQUEST_READ_PHONE_STATE = 1;
    String mUserAgent;
    String token;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, REQUEST_READ_PHONE_STATE);
        } else {
            //TODO
        }
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "For emergency needs", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        Button btn_login = (Button) findViewById(R.id.button_login);
        Button btn_clock = (Button) findViewById(R.id.button_clock);
        Button btn_info = (Button) findViewById(R.id.button_info);
        btn_login.setOnClickListener(new MyListener());
        btn_clock.setOnClickListener(new MyListener());
        btn_info.setOnClickListener(new MyListener());

        WebView mWebView = new WebView(this);
        mUserAgent =mWebView.getSettings().getUserAgentString();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private class MyListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            TextView textusername = (TextView) findViewById(R.id.text_username);
            String username = textusername.getText().toString();
            TextView textdevid = (TextView) findViewById(R.id.text_devid);
            if (username.equals("junkai.niu")){
                textdevid.setText("7732fb7cf19c263e");
            }
            if (username.equals("xiangdong.meng")){
                textdevid.setText("e68376563d42f609");
            }
            if (username.equals("biao.xu")
                    || username.equals("wenzhe.hao")
                    || username.equals("junkai.niu")
                    || username.equals("qinggang.gao")
                    || username.equals("zhenmin.wei")
                    || username.equals("xiangdong.meng")
                    ){
                switch (view.getId()){
                    case R.id.button_login:
                        login();
                        break;
                    case R.id.button_clock:
                        clock();
                        break;
                    case R.id.button_info:
                        getInfo();
                        break;
                }
            }else{
                TextView textmessage = (TextView) findViewById(R.id.text_message);
                textmessage.setText("无效的用户！！");
            }
        }
    }

    public void login(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                // url
                String url = "http://192.168.100.169/ws/getToken";
                // data
                TextView textusername = (TextView) findViewById(R.id.text_username);
                TextView textpassword = (TextView) findViewById(R.id.text_password);
                String username = textusername.getText().toString();
                String password = textpassword.getText().toString();
                JSONStringer jsonText = new JSONStringer();
                try {
                    jsonText.object();
                    jsonText.key("username");
                    jsonText.value(username);
                    jsonText.key("password");
                    jsonText.value(password);
                    jsonText.endObject();
                } catch (JSONException ex) {
                    throw new RuntimeException(ex);
                }

                String dataStr = jsonText.toString();
                // header
                HashMap<String,String> headers = new HashMap<String,String>();
                headers.put("Host", "192.168.100.169");
                headers.put("Connection", "keep-alive");
                headers.put("Content-Length", String.valueOf(dataStr.length()));
                headers.put("Accept", "application/json, text/plain, */*");
                headers.put("Origin", "file://");
                headers.put("User-Agent", mUserAgent);
                headers.put("Content-Type", "application/json;charset=UTF-8");
                headers.put("Accept-Encoding", "gzip, deflate");
                headers.put("Accept-Language", "zh-cn-#hans");

                final String state=NetUtil.doPost(url,dataStr.toString(),headers);
                //执行在主线程上
                runOnUiThread(new Runnable() {
                    public void run() {
                        //就是在主线程上操作,弹出结果
                        TextView textmessage = (TextView) findViewById(R.id.text_message);
                        textmessage.setText("");
                        textmessage.setText(textmessage.getText(), TextView.BufferType.EDITABLE);
                        System.out.println(state);
                        textmessage.append("-------------------------" + "\n");
                        try {
                            JSONObject jsonObject = new JSONObject(state);
                            String status = jsonObject.getString("resultType");
                            System.out.println(status);
                            if (status.equals("1")){
                                token = jsonObject.getString("TOKEN");
                                textmessage.append("auth success！！！my token is" + "\n");
                                textmessage.append(token + "\n");
                            }else{
                                textmessage.append("auth failed" + "\n");
                                textmessage.append(state + "\n");
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            textmessage.append(e.toString() + "\n");
                        }
                        textmessage.append("-------------------------" + "\n");

                        getNotice();
                        getInfo();
                    }
                });

            }
        }).start();
    }

    public void clock(){
        if(token==null){
            TextView textmessage = (TextView) findViewById(R.id.text_message);

            TextView textDevId = (TextView) findViewById(R.id.text_devid);
            String devId = textDevId.getText().toString();
            String dataStr = "{\"SLOC\":[\"电通创意广场\"],\"ISAPP\":\"1\",\"DEVID\":\"devID\"}";
            dataStr = dataStr.replace("devID",devId);
            textmessage.setText("please auth first !!!" + dataStr + "\n");
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                // url
                String url = "http://192.168.100.169/ws/saveCheckInfo";
                TextView textDevId = (TextView) findViewById(R.id.text_devid);
                String devId = textDevId.getText().toString();
                // data
//                JSONStringer jsonText = new JSONStringer();
//                try {
//                    jsonText.object();
//                    jsonText.key("SLOC");
//                    ArrayList l = new ArrayList();
//                    l.add("电通创意广场");
//                    jsonText.value(l);
//                    jsonText.key("ISAPP");
//                    jsonText.value("1");
//                    jsonText.endObject();
//                } catch (JSONException ex) {
//                    throw new RuntimeException(ex);
//                }
//                String dataStr = jsonText.toString();
//                String devId = TelephonyManager.getDeviceId();

                String dataStr = "{\"SLOC\":[\"电通创意广场\"],\"ISAPP\":\"1\",\"DEVID\":\"devID\"}";
                dataStr = dataStr.replace("devID",devId);
//                String dataStr1 = "{\"SLOC\":[\"电通创意广场\"],\"ISAPP\":\"1\",\"DEVID\":\"7732fb7cf19c263e\"}";

                // header
                HashMap<String,String> headers = new HashMap<String,String>();
                headers.put("Host", "192.168.100.169");
                headers.put("Connection", "keep-alive");
                int dataStrLen = 0;
                dataStrLen = dataStr.getBytes().length;
                headers.put("Content-Length", String.valueOf(dataStrLen));
                headers.put("Accept", "application/json, text/plain, */*");
                headers.put("Origin", "file://");
                headers.put("User-Agent", mUserAgent);
                headers.put("Authorization", token);
                headers.put("Content-Type", "application/json;charset=UTF-8");
                headers.put("Accept-Encoding", "gzip, deflate");
                headers.put("Accept-Language", "zh-cn-#hans");

                final String state=NetUtil.doPost(url,dataStr.toString(),headers);
                //执行在主线程上
                runOnUiThread(new Runnable() {
                    public void run() {
                        //就是在主线程上操作,弹出结果
                        TextView textmessage = (TextView) findViewById(R.id.text_message);
                        textmessage.setText("");
                        textmessage.setText(textmessage.getText(), TextView.BufferType.EDITABLE);
                        textmessage.append("-------------------------" + "\n");
                        try {
                            JSONObject jsonObject = new JSONObject(state);
                            String status = jsonObject.getString("resultType");
                            if (status.equals("1")){
                                textmessage.append("clock success" + "\n");
                                JSONArray data = jsonObject.getJSONArray("dataList");
                                for (int j= 0;j<data.length();j++) {
                                    JSONObject d = data.getJSONObject(j);
                                    textmessage.append(d.getString("SDATE") + " " + d.getString("SATS")
                                            + "   " + d.getString("SLOC") + "\n");
                                }
                            }else{
                                textmessage.append("clock failed" + "\n");
                                textmessage.append(state + "\n");
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            textmessage.append(e.toString() + "|" + state + "\n");
                        }
                        textmessage.append("-------------------------" + "\n");
                        getNotice();
                        getInfo();
                    }
                });

            }
        }).start();
    }

    public void getInfo(){
        if(token==null){
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                // url
                String url = "http://192.168.100.169/ws/getCheckInfo";
                // header
                HashMap<String,String> headers = new HashMap<String,String>();
                headers.put("Host", "192.168.100.169");
                headers.put("Connection", "keep-alive");
                headers.put("Accept", "application/json, text/plain, */*");
                headers.put("Authorization", token);
                headers.put("User-Agent", mUserAgent);
                headers.put("Accept-Encoding", "gzip, deflate");
                headers.put("Accept-Language", "zh-cn-#hans");

                final String state=NetUtil.doGet(url,headers);
                //执行在主线程上
                runOnUiThread(new Runnable() {
                    public void run() {
                        //就是在主线程上操作,弹出结果
                        TextView textmessage = (TextView) findViewById(R.id.text_message);
                        textmessage.setText(textmessage.getText(), TextView.BufferType.EDITABLE);
                        try {
                            JSONObject jsonObject = new JSONObject(state);
                            String status = jsonObject.getString("resultType");
                            if (status.equals("1")){
                                textmessage.append("get clock info success！！！" + "\n");
                                JSONArray data = jsonObject.getJSONArray("dataList");
                                for (int j= 0;j<data.length();j++){
                                    JSONObject d = data.getJSONObject(j);
                                    textmessage.append(d.getString("SDATE") + " " + d.getString("SATS")
                                            + "   " + d.getString("SLOC")  + "\n");
                                }
                            }else{
                                textmessage.append("get clock info failed" + "\n");
                                textmessage.append(state + "\n");
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            textmessage.append(e.toString() + "\n");
                        }
                        textmessage.append("-------------------------" + "\n");
                    }
                });

            }
        }).start();
    }

    public void getNotice(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                // url
                String url = "http://192.168.100.169/ws/getNotice";
                // header
                HashMap<String,String> headers = new HashMap<String,String>();
                headers.put("Host", "192.168.100.169");
                headers.put("Connection", "keep-alive");
                headers.put("Accept", "application/json, text/plain, */*");
                headers.put("Authorization", token);
                headers.put("User-Agent", mUserAgent);
                headers.put("Accept-Encoding", "gzip, deflate");
                headers.put("Accept-Language", "zh-cn-#hans");

                final String state=NetUtil.doGet(url,headers);
                //执行在主线程上
                runOnUiThread(new Runnable() {
                    public void run() {
                        //就是在主线程上操作,弹出结果
                        TextView textmessage = (TextView) findViewById(R.id.text_message);
                        textmessage.setText(textmessage.getText(), TextView.BufferType.EDITABLE);
                        try {
                            JSONObject jsonObject = new JSONObject(state);
                            String status = jsonObject.getString("resultType");
                            if (status.equals("1")){
                                JSONArray data = jsonObject.getJSONArray("dataList");
                                textmessage.append("get notice success！！！" + "\n");
                                for (int j= 0;j<data.length();j++){
                                    JSONObject d = data.getJSONObject(j);
                                    textmessage.append(d.getString("TITLE")+ "  " + d.getString("PUBTIME") + "\n");
                                    textmessage.append(d.getString("CONTENT")+ "\n");
                                    textmessage.append("\n");
                                }
                            }else{
                                textmessage.append("get notice failed" + "\n");
                                textmessage.append(state + "\n");
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            textmessage.append(e.toString() + "\n");
                        }
                        textmessage.append("-------------------------" + "\n");
                    }
                });

            }
        }).start();
    }
}
