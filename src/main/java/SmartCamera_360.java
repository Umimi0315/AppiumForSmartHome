import Utils.ConnectionUtils;
import Utils.Util;
import io.appium.java_client.android.AndroidDriver;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class SmartCamera_360 {
    public static void main(String[] args) throws Exception {
        if (!Util.available()){
            return;
        }

        String ip = args[0];
        if (!Util.isIp(ip)) {
            System.out.println("输入的ip地址不合法!");
            return;
        }
        ip = Util.deleteSpace(ip);

        String portStr = args[1];
        if (!Util.isInteger(portStr)) {
            System.out.println("输入的端口号不合法!");
            return;
        }
        portStr = Util.deleteSpace(portStr);
        int port = Integer.valueOf(portStr);

        String udid=args[2];

        int appiumPort=Integer.parseInt(args[3]);

        String imsi=args[4];

        String phone=args[5];

        String phone_nation_code=args[6];

        Socket socket = new Socket(ip, port);

        OutputStream writeProgress = socket.getOutputStream();
        InputStream readProgress = socket.getInputStream();

        AndroidDriver driver =null;

        try {
            Util.waitForStart(readProgress);
            Util.sendProgressStartMessage(writeProgress);

            driver = Util.appiumParamSetting("com.qihoo.camera", "com.qihoo.jia.ui.activity.SplashActivity", writeProgress, udid, appiumPort);

            Thread.sleep(20000);

            Util.writeProgress(50,0,"正在登录360智能摄像机", writeProgress);

            if(Util.isElementExits("xpath", "//*[@resource-id='com.qihoo.camera:id/arg' and @text='同意']", driver)){
                driver.findElementByXPath("//*[@resource-id='com.qihoo.camera:id/arg' and @text='同意']").click();
                Thread.sleep(5000);
            }

            int x = driver.manage().window().getSize().width;
            int y = driver.manage().window().getSize().height;

            for (int i=0;i<2;i++){
                driver.swipe(x*9/10, y/2, x*1/10, y/2, 1000);
                Thread.sleep(5000);
            }

            if(Util.isElementExits("xpath", "//*[@class='androidx.viewpager.widget.ViewPager' and @resource-id='com.qihoo.camera:id/arg']", driver)){
                driver.findElementByXPath("//*[@class='androidx.viewpager.widget.ViewPager' and @resource-id='com.qihoo.camera:id/arg']").click();
                Thread.sleep(5000);
            }

            driver.findElementByXPath("//*[@resource-id='com.qihoo.camera:id/arg' and @text='登录']").click();
            Thread.sleep(5000);

            //验证码逻辑
            if (!ConnectionUtils.SendPhoneInfo(imsi, phone, phone_nation_code)){
                Util.writeProgress(50, 1, "服务器未准备好", writeProgress);
                return;
            }

            //获取短信逻辑
            driver.findElementByXPath("//*[@resource-id='com.qihoo.camera:id/arg' and @text='手机号']").sendKeys(phone);
            Thread.sleep(5000);


            //等待允许发短信的命令
            int status=ConnectionUtils.isReady();
            while (status!=2&&status!=1){

                if (status==-1){
                    //说明出错
                    Util.writeProgress(50, 1, "与服务器连接出错", writeProgress);
                    return;
                }
                Thread.sleep(5000);
                status=ConnectionUtils.isReady();
            }


            driver.findElementByXPath("//*[@resource-id='com.qihoo.camera:id/arg' and @text='点击获取短信验证码']").click();
            Thread.sleep(5000);

            //每隔5秒向服务器询问验证码是否准备好
            status=ConnectionUtils.getCodeStatus();
            while (status==-1){
                Thread.sleep(5000);
                status=ConnectionUtils.isReady();
            }

            Thread.sleep(8000);


            String verificationCode=ConnectionUtils.getVerficationCode(6);
            if(verificationCode==null){
                Util.writeProgress(50, 1, "短信中提取验证码出错", writeProgress);
                return;
            }

            driver.findElementByXPath("//*[@class='android.widget.EditText' and @text='短信验证码']").sendKeys(verificationCode);
            Thread.sleep(5000);


            while (Util.isElementExits("xpath", "//*[@resource-id='com.qihoo.camera:id/arg' and @text='请稍候…']", driver)){
                Thread.sleep(5000);
            }


            Util.writeProgress(100, 0, "执行完成", writeProgress);
        } catch (Exception e) {
            e.printStackTrace();

            Util.writeProgress(50, 1, "自动登录360智能摄像机出错", writeProgress);

        } finally {
            Util.sendExitMessage(writeProgress);
            Util.close(driver,readProgress, writeProgress, socket);

        }

    }
}
