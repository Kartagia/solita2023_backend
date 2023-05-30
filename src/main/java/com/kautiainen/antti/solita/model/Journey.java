package com.kautiainen.antti.solita.model;

import java.util.Map;
import java.util.Optional;
import java.util.HashMap;

import com.kautiainen.antti.solita.exceptions.InvalidFieldsException;

public class Journey {
    public static enum Fields {
        ID, RETURN_STATION, DEPARTURE_STATION, DISTANCE, DURATION;
    }

    private Map<Fields, Object> data = new HashMap<>();

    public Journey(Integer id, PartialModel departureStation, PartialModel returStation, Integer distance, Integer duration)
            throws InvalidFieldsException {

    }

    public boolean isNewJourney() {
        return !data.containsKey(Fields.ID);
    }

    public Optional<Integer> getId() {
        return Optional.ofNullable((Integer) data.get(Fields.ID));
    }

    public Optional<Station> getDepartureStation() {
        return Optional.ofNullable((Station) data.get(Fields.DEPARTURE_STATION));
    }

    public Optional<Station> getReturnStation() {
        return Optional.ofNullable((Station) data.get(Fields.DEPARTURE_STATION));
    }

    public Optional<Integer> getDistance() {
        return Optional.ofNullable((Integer) data.get(Fields.DISTANCE));
    }

    public Optional<Integer> getDuration() {
        return Optional.ofNullable((Integer) data.get(Fields.DURATION));
    }

    /**
     * Is the journey valid.
     * 
     * @return True, if and only if the journey is valid.
     */
    public boolean isValid() {
        return this.getDepartureStation().isPresent()
                && this.getDistance().isPresent() && this.getDuration().isPresent()
                && this.getDistance().get() >= 0 && this.getDuration().get() >= 0;
    }

    /**
     * Is the journey complete.
     * 
     * @return True, if and only if the journey is complete.
     */
    public boolean isComplete() {
        return isValid() && this.getReturnStation().isPresent();
    }
}