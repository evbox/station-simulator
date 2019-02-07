package com.evbox.everon.ocpp.simulator.websocket;

import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class LoggingInterceptor implements Interceptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoggingInterceptor.class);

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();

        long t1 = System.nanoTime();
        HttpUrl requestUrl = request.url();
        Headers requestHeaders = request.headers();
        LOGGER.debug("Sending request {} via {}", requestUrl, requestHeaders);

        Response response = chain.proceed(request);

        long t2 = System.nanoTime();
        HttpUrl responseReqUrl = response.request().url();
        Headers responseHeaders = response.headers();
        double responseTime = (t2 - t1) / 1e6;
        LOGGER.debug("Received response for {} in {}ms \n{}", responseReqUrl, responseTime, responseHeaders);
        return response;
    }
}
