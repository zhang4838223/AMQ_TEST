package com.zxj.dao;

import com.zxj.mybatis.map.Employee;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * Created by zhang4838223 on 2016/6/30.
 */
@Repository
public interface EmployeeDao {
    public Employee select(Map<String,Integer> map);

    public void insert(Employee blog);

    public void update(Employee blog);

    public void delete(int id);

    public List<Employee> selectAll();

    /**
     * 查询未发送数据
     * @return
     */
    public List<Employee> selectAllWithNotSend(Map<String,Integer> map);

    /**
     * 分页查询未发送数据
     * state != 1
     * startIndex
     * endIndex
     *
     * @return
     */
    public List<Employee> findPageList(Map<String,Integer> map);

    /**
     * 查询未发送数据量
     * state != 1
     * @return
     */
    public Integer findTotalCountNotSend(Map<String,Integer> map);
}
