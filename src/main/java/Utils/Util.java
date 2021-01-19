package Utils;

import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.management.ManagementFactory;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;
import java.util.regex.Pattern;

public class Util {
    public static String pid= ManagementFactory.getRuntimeMXBean().getName().split("@")[0];
    /**
     * 去掉字符串中的空格
     * @param IP 传入的字符串
     * @return  返回去掉空格的字符串
     */
    public static String deleteSpace(String IP){//去掉IP字符串前后所有的空格
        while(IP.startsWith(" ")){
            IP= IP.substring(1,IP.length()).trim();
        }
        while(IP.endsWith(" ")){
            IP= IP.substring(0,IP.length()-1).trim();
        }
        return IP;
    }

    /**
     * 判断字符串是否是一个ip地址
     * @param IP 传入的字符串
     * @return 是ip地址返回true，否则返回false
     */
    public static boolean isIp(String IP){//判断是否是一个IP
        boolean b = false;
        IP = deleteSpace(IP);
        if(IP.matches("\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}")){
            String s[] = IP.split("\\.");
            if(Integer.parseInt(s[0])<255)
                if(Integer.parseInt(s[1])<255)
                    if(Integer.parseInt(s[2])<255)
                        if(Integer.parseInt(s[3])<255)
                            b = true;
        }
        return b;
    }

    /**
     * 判断字符串是否为整数
     * @param str 传入的字符串
     * @return 是整数返回true,否则返回false
     */
    public static boolean isInteger(String str) {
        str=deleteSpace(str);
        Pattern pattern = Pattern.compile("^[-\\+]?[\\d]*$");
        return pattern.matcher(str).matches();
    }

    /**
     * 获取字符串的byte数组长度？已废弃
     * @param s
     * @return
     */
    public static int getByteLength(String s) {
        int length=0;
        for(int i=0;i<s.length();i++) {
            int ascii=Character.codePointAt(s, i);
            if (ascii>=0&&ascii<=255) {
                length++;
            }else {
                length+=2;
            }
        }
        return length;
    }

    /**
     * int型转换成byte数组
     * @param value 待转换的int变量
     * @return 返回已转换的数组
     */
    public static  byte[] intToBytes(int value) {
        byte[] bytes=new byte[4];
        bytes[3]=(byte) (value>>24);
        bytes[2]=(byte) (value>>16);
        bytes[1]=(byte) (value>>8);
        bytes[0]=(byte) (value>>0);
        return bytes;
    }

    /**
     * byte数组拼接函数
     * @param a 待拼接数组a
     * @param b 待拼接数组b
     * @return 返回已拼接好的数组
     */
    public static byte[] arrayJoin(byte[] a,byte[] b) {
        byte[] arr=new byte[a.length+b.length];
        for (int i = 0; i < a.length; i++) {
            arr[i]=a[i];
        }
        for (int j = 0; j < b.length; j++) {
            arr[a.length+j]=b[j];
        }

        return arr;

    }

    /**
     * byte数组转为int型
     * @param bytes 待转换数组
     * @return 返回已转换的int变量
     */
    public static int bytesToInt(byte[] bytes) {
        return (int)((((bytes[3]&0xff)<<24)
                |((bytes[2]&0xff)<<16)
                |((bytes[1]&0xff)<<8)
                |((bytes[0]&0xff)<<0)));
    }

    /**
     * 已在winform界面端实现，该方法废弃
     * @throws IOException
     */
    public static void startAppium() throws IOException {
        ProcessBuilder processBuilder=new ProcessBuilder("cmd","/c","appium -a 127.0.0.1 -p 4723");
        Process process = processBuilder.start();
        Scanner scanner=new Scanner(process.getInputStream());
        while (scanner.hasNextLine()){
            System.out.println(scanner.nextLine());
        }
        scanner.close();
    }

