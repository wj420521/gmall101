package cn.wj.gmall.manage.controller;

import cn.wj.gmall.bean.PmsProductImage;
import cn.wj.gmall.bean.PmsProductInfo;
import cn.wj.gmall.bean.PmsProductSaleAttr;
import cn.wj.gmall.manage.util.FastDfsUtil;
import cn.wj.gmall.service.SpuService;
import com.alibaba.dubbo.config.annotation.Reference;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Controller
@CrossOrigin
public class SpuController {

    @Reference
    private SpuService spuService;

    @RequestMapping("spuList")
    @ResponseBody
    public List<PmsProductInfo> spuList(String catalog3Id){
        List<PmsProductInfo> pmsProductInfos =  spuService.spuList(catalog3Id);
        return pmsProductInfos;
    }

    @RequestMapping("fileUpload")
    @ResponseBody
    //参数默认就是file
    public String fileUpload(@RequestParam("file") MultipartFile multipartFile){
        //FastDfs 文件上传
        String fileUrl = FastDfsUtil.uploadImage(multipartFile);
        return fileUrl;
    }

    @RequestMapping("saveSpuInfo")
    @ResponseBody
    public String saveSpuInfo(@RequestBody PmsProductInfo pmsProductInfo){
        spuService.saveSpuInfo(pmsProductInfo);
        return "success";
    }

    @RequestMapping("spuSaleAttrList")
    @ResponseBody
    public List<PmsProductSaleAttr> spuSaleAttrList(String spuId){
        List<PmsProductSaleAttr> pmsProductSaleAttrs =  spuService.spuSaleAttrList(spuId);
        return pmsProductSaleAttrs;
    }

    @RequestMapping("spuImageList")
    @ResponseBody
    public List<PmsProductImage> spuImageList(String spuId){
        List<PmsProductImage> pmsProductImages = spuService.spuImageList(spuId);
        return pmsProductImages;
    }
}
