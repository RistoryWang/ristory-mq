/**
 *
 */
package com.zenzet;

import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.FullHttpRequest;

import java.util.HashMap;
import java.util.Map;


/**
 * Created by ristory on 16/5/29.
 */
public class HttpRequestExt extends DefaultFullHttpRequest implements FullHttpRequest {
	 private Map<String, Object> properties = new HashMap<String, Object>();
	    

    public HttpRequestExt(FullHttpRequest request) {
        super(request.getProtocolVersion(), request.getMethod(), request.getUri(), request.content());
        headers().set(request.headers());
        trailingHeaders().set(request.trailingHeaders());
    }

    public Object getProperty(String key) {
        return properties.get(key);
    }


    public void setProperty(String key, Object value) {
    	properties.put(key, value);
    }


}
