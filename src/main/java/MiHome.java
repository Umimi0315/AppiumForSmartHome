import Utils.ConnectionUtils;
import Utils.Util;
import io.appium.java_client.android.AndroidDriver;
import sun.security.util.Length;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class MiHome {
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

            driver = Util.appiumParamSetting("com.xiaomi.smarthome", "com.xiaomi.smarthome.SmartHomeMainActivity", writeProgress, udid, appiumPort);

            Thread.sleep(10000);

            Util.writeProgress(50,0,"正在登录米家", writeProgress);

            if (Util.isElementExits("id", "com.xiaomi.smarthome:id/ok", driver)){
                driver.findElementById("com.xiaomi.smarthome:id/ok").click();
                Thread.sleep(5000);
            }

            if (Util.isElementExits("id", "com.xiaomi.smarthome:id/ok", driver)){
                driver.findElementById("com.xiaomi.smarthome:id/ok").click();
                Thread.sleep(5000);
            }

            if(Util.isElementExits("id", "com.xiaomi.smarthome:id/btn_confirm", driver)){
                driver.findElementById("com.xiaomi.smarthome:id/btn_confirm").click();
                Thread.sleep(5000);
            }

            driver.findElementByXPath("//*[@text='立即登录']").click();
            Thread.sleep(5000);

            if (Util.isElementExits("xpath", "//*[@resource-id='com.xiaomi.smarthome:id/action_ph_ticket_signin' and @text='手机号登录']", driver)){
                driver.findElementByXPath("//*[@resource-id='com.xiaomi.smarthome:id/action_ph_ticket_signin' and @text='手机号登录']").click();
                Thread.sleep(5000);
            }

            driver.findElementById("com.xiaomi.smarthome:id/phone").sendKeys(phone);
            Thread.sleep(5000);

            //验证码逻辑
            if (!ConnectionUtils.SendPhoneInfo(imsi, phone, phone_nation_code)){
                Util.writeProgress(50, 1, "服务器未准备好", writeProgress);
                return;
            }


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


            driver.findElementById("com.xiaomi.smarthome:id/ph_sign_in_btn").click();
            Thread.sleep(5000);


            //每隔5秒向服务器询问验证码是否准备好
            status=ConnectionUtils.getCodeStatus();
            while (status==-1){
                Thread.sleep(5000);
                status=ConnectionUtils.isReady();
            }

            Thread.sleep(8000);


            String verificationCode=ConnectionUtils.getVerficationCode(4);

            System.out.println(verificationCode);
            if(verificationCode==null){
                Util.writeProgress(50, 1, "短信中提取验证码出错", writeProgress);
                return;
            }

            driver.findElementById("com.xiaomi.smarthome:id/ticket").sendKeys(verificationCode);
            Thread.sleep(5000);

            driver.findElementById("com.xiaomi.smarthome:id/ph_sign_in_btn").click();
            Thread.sleep(5000);

            //等待登录成功
            while (Util.isElementExits("id", "com.xiaomi.smarthome:id/progress_message",driver)){
                Thread.sleep(5000);
            }


            Util.writeProgress(100, 0, "执行完成", writeProgress);
        } catch (Exception e) {
            e.printStackTrace();

            Util.writeProgress(50, 1, "自动登录米家出错", writeProgress);

        } finally {
            Util.sendExitMessage(writeProgress);
            Util.close(driver,readProgress, writeProgress, socket);
        }

    }
}
