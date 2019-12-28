package cn.wj.gmall.passport.controller;

import cn.wj.gmall.bean.UmsMember;
import cn.wj.gmall.service.UserService;
import cn.wj.gmall.util.HttpclientUtil;
import cn.wj.gmall.util.JwtUtil;
import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Controller
public class PassportController {

    @Reference
    private UserService userService;

    @RequestMapping("vlogin")
    public String vlogin(String code,HttpServletRequest request, HttpServletResponse response, ModelMap modelMap) {
        //获取授权码code

        //通过授权吗交换accessToken
        //https://api.weibo.com/oauth2/access_token?client_id=YOUR_CLIENT_ID&client_secret=YOUR_CLIENT_SECRET&grant_type=authorization_code&redirect_uri=YOUR_REGISTERED_REDIRECT_URI&code=CODE
        String s2 = "https://api.weibo.com/oauth2/access_token";
        Map<String,String> map = new HashMap<>();
        map.put("client_id","1631547496");
        map.put("client_secret","561dddf344230c646b2236eccb71d396");
        map.put("grant_type","authorization_code");
        map.put("redirect_uri","http://passport.gmall.com:8085/vlogin");
        map.put("code",code);
        String access_token_json = HttpclientUtil.doPost(s2, map);
        Map<String,Object> access_token_map = JSON.parseObject(access_token_json, Map.class);
        String access_token =(String) access_token_map.get("access_token");
         Long uid = Long.parseLong((String)access_token_map.get("uid"));

        //通过accessToken 获取用户信息
        String s3 ="https://api.weibo.com/2/users/show.json?access_token="+access_token+"&uid="+uid;
        String user_json = HttpclientUtil.doGet(s3);
        Map<String,String> userMap = JSON.parseObject(user_json, Map.class);

        //将用户信息添加进数据库
        UmsMember umsMember = new UmsMember();
        umsMember.setNickname(userMap.get("screen_name"));
        umsMember.setCity(userMap.get("city"));
        umsMember.setAccessCode(code);
        umsMember.setAccessToken(access_token);
        umsMember.setCreateTime(new Date());
        String gender="0";
        gender = userMap.get("gender"); //性别：0->未知；1->男；2->女
        if(gender.equals("m")){
            gender="1";
        }else if(gender.equals("f")){
            gender="2";
        }else{
            gender="0";
        }
        umsMember.setGender(gender);
        umsMember.setSourceType(2);
        umsMember.setSourceUid(userMap.get("idstr"));

        //检查uid 如果已经有了就不用添加了
        UmsMember u1 = userService.checkUserUid(userMap.get("idstr"));
        if(u1==null){
            userService.addUserFromSina(umsMember);
        }
        //通过jwt生成token
        UmsMember u2 = userService.checkUserUid(userMap.get("idstr"));
        Map<String, Object> map1 = new HashMap<>();
        String memeberId = u2.getId();
        String nickName = u2.getNickname();
        map1.put("memberId", memeberId);
        map1.put("nickName", nickName);

        String ip = "127.0.0.1";
        String time = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        String salt = ip;
        String token ="" ;
        token = JwtUtil.encode("gmall101", map1, salt);
        //缓存添加token
        userService.addUserToken(token,memeberId);
        //重定向到首页
        return "redirect:http://search.gmall.com:8083/index?token="+token;
    }


    @RequestMapping("index")
    public String index(String ReturnUrl,ModelMap modelMap){
        //获取请求的url
        modelMap.put("ReturnUrl",ReturnUrl);
        return "index";

    }
    @RequestMapping("login")
    @ResponseBody
    public String login(UmsMember member,HttpServletRequest request, HttpServletResponse response, ModelMap modelMap) {
        //验证用户名密码
        UmsMember umsMember = userService.getUser(member);
        String token = "";
        if (umsMember != null) {
            //如果通过这个用户名和密码能查到用户 则通过
            //Jwt生成token
            //封装用户数据
            Map<String, Object> map = new HashMap<>();
            String memeberId = umsMember.getId();
            String nickName = umsMember.getNickname();
            map.put("memberId", memeberId);
            map.put("nickName", nickName);
            //通过nginx 获取ip(转发代理后)
          //  String ip = request.getHeader("x-forwarded-for");
           // if (StringUtils.isBlank(ip)) {
                //如果没有用nginx 则直接获取请求的ip
            String  ip = request.getRemoteAddr();
          //  }
            ip = "127.0.0.1";
            String time = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
            String salt = ip;

            token = JwtUtil.encode("gmall101", map, salt);
            //缓存添加token
            userService.addUserToken(token,memeberId);
        } else {
            //用户名或密码错误 给页面个提示
            token = "fail";
        }
        return token;
    }
    //检验token
    @RequestMapping("verify")
    @ResponseBody
    public String verify(String token,String ip){
        //jwt解密
        String time = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        String salt = ip;
        Map<String,String> map = new HashMap<>();
        Map<String, Object> decode = JwtUtil.decode(token, "gmall101", salt);
        if(decode!=null){
            //解密成功 合法用户
            map.put("state","success");
            map.put("memberId",(String)decode.get("memberId"));
            map.put("nickName",(String)decode.get("nickName"));
        }else{
            map.put("state","fail");
        }
        return JSON.toJSONString(map);
    }
}
