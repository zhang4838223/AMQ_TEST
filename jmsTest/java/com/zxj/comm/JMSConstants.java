package com.zxj.comm;

import org.springframework.core.io.support.PropertiesLoaderUtils;

import java.io.IOException;
import java.util.Properties;

/**
 * Created by zhang4838223 on 2016/7/9.
 */
public class JMSConstants {

    /** 每BATCH_SIZE条数据发送*/
    public static final int BATCH_SIZE = 10000;

    public static int queuqSize = 1;

    public static int getQueueSize(){
        try {
            Properties prop = PropertiesLoaderUtils.loadAllProperties("prop.properties");
            String size = prop.getProperty("queueSize", "1");
            queuqSize = Integer.valueOf(size);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return queuqSize;
    }
}
