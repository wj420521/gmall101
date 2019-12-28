package cn.wj.gmall.manage.util;

import org.csource.common.MyException;
import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.StorageClient;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerServer;
import org.springframework.web.multipart.MultipartFile;


public class FastDfsUtil {
    public static String uploadImage(MultipartFile multipartFile) {

        String fileUrl = "http://192.168.25.142";
        //读取配置文件位置
        String file = FastDfsUtil.class.getResource("/tracker.conf").getFile();
        try {
            ClientGlobal.init(file);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //创建trackeClient
        TrackerClient trackerClient = new TrackerClient();
        try {
            //获得trackerServer
            TrackerServer trackerServer = trackerClient.getConnection();
            StorageClient storageClient = new StorageClient(trackerServer, null);
            String fileName = multipartFile.getOriginalFilename();
            byte[] bytes = multipartFile.getBytes();
            //获取最后一个 . 出现的位置
            int i = fileName.lastIndexOf(".");
            //截取最后一个 . 之后的字符串  扩展名
            String exName = fileName.substring(i+1);
            //upload_file 两种 1 文件名,文件扩展名,null 2.二进制对象 ，文件扩展名,null
            String[] s = storageClient.upload_file(bytes,exName,null);

            for (String s1 : s) {
                fileUrl +="/"+s1;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return  fileUrl;

    }
}
