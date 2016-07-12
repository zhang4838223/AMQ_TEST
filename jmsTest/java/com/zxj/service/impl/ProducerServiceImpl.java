package com.zxj.service.impl;

import com.zxj.dao.EmployeeDao;
import com.zxj.service.ProducerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.stereotype.Service;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

/**
 * Created by zhang4838223 on 2016/7/4.
 */
@Service
public class ProducerServiceImpl implements ProducerService {

    private JmsTemplate jmsTemplate;

    public void sendMessage(Destination destination, final String message) {

        jmsTemplate.send(destination, new MessageCreator() {
            public Message createMessage(Session session) throws JMSException {
                return session.createTextMessage(message);
            }
        });
    }
}