    /**
     * 设置appium启动APP的相关参数
     * @param appPackage 包名
     * @param appActivity 入口Activity名
     * @return
     * @throws MalformedURLException
     */
    public static AndroidDriver appiumParamSetting(String appPackage, String appActivity, OutputStream writeProgress, String udid, int appiumPort) throws Exception {

        try {
            DesiredCapabilities capabilities=new DesiredCapabilities();
            capabilities.setCapability("platformName", "Android");
            capabilities.setCapability("automationName", "UiAutomator2");
            capabilities.setCapability("deviceName", "MuMu");
            capabilities.setCapability("appPackage", appPackage);
            capabilities.setCapability("appActivity", appActivity);
            //capabilities.setCapability("appWaitActivity", "com.didi.sdk.app.MainActivity");
            capabilities.setCapability("udid", udid);
            capabilities.setCapability("noReset", true);
            capabilities.setCapability("unicodeKeyboard", true);
            capabilities.setCapability("resetKeyboard", true);

            return new AndroidDriver<WebElement>(new URL("http://127.0.0.1:"+appiumPort+"/wd/hub"),capabilities);
        } catch (MalformedURLException e) {
            writeProgress(1, 1, "初始化appium出错", writeProgress);
            throw e;
        }

    }

    /**
     * 等待winform界面通知任务开始
     * @param readProgress socket的输入流
     * @throws IOException
     */
    public static void waitForStart(InputStream readProgress) throws IOException {
        byte[] frameCmd=new byte[4];
        do {
            byte[] frameHeader=new byte[1];
            readProgress.read(frameHeader);
            byte[] frameLengthBytes=new byte[4];
            readProgress.read(frameLengthBytes);
            int frameLength=bytesToInt(frameLengthBytes);
            readProgress.read(frameCmd);
            byte[] frameContentAndEnd=new byte[frameLength-4+1];
            readProgress.read(frameContentAndEnd);
        }while(!(frameCmd[0]==(byte)0x00&&frameCmd[1]==(byte)0x00&&frameCmd[2]==(byte)0x00&&frameCmd[3]==(byte)0x00));
    }

