package cn.wj.gmall.service;

import cn.wj.gmall.bean.PmsSearchParam;
import cn.wj.gmall.bean.PmsSearchSkuInfo;

import java.util.List;

public interface SearchService {

    List<PmsSearchSkuInfo> getAll(PmsSearchParam pmsSearchParam);
}
