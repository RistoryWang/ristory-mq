
package com.zenzet;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.QueryStringDecoder;
import org.apache.log4j.Logger;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpResponseStatus.*;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

import org.graylog2.log.GelfAppender;

/**
 * Created by ristory on 16/5/29.
 */
public class FrontendHandler extends ChannelInboundHandlerAdapter {
    private final static Logger logger = Logger.getLogger(FrontendHandler.class);
    private JedisUtil jedisUtil = (JedisUtil) Server.context.getBean("jedisUtil");
    private final static String PATTERN = "([^?|!]*)(.*)";
    public FrontendHandler() {

    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
    }

    @Override
    public void channelRead(final ChannelHandlerContext ctx, Object msg) throws Exception {

        if (msg instanceof FullHttpRequest) {

            HttpRequestExt requestExt = new HttpRequestExt((FullHttpRequest) msg);

            //String uri = request.getUri();
            ByteBuf buf = requestExt.content();
            String uri = decodeUri(requestExt.getUri());
            QueryStringDecoder queryStringDecoder = new QueryStringDecoder(
                    requestExt.getUri());
            Map<String, List<String>> params = queryStringDecoder.parameters();

            String queueName = null;
            String message = null;
            String optName = null;
            if (uri.startsWith("/dequeue/")) {
                optName = "dequeue";
                queueName = getParameterFromUri("/dequeue/", uri);
            } else if (uri.startsWith("/enqueue/")) {
                optName = "enqueue";
                queueName = getParameterFromUri("/enqueue/", uri);
            } else {
                if (params.isEmpty()) {
                    //need define by wzr //miss parameters!
                    String backContent = new ResponseBody(ResponseBody.ILLEGAL_URI_SYNTAX, "1").toString();
                    returnResponse(FORBIDDEN, backContent, ctx, buf);
                    return;
                }

                queueName = getParameterValue("name", params);
                optName = getParameterValue("opt", params);
            }

            if (optName == null || optName.trim().equals("")) {
                optName = "get";
            }

            if (queueName == null || queueName.trim().equals("")) {
                //TODO need define by wzr miss queue name!
                String backContent = new ResponseBody(ResponseBody.ILLEGAL_URI_SYNTAX, "2").toString();
                returnResponse(FORBIDDEN, backContent, ctx, buf);
                return;
            }

            if (optName.toLowerCase().equals("dequeue")) {
                String result = jedisUtil.pop(queueName);
                if (result == null) {
                    result = "";
                }
                //TODO need define by wzr //执行获取操作!

                String backContent = new ResponseBody(ResponseBody.NO_ERROR, result).toString();
                returnResponse(OK, backContent, ctx, buf);
                return;
            }

//            message = getParameterValue("msg", params);
//
//            if (message == null) {
//                //need define by wzr //miss parameter msg!
//                String backContent = new ResponseBody(ResponseBody.ILLEGAL_URI_SYNTAX, "3").toString();
//                returnResponse(FORBIDDEN, backContent, ctx, buf);
//                return;
//            }

            String content = buf.toString(StandardCharsets.UTF_8);

            // 执行插入队列操作
            long result = jedisUtil.push(queueName, content);

            //TODO need define by wzr //执行插入队列操作!

            String backContent = new ResponseBody(ResponseBody.NO_ERROR, String.valueOf(result)).toString();
            returnResponse(OK, backContent, ctx, buf);




        }
    }

    private String decodeUri(String uri) {
        try {
            return URLDecoder.decode(uri, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            try {
                return URLDecoder.decode(uri, "ISO-8859-1");
            } catch (UnsupportedEncodingException e1) {
                return uri;
            }
        }
    }

    private static String getParameterFromUri(String startPrefix, String uri) {
        Pattern pattern = Pattern.compile(startPrefix + PATTERN,
                Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(uri);
        if (!matcher.find())
            return null;

        return matcher.group(1);
    }

    private String getParameterValue(String parameterName,
                                     Map<String, List<String>> params) {
        List<String> values = params.get(parameterName);
        String parameterValue = null;
        if (values != null && !values.isEmpty()) {
            parameterValue = values.get(0);
        }

        return parameterValue;
    }

    private void returnResponse(HttpResponseStatus status, String content, ChannelHandlerContext ctx, ByteBuf buf) throws Exception {
        logger.trace(content);
        try {
            FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, status);
            response.headers().set(CONTENT_TYPE, "application/json");

            ByteBuf buf1 = Unpooled.wrappedBuffer(content.getBytes("UTF-8"));
            response.headers().set(CONTENT_LENGTH, buf1.readableBytes());
            response.content().writeBytes(buf1);
            buf1.release();
            ctx.write(response);
            ctx.flush();
        } finally {
            buf.release();
        }
    }

}
