package rxy.android.sduinhand.activitys;

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

import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;

import rxy.android.sduinhand.R;
import rxy.android.sduinhand.utils.CM;
import rxy.android.sduinhand.utils.T;
import rxy.android.sduinhand.utils.V;
public class DoTransferActivity extends AppCompatActivity implements View.OnClickListener{
    private EditText edt_bankcard;
    private EditText edt_money;
    private EditText edt_checkcode;
    private EditText edt_passwd;
    private ImageView iv_checkcode;
    private ImageView iv_number_pad;// only for test
    private Button btn_submit;

    //中间结果
    private Bitmap numberPad;
    private Bitmap checkCode;
    private String dopay_result;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_do_transfer);
        initViews();
        initEvents();

        FetchTransHtml();
        FetchNumberPad();
        FetchCheckCode();
    }
    private void FetchTransHtml() {
        CM.FetchTransferHtml();
    }

    private void FetchCheckCode() {
        CM.FetchCheckCodeWhenDoPay(new CM.CMCallBack() {
            @Override
            public void onFail(Request request, IOException e) {
                handler.sendEmptyMessage(0x123);
            }
            @Override
            public void onSuccess(Response response) {
                 checkCode = BitmapFactory.decodeStream(response.body().byteStream());
                 handler.sendEmptyMessage(0x124);
            }
        });
    }

    private void FetchNumberPad() {
       CM.FetchNumberPad(new CM.CMCallBack() {
           @Override
           public void onFail(Request request, IOException e) {
                 handler.sendEmptyMessage(0x121);
           }
           @Override
           public void onSuccess(Response response) {
                  numberPad = BitmapFactory.decodeStream(response.body().byteStream());
                  handler.sendEmptyMessage(0x122);
           }
       });
    }
    private void initEvents() {
           iv_checkcode.setOnClickListener(this);
           iv_number_pad.setOnClickListener(this);
           btn_submit.setOnClickListener(this);
    }

    private void initViews() {
        edt_bankcard = V.$(this,R.id.edt_bankcard);
        edt_money = V.$(this,R.id.edt_money);
        edt_checkcode = V.$(this,R.id.edt_checkcode_transfer);
        edt_passwd = V.$(this,R.id.edt_passwd);
        iv_checkcode = V.$(this,R.id.iv_checkcode_transfer);
        iv_number_pad = V.$(this,R.id.iv_number_pad);
        btn_submit = V.$(this,R.id.btn_login_transfer);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.iv_checkcode_transfer:
                FetchCheckCode();
                break;
            case R.id.iv_number_pad:
                FetchNumberPad();
                break;
            case R.id.btn_login_transfer:
                DoPay();
                break;
        }
    }

    private void DoPay() {
        String passwd = edt_passwd.getText().toString();
        String bankno = edt_bankcard.getText().toString();
        String check_code = edt_checkcode.getText().toString();
        String amount = edt_money.getText().toString();
        if(isVaild(passwd)
                &&isVaild(bankno)&&isVaild(check_code)&&isVaild(amount))
        CM.DoPay(passwd, bankno, amount, check_code, new CM.CMCallBack() {
            @Override
            public void onFail(Request request, IOException e) {
                   handler.sendEmptyMessage(0x125);
            }
            @Override
            public void onSuccess(Response response) {
                try {
                    dopay_result = response.body().string();
                    Log.e("DoPay",dopay_result);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                handler.sendEmptyMessage(0x126);
            }
        });
    }

    private boolean isVaild(String str) {
        //此处后续可使用正则表达式处理
        return !str.equals("");
    }

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 0x121://numberpad failed
                    break;
                case 0x122://numberpad succeed
                    iv_number_pad.setBackground(null);
                    iv_number_pad.setImageBitmap(numberPad);
                    break;
                case 0x123://checkcode failed
                    break;
                case 0x124://checkcode succeed
                    iv_checkcode.setBackground(null);
                    iv_checkcode.setImageBitmap(checkCode);
                    break;
                case 0x125://dopay failed
                    break;
                case 0x126://dopay succeed
                    T.showShort(DoTransferActivity.this,dopay_result);
                    break;
            }
        }
    };
}
