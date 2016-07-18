package com.zxj.comm.utils;

import com.zxj.comm.EColumn;
import com.zxj.comm.ETable;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.springframework.util.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by zhang4838223 on 2016/7/15.
 */
public class XMLParseUtil {
    private final static Log logger = LogFactory.getLog(XMLParseUtil.class);

    private final static XMLParseUtil instance = new XMLParseUtil();

    private List<ETable> etables = new ArrayList<ETable>();;
    private XMLParseUtil(){
        try {
            init();

        } catch (DocumentException e) {
            logger.error("file [tables.xml] not found!");
        }
    }

    public void init() throws DocumentException {
        etables.clear();
        SAXReader reader = new SAXReader();
        Document document = reader.read(new File(XMLParseUtil.class.getResource("/tables.xml").getPath()));
        Element root = document.getRootElement();

        //获取所有table节点
        List<Element> tables = root.elements("table");
        for (Element table : tables){
            String tableName = table.attribute("name").getValue();
//            System.out.println(tableName);

            Element operate = table.element("operate");
            String updateSql = operate.element("update").getTextTrim();
            String updateSqlA = operate.element("updateAll").getTextTrim();
            String querySql = operate.element("query").getTextTrim();
            String queryCountSql = operate.element("queryCount").getTextTrim();
            String insertSql = operate.element("insert").getTextTrim();

            List<Element> columns = table.element("columns").elements("column");

            List<EColumn> eColumns = new ArrayList<EColumn>();
            for (Element col : columns){
                String type = col.attribute("type").getValue();
                String colName = col.getTextTrim();
                EColumn eColumn = new EColumn(null, colName, Integer.valueOf(type));
                eColumns.add(eColumn);
            }

            etables.add(new ETable(updateSql, tableName, eColumns, querySql, queryCountSql, insertSql,updateSqlA));
        }
    }

    public static void main(String[] args){
//        listNodes(root);
//        listNodes_1(root);
    }

    private static void listNodes(Element node) {
        System.out.println("当前节点的名称：：" + node.getName());
        // 获取当前节点的所有属性节点
        List<Attribute> list = node.attributes();
        // 遍历属性节点
        for (Attribute attr : list) {
            System.out.println(attr.getText() + "-----" + attr.getName()
                    + "---" + attr.getValue());
        }

        if (!(node.getTextTrim().equals(""))) {
            System.out.println("文本内容：：：：" + node.getText());
        }

        // 当前节点下面子节点迭代器
        Iterator<Element> it = node.elementIterator();
        // 遍历
        while (it.hasNext()) {
            // 获取某个子节点对象
            Element e = it.next();
            // 对子节点进行遍历
            listNodes(e);
        }
    }

    public static XMLParseUtil getInstance(){
        return instance;
    }

    public List<ETable> getEtables() {
        return etables;
    }

    public void setEtables(List<ETable> etables) {
        this.etables = etables;
    }

    /**
     * 许要同步的table数量
     * @return
     */
    public int getEtableSize() {
        return this.etables.size();
    }
    /**
     * 根据索引获取xml配置的table信息
     * @return
     */
    public ETable getEtableByIndex(int index) {
        return this.etables.get(index);
    }

    /**
     * 根据name获取xml配置的table信息
     * @return
     */
    public ETable getEtableByName(String name) {
        if (StringUtils.isEmpty(name)){
            return null;
        }
        for (ETable tab : this.etables){
            if (name.equals(tab.getName())){
                return tab;
            }
        }

        return null;
    }


}