package com.zxj.dao;

import com.zxj.model.Emp;
import com.zxj.model.Employee;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * Created by zhang4838223 on 2016/6/30.
 */
@Repository
public interface EmpMapper {
    public Employee select(Map<String, Integer> map);

    public void insert(Emp blog);

    public void update(Emp blog);

    public void delete(int id);

    public List<Emp> selectAll();
}
