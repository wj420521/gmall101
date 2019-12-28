package cn.wj.gmall.config;

import cn.wj.gmall.annotations.LoginRequired;
import cn.wj.gmall.util.CookieUtil;
import cn.wj.gmall.util.HttpclientUtil;
import com.alibaba.fastjson.JSON;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

@Component
public class AuthInterceptor extends HandlerInterceptorAdapter{


public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

    HandlerMethod hm = (HandlerMethod) handler;
    LoginRequired loginRequired = hm.getMethodAnnotation(LoginRequired.class);
    //如果没有这个注解 代表不用拦截 直接放行
    if(loginRequired==null){
        return true;
    }

    //必须拦截验证
    boolean login = loginRequired.loginSuccess();
    String token = "";
    String oldToken = CookieUtil.getCookieValue(request, "oldToken", true);
    String newToken = request.getParameter("token");

    //              oldToken空   oldToken不空
    // 新token空    从来没登陆过    以前登陆过
    // 新token不空   刚刚登陆       以前的过期了
    if(StringUtils.isNotBlank(oldToken)){
        token = oldToken;
    }
    if(StringUtils.isNotBlank(newToken)){
        token = newToken;
    }

    //验证token
    Map<String,Object> map = new HashMap<>();
    String success= "fail";
    if(StringUtils.isNotBlank(token)){
        String ip = request.getRemoteAddr();
        ip = "127.0.0.1";
        String successJson = HttpclientUtil.doGet("http://localhost:8085/verify?token=" + token+"&ip="+ip);
        map = JSON.parseObject(successJson, Map.class);
        success = (String)map.get("state");
    }

    if(login){
        //注解返回true 必须验证通过才行
        if(success.equals("success")){
            //token验证成功
            request.setAttribute("memberId",map.get("memberId"));
            request.setAttribute("nickName",map.get("nickName"));
            //将token写入cookie  刷新cookieg过期时间
            CookieUtil.setCookie(request,response,"oldToken",token,60*60*2,true);
        }else{
            //注解返回true.但是token 没通过 返回登陆页面 并且带上 原来的url
            StringBuffer requestURL = request.getRequestURL();
            response.sendRedirect("http://localhost:8085/index?ReturnUrl="+requestURL);
            return false;
        }
    }else{
        //注解返回false  验证通不通过都可以
        if(success.equals("success")){
            //如果验证通过
            request.setAttribute("memberId",map.get("memberId"));
            request.setAttribute("nickName",map.get("nickName"));
            //将token写入cookie  刷新cookieg过期时间
            CookieUtil.setCookie(request,response,"oldToken",token,60*60*2,true);
        }
    }

    return true;
}
}