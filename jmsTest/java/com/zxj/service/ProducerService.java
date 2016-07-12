package com.zxj.service;

import javax.jms.Destination;

/**
 * Created by zhang4838223 on 2016/7/4.
 */
public interface ProducerService {

    public void sendMessage(Destination destination, final String message);
}
