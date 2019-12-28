package cn.wj.gmall.search.service.impl;

import cn.wj.gmall.bean.PmsSearchParam;
import cn.wj.gmall.bean.PmsSearchSkuInfo;
import cn.wj.gmall.bean.PmsSkuAttrValue;
import cn.wj.gmall.service.SearchService;
import cn.wj.gmall.service.SkuService;
import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import io.searchbox.client.JestClient;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import org.apache.commons.lang.StringUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.highlight.HighlightBuilder;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class SearchServiceImpl implements SearchService {

    @Autowired
    private JestClient jestClient;

    @Override
    public List<PmsSearchSkuInfo> getAll(PmsSearchParam pmsSearchParam) {
        //获取dsl语句
        String dslStr = getDsl(pmsSearchParam);
        Search select = new Search.Builder(dslStr).addIndex("gmall101").addType("PmsSkuInfo").build();
        List<PmsSearchSkuInfo> pmsSearchSkuInfos = new ArrayList<>();

            //查询结果
        SearchResult result=null;
        try {
            result = jestClient.execute(select);
        } catch (IOException e) {
            e.printStackTrace();
        }
        List<SearchResult.Hit<PmsSearchSkuInfo, Void>> hits = result.getHits(PmsSearchSkuInfo.class);
        for (SearchResult.Hit<PmsSearchSkuInfo, Void> hit : hits) {
            PmsSearchSkuInfo source = hit.source;
            //获取高亮的字段
            Map<String, List<String>> high = hit.highlight;
            if(high!=null){
                String skuName = high.get("skuName").get(0);
                //将高亮里面字段的内容 替换到source 里面的该字段上面
                source.setSkuName(skuName);
            }
            pmsSearchSkuInfos.add(source);
        }
        return pmsSearchSkuInfos;
    }

    private String getDsl(PmsSearchParam pmsSearchParam) {
        String keyword = pmsSearchParam.getKeyword();
        String catalog3Id =  pmsSearchParam.getCatalog3Id();
        String[] valueIds = pmsSearchParam.getValueId();
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //bool
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();

        if(StringUtils.isNotBlank(catalog3Id)){
            TermQueryBuilder termQueryBuilder = new TermQueryBuilder("catalog3Id",catalog3Id);
            boolQueryBuilder.filter(termQueryBuilder);
        }
        //term
        if(valueIds!=null){
            for (String valueId : valueIds) {
                TermQueryBuilder termQueryBuilder = new TermQueryBuilder("skuAttrValueList.valueId",valueId);
                boolQueryBuilder.filter(termQueryBuilder);
            }
        }
        //match
        if(StringUtils.isNotBlank(keyword)){
            MatchQueryBuilder matchQueryBuilder = new MatchQueryBuilder("skuName",keyword);
            boolQueryBuilder.must(matchQueryBuilder);
        }
        //from
        searchSourceBuilder.from(0);
        //size
        searchSourceBuilder.size(20);
        //highlight
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.field("skuName").preTags("<span style='color:red';>").postTags("</span>");
        //前缀
       // highlightBuilder.preTags("<font color='red'>");
        //后缀
       // highlightBuilder.postTags("</font>");
        searchSourceBuilder.highlight(highlightBuilder);
        searchSourceBuilder.query(boolQueryBuilder);

        String str = searchSourceBuilder.toString();
      //  System.out.println(str);
        return  str;
    }
}
