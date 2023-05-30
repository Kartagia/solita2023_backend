package com.kautiainen.antti.solita.controllers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.restexpress.Request;
import org.restexpress.Response;
import org.restexpress.exception.BadRequestException;

import com.kautiainen.antti.solita.Constants;
import com.kautiainen.antti.solita.exceptions.InvalidFieldsException;
import com.kautiainen.antti.solita.model.Journey;
import com.kautiainen.antti.solita.model.Station;

import io.netty.handler.codec.http.HttpResponseStatus;

/**
 * Station controller handles station REST requests.
 */
public class StationController {

    /**
     * The in memory collection of stations.
     */
    private static volatile java.util.Map<Integer, Station> stations = Collections
            .synchronizedMap(new java.util.TreeMap<>());

    /**
     * Get new identifier.
     * 
     * @return The new identifier for the stations.
     */
    public synchronized Integer getNewIdentifier() {
        Optional<Integer> max = stations.keySet().stream().max(Comparator.naturalOrder());
        Integer result = 1;
        if (max.isPresent()) {
            result = max.get() + 1;
            stations.put(result, new Station(result, null, null));
        } else {
            result = 1;
            stations.put(result, new Station(result, null, null));
        }
        return result;
    }

    
    /**
     * Serves creation of a new station.
     * @param request The request.
     * @param response The response.
     */
    public Station create(Request request, Response response) {
        try {
            Station station = request.getBodyAs(Station.class, "Station details not provided");
            if (!station.isValid()) {
                // Invalid station.
                response.setResponseStatus(HttpResponseStatus.BAD_REQUEST);
            }
            if (station.isNew()) {
                // Adding identifier.
                final Integer id = getNewIdentifier();
                station.setId(id);
                stations.put(id, station);
            } else {
                // Checking if there is a journey with given identifier.
                final Integer id = station.getId();
                if (stations.containsKey(station.getId())) {
                    // The id is reserved.
                    throw new InvalidFieldsException("Identifier not unique",
                            null,
                            new InvalidFieldsException.FieldError(Journey.Fields.ID.toString(),
                                    "Reserved identifier"));
                } else {
                    stations.put(id, station);
                }
            }

            response.setResponseCreated();
            return station;
        } catch (Exception ife) {
            response.setException(ife);
            return null;
        }
    }


    /**
     * Serves acquisition of a single station.
     * @param request The request.
     * @param response The response.
     */
    public Station read(Request request, Response response) {
        String id = request.getHeader(Constants.Url.STATION_ID, "No Station ID supplied");
        try {
            int idValue = Integer.parseInt(id);
            return stations.get(idValue);
        } catch (NumberFormatException nfe) {
            response.setException(new BadRequestException("Invalid station identifier", nfe));
        }
        return null;
    }


    /**
     * Serves getting collection of stations.
     * 
     * Paging or filtered requests are not yet implemented.
     * 
     * @param request The request.
     * @param response The response.
     * @todo Filtering
     * @todo Paging.
     */
    public List<Station> readAll(Request request, Response response) {
        ArrayList<Station> result = new ArrayList<>();
        result.addAll(StationController.stations.values());
        return result;
    }

    /**
     * Serves update of a station requests.
     * 
     * Partial updates does not allow incomplete keys, and requires
     * an existing target. 
     * 
     * Complete updates performs creation of a new station if necessary.
     * 
     * @param request The request.
     * @param response The response.
     */
    public void update(Request request, Response response) {
        try {
            Station station = request.getBodyAs(Station.class, "Station details not provided");
            if (station.isIncomplete()) {
                // Partial update. 
                // - Partial update does not allow incomplete keys.
                // - Target must exist. 
                if (station.isNew()) {
                    // Incomplete value.
                    response.setResponseStatus(HttpResponseStatus.BAD_REQUEST);
                    return;                    
                } else if (stations.containsKey(station.getId())) {
                    Station target = stations.get(station.getId());

                    // TODO: create update method to Station.
                    if (station.getLang() != null) {
                        target.setLang(station.getLang());
                    }
                    if (station.getName() != null) {
                        target.setName(station.getName());
                    }
                } else {
                    // Missing station to update.
                    response.setResponseStatus(HttpResponseStatus.NOT_FOUND);
                }

            } else if (station.isNew()) {
                // Creating a new idetnifier.
                // TODO: Logger for adding a new station.
                final Integer id = getNewIdentifier();
                station.setId(id);
                stations.put(id, station);
            } else {
                // Checking if there is a journey with given identifier.
                final Integer id = station.getId();
                if (stations.containsKey(station.getId())) {
                    // TODO: Logger for replacing station.
                    stations.put(id, station);
                } else {
                    // TODO: Logger for adding new station.
                    stations.put(id, station);
                }
            }
            response.setResponseNoContent();
        } catch (Exception ife) {
            response.setException(new BadRequestException("Invalid updated station", ife));
        }
    }

    /**
     * Serves removal of a station.
     * @param request The request.
     * @param response The response.
     */
    public void delete(Request request, Response response) {
        try {
            Integer id = Integer.parseInt(request.getHeader(Constants.Url.STATION_ID, "No Station ID supplied"));
            if (stations.containsKey(id)) {
                stations.remove(id);
                response.setResponseNoContent();
            } else {
                response.setResponseStatus(HttpResponseStatus.NOT_FOUND);
            }
        } catch (NumberFormatException nfe) {
            throw new BadRequestException("Invalid station identifier", nfe);
        }
    }

}
