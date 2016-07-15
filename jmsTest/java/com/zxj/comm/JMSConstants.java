package com.zxj.comm;

import org.springframework.core.io.support.PropertiesLoaderUtils;

import java.io.IOException;
import java.util.Properties;

/**
 * Created by zhang4838223 on 2016/7/9.
 */
public class JMSConstants {

    /** 每BATCH_SIZE条数据发送*/
    private static int batchSize = 10000;
    /** 所需要的队列数量，默认为1*/
    private static int queuqSize = 1;

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

    public static int getBatchSize(){
        try {
            Properties prop = PropertiesLoaderUtils.loadAllProperties("prop.properties");
            String size = prop.getProperty("batchSize", "1");
            batchSize = Integer.valueOf(size);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return batchSize;
    }

    /**
     * 表示int类型
     */
    public static final int INT = 1;
    /**
     * 表示String类型
     */
    public static final int STRING  = 2;
    /**
     * 表示double类型
     */
    public static final int DOUBLE = 3;
    /**
     * 表示Date类型
     */
    public static final int DATE = 4;
}
