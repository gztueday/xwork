package remote;

import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("ALL")
public class OkHTTPUtil {
    private static Logger logger = LoggerFactory.getLogger(OkHTTPUtil.class);
    private String responseBody = null;
    private boolean isResponsed = false;
    //提交json数据
    public final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    public final MediaType MEDIA_TYPE_MARKDOWN = MediaType.parse("application/json; charset=utf-8");

    public static OkHttpClient client = null;
    private static final long TIMEOUT_CONNECT = 120;
    private static final long TIMEOUT_READ = 120;

    static {
        if (client == null) {
            client = new OkHttpClient.Builder()
                    .connectTimeout(TIMEOUT_CONNECT, TimeUnit.SECONDS)
                    .readTimeout(TIMEOUT_READ, TimeUnit.SECONDS)
                    .build();
        }
    }

    public String httpRequestGet(String requestUrl) {
        Request request = new Request.Builder().url(requestUrl).build();
        try {
            //添加Timeout重试机制
            client.newCall(request).enqueue(new Callback() {
                int serversLoadTimes = 0;
                int maxLoadTimes = 5;

                @Override
                public void onFailure(Call call, IOException e) {
                    if (e.getClass().equals(SocketTimeoutException.class) && serversLoadTimes < maxLoadTimes) {// 如果超时并未超过指定次数，则重新连接
                        serversLoadTimes++;
                        logger.error("尝试发送失败，当前重试连接次数:[{}]", serversLoadTimes);
                        client.newCall(call.request()).enqueue(this);
                    } else {
                        e.printStackTrace();
                        logger.error("发送HTTP请求失败", e);
                        throw new RuntimeException("发送HTTP请求失败");
                    }
                    isResponsed = true;
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
                    int respCode = response.code();
                    logger.info("响应码:[{}]", respCode);
                    responseBody = response.body().string();
                    // logger.info(responseBody);
                    isResponsed = true;
                }
            });
        } catch (Exception e) {
            logger.error("HttpRequestGet-HTTP请求错误", e);
        }

        //阻塞(OkHttp暂时不提供异步get)
        while (!isResponsed) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                logger.error("HttpRequestGet-等待结果时发生错误", e);
            }
        }
        return responseBody;
    }

    /**
     * put,body为json模式
     *
     * @param url
     * @param json
     * @return
     */
    public String httpRequestPut(String url, String json) {
        RequestBody body = RequestBody.create(JSON, json);
        Request request = new Request.Builder().url(url).put(body).build();
        client.newCall(request).enqueue(new Callback() {
            int serversLoadTimes = 0;
            int maxLoadTimes = 5;

            @Override
            public void onFailure(Call call, IOException e) {
                if (e.getClass().equals(SocketTimeoutException.class) && serversLoadTimes < maxLoadTimes) {// 如果超时并未超过指定次数，则重新连接
                    serversLoadTimes++;
                    logger.error("尝试发送失败，当前重试连接次数:[{}]", serversLoadTimes);
                    client.newCall(call.request()).enqueue(this);
                } else {
                    e.printStackTrace();
                    logger.error("发送HTTP请求失败", e);
                    throw new RuntimeException("发送HTTP请求失败");
                }
                isResponsed = true;
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
                int respCode = response.code();
                logger.info("响应码:[{}]", respCode);
                responseBody = response.body().string();
                logger.info(responseBody);
                isResponsed = true;
            }
        });
        while (!isResponsed) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                logger.error("HttpRequestPut-等待结果时发生错误", e);
            }
        }
        return responseBody;
    }

    /**
     * post,body为json模式
     *
     * @param url
     * @param json
     * @return
     */
    public String httpRequstPost(String url, String json, Map<String, String> param) {
        Request request = null;
        if (json != null) {
            RequestBody body = RequestBody.create(JSON, json);
            if (param != null) {
                FormBody.Builder builder = new FormBody.Builder();
                for (String key : param.keySet()) {
                    builder.add(key, param.get(key));
                }
                RequestBody body2 = builder.build();
                request = new Request.Builder().url(url).post(body).post(body2).build();
            } else {
                request = new Request.Builder().url(url).post(body).build();
            }
        }else{
            if (param != null) {
                FormBody.Builder builder = new FormBody.Builder();
                for (String key : param.keySet()) {
                    builder.add(key, param.get(key));
                }
                RequestBody body2 = builder.build();
                request = new Request.Builder().url(url).post(body2).build();
            }
        }

        client.newCall(request).enqueue(new Callback() {
            int serversLoadTimes = 0;
            int maxLoadTimes = 5;

            @Override
            public void onFailure(Call call, IOException e) {
                if (e.getClass().equals(SocketTimeoutException.class) && serversLoadTimes < maxLoadTimes) {// 如果超时并未超过指定次数，则重新连接
                    serversLoadTimes++;
                    logger.error("尝试发送失败，当前重试连接次数:[{}]", serversLoadTimes);
                    client.newCall(call.request()).enqueue(this);
                } else {
                    e.printStackTrace();
                    logger.error("发送HTTP请求失败", e);
                    throw new RuntimeException("发送HTTP请求失败");
                }
                isResponsed = true;
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
                int respCode = response.code();
                logger.info("响应码:[{}]", respCode);
                responseBody = response.body().string();
                logger.info(responseBody);
                isResponsed = true;
            }
        });
        while (!isResponsed) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                logger.error("HttpRequestPost-等待结果时发生错误", e);
            }
        }
        return responseBody;
    }

    public String httpRequestDelHasBody(String url, String json) {
        RequestBody body = RequestBody.create(JSON, json);
        Request request = new Request.Builder().url(url).delete(body).build();
        client.newCall(request).enqueue(new Callback() {
            int serversLoadTimes = 0;
            int maxLoadTimes = 5;

            @Override
            public void onFailure(Call call, IOException e) {
                if (e.getClass().equals(SocketTimeoutException.class) && serversLoadTimes < maxLoadTimes) {// 如果超时并未超过指定次数，则重新连接
                    serversLoadTimes++;
                    logger.error("尝试发送失败，当前重试连接次数:[{}]", serversLoadTimes);
                    client.newCall(call.request()).enqueue(this);
                } else {
                    e.printStackTrace();
                    logger.error("发送HTTP请求失败", e);
                    throw new RuntimeException("发送HTTP请求失败");
                }
                isResponsed = true;
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
                int respCode = response.code();
                logger.info("响应码:[{}]", respCode);
                responseBody = response.body().string();
                logger.info(responseBody);
                isResponsed = true;
            }
        });
        while (!isResponsed) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                logger.error("httpRequestDelHasBody-等待结果时发生错误", e);
            }
        }
        return responseBody;
    }

    public String httpRequestDelNoBody(String url) {
        Request request = new Request.Builder().url(url).delete().build();
        client.newCall(request).enqueue(new Callback() {
            int serversLoadTimes = 0;
            int maxLoadTimes = 5;

            @Override
            public void onFailure(Call call, IOException e) {
                if (e.getClass().equals(SocketTimeoutException.class) && serversLoadTimes < maxLoadTimes) {// 如果超时并未超过指定次数，则重新连接
                    serversLoadTimes++;
                    logger.error("尝试发送失败，当前重试连接次数:[{}]", serversLoadTimes);
                    client.newCall(call.request()).enqueue(this);
                } else {
                    e.printStackTrace();
                    logger.error("发送HTTP请求失败", e);
                    throw new RuntimeException("发送HTTP请求失败");
                }
                isResponsed = true;
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
                int respCode = response.code();
                logger.info("响应码:[{}]", respCode);
                responseBody = response.body().string();
                logger.info(responseBody);
                isResponsed = true;
            }
        });
        while (!isResponsed) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                logger.error("httpRequestDelNoBody-等待结果时发生错误", e);
            }
        }
        return responseBody;
    }
}