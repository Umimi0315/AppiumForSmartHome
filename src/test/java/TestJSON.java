import Utils.ConnectionUtils;
import Utils.Util;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonObject;
import io.appium.java_client.android.AndroidDriver;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.Consts;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.junit.Test;
import org.omg.CORBA.PUBLIC_MEMBER;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class TestJSON {

    @Test
    public void testHttp() throws IOException {
        CloseableHttpClient client = HttpClients.createDefault();

        HttpPost post = new HttpPost("http://localhost:8989/ghost/send!m.action");

        ArrayList<NameValuePair> list = new ArrayList<NameValuePair>();

        list.add(new BasicNameValuePair("imsi", "test_imsi"));
        list.add(new BasicNameValuePair("phone", "18061714787"));
        list.add(new BasicNameValuePair("phone_nation_code", "86"));

        UrlEncodedFormEntity entity = new UrlEncodedFormEntity(list, Consts.UTF_8);

        post.setEntity(entity);

        CloseableHttpResponse response = client.execute(post);

        System.out.println(EntityUtils.toString(response.getEntity()));
    }

    @Test
    public void testHttp2() throws Exception {
        //验证码逻辑
        if (!ConnectionUtils.SendPhoneInfo("460037470512265", "17751000315", "86")){
/*            Util.writeProgress(50, 1, "服务器未准备好", writeProgress);
            return;*/
        }


        //等待允许发短信的命令
        int status=ConnectionUtils.isReady();
        while (status!=2&&status!=1){

            if (status==-1){
                //说明出错
/*                Util.writeProgress(50, 1, "与服务器连接出错", writeProgress);
                return;*/
            }
            Thread.sleep(5000);
            status=ConnectionUtils.isReady();
        }


/*        driver.findElementById("com.xiaomi.smarthome:id/ph_sign_in_btn").click();
        Thread.sleep(5000);*/


        //每隔5秒向服务器询问验证码是否准备好
        status=ConnectionUtils.getCodeStatus();
        while (status==-1){
            Thread.sleep(5000);
            status=ConnectionUtils.isReady();
        }


        String verificationCode=ConnectionUtils.getVerficationCode(6);

        System.out.println(verificationCode);
        if(verificationCode==null){
 /*           Util.writeProgress(50, 1, "短信中提取验证码出错", writeProgress);
            return;*/
        }

/*        driver.findElementById("com.xiaomi.smarthome:id/ticket").sendKeys(verificationCode);
        Thread.sleep(5000);

        driver.findElementById("com.xiaomi.smarthome:id/ph_sign_in_btn").click();
        Thread.sleep(10000);*/

       /* Util.writeProgress(100, 0, "执行完成", writeProgress);*/
        System.out.println(verificationCode);
    }

    @Test
    public void testThread() throws InterruptedException {
        Thread t1=new Thread(new Runnable() {
            @Override
            public void run() {
                count();
            }
        });
        t1.start();
        Thread.sleep(5000);
        t1.stop();
    }

    public void count(){

        try{
            while (true){
                int t=1+1;
            }
        }catch (Exception e) {
            e.printStackTrace();
        }finally {
            System.out.println("执行了finally方法");
        }

    }



}
