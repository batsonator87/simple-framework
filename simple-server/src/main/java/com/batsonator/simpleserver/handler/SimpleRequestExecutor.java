package com.batsonator.simpleserver.handler;

import static java.util.stream.Collectors.toSet;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import com.batsonator.simpleinjections.annotations.Injectable;
import com.batsonator.simpleinjections.annotations.Injected;
import com.batsonator.simpleserver.annotations.HttpRequestHandler;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

@Injectable
public class SimpleRequestExecutor implements HttpHandler {

	private Set<Method> handlerMethods = null;

	@Injected
	private Set<RequestHandler> requestHandlers;

	@Override
	public void handle( HttpExchange exchange ) throws IOException {

		if( handlerMethods == null ) {
			populateHandlerMethods();
		}

		Optional<Method> matchedMethod = handlerMethods.stream()
				.filter( handlerMethod -> {
					HttpRequestHandler httpRequestHandler = handlerMethod.getAnnotation( HttpRequestHandler.class );
					boolean matchesHttpMethod = exchange.getRequestMethod().equals( httpRequestHandler.method() );
					boolean matchesPath = exchange.getRequestURI().getPath().equals( httpRequestHandler.path() );
					return matchesHttpMethod && matchesPath;
				} )
				.findFirst();

		if( matchedMethod.isPresent() ) {

			try {

				matchedMethod.get().invoke( getMethodOwner( matchedMethod.get() ) );
			}
			catch( IllegalAccessException | InvocationTargetException e ) {

				e.printStackTrace( System.err );
			}
		}
		else {
			System.err.println( "No request handler matching" + exchange.getRequestMethod() + " " + exchange.getRequestURI() );
		}

		System.out.println( exchange.getRequestMethod() + " " + exchange.getRequestURI() + "{ " + new String( exchange.getRequestBody().readAllBytes() ) + "}" );
	}

	private void populateHandlerMethods() {

		handlerMethods = new HashSet<>();

		for( RequestHandler requestHandler : requestHandlers ) {

			handlerMethods.addAll( Arrays.stream( requestHandler.getClass().getDeclaredMethods() )
					.filter( method -> method.isAnnotationPresent( HttpRequestHandler.class ) )
					.collect( toSet() ) );
		}
	}

	private RequestHandler getMethodOwner( Method method ) {

		return requestHandlers.stream()
				.filter( requestHandler -> requestHandler.getClass().equals( method.getDeclaringClass() ) )
				.findFirst()
				.orElseThrow();
	}
}