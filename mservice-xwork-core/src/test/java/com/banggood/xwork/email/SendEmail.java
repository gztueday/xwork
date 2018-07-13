package com.banggood.xwork.email;

import org.junit.Test;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Calendar;
import java.util.Properties;


@SuppressWarnings("ALL")
public class SendEmail {

    @Test
    public void sendEmail() {
        Properties props = new Properties();
        // 开启debug调试
        //props.put("mail.debug", "true");
        // 发送服务器需要身份验证
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable","true");
        //props.
        // 设置邮件服务器主机名
        props.put("mail.smtp.host", "smtp.126.com");
        // 发送邮件协议名称
        props.put("mail.transport.protocol", "smtp");

        // 设置环境信息
        Session mailSession = Session.getInstance(props,new MyAuthenticator("18825106168@126.com","banggood1234") );
        try {
            InternetAddress fromAddress = new InternetAddress("18825106168@126.com");
           // InternetAddress toAddress = new InternetAddress("zouyi@banggood.com");
            /**
             * 编写消息
             */
            // 创建邮件对象
            Message msg = new MimeMessage(mailSession);
            msg.setSubject("xworkFlow调度系统报错邮件");
            // 设置邮件内容
            msg.setText("发送了一封邮件");
            // 设置发件人
            msg.setFrom(fromAddress);
           // msg.addRecipient(Message.RecipientType.TO,toAddress);
            msg.setSentDate(Calendar.getInstance().getTime());
            /**
             * 发送消息
             */
            Transport transport = mailSession.getTransport();
            // 连接邮件服务器
            transport.connect("smtp.126.com", "banggood1234");
            // 发送邮件
            transport.sendMessage(msg, new Address[] {new InternetAddress("zouyi@banggood.com"),new InternetAddress("chenjiahao@banggood.com")});
           // transport.sendMessage(msg,msg.getRecipients(Message.RecipientType.TO),);
            // 关闭连接
            transport.close();
        } catch (MessagingException e) {
            e.printStackTrace();
        } finally {

        }
    }

    class MyAuthenticator extends Authenticator{
        String userName="";
        String password="";
        public MyAuthenticator(){

        }
        public MyAuthenticator(String userName,String password){
            this.userName=userName;
            this.password=password;
        }
        protected PasswordAuthentication getPasswordAuthentication(){
            return new PasswordAuthentication(userName, password);
        }
    }
}