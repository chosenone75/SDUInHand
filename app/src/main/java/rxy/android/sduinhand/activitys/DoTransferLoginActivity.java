package rxy.android.sduinhand.activitys;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;

import rxy.android.sduinhand.R;
import rxy.android.sduinhand.utils.CM;
import rxy.android.sduinhand.utils.T;
import rxy.android.sduinhand.utils.V;
public class DoTransferLoginActivity extends AppCompatActivity {
    private Button btn_submit = null;
    private ImageView iv_checkcode = null;
    private Bitmap checkcode = null;
    private EditText edt_checkcode = null;
    private EditText edt_passwd = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_do_transfer_login);
        initViews();
        initEvents();
        PreLogin();
    }
    private void initEvents() {
        btn_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    Login();
            }
        });
        iv_checkcode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                 PreLogin();
            }
        });
    }
    private void initViews() {
        btn_submit = V.$(this,R.id.btn_login_transfer);
        iv_checkcode = V.$(this,R.id.iv_checkcode);
        edt_checkcode = V.$(this,R.id.edt_checkcode);
    }

    public void PreLogin(){
        try {
            CM.doPreTransferLogin(this,new CM.CMCallBack() {
                @Override
                public void onFail(Request request, IOException e) {
                    handler.sendEmptyMessage(0x121);
                }
                @Override
                public void onSuccess(Response response) {
                    checkcode = BitmapFactory.decodeStream(response.body().byteStream());
                    handler.sendEmptyMessage(0x123);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void Login(){
       CM.doTransferLogin("201300301187", "941005", edt_checkcode.getText().toString(), new CM.CMCallBack() {
           @Override
           public void onFail(Request request, IOException e) {
               e.printStackTrace();
               handler.sendEmptyMessage(0x125);
           }
           @Override
           public void onSuccess(Response response) {
               try {
                   String result = response.body().string();
                   Log.e("TAG",result);
                   if(result.contains("success"))
                   handler.sendEmptyMessage(0x124);
                   else
                   handler.sendEmptyMessage(0x125);
               } catch (IOException e) {
                   e.printStackTrace();
               }
           }
       });
    }
    /*
    0x123 -> 验证码获取成功 0x121 -> 获取失败
    0x124 -> 登录访问成功（不代表登录成功） 0x125 -> 登录访问失败
     */
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 0x121:
                    T.show(DoTransferLoginActivity.this,"gg", Toast.LENGTH_SHORT);
                    break;
                case 0x123:
                    iv_checkcode.setBackground(null);
                    iv_checkcode.setImageBitmap(checkcode);
                    break;
                case 0x124:
                    T.show(DoTransferLoginActivity.this,"ok", Toast.LENGTH_SHORT);
                    Intent i = new Intent(DoTransferLoginActivity.this,DoTransferActivity.class);
                    startActivity(i);
                    finish();
                    break;
                case 0x125:
                    T.show(DoTransferLoginActivity.this,"gg", Toast.LENGTH_SHORT);
                    break;
            }
        }
    };
}
