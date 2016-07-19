package com.zxj.test;

import com.google.gson.Gson;
import com.zxj.comm.*;
import com.zxj.dao.EmployeeDao;
import com.zxj.dao.SqlDao;
import com.zxj.jms.Producer;
import com.zxj.mybatis.map.Employee;
import com.zxj.utils.UtilsFun;
import com.zxj.utils.XMLParseUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import javax.jms.TextMessage;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by zhang4838223 on 2016/7/18.
 */
public class SendTest {


    private final static Log logger = LogFactory.getLog(Producer.class);
    public static ApplicationContext context = new ClassPathXmlApplicationContext("applicationContext-jms.xml");
    public static JmsTemplate template =(JmsTemplate)context.getBean("jmsTemplate");
    public static ApplicationContext ctx = new ClassPathXmlApplicationContext("applicationContext-database.xml");
    private static EmployeeDao dao = (EmployeeDao)ctx.getBean("emplyoeeDao");
    private final static SqlDao sqlDao = (SqlDao)ctx.getBean("sqlDao");

    public static void main(String[] args){
        sendDirecWithMultiQueue();
        System.out.println("all data is sended");
    }

    /**
     * 多个队列无响应发送
     */

    public static void sendDirecWithMultiQueue(){

        //配置了多少个表需要同步数据
        int tabSize = XMLParseUtil.getInstance().getEtableSize();
        if (tabSize <= 0){
            return;
        }

        //循环发送各个表数据同步
        for (int i = 0; i < tabSize; i++) {
            Integer startIndex = 0;
            Integer endIndex = 0;
            Integer pageSize = JMSConstants.getBatchSize();//每次查询一万条
            List<Integer> ids = new ArrayList<Integer>();

            int count = -1;
            ETable table = XMLParseUtil.getInstance().getEtableByIndex(i);
            logger.info("------->"+table.getName()+" is ready");
            String updateSql = table.getUpdateSql();
            try {
                //每个表的数据分页查询后发送
                while (true) {
                    System.out.println("DBThread task nunm is:  "+ DBThread.getQueueSize());
                    //等待当前数据保存完毕
//                    if(DBThread.getQueueSize() > 0){
//                        System.out.println("DBThread.getQueueSize() = " + DBThread.getQueueSize());
//                        TimeUnit.SECONDS.sleep(15);
//                        startIndex = 0;
//                        endIndex = 0;
//                        count = -1;
//    //                    ids.clear();
//                        continue;
//                    }

                    //新一轮发送开始，重新查询
                    if(count == -1) {
                        count = sqlDao.queryCountNotSend(table.getQueryCountSql());
                        System.out.println("the new count is : "+ count);
                    }

                    if (count <= 0){
                        count = -1;
                        System.out.println("===========waiting for new data=========="+count);
                        TimeUnit.SECONDS.sleep(15);
                        startIndex = 0;
                        endIndex = 0;
                        break;
                    }

                    int totalPages =  (count + pageSize -1) / pageSize;

                    // 0-10001-20001-30001
                    startIndex = endIndex == 0 ? 0 : endIndex + 1;
                    endIndex = endIndex + pageSize;

                    String querySql = table.getQuerySql();
                    //分页查询数据
                    String newSql = UtilsFun.format(querySql, endIndex, startIndex);
                    logger.info("query sql:"+ newSql);
                    List<ERecord> list = sqlDao.queryPageList(newSql, table.getColumns());

                    String data = toJson(list, table.getName());
                    //发送数据java.io.IOException: Wire format negotiation timeout: peer did not send his wire format.
                    System.out.println("begin to send mes!");

                    int var = endIndex / pageSize;

                    int index = var % JMSConstants.getQueueSize();

                    sendMesWithDes(data,"queue_"+(index+1));

                    for (ERecord record : list) {
                        //第一列是id
                        EColumn eColumn = record.getColumns().get(0);

                        ids.add(Integer.valueOf(eColumn.getValue()));
                    }

                    if (startIndex >= count && startIndex != 0 && count != -1) {//本轮发送结束
                        System.out.println("===========begin to the next=========="+startIndex);
                        startIndex = 0;
                        endIndex = 0;
                        //更改为已经发送状态
                        logger.info("update state sql: "+ updateSql);
                        saveDataAsyn(ids,updateSql);
                        ids.clear();
    //                        return;
                        break;
                    }
                }
            }catch (Exception e){
                logger.error(e);
                saveDataAsyn(ids,updateSql);
            }
        }

        logger.info("----------> the game is end, waiting the next");
    }

    private static void saveDataAsyn(List<Integer> ids, final String sql){
        final List<Integer> list = new ArrayList<Integer>();
        list.addAll(ids);
        if (!CollectionUtils.isEmpty(ids)){
            DBThread.execute(new Runnable() {
                public void run() {
                    try {
                        System.out.print("update db size:"+ list.size());
                        sqlDao.updateState(list, sql);
                    } catch (SQLException e) {
                        logger.error(e);
                    }
                }
            });
        }
    }

    private static String toJson(List<ERecord> list, String tabName)throws JSONException {
        if (CollectionUtils.isEmpty(list) && StringUtils.isEmpty(tabName)){
            return null;
        }

        Gson gson = new Gson();
        Map<String,List<ERecord>> data = new HashMap<String, List<ERecord>>();
        data.put(tabName,list);
        String json = gson.toJson(data);
        return json;
    }

    /**
     * 所有消息一次发送
     * @param json 所要发送的消息
     */
    private static void sendMesWithDes(final String json, String destination) throws Exception{

        try {
            template.send(destination,new MessageCreator() {

                public Message createMessage(Session sen) throws JMSException {
                    TextMessage msg = sen.createTextMessage(json);
                    return msg;
                }

            });
        }catch (Exception e){
            e.printStackTrace();
            logger.error(e);
            //发送失败则记录，可以后台重发
            throw e;
        }
    }
}
