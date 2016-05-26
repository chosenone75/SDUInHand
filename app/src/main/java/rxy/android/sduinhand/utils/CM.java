package rxy.android.sduinhand.utils;

import android.content.Context;
import android.util.Log;

import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.Headers;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import rxy.android.sduinhand.constants.Constant;

/**
 * Created by Admin on 2016/5/21 0021.
 */

public class CM {

    private static final String TAG = "CM";
    private static OkHttpClient bksClient = new OkHttpClient();
    private static OkHttpClient transferClient = new OkHttpClient();

    public static void doLogin(String username, String password, final CMCallBack cmcb) {
        Headers.Builder hb = new Headers.Builder();
        hb.add("Content-Type", "application/x-www-form-urlencoded");
        hb.add("User-Agent", "Mozilla/5.0 (Windows NT 6.3; WOW64; rv:45.0) Gecko/20100101 Firefox/45.0");
        hb.add("Referer", "http://jwxt.sdu.edu.cn:7890/zhxt_bks/xk_login.html");
        FormEncodingBuilder fb = new FormEncodingBuilder();
        fb.add("stuid", username);
        fb.add("pwd", password);
        final Request request = new Request.Builder()
                .url(Constant.BKS_URL).headers(hb.build()).post(fb.build()).build();
        Call call = bksClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                  cmcb.onFail(request,e);
            }
            @Override
            public void onResponse(Response response) throws IOException {
                 if(!response.body().string().contains("错误"))
                  cmcb.onSuccess(response);
                 else
                  cmcb.onFail(null,null);
            }
        });
    }

    public static void doPreTransferLogin(Context cnt,final CMCallBack cmcb) throws KeyManagementException, NoSuchAlgorithmException {
        initClient4Https(cnt);
        Headers.Builder hb = new Headers.Builder();
        hb.add("User-Agent",
                "Mozilla/5.0 (Windows NT 6.3; WOW64; rv:46.0) Gecko/20100101 Firefox/46.0");
        final Request request = new Request.Builder().url(Constant.CARD_CHECKCODE_URL).headers(hb.build()).build();
        Call call = transferClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                Log.e(TAG,"FAIL!\n"+e.getMessage());
                cmcb.onFail(request,e);
            }
            @Override
            public void onResponse(Response response) throws IOException {
                Log.e(TAG,"SUCCEED!\n");
                cmcb.onSuccess(response);
            }
        });
    }



    private static void initClient4Https(Context cnt) throws NoSuchAlgorithmException, KeyManagementException {
        //初始化
        transferClient.setConnectTimeout(8000, TimeUnit.MILLISECONDS);
        transferClient.setReadTimeout(8000,TimeUnit.MILLISECONDS);
        transferClient.setCookieHandler(new CookieManager(new PersistentCookieStore(cnt), CookiePolicy.ACCEPT_ALL));

        //第一种方法信任所有的证书
        SSLContext sc = SSLContext.getInstance("SSL");
        sc.init(null,new TrustManager[]{new X509TrustManager() {
            @Override
            public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            }
            @Override
            public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            }
            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0];
            }
        }},new SecureRandom());

        transferClient.setSslSocketFactory(sc.getSocketFactory());
        transferClient.setHostnameVerifier(new HostnameVerifier() {
            @Override
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        });
        //第二种方法保存对应的证书
        //待定
    }

    public static  void doTransferLogin(String username, String passwd, String checkcode, final CMCallBack cmcb){
        Headers.Builder hb = new Headers.Builder();
        hb.add("User-Agent",
                "Mozilla/5.0 (Windows NT 6.3; WOW64; rv:46.0) Gecko/20100101 Firefox/46.0");
        hb.add("Referer", "https://card.sdu.edu.cn/");
        FormEncodingBuilder fb = new FormEncodingBuilder();
        fb.add("checkcode", checkcode);
        fb.add("password", passwd);
        fb.add("signtype","SynSno");
        fb.add("isUsedKeyPad","false");
        fb.add("username",username);
        final Request request = new Request.Builder()
                .url(Constant.CARD_CHECK_IN).headers(hb.build()).post(fb.build()).build();
        Call call = transferClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                cmcb.onFail(request,e);
            }
            @Override
            public void onResponse(Response response) throws IOException {
                    cmcb.onSuccess(response);
            }
        });
    }

    public  static void FetchTransferHtml(){
        Headers.Builder hb = new Headers.Builder();
        hb.add("User-Agent",
                "Mozilla/5.0 (Windows NT 6.3; WOW64; rv:46.0) Gecko/20100101 Firefox/46.0");
        FormEncodingBuilder fb = new FormEncodingBuilder();
        fb.add("needHeader","false");
        final Request request = new Request.Builder().url(Constant.CARD_TRANSFER_HTML).headers(hb.build()).post(fb.build()).build();
        Call call = transferClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
            }
            @Override
            public void onResponse(Response response) throws IOException {
            }
        });
    }

    public static void FetchNumberPad(final CMCallBack cmcb){
        Headers.Builder hb = new Headers.Builder();
        hb.add("User-Agent",
                "Mozilla/5.0 (Windows NT 6.3; WOW64; rv:46.0) Gecko/20100101 Firefox/46.0");
        final Request request = new Request.Builder().url(Constant.CARD_NUMBER_PAD).headers(hb.build()).build();
        Call call = transferClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                cmcb.onFail(request,e);
            }
            @Override
            public void onResponse(Response response) throws IOException {
                cmcb.onSuccess(response);
            }
        });
    }
    public static void FetchCheckCodeWhenDoPay(final CMCallBack cmcb){
        Headers.Builder hb = new Headers.Builder();
        hb.add("User-Agent",
                "Mozilla/5.0 (Windows NT 6.3; WOW64; rv:46.0) Gecko/20100101 Firefox/46.0");
        final Request request = new Request.Builder().url(Constant.CARD_DO_PAY_CHECKCODE).headers(hb.build()).build();
        Call call = transferClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                cmcb.onFail(request,e);
            }
            @Override
            public void onResponse(Response response) throws IOException {
                cmcb.onSuccess(response);
            }
        });
    }

    public static void DoPay(String passwd,String bankno,String amount,String checkcode,final CMCallBack cmcb){
        Headers.Builder hb = new Headers.Builder();
        hb.add("User-Agent",
                "Mozilla/5.0 (Windows NT 6.3; WOW64; rv:46.0) Gecko/20100101 Firefox/46.0");
        hb.add("Referer", "https://card.sdu.edu.cn/");
        hb.add("X-Requested-With",
                "XMLHttpRequest");
        FormEncodingBuilder fb = new FormEncodingBuilder();
        fb.add("pwd",passwd);
        fb.add("bankno", bankno);
        fb.add("bankpwd","");
        fb.add("checkcode", checkcode);
        fb.add("amt",amount);
        fb.add("from","Bank");
        fb.add("fromtype","Bank");
        fb.add("to","Card");
        fb.add("totype","Card");
        final Request request = new Request.Builder()
                .url(Constant.CARD_DO_PAY).headers(hb.build()).post(fb.build()).build();
        Call call = transferClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                cmcb.onFail(request,e);
            }
            @Override
            public void onResponse(Response response) throws IOException {
                cmcb.onSuccess(response);
            }
        });
    }
    public interface CMCallBack{
        public void onFail(Request request, IOException e);
        public void onSuccess(Response response);
    }
}