    /**
     * 发送取证任务开始报文
     * @param writeProgress socket输出流
     * @throws Exception
     */
    public static void sendProgressStartMessage(OutputStream writeProgress) throws Exception {
        //报文内容字符串
        String startProgressStr="{"
                +"\"TASK_ID\":\""+pid+"\","
                +"\"SUB_TASK_ID\":"+"\"\","
                +"\"PROGRESS_CUR\":"+"1"+","
                +"\"PROGRESS_TOTAL\":"+"100"+","
                +"\"STATUS_CODE\":"+"\""+"0"+"\""+","
                +"\"STATUS_TEXT\":"+"\""+"正在初始化appium..."+"\""
                +"}";

        //报文内容字符串转换成byte数组
        byte[] startProgressArr=startProgressStr.getBytes("UTF-8");
        //长度
        int startLengthInt=startProgressArr.length+4;
        //帧头
        byte[] startFrameHeader=new byte[]{(byte)0x00};
        //帧长
        byte[] startFrameLength=intToBytes(startLengthInt);
        //命令
        byte[] startCmd=new byte[] {(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x01};
        //帧尾
        byte[] startFrameEnd=new byte[] {(byte)0xff};

        byte[] startFrame=segmentSplice(startFrameHeader,startFrameLength,startCmd,startProgressArr,startFrameEnd);

        writeProgress.write(startFrame, 0, startFrame.length);

        writeProgress.flush();
    }

    /**
     * 报文段拼接
     * @param frameHeader 报头
     * @param frameLength 长度
     * @param cmd 命令
     * @param progressArr 内容
     * @param frameEnd 报尾
     * @return 拼接好的报文
     */
    public static byte[] segmentSplice(byte[] frameHeader,byte[] frameLength,byte[] cmd,byte[] progressArr,byte[] frameEnd){
        byte[] Frame=null;
        if (progressArr==null){
            byte[] frameSegment1 = Util.arrayJoin(frameHeader, frameLength);
            byte[] frameSegment2 = Util.arrayJoin(frameSegment1, cmd);
            Frame = Util.arrayJoin(frameSegment2, frameEnd);
        }else {
            byte[] frameSegment1=arrayJoin(frameHeader, frameLength);
            byte[] frameSegment2=arrayJoin(frameSegment1, cmd);
            byte[] frameSegment3=arrayJoin(frameSegment2, progressArr);
            Frame=arrayJoin(frameSegment3, frameEnd);
        }

        return Frame;
    }

    /**
     * 发送取证任务结束报文
     * @param writeProgress socket输出流
     * @throws Exception
     */
    public static void sendProgressEndMessage(OutputStream writeProgress) throws Exception {
        String endProgressStr="{"
                +"\"TASK_ID\":\""+pid+"\","
                +"\"SUB_TASK_ID\":"+"\"\","
                +"\"PROGRESS_CUR\":"+"100"+","
                +"\"PROGRESS_TOTAL\":"+"100"+","
                +"\"STATUS_CODE\":"+"\""+"0"+"\""+","
                +"\"STATUS_TEXT\":"+"\""+"数据提取完成"+"\""
                +"}";

        //内容
        byte[] endProgressArr=endProgressStr.getBytes("UTF-8");

        int endLengthInt=endProgressArr.length+4;
        //帧头
        byte[] endFrameHeader=new byte[]{(byte)0x00};
        //帧长
        byte[] endFrameLength=intToBytes(endLengthInt);
        //命令
        byte[] endCmd=new byte[] {(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x01};
        //帧尾
        byte[] endFrameEnd=new byte[] {(byte)0xff};

        byte[] endFrame=segmentSplice(endFrameHeader,endFrameLength,endCmd,endProgressArr,endFrameEnd);

        writeProgress.write(endFrame, 0, endFrame.length);

        writeProgress.flush();
    }

    /**
     * 传送可下载文件信息结束报文
     * @param writeProgress
     * @throws Exception
     */
    public static void sendAttachmentEndMessage(OutputStream writeProgress) throws Exception {

        byte[] exitFrameHeader=new byte[] {(byte)0x00};
        byte[] exitFrameLength= Util.intToBytes(4);
        byte[] exitFrameCmd=new byte[] {(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x03};
        byte[] exitFrameEnd=new byte[] {(byte)0xff};

        byte[] exitFrame = segmentSplice(exitFrameHeader,exitFrameLength,exitFrameCmd,null,exitFrameEnd);

        writeProgress.write(exitFrame, 0, exitFrame.length);

        writeProgress.flush();
    }

    public static boolean available() throws ParseException {
        SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd");
        Date currentDate=new Date(System.currentTimeMillis());
        String minDateString="2020-01-01";
        String maxDateString="2022-06-01";
        Date minDate = format.parse(minDateString);
        Date maxDate = format.parse(maxDateString);
        if (currentDate.getTime()<minDate.getTime()||currentDate.getTime()>maxDate.getTime()){
            return false;
        }
        else {
            return true;
        }
    }

    /**
     * 传送可下载文件信息结束报文
     * @param writeProgress
     * @throws Exception
     */
    public static void sendDownloadFileEndMessage(OutputStream writeProgress) throws Exception {

        byte[] exitFrameHeader=new byte[] {(byte)0x00};
        byte[] exitFrameLength= Util.intToBytes(4);
        byte[] exitFrameCmd=new byte[] {(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x04};
        byte[] exitFrameEnd=new byte[] {(byte)0xff};

        byte[] exitFrame = segmentSplice(exitFrameHeader,exitFrameLength,exitFrameCmd,null,exitFrameEnd);

        writeProgress.write(exitFrame, 0, exitFrame.length);

        writeProgress.flush();
    }

    /**
     * 发送任务结束退出报文
     * @param writeProgress
     * @throws Exception
     */
    public static void sendExitMessage(OutputStream writeProgress) throws Exception {

        byte[] exitFrameHeader=new byte[] {(byte)0x00};
        byte[] exitFrameLength= Util.intToBytes(4);
        byte[] exitFrameCmd=new byte[] {(byte)0x00,(byte)0x00,(byte)0xff,(byte)0xff};
        byte[] exitFrameEnd=new byte[] {(byte)0xff};

        byte[] exitFrame = segmentSplice(exitFrameHeader,exitFrameLength,exitFrameCmd,null,exitFrameEnd);

        writeProgress.write(exitFrame, 0, exitFrame.length);

        writeProgress.flush();
    }

    /**
     * 关闭资源
     * @param readProgress socket输入流
     * @param writeProgress socke输出流
     * @param socket socket
     */
    public static void close(AndroidDriver driver, InputStream readProgress, OutputStream writeProgress, Socket socket){
        try {
            if (driver!=null){
                driver.quit();
                driver.close();
            }
            if (readProgress!=null){
                readProgress.close();
            }
            if (writeProgress!=null){
                writeProgress.close();
            }
            if (socket!=null){
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /**
     * 发送任务执行进度
     * @param nowProgress 当前进度百分比数字
     * @param status_Code 当前进度状态代码，0为正常执行，1为执行出错
     * @param status_Text 当前进度状态描述信息
     * @param writeProgress socket输出流
     * @throws Exception
     */
    public static void writeProgress(int nowProgress,int status_Code,String status_Text,OutputStream writeProgress) throws Exception {

        String progressStr="{"
                +"\"TASK_ID\":\""+pid+"\","
                +"\"SUB_TASK_ID\":"+"\"\","
                +"\"PROGRESS_CUR\":"+nowProgress+","
                +"\"PROGRESS_TOTAL\":"+"100"+","
                +"\"STATUS_CODE\":"+"\""+status_Code+"\""+","
                +"\"STATUS_TEXT\":"+"\""+status_Text+"\""
                +"}";
        //内容
        byte[] progressArr=progressStr.getBytes("UTF-8");

        int lengthInt=progressArr.length+4;
        //帧头
        byte[] frameHeader=new byte[]{(byte)0x00};
        //帧长
        byte[] frameLength= Util.intToBytes(lengthInt);
        //命令
        byte[] cmd=new byte[] {(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x01};
        //帧尾
        byte[] frameEnd=new byte[] {(byte)0xff};

        byte[] frame = Util.segmentSplice(frameHeader,frameLength,cmd,progressArr,frameEnd);

        writeProgress.write(frame, 0, frame.length);

        writeProgress.flush();

    }

    /**
     * 发送可下载文件信息
     * @param attachmentsList 可下载文件信息
     * @param writeProgress socket输出流
     * @throws Exception
     */
    public static void sendAttachments(ArrayList<String> attachmentsList,OutputStream writeProgress) throws Exception {
        for (int i=0;i<attachmentsList.size();i++){
            String attachment=attachmentsList.get(i);

            //内容
            byte[] attachmentArr=attachment.getBytes("UTF-8");

            int lengthInt=attachmentArr.length+4;
            //帧头
            byte[] frameHeader=new byte[]{(byte)0x00};
            //帧长
            byte[] frameLength= Util.intToBytes(lengthInt);
            //命令
            byte[] cmd=new byte[] {(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x02};
            //帧尾
            byte[] frameEnd=new byte[] {(byte)0xff};

            byte[] frame = Util.segmentSplice(frameHeader,frameLength,cmd,attachmentArr,frameEnd);

            writeProgress.write(frame, 0, frame.length);

            writeProgress.flush();
        }
    }

    /**
     * 通过AndroidDriver判断元素是否存在
     * @param by
     * @param path
     * @param driver
     * @return
     */
    public static boolean isElementExits(String by, String path, AndroidDriver driver) {
        try {
            driver.findElement(by, path);
            return true;
        } catch (Exception e) {
            return false;
            // TODO: handle exception
        }
    }





}
