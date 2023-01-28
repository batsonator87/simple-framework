package com.batsonator.simpleinjections.exception;

public class InjectionException extends DependencyException {

	public InjectionException( Exception e ) {

		super( e );
	}
}