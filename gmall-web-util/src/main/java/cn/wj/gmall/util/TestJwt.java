package cn.wj.gmall.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class TestJwt {

    public static void main(String[] args) {
        Map<String,Object> map = new HashMap<>();
        map.put("memberId","1");
        map.put("nickname","zhangsan");
        String s = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        String salt = "192.168.25.142"+s;
        String encode = JwtUtil.encode("gmall101", map, salt);
        System.out.println(encode);
    }
}
