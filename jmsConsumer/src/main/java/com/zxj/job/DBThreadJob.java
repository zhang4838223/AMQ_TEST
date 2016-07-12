package com.zxj.job;

import com.zxj.comm.DBThread;
import com.zxj.comm.LogicThread;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Created by zhang4838223 on 2016/7/12.
 */
public class DBThreadJob implements Runnable {

    Log logger = LogFactory.getLog(this.getClass());


    public void run() {
        logger.warn("the db task is:"+ DBThread.getQueueSize()+"; logic task num is: "+ LogicThread.getLogicQueueSize());
    }
}
