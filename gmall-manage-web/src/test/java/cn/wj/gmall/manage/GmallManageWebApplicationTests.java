package cn.wj.gmall.manage;

import org.csource.common.MyException;
import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.StorageClient;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerServer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GmallManageWebApplicationTests {

    @Test
    public void contextLoads() throws IOException, MyException {
        //读取配置文件位置
        String file = GmallManageWebApplicationTests.class.getResource("/tracker.conf").getFile();
        //System.out.println(file);
        ClientGlobal.init(file);
        //创建trackeClient
        TrackerClient trackerClient = new TrackerClient();
        //获得trackerServer
        TrackerServer trackerServer = trackerClient.getConnection();
        StorageClient storageClient = new StorageClient(trackerServer, null);
        /**
         *  文件地址，文件扩展名，null
         */
        String[] s = storageClient.upload_file("d:/2.jpg","jpg",null);
        String url = "http://192.168.25.142";
        for (String s1 : s) {
            //System.out.println(s1);  group1 M00/00/00/wKgZjl3HnjOAa_5fAAEZyTkFdGU013.jpg
            url+="/"+s1;
        }
        System.out.println(url);
    }

}
