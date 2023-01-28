package com.batsonator.simpleinjections.context;

import static java.lang.ClassLoader.getSystemClassLoader;
import static java.lang.String.format;
import static java.util.stream.Collectors.toSet;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.batsonator.simpleinjections.annotations.Injectable;
import com.batsonator.simpleinjections.annotations.Injected;
import com.batsonator.simpleinjections.annotations.PackageScan;
import com.batsonator.simpleinjections.annotations.PackageScans;
import com.batsonator.simpleinjections.exception.DependencyException;
import com.batsonator.simpleinjections.exception.InjectionException;
import com.batsonator.simpleinjections.exception.PackageScanException;

public class AppContext {

	private final Map<Class<?>, Object> objectRegistryMap = new HashMap<>();

	public AppContext( Class<?> clazz ) throws DependencyException {

		initializeContext( clazz );
	}

	public <T> T getInstance( Class<T> clazz ) throws InjectionException {

		T object = clazz.cast( objectRegistryMap.get( clazz ) );

		Field[] declaredFields = clazz.getDeclaredFields();
		injectAnnotatedFields( object, declaredFields );

		return object;
	}

	private <T> void injectAnnotatedFields( T object, Field[] declaredFields ) throws InjectionException {

		for( Field field : declaredFields ) {

			if( field.isAnnotationPresent( Injected.class ) ) {

				field.setAccessible( true );
				Class<?> type = field.getType();

				if( type.isAssignableFrom( Collection.class ) ) {

					try {
						ParameterizedType fieldType = (ParameterizedType) field.getGenericType();
						Class<?> fieldGenericType = (Class<?>) fieldType.getActualTypeArguments()[ 0 ];

						Set<Object> innerObjects = objectRegistryMap.entrySet().stream()
								.filter( classObjectEntry -> fieldGenericType.isAssignableFrom( classObjectEntry.getKey() ) )
								.collect( toSet() );

						field.set( object, innerObjects );

						for( Object innerObject : innerObjects ) {
							injectAnnotatedFields( innerObject, type.getDeclaredFields() );
						}
					}
					catch( IllegalAccessException e ) {

						throw new InjectionException( e );
					}
				}
				else {

					try {

						Object innerObject = objectRegistryMap.get( type );

						field.set( object, innerObject );

						injectAnnotatedFields( innerObject, type.getDeclaredFields() );
					}
					catch( IllegalAccessException e ) {

						throw new InjectionException( e );
					}
				}
			}
		}
	}

	private void initializeContext( Class<?> clazz ) throws DependencyException {

		PackageScan[] packagesToScan;
		if( clazz.isAnnotationPresent( PackageScans.class ) ) {
			PackageScans packageScans = clazz.getAnnotation( PackageScans.class );
			packagesToScan = packageScans.value();
		}
		else if( clazz.isAnnotationPresent( PackageScan.class ) ) {
			packagesToScan = new PackageScan[]{ clazz.getAnnotation( PackageScan.class ) };
		}
		else {
			throw new RuntimeException( "Please provide at least one package to scan!" );
		}
		Set<String> packageNames = Arrays.stream( packagesToScan )
				.map( PackageScan::value )
				.collect( toSet() );
		Set<Class<?>> classes = findClassesInPackages( packageNames );

		for( Class<?> loadingClass : classes ) {
			try {
				if( loadingClass.isAnnotationPresent( Injectable.class ) ) {
					Constructor<?> constructor = loadingClass.getDeclaredConstructor();
					Object newInstance = constructor.newInstance();
					objectRegistryMap.put( loadingClass, newInstance );
				}
			}
			catch( NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e ) {

				throw new PackageScanException( e );
			}
		}
	}

	private static Set<Class<?>> findClassesInPackages( Set<String> packageNames ) throws PackageScanException {

		Set<Class<?>> classesInPackages = new HashSet<>();

		for( String packageName : packageNames ) {

			classesInPackages.addAll( getClassesInPackage( packageName ) );
		}

		return classesInPackages;
	}

	private static Set<Class<?>> getClassesInPackage( String packageName ) throws PackageScanException {

		Set<Class<?>> classesInPackage = new HashSet<>();

		InputStream stream = getSystemClassLoader().getResourceAsStream( packageName.replaceAll( "[.]", "/" ) );
		if( stream != null ) {

			BufferedReader reader = new BufferedReader( new InputStreamReader( stream ) );

			Set<String> collect = reader.lines().collect( toSet() );

			for( String line : collect ) {

				if( line.endsWith( ".class" ) ) {

					classesInPackage.add( getClass( line, packageName ) );
				}
			}
		}

		return classesInPackage;
	}

	private static Class<?> getClass( String className, String packageName ) throws PackageScanException {

		String sanitisedClassName = className.substring( 0, className.lastIndexOf( '.' ) );

		try {

			return Class.forName( format( "%s.%s", packageName, sanitisedClassName )  );
		}
		catch( ClassNotFoundException e ) {

			throw new PackageScanException( e );
		}
	}
}