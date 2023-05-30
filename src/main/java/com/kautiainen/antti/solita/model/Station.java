package com.kautiainen.antti.solita.model;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;

import org.apache.commons.collections.map.MultiValueMap;
import org.apache.commons.collections4.MultiValuedMap;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvNumber;
import com.opencsv.bean.CsvBindAndJoinByName;

/**
 * Model representing a station of a Solita Journeys.
 * The model allows values to be undefined allowing structure
 * use for partial station information as well as complete station information.
 */
public class Station implements PartialModel {

    /**
     * Fields of the station.
     */
    public static enum Fields {
        IDENTIFIER, NAME, LANGUAGE;
    }

    @CsvBindByName(column="ID", required=true)
    private Integer id;

    /**
     * Mapping from languages to station names. 
     */
    @CsvBindAndJoinByName(column="Nimi|Namn|Name", elementType = String.class, required=true)
    private MultiValuedMap<String, String> names;

    @CsvBindAndJoinByName(column="Osoite|Adress|Address", elementType = String.class, required=true)
    private MultiValuedMap<String, String> address;

    @CsvBindAndJoinByName(column="Kaupunki|Stad|City", elementType = String.class, required=true)
    private MultiValuedMap<String, String> city;

    @CsvBindAndJoinByName(column="Operaattori|Operaattor|Operator", elementType = String.class, required=true)
    private MultiValuedMap<String, String> operatorName;

    @CsvBindByName(column="Kapasiteetti|Kapasiteet|Capacity")
    private int capacity = 0; 

    @CsvBindByName(column="X|x", required=true)
    @CsvNumber("#00.00000#")
    private float xCoordinate;

    @CsvBindByName(column="Y|y", required=true)
    @CsvNumber("#00.00000#")
    private float yCoordinate;



    /**
     * Get station identifier.
     */
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName(String lang) {
        return name;
    }

    public void setName(String name, String lang) {
        this.name = name;
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    private String name;

    /**
     * The language of the station name. 
     */
    private String lang;

    public Station(Station another) {
        setFields(another);
    }

    public synchronized void setFields(Station another) {
        setId(another.getId());
        this.names.putAll(another.names);
        this.address.putAll(another.address);
        this.city.putAll(another.city);
        this.operatorName.putAll(another.operatorName);
        this.capacity = (another.capacity);
        this.xCoordinate = (another.xCoordinate);
        this.yCoordinate = (another.yCoordinate);
    }

    public Collection<Entry<String, String>> getNames() {
        return this.names.entries();
    }

    public Station(Integer id, String name, String language) {
        setId(id);
        setName(name, language);
    }

    public Station(Integer id, String name, Locale locale) {
        this(id, name, (locale == null ? null : locale.getLanguage()));
    }

    public Station(Integer id, String name) {
        this(id, name, Locale.forLanguageTag("fi"));
    }

    @Override
    public boolean isNew() {
        return getId() == null;
    }

    @Override
    public boolean isValid() {
        return !this.names.entries().isEmpty() && !this.address.isEmpty() &&
        !this.city.isEmpty() && !this.operatorName.isEmpty();
    }
}