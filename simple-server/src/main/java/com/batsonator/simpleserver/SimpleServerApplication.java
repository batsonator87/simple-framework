package com.batsonator.simpleserver;

import com.batsonator.simpleinjections.context.AppContext;
import com.batsonator.simpleserver.server.SimpleServer;

public class SimpleServerApplication {

	public static void run( Class<?> clazz ) {

		try {

			AppContext appContext = new AppContext( clazz );
			SimpleServer simpleServer = appContext.getInstance( SimpleServer.class );

			simpleServer.start();
		}
		catch( Exception e ) {

			e.printStackTrace();
		}
	}
}