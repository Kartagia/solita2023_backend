package com.kautiainen.antti.solita.controllers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

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

    private volatile java.util.ArrayList<Station> stations;

    /**
     * The station identifier index map from station identifeir to its index in the
     * stations
     * map.
     */
    private volatile transient java.util.TreeMap<Integer, Integer> idIndex = new java.util.TreeMap<>();

    protected synchronized boolean addStation(Station station) throws InvalidFieldsException {
        LinkedList<BiConsumer<Map<Integer, Integer>, List<Station>>> undos = new LinkedList<>();
        try {
            return this.addStation(station, false, undos);
        } catch (Exception e) {
            while (undos.size() > 0) {
                undos.pop().accept(this.idIndex, this.stations);
            }
            throw e;
        }
    }

    protected synchronized boolean addStation(
            Station station,
            boolean allowDupliateIds,
            LinkedList<BiConsumer<Map<Integer, Integer>, List<Station>>> undos) throws InvalidFieldsException {
        Integer id = station.getId();
        Station added = station;
        if (id == null) {
            // Station does not have identifier, thus generating an ide.
            do {
                id = StationController.this.getNewIdentifier();
            } while (idIndex.containsKey(id));
            final int newId = id;
            idIndex.put(newId, stations.size());
            undos.add((ids, list) -> {
                ids.remove(newId);
            });
            added = new Station(station);
            added.setId(newId);
            final int size = stations.size();
            stations.add(added);
            undos.add((ids, list) -> {
                stations.remove(size);
            });
            return true;
        } else if (idIndex.containsKey(id)) {
            // Invalid value to add.
            if (allowDupliateIds) {
                final int setId = id;
                final Station oldValue = stations.set(idIndex.get(setId), added);
                undos.add((ids, list) -> {
                    stations.set(idIndex.get(setId), oldValue);
                });
                return true;
            } else {
                throw new InvalidFieldsException(Station.Fields.IDENTIFIER.toString(), (Throwable) null,
                        new InvalidFieldsException.FieldError(Station.Fields.IDENTIFIER.toString(),
                                "Invalid identifier"));
            }
        } else {
            final int index = stations.size();
            final int newId = id;
            idIndex.put(id, index);
            undos.add((ids, list) -> {
                idIndex.remove(newId);
            });
            stations.add(added);
            undos.add((ids, list) -> {
                list.remove(index);
            });
            return true;
        }
    }

    protected synchronized boolean addStations(
            List<Station> stations,
            boolean allowsDuplicateIds,
            LinkedList<BiConsumer<Map<Integer, Integer>, List<Station>>> undos) {
        int undoCount = (undos != null ? undos.size() : 0);
        if (undos == null) {
            return addStations(stations, allowsDuplicateIds, new LinkedList<>());
        } else {
            try {
                // Starting operation.
                final List<Boolean> result = Arrays.asList(false);
                if (stations != null) {
                    stations.stream().forEachOrdered(
                            (station) -> {
                                result.set(0, addStation(station, false, undos));
                            });
                }
                return result.get(0);
            } catch (Exception e) {
                // Rollback.
                while (undos.size() > undoCount) {
                    undos.pop().accept(this.idIndex, this.stations);
                }
                throw e;
            }
        }
    }

    /**
     * 
     * 
     * @param stations The initial station list.
     * 
     */
    public StationController(java.util.List<Station> stations) {
        addStations(stations, false,
                (LinkedList<BiConsumer<Map<Integer, Integer>, List<Station>>>) null);
    }

    public StationController() {
        this(java.util.Collections.synchronizedList(new ArrayList<Station>()));
    }

    /**
     * Get new identifier. 
     * 
     * @return The new identifier for the stations.
     */
    protected synchronized Integer getNewIdentifier() {
        Optional<Integer> max = idIndex.keySet().stream().max(Comparator.naturalOrder());
        Integer result = max.orElse(0) +1;
        return result;
    }

    /**
     * Serves creation of a new station.
     * 
     * @param request  The request.
     * @param response The response.
     */
    public synchronized Station create(Request request, Response response) {
        Station station = request.getBodyAs(Station.class, "Station details not provided");
        if (!station.isValid()) {
            // Invalid station.
            response.setResponseStatus(HttpResponseStatus.BAD_REQUEST);
        } else if (idIndex.containsKey(station.getId())) {
            // Duplicate identifier.
            throw new BadRequestException("Identifier already reserved", null);
        }
        if (addStation(station)) {
            response.setResponseStatus(HttpResponseStatus.CREATED);
            return this.stations.get(stations.size() - 1);
        } else {
            response.setResponseStatus(HttpResponseStatus.NOT_MODIFIED);
            return station;
        }
    }

    public synchronized List<Station> readAll(Request request, Response response) {
        ArrayList<Station> result = new ArrayList<>();
        result.addAll(new ArrayList<Station>(this.stations));
        return result;
    }

    /**
     * Serves update of a station requests.
     * 
     * Partial updates does not allow incomplete keys, and requires
     * an existing ta get.
     * 
     * Complete updates performs creation of a new station if necessary.
     * 
     * @param request  The request.
     * @param response The response.
     */
    public synchronized void update(Request request, Response response) {
        Station station = request.getBodyAs(Station.class, "Station details not provided");
        if (station.isIncomplete()) {
            // Partial update.
            // - Partial update does not allow incomplete keys.
            // - Target
            if (station.isNew()) {
                // Incomplete value.
                response.setResponseStatus(HttpResponseStatus.BAD_REQUEST);
                return;
            } else if (idIndex.containsKey(station.getId())) {
                Station target = stations.get(idIndex.get(station.getId()));

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
            if (addStation(station)) {
                response.setResponseStatus(HttpResponseStatus.CREATED);
            } else {
                response.setResponseStatus(HttpResponseStatus.NOT_MODIFIED);
            }
        } else {
            // Checking if there is a journey with given identifier.
            final Integer id = station.getId();
            if (idIndex.containsKey(id)) {
                // TODO: Logger for replacing station.
                stations.set(idIndex.get(id), station);
            } else {
                // TODO: Logger for adding new station.
                if (addStation(station)) {
                    response.setResponseStatus(HttpResponseStatus.CREATED);
                } else {
                    response.setResponseStatus(HttpResponseStatus.NOT_MODIFIED);
                }
            }

        }
    }

    /**
     * Serves removal of a station.
     * 
     * @param request  The request.
     * @param response The response.
     */
    public synchronized void delete(Request request, Response response) {
        try {
            Integer id = Integer.parseInt(request.getHeader(Constants.Url.STATION_ID, "No Station ID supplied"));
            if (idIndex.containsKey(id)) {
                stations.remove(idIndex.get(id));
                idIndex.remove(id);
                response.setResponseNoContent();
            } else {
                response.setResponseStatus(HttpResponseStatus.NOT_FOUND);
            }
        } catch (NumberFormatException nfe) {
            throw new BadRequestException("Invalid station identifier", nfe);
        }
    }

}
