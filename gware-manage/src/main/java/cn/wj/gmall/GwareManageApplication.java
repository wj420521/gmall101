package cn.wj.gmall;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import tk.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@MapperScan(basePackages = "cn.wj.gmall.gware.mapper")
public class GwareManageApplication {

    public static void main(String[] args) {
        SpringApplication.run(GwareManageApplication.class, args);
    }

}
