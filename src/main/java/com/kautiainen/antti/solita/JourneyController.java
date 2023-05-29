package com.kautiainen.antti.solita;

import java.io.Serializable;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

import org.restexpress.Request;
import org.restexpress.Response;

import com.kautiainen.antti.solita.InvalidFieldsException.FieldError;

class Station {

    private Integer id;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    private String name;
    private String lang;

    public Station(Integer id, String name, Locale locale) {
        setId(id);
        setName(name);
        setLang(locale == null ? null : locale.getLanguage());
    }

    public Station(Integer id, String name) {
        this(id, name, Locale.forLanguageTag("fi"));
    }
}

class InvalidFieldsException extends IllegalArgumentException {

    public static class FieldError implements Serializable {
        public final String fieldName;

        public final String description;

        public FieldError(String fieldName, String description) throws IllegalArgumentException {
            if (validFieldName(fieldName)) {
                this.fieldName = fieldName;
            } else {
                throw new IllegalArgumentException("Invalid field name");
            }
            if (validDescription(description)) {
                this.description = description;
            } else {
                throw new IllegalArgumentException("Invalid description");
            }
        }

        public FieldError(Map.Entry<String, String> entry) throws IllegalArgumentException {
            this(entry == null ? null : entry.getKey(), entry == null ? null : entry.getValue());
        }

        public boolean validDescription(String description) {
            return true;
        }

        public boolean validFieldName(String fieldName) {
            return true;
        }

        public Map.Entry<String, String> toMapEntry() {
            return new AbstractMap.SimpleEntry<String, String>(this.fieldName, this.description);
        }
    }

    private List<FieldError> errors = new ArrayList<FieldError>();

    public InvalidFieldsException(String message, Throwable cause, FieldError... errors) {
        super(message, cause);
        if (errors != null)
            this.errors.addAll(Arrays.asList(errors));
    }

    public List<String> getErrors(String fieldName) {
        return errors.stream().filter((FieldError error) -> (error.fieldName == fieldName)).map(
                (FieldError entry) -> (entry.description)).toList();
    }

    public boolean hasErrors(String fieldName) {
        return errors.stream().anyMatch((FieldError error) -> (error.fieldName == fieldName));
    }

    public List<String> getInvalidFields() {
        return errors.stream().collect(
                () -> (new ArrayList<String>()),
                (List<String> result, FieldError error) -> {
                    String sought = (error == null ? null : error.fieldName);
                    if (!result.contains(sought)) {
                        result.add(sought);
                    }
                },
                (List<String> first, List<String> second) -> {
                    first.addAll(second);
                });
    }
}

class Journey {
    public static enum Fields {
        ID, RETURN_STATION, DEPARTURE_STATION, DISTANCE, DURATION;
    }

    private Map<Fields, Object> data = new TreeMap<>();

    public Journey(Integer id, Station departureStation, Station returStation, Integer distance, Integer duration)
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

public class JourneyController {

    private List<Journey> journeys = Collections.synchronizedList(new ArrayList<Journey>());

    public Journey create(Request request, Response response) {
        try {
            Journey journey = request.getBodyAs(Journey.class, "Journey details not provided");
            if (!journey.isValid()) {
                // Invalid journey.
            }
            if (!journey.isNewJourney()) {
                // Checking if there is a journey with given identifier.
                final Integer id = journey.getId().orElse(null);
                if (journeys.stream().anyMatch((Journey oldJourney) -> {
                    return (oldJourney.getId().orElse(null)) == id;
                })) {
                    // The id is reserved.
                    throw new InvalidFieldsException("Identifier not unique",
                            null,
                            new InvalidFieldsException.FieldError(Journey.Fields.ID.toString(), 
                            "Reserved identifier"));
                }
            }
            response.setResponseCreated();
            return journey;
        } catch (Exception ife) {
            response.setException(ife);
            return null;
        } 
    }

    public Journey read(Request request, Response response) {
        String id = request.getHeader(Constants.Url.JOURNEY_ID, "No Journey ID supplied");
        try {
            int idValue = Integer.parseInt(id);
            return journeys.get(idValue);
        } catch(NumberFormatException nfe) {
            response.setException(nfe);
        }
        return null;
    }

    public List<Journey> readAll(Request request, Response response) {
        // TODO: Your 'GET collection' logic here...
        return Collections.emptyList();
    }

    public void update(Request request, Response response) {
        // TODO: Your 'PUT' logic here...
        response.setResponseNoContent();
    }

    public void delete(Request request, Response response) {
        // TODO: Your 'DELETE' logic here...
        response.setResponseNoContent();
    }

}
