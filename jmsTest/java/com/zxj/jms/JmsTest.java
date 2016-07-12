package com.zxj.jms;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import javax.jms.*;

/**
 * Created by zhang4838223 on 2016/6/30.
 */
public class JmsTest {
    public static void main(String[] args) throws JMSException {
        ApplicationContext context = new ClassPathXmlApplicationContext("applicationContext-jms.xml");
        ConnectionFactory factory = (ConnectionFactory) context.getBean("pooledConnectionFactory");
        Connection conn = factory.createConnection();
        conn.start();

        Destination queue = (Destination) context.getBean("queueDestination");
        Session sen = conn.createSession(false, Session.CLIENT_ACKNOWLEDGE);
        MessageProducer producer = sen.createProducer(queue);
        TextMessage msg = sen.createTextMessage("The only way by the sword");
        producer.send(msg);

        producer.close();
        sen.close();
        conn.close();

    }
}
