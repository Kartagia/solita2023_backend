package com.kautiainen.antti.solita.model;

import java.util.Locale;

/**
 * Model representing a station of a Solita Journeys.
 * The model allows values to be undefined allowing structure
 * use for partial station information as well as complete station information.
 */
public class Station implements PartialModel {

    private Integer id;

    /**
     * Get station identifier.
     */
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

    /**
     * The language of the station name. 
     */
    private String lang;

    public Station(Integer id, String name, Locale locale) {
        setId(id);
        setName(name);
        setLang(locale == null ? null : locale.getLanguage());
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
        String name = getName(), lang = getLang();
        return name != null && !name.isBlank() && lang  != null && !lang.isBlank();
    }
}