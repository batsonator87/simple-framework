package com.batsonator.simpleinjections.exception;

public class DependencyException extends Exception {

	public DependencyException( Exception e ) {

		super ( e );
	}
}
