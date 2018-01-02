package com.zenzet;

import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Created by ristory on 16/5/29.
 */
public class Server {

	final static Logger logger = Logger.getLogger(Server.class);
	static public final ApplicationContext context = new ClassPathXmlApplicationContext("ServerConfig.xml");

	private int port;

	public Server(int port) {
		this.port = port;
	}

	public static void main(String[] args) throws Exception {

		final int LOCAL_PORT =6690;
		final EventLoopGroup bossGroup = new NioEventLoopGroup(1);
		final EventLoopGroup workerGroup = new NioEventLoopGroup();
		try {
			io.netty.bootstrap.ServerBootstrap b = new io.netty.bootstrap.ServerBootstrap();
			b.group(bossGroup, workerGroup)
					.channel(NioServerSocketChannel.class)
					.option(ChannelOption.TCP_NODELAY, true)
					.option(ChannelOption.SO_KEEPALIVE, true)
					.childOption(ChannelOption.SO_KEEPALIVE, true)
					.childHandler(new HttpRequestInitializer());

			b.bind(LOCAL_PORT).sync();


		} catch (Exception e){
			logger.error("Exception occurred while bootstrapping", e);
		}



	}
}