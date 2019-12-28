package cn.wj.gmall;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import tk.mybatis.spring.annotation.MapperScan;


@SpringBootApplication
@MapperScan("cn.wj.gmall.user.mapper")
public class GmallUserServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(GmallUserServiceApplication.class, args);
    }
}



