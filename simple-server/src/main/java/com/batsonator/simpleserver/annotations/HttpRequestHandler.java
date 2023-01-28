package com.batsonator.simpleserver.annotations;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.batsonator.simpleinjections.annotations.Injectable;

@Retention( RUNTIME )
@Target( METHOD )
@Injectable
public @interface HttpRequestHandler {

	String method();
	String path() default "/";
}