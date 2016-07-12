package com.zxj.jms;

import org.json.JSONObject;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jms.core.JmsTemplate;

import javax.jms.Destination;
import javax.jms.TextMessage;

/**
 * Created by zhang4838223 on 2016/6/30.
 */
public class Consumer {
    public static void main(String[] args) throws Exception {
        ApplicationContext context = new ClassPathXmlApplicationContext(
                "applicationContext-jms.xml");
        JmsTemplate template = (JmsTemplate) context.getBean("jmsTemplate");
        TextMessage msg = (TextMessage) template.receive();
        JSONObject json = new JSONObject(msg.getText());
        System.out.println(json.toString());
    }
}
