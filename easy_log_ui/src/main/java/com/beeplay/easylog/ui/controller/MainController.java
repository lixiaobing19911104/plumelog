package com.beeplay.easylog.ui.controller;

import com.beeplay.easylog.core.util.GfJsonUtil;
import com.beeplay.easylog.ui.es.ElasticLowerClient;
import com.beeplay.easylog.ui.es.ElasticSearchClient;
import com.beeplay.easylog.ui.util.LogUtil;
import org.apache.http.*;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @ClassName MainController
 * @Deacription TODO
 * @Author Frank.Chen
 * @Date 2020/5/18 11:51
 * @Version 1.0
 **/
@RestController
@CrossOrigin
public class MainController {

    @Value("${es.esHosts}")
    private String esHosts;

    @RequestMapping("/query")
    public String query(@RequestBody String queryStr,String index,String size,String from) {
        String message="";
        String indexStr="";
        try {

            ElasticLowerClient elasticLowerClient=ElasticLowerClient.getInstance(esHosts);
            String[] indexs=index.split(",");
            List<String> reindexs=elasticLowerClient.getExistIndices(indexs);
            indexStr=String.join(",",reindexs);
            String url = "http://"+esHosts+"/"+indexStr+"/_search?from="+from+"&size="+size;
            return EntityUtils.toString(LogUtil.getInfo(url,queryStr), "utf-8");
        }catch (IOException e){
          e.printStackTrace();
        }
        return message;
    }
    @RequestMapping("/getServerInfo")
    public String query(String index) {
        ElasticLowerClient elasticLowerClient=ElasticLowerClient.getInstance(esHosts);
        String res=elasticLowerClient.cat(index);
        BufferedReader br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(res.getBytes(Charset.forName("utf8"))), Charset.forName("utf8")));
        List<String> list=new ArrayList<>();
        try {
            while (true) {
                String aa = br.readLine();
                if(StringUtils.isEmpty(aa)){
                    break;
                }
                list.add(aa);
            }
            List<Map<String,String>> listMap=new ArrayList<>();
            if(list.size()>0){
                String[] title=list.get(0).split("\\s+");
                for(int i=1;i<list.size();i++){
                    String[] values=list.get(i).split("\\s+");
                    Map<String,String> map=new HashMap<>();
                    for(int j=0;j<title.length;j++){
                        map.put(title[j],values[j]);
                    }
                    listMap.add(map);
                }
            }
            return GfJsonUtil.toJSONString(listMap);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }
}
