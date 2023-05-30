package com.kautiainen.antti.solita;

public class Constants
{
	/**
	 * These define the URL parmaeters used in the route definition strings (e.g. '{userId}').
	 */
	public class Url
	{
		/**
		 * The journey id.
		 */
		public static final String JOURNEY_ID = "journeyId";

		/**
		 * The journey filter parameter.
		 */
		public static final String JOURNEY_FILTER = "journeyFilter";


		/**
		 * The station identifier. 
		 */
		public static final String STATION_ID = "stationId";


	}

	/**
	 * These define the route names used in naming each route definitions.  These names are used
	 * to retrieve URL patterns within the controllers by name to create links in responses.
	 */
	public class Routes
	{
		public static final String SINGLE_JOURNEY = "journey";
		public static final String JOURNEY_COLLECTION = "journeys";
		public static final String SINGLE_STATION = "station";
		public static final String STATION_CONNECTION = "stations";
	}
}
