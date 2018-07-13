package com.banggood.xwork.core.service;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.banggood.xwork.query.DeleteQueryObject;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zouyi on 2018/3/1.
 */
public class ParametersParse {

    @Test
    public void parseMap() {
        Map<String, Map<String, String>> paramMaps = new HashMap<>();
        Map<String, String> map = new HashMap<>(3);
        for (int i = 0; i < 3; i++) {
            map.put("1" + i, "2" + i);
        }
        paramMaps.put("123", map);
        String runningParams = JSONObject.toJSONString(paramMaps);
        Map<String, Map<String, String>> stringMapMap = JSONObject.parseObject(runningParams, new TypeReference<Map<String, Map<String, String>>>() {
        });
        System.out.println(stringMapMap);
    }

    @Test
    public void parseRunParam() {
        Map<String, Map<String, String>> parseMap = new HashMap<>();
        Map<String, String> map = new HashMap<>();
        map.put("myTime", "2015-05-06");
        map.put("xxx", "date > 20000");
        map.put("xxx1", "date > 20000");
        map.put("xxx2", "date > 20000");
        map.put("xxx3", "date > 20000");
        parseMap.put("workFlowName_ActionName", map);
        String parseRunParam = JSONObject.toJSONString(parseMap);
        System.out.println(parseRunParam);
        String[] v = new String[map.size()];
        int i = 0;
        for (String key : map.keySet()) {
            v[i] = key;
            i++;
        }
        System.out.println(v);
    }

    @Test
    public void putActionsArguments() {

        Map<String, Map<String, String>> parseMap = new HashMap<>();
        Map<String, String> map = new HashMap<>();
        map.put("params1", "-------------------参数1----------------");
        parseMap.put("action_1", map);
        map = new HashMap<>();
        map.put("params2", "-------------------参数2----------------");
        parseMap.put("action_2", map);
        map = new HashMap<>();
        map.put("params3", "-------------------参数3----------------");
        parseMap.put("action_3", map);
    }

    @Test
    public void deleteQueryObject() {
        List<String> list = new ArrayList<>();
        list.add("wf_simple_shell_d5be4669-c361-4b0d-acef-55388c7b0725_1520050210345");
        DeleteQueryObject dqo = new DeleteQueryObject();
        dqo.setList(list);
        System.out.println(JSONObject.toJSONString(dqo));
    }


}
