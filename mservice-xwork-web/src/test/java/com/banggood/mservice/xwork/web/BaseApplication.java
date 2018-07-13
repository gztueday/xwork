package com.banggood.mservice.xwork.web;

import java.io.IOException;
import java.util.List;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.core.JsonProcessingException;

import okhttp3.FormBody.Builder;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = WebAPP.class)
@WebAppConfiguration
public class BaseApplication {
	
	@Autowired
    protected WebApplicationContext webApplicationConnext;

    protected MockMvc mvc;
    
    private final  OkHttpClient client = new OkHttpClient();

    protected static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    @Before
    public void setUp() throws JsonProcessingException {
        mvc = MockMvcBuilders.webAppContextSetup(webApplicationConnext).build();
    }

    
    public  Response post(String url,List<RequestParam> params) throws IOException {
        

		Builder builder=new Builder();
		for(RequestParam param:params)
		{
			builder.add(param.getPname(), param.getPvalue());
		}
		
		Request request = new Request.Builder()
			    .url(url)
			    .post(builder.build()).build();
		
		

		return client.newCall(request).execute();
		
    }

}
