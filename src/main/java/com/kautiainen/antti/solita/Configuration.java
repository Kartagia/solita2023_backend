package com.kautiainen.antti.solita;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

import org.restexpress.RestExpress;
import org.restexpress.util.Environment;

import com.kautiainen.antti.solita.controllers.JourneyController;
import com.kautiainen.antti.solita.controllers.StationController;
import com.kautiainen.antti.solita.model.Station;
import com.opencsv.bean.CsvToBeanBuilder;

public class Configuration
extends Environment
{
	private static final String DEFAULT_EXECUTOR_THREAD_POOL_SIZE = "20";

	private static final String PORT_PROPERTY = "port";
	private static final String BASE_URL_PROPERTY = "base.url";
	private static final String EXECUTOR_THREAD_POOL_SIZE = "executor.threadPool.size";
	private static final String STATION_FILE_PROPERTY = "init.station.file";
	private static final String STATION_FORMAT_PROPERTY = "init.station.format";

	private int port;
	private String baseUrl;
	private int executorThreadPoolSize;

	private JourneyController journeyController;

	private StationController stationController; 

	private String stationFileName; 

	private String stationFileFormat;

	@Override
	protected void fillValues(Properties p)
	{
		this.port = Integer.parseInt(p.getProperty(PORT_PROPERTY, String.valueOf(RestExpress.DEFAULT_PORT)));
		this.baseUrl = p.getProperty(BASE_URL_PROPERTY, "http://localhost:" + String.valueOf(port));
		this.executorThreadPoolSize = Integer.parseInt(p.getProperty(EXECUTOR_THREAD_POOL_SIZE, DEFAULT_EXECUTOR_THREAD_POOL_SIZE));
		this.stationFileName = p.getProperty(STATION_FILE_PROPERTY, null);
		this.stationFileFormat = p.getProperty(STATION_FORMAT_PROPERTY, "CSV");
		initialize();
	}

	private void initialize()
	{
		this.journeyController = new JourneyController();
		this.stationController = new StationController();

		if (getStationFileName().isPresent()) {
			// Start initialization of the stations. 
			try {
				java.io.File file = new java.io.File(getStationFileName().get());
				if (file.exists() && file.canRead() && file.isFile()) {
					List<Station> beans = new CsvToBeanBuilder<Station>(new java.io.FileReader(file))
					.withType(Station.class).build().parse();
					stationController.addStations(beans, false, 
					new LinkedList<>());
				} else {
					
				}
			} catch(java.io.IOException ioe) {
			}
		}
	}

	public int getPort()
	{
		return port;
	}
	
	public String getBaseUrl()
	{
		return baseUrl;
	}
	
	public int getExecutorThreadPoolSize()
	{
		return executorThreadPoolSize;
	}

	public JourneyController getJourneyController()
	{
		return journeyController;
	}

	public StationController getStationController() 
	{
		return stationController;
	}

	/**
	 * Get the station file name.
	 * @return The name of the station file, if it exists.
	 */
	public Optional<String> getStationFileName() {
		return Optional.ofNullable(this.stationFileName);
	}

	/**
	 * Get the station file format.
	 * @return The station file format.
	 */
	public Optional<String> getStationFileFormat() {
		return Optional.ofNullable(this.stationFileFormat);
	}
}
