package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.Employee;
import com.itheima.reggie.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;


@Slf4j
@RestController
@RequestMapping("/employee")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    @PostMapping("/login")
    public R<Employee> login(HttpServletRequest request, @RequestBody Employee employee) {
        // 将其传输过来的密码进行md5加密
        String password = employee.getPassword();
        String pw = DigestUtils.md5DigestAsHex(password.getBytes());

        // 查询数据库
        LambdaQueryWrapper<Employee> wrapper = new LambdaQueryWrapper<Employee>();
        wrapper.eq(Employee::getUsername, employee.getUsername());
        Employee employeeServiceOne = employeeService.getOne(wrapper);

        if (StringUtils.isEmpty(employeeServiceOne)) {
            return R.error("登陆失败");
        }
        // 进行密码比对
        if (!(employeeServiceOne.getPassword().equals(pw))) {
            return R.error("密码错误！！");
        }
        // 进行员工的装填比对
        if (employeeServiceOne.getStatus() == 0) {
            return R.error("员工已经被禁用");
        }
        //将其结果储存在session对象中
        request.getSession().setAttribute("employee", employeeServiceOne);
        return R.success(employeeServiceOne);
    }

    @PostMapping("/logout")
    public R<String> logout(HttpServletRequest request) {
        // 清除session中的对象
        request.removeAttribute("employee");
        return R.success("退出成功");
    }

    @PostMapping
    public R<String> save(HttpServletRequest request, @RequestBody Employee employee) {
        // 创建初始密码
        employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));

//        employee.setCreateTime(LocalDateTime.now());
//        employee.setUpdateTime(LocalDateTime.now());
//
//        // 获取添加人的id
//        Employee em = employeeService.getById(((Employee) request.getSession().getAttribute("employee")).getId());
//        employee.setCreateUser(em.getId());
//        employee.setUpdateUser(em.getId());

        boolean save = employeeService.save(employee);
        return R.success("新增员工成功");
    }

    @GetMapping("/page")
    public R<Page> findPage(int page, int pageSize, String name) {
        // 构建分页
        Page<Employee> pageInfo = new Page<>(page, pageSize);
        QueryWrapper<Employee> wrapper = new QueryWrapper<>();
        // 判断是否需要进行条件查询
        if (!StringUtils.isEmpty(name)) {
            wrapper.like("username", name);
        }
        wrapper.orderByDesc("update_time");
        employeeService.page(pageInfo, wrapper);
        // 通过查看Page的属性 可以将其直接返回
        return R.success(pageInfo);
    }

    @PutMapping
    public R<String> update(HttpServletRequest request, @RequestBody Employee employee) {
//        // 获取管理员的id
//        Employee em = (Employee) request.getSession().getAttribute("employee");
//        employee.setUpdateUser(em.getId());
//        employee.setUpdateTime(LocalDateTime.now());

        employeeService.updateById(employee);

        return R.success("员工信息修改成功");
    }

    @GetMapping("/{id}")
    public R<Employee> getById(@PathVariable("id") Long id) {
        Employee em = employeeService.getById(id);
        if (em == null) {
            return R.error("没有查到员工的信息");
        }
        return R.success(em);
    }
}
