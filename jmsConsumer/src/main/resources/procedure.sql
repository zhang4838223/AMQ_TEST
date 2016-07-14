
--
-- create table EMP
-- (
--  EMPNO    NUMBER(9) not null,
--  ENAME    VARCHAR2(32),
--  JOB      VARCHAR2(32),
--  MGR      NUMBER(4),
--  HIREDATE DATE,
--  SAL      NUMBER(7,2),
--  COMM     NUMBER(7,2),
--  DEPTNO   NUMBER(2),
--  STATE    INTEGER
-- )
-- 建立一个type,对应JAVA端要传入的对象结构:

CREATE OR REPLACE TYPE BUT_UKBNOV_EMP_REC AS OBJECT (
  empno number(9),
  ename nVARCHAR2(32),
  job  nvarchar2(32),
  mgr number(4),
  hiredate date,
  sal number(7,2),
  comm number(7,2),
  deptno number(2),
  state integer
);

-- 为了数组传输,建立一个数组类型的type:
CREATE OR REPLACE TYPE BUT_UKBNOV_EMP_TAB AS TABLE OF BUT_UKBNOV_EMP_REC ;
-- 建立存储过程做插入工作:
CREATE OR REPLACE procedure bulkInsertEmp(i_emps IN BUT_UKBNOV_EMP_TAB)
as
emp BUT_UKBNOV_EMP_REC;
begin
    FOR idx IN i_emps.first()..i_emps.last() LOOP
        emp:=i_emps(idx);
        INSERT INTO EMP
          (EMPNO,
           ENAME,
           JOB,
           MGR,
           HIREDATE,
           SAL,
           COMM,
           DEPTNO,
           STATE
           )
        VALUES
          (emp.empno,
           emp.ename,
           emp.job,
           emp.mgr,
           emp.hiredate,
           emp.sal,
           emp.comm,
           emp.deptno,
           emp.state
           );
    end loop;
    exception when others then
    raise;
end;
