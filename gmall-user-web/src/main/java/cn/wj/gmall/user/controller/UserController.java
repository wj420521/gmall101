package cn.wj.gmall.user.controller;

import cn.wj.gmall.bean.UmsMember;
import cn.wj.gmall.bean.UmsMemberReceiveAddress;
import cn.wj.gmall.service.UserService;
import com.alibaba.dubbo.config.annotation.Reference;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
public class UserController {

    @Reference
    private UserService userService;


    @RequestMapping("/hello")
    @ResponseBody
    public String test(){
        return "hello user";
    }

    //查询所有用户
    @RequestMapping("/findAllUser")
    @ResponseBody
    public List<UmsMember> findAllUser(){
        List<UmsMember> list= userService.findAllUser();
        return list;
    }
    //根据用户id查询收货地址
    @RequestMapping("/getReceiveAddressByMemberId")
    @ResponseBody
    public List<UmsMemberReceiveAddress> getReceiveAddressByMemberId(String memberId){
        List<UmsMemberReceiveAddress> umsMemberReceiveAddress = userService.getReceiveAddressByMemberId(memberId);
        return umsMemberReceiveAddress;
    }
}
