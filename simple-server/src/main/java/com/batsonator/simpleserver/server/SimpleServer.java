package com.batsonator.simpleserver.server;

import java.io.IOException;
import java.net.InetSocketAddress;

import com.batsonator.simpleserver.exception.ServletException;
import com.batsonator.simpleserver.handler.SimpleRequestExecutor;
import com.batsonator.simpleinjections.annotations.Injectable;
import com.batsonator.simpleinjections.annotations.Injected;
import com.sun.net.httpserver.HttpServer;

@Injectable
public class SimpleServer {

	@Injected
	private SimpleRequestExecutor simpleRequestExecutor;

	private final HttpServer httpServer;

	public SimpleServer() throws ServletException {

		this( 8080 );
	}

	public SimpleServer( int port ) throws ServletException {

		try {

			this.httpServer = HttpServer.create( new InetSocketAddress( port ), 0 );
		}
		catch( IOException e ) {

			throw new ServletException( e );
		}
	}

	public void start() {

		this.httpServer.createContext( "/", simpleRequestExecutor );

		httpServer.start();
	}

	public void stop() {

		httpServer.stop( 0 );
	}
}