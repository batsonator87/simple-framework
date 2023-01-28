package com.batsonator.demo.handlers;

import com.batsonator.simpleserver.annotations.HttpExecutor;
import com.batsonator.simpleserver.annotations.HttpRequestHandler;
import com.batsonator.simpleserver.handler.RequestHandler;

@HttpExecutor
public class BaseRequestHandler implements RequestHandler {

	@HttpRequestHandler( method = "POST", path = "test/" )
	public void testEndpoint() {

		System.out.println( "You hit the TEST endpoint!" );
	}

	@HttpRequestHandler( method = "GET", path = "example/" )
	public void exampleEndpoint() {

		System.out.println( "You hit the EXAMPLE endpoint!" );
	}
}