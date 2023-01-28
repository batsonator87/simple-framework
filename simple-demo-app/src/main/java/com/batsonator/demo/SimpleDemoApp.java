package com.batsonator.demo;

import com.batsonator.simpleinjections.annotations.PackageScan;
import com.batsonator.simpleinjections.annotations.PackageScans;
import com.batsonator.simpleserver.SimpleServerApplication;

@PackageScans(  {
		@PackageScan( "com.batsonator.simpleserver.server" ),
		@PackageScan( "com.batsonator.simpleserver.handler" ),
		@PackageScan( "com.batsonator.demo.handlers" )
})
public class SimpleDemoApp {

	public static void main( String[] args ) {

		SimpleServerApplication.run( SimpleDemoApp.class );
	}
}