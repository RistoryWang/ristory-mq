
package com.zenzet;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import org.apache.log4j.Logger;

/**
 * Created by ristory on 16/5/29.
 */
public class HttpRequestInitializer extends ChannelInitializer<SocketChannel> {
    final static Logger logger = Logger.getLogger(HttpRequestInitializer.class);
    private int maxContentLength = 1024 * 10240;
    public HttpRequestInitializer() {
    }

    @Override
    public void initChannel(SocketChannel ch) {
        ch.pipeline().addLast(new HttpRequestDecoder());
        ch.pipeline().addLast(new HttpResponseEncoder());
        ch.pipeline().addLast(new HttpObjectAggregator(maxContentLength));
        ch.pipeline().addLast(new FrontendHandler());
    }
}
