package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.itheima.reggie.common.CustomException;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.User;
import com.itheima.reggie.entity.UserInfo;
import com.itheima.reggie.service.UserService;
import com.itheima.reggie.utils.MailUtils;
import com.itheima.reggie.utils.ValidateCodeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.Map;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/sendMsg")
    public R<String> sendMsg(@RequestBody User user, HttpSession session) {
        // 获取手机号
        String phone = user.getPhone();
        if (StringUtils.isEmpty(phone)) {
            throw new CustomException("请正确输入手机号");
        }
        // 生成4位验证码
        String code = ValidateCodeUtils.generateValidateCode(4).toString();
        // 调用工具类发送短信
        String content = "尊敬的用户 : " + phone + "您本次的验证码为 ： " + code;
        MailUtils.sendMail(phone, content, "登陆验证");
        // 将其验证码储存到session中
        session.setAttribute(phone, code);

        return R.success("手机验证码短信发送成功");
    }

    @PostMapping("/login")
    public R<User> login(@RequestBody Map map, HttpSession session) {
        String phone = (String) map.get("phone");
        String code = (String) map.get("code");

        // 从session中获取对象
        String sCode = (String) session.getAttribute(phone);

        // 进行验证
        if (StringUtils.isEmpty(code)) {
            throw new CustomException("请输入正确的验证码！！！");
        }

        if (!code.equals(sCode)) {
            throw new CustomException("验证码错误");
        }

        // 判断用户是否存在
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("phone", phone);
        User user = userService.getOne(wrapper);

        if (!StringUtils.isEmpty(user)) {
            //用户存在进行登陆
            session.setAttribute("user", user.getId());
            return R.success(user);
        }
        // 用户不存在 进行注册
        user = new User();
        user.setPhone(phone);
        user.setStatus(1);

        userService.save(user);
        return R.success(user);
    }

    @PostMapping("/loginout")
    public R<String> loginout(HttpSession session) {
        session.removeAttribute("user");
        return R.success("退出登陆成功");
    }

    @GetMapping("/get")
    public R<UserInfo> getUser(HttpSession session) {
        UserInfo userInfo = new UserInfo();
        Long userId = (Long) session.getAttribute("user");
        User user = userService.getById(userId);
        if (user.getName() == null) {
            userInfo.setUsername(user.getPhone());
        } else {
            userInfo.setUsername(user.getName());
        }
        if (user.getSex() == null) {
            userInfo.setSex("女");
        } else {
            userInfo.setSex("男");
        }
        return R.success(userInfo);
    }
}
