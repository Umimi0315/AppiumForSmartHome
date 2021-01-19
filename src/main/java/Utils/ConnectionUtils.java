package Utils;

import org.apache.http.Consts;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import sun.misc.BASE64Decoder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConnectionUtils {

    /**
     * 向服务端发送手机的sim卡信息
     * @param imsi
     * @param phone
     * @param phone_nation_code
     * @return
     * @throws Exception
     */
    public static boolean SendPhoneInfo(String imsi,String phone,String phone_nation_code) throws Exception {

        RequestConfig config = RequestConfig.custom().setSocketTimeout(60000).build();
        CloseableHttpClient client = HttpClients.custom().setDefaultRequestConfig(config).build();

        HttpPost post = new HttpPost("http://47.96.5.240:8989/ghost/getVerificationCode");
//        HttpPost post = new HttpPost("http://192.168.17.232:8989/ghost/getVerificationCode");

        ArrayList<NameValuePair> list = new ArrayList<NameValuePair>();

        list.add(new BasicNameValuePair("imsi", imsi));
        list.add(new BasicNameValuePair("phone", phone));
        list.add(new BasicNameValuePair("phone_nation_code", phone_nation_code));

        UrlEncodedFormEntity entity = new UrlEncodedFormEntity(list, Consts.UTF_8);

        post.setEntity(entity);

        CloseableHttpResponse response = client.execute(post);

        String result = EntityUtils.toString(response.getEntity());

        System.out.println(result);

        return "ok".equals(result);

    }

    /**
     * 查询服务端是否已获取到验证码
     * @return
     */
    public static int isReady() throws IOException {

        RequestConfig config = RequestConfig.custom().setSocketTimeout(60000).build();
        CloseableHttpClient client = HttpClients.custom().setDefaultRequestConfig(config).build();

        HttpPost post = new HttpPost("http://47.96.5.240:8989/ghost/getState");
//        HttpPost post = new HttpPost("http://192.168.17.232:8989/ghost/getState");

        CloseableHttpResponse response = client.execute(post);

        String result = EntityUtils.toString(response.getEntity());

        //正在运行
        if ("1001".equals(result)){
            return 0;
        }

        //拿到结果
        if ("1003".equals(result)){
            return 1;
        }

        //确认可以发短信
        if ("1004".equals(result)){
            return 2;
        }

        //不是以上两种结果说明出错
        return -1;

    }

    /**
     * 查询是否可以获取短信
     * @return
     */
    public static int getCodeStatus() throws Exception {

        RequestConfig config = RequestConfig.custom().setSocketTimeout(60000).build();
        CloseableHttpClient client = HttpClients.custom().setDefaultRequestConfig(config).build();

        HttpPost post = new HttpPost("http://47.96.5.240:8989/ghost/getCodeState");
//        HttpPost post = new HttpPost("http://192.168.17.232:8989/ghost/getCodeState");

        CloseableHttpResponse response = client.execute(post);

        String result = EntityUtils.toString(response.getEntity());

        if ("ok".equals(result)){
            return 1;
        }
        return -1;
    }

    /**
     * 获取短信验证码
     * @return
     */
    public static String getVerficationCode(int length) throws Exception {

        RequestConfig config = RequestConfig.custom().setSocketTimeout(60000).build();
        CloseableHttpClient client = HttpClients.custom().setDefaultRequestConfig(config).build();

        HttpPost post = new HttpPost("http://47.96.5.240:8989/ghost/getCodeContent");
//        HttpPost post = new HttpPost("http://192.168.17.232:8989/ghost/getCodeContent");

        CloseableHttpResponse response = client.execute(post);

        String result = EntityUtils.toString(response.getEntity());

        BASE64Decoder decoder = new BASE64Decoder();
        byte[] buffer = decoder.decodeBuffer(result);
        result=new String(buffer);

        System.out.println(result);

        if ("NotGet".equals(result)){
            return null;
        }

        return getYzmFromSms(result,length);
    }

    /**
     * 获取手机验证码
     * @param smsBody
     * @param length
     * @return
     */
    public static String getYzmFromSms(String smsBody,int length) {
        Pattern pattern = Pattern.compile("\\d{"+length+"}");
        Matcher matcher = pattern.matcher(smsBody);

        if (matcher.find()) {
            return matcher.group();
        }

        return null;
    }


}
