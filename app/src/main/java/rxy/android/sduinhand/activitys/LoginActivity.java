package rxy.android.sduinhand.activitys;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;

import rxy.android.sduinhand.R;
import rxy.android.sduinhand.constants.Constant;
import rxy.android.sduinhand.utils.CM;
import rxy.android.sduinhand.utils.T;
import rxy.android.sduinhand.utils.V;

/**
 * Created by Admin on 2016/5/21 0021.
 */
public class LoginActivity extends Activity {
    private EditText edt_username;
    private EditText edt_passwd;
    private Button btn_submit;

    //登录相关
    private ProgressDialog pd;
    private String username;
    private String passwd;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initViews();
        initEvents();
    }

    private void initEvents() {
        btn_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                username = edt_username.getText().toString();
                passwd = edt_passwd.getText().toString();
                if (isVaild(username) && isVaild(passwd)) {
                    pd = ProgressDialog.show(LoginActivity.this, "", "wait a minute =。=");
                    //此处调用登录方法
                    CM.doLogin(username, passwd, new CM.CMCallBack() {
                        @Override
                        public void onFail(Request request, IOException e) {
                            handler.sendEmptyMessage(0x121);
                        }
                        @Override
                        public void onSuccess(Response response) {
                            handler.sendEmptyMessage(0x123);
                        }
                    });
                } else {
                    T.show(LoginActivity.this, "还有东西没填呢", Toast.LENGTH_SHORT);
                }
            }
        });
    }

    public void initViews() {
        edt_username = V.$(this, R.id.edt_username);
        edt_passwd = V.$(this, R.id.edt_password);
        btn_submit = V.$(this, R.id.btn_login);
    }

    private boolean isVaild(String str) {
        //此处可用正则表达式后续处理
        return !str.equals("");
    }

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
           switch (msg.what){
               case 0x123:// 4 success
                    //暂时保存身份
                    Constant.username = username;
                    Constant.password = passwd;
                    pd.cancel();
                    T.show(LoginActivity.this,"got it!",Toast.LENGTH_SHORT);
                    Intent i = new Intent(LoginActivity.this,MainActivity.class);
                    startActivity(i);
                    overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
                    LoginActivity.this.finish();
                   break;
               case 0x121:// 4 failure
                   pd.cancel();
                   T.show(LoginActivity.this,"gg",Toast.LENGTH_SHORT);
                   break;
           }
        }
    };
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
