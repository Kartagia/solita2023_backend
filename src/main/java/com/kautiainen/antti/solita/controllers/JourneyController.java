package com.kautiainen.antti.solita.controllers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.restexpress.Request;
import org.restexpress.Response;

import com.kautiainen.antti.solita.Constants;
import com.kautiainen.antti.solita.exceptions.InvalidFieldsException;
import com.kautiainen.antti.solita.model.Journey;

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
