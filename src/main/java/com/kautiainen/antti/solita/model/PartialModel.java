package com.kautiainen.antti.solita.model;

/**
 * Partial model interface implies the model
 * may not contain all required information.
 */
public interface PartialModel {

    /**
     * The model is with partial or missing key.
     * @return True, if and only if the model key is not
     *  complete. 
     */
    boolean isNew();

    /**
     * Is the model valid representation. 
     * @return True, if and only if the model is not 
     *  incomplete. 
     */
    boolean isValid();

    /**
     * Is the model incomplete.
     * @return True, if and only if the model is incomplete.
     */
    default boolean isIncomplete() {
        return !this.isValid();
    }

    /**
     * Is the model complete. 
     * @return True, if and only if the model has sufficient
     *  fields for either creation a new model reprsentation or act
     *  as replacement of an existing field. 
     */
    default boolean isComplete() {
        return this.isValid();
    }

    /**
     * DOes the model represent an enxisting object. 
     * @return True, if and only if the model has a complete
     *  key indicating it refers to an existing model. 
     */
    default boolean isExisting() {
        return !this.isNew();
    }
}