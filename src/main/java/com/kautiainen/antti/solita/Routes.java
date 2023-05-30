package com.kautiainen.antti.solita;

import com.kautiainen.antti.solita.controllers.*;

import io.netty.handler.codec.http.HttpMethod;

import org.restexpress.RestExpress;


public abstract class Routes
{
	public static void define(Configuration config, RestExpress server)
    {
		//TODO: Your routes here...
		server.uri("/journey/{journeyId}.{format}", config.getJourneyController())
			.method(HttpMethod.GET, HttpMethod.PUT, HttpMethod.DELETE)
			.name(Constants.Routes.SINGLE_JOURNEY);

		server.uri("/journeys/all.{format}", config.getJourneyController())
			.action("readAll", HttpMethod.GET)
			.method(HttpMethod.POST)
			.name(Constants.Routes.JOURNEY_COLLECTION);


		server.uri("/station/{stationId}.{format}", config.getStationController())
		.method(HttpMethod.GET, HttpMethod.PUT, HttpMethod.DELETE)
		.name(Constants.Routes.SINGLE_STATION);

		server.uri("stations/{format}", config.getStationController())
		.action("readAll", HttpMethod.GET)
		.method(HttpMethod.POST)
		.name(Constants.Routes.JOURNEY_COLLECTION);
// or...
//		server.regex("/some.regex", config.getRouteController());
    }
}
