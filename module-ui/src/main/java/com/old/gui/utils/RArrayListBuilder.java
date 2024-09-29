package com.old.gui.utils;

import java.util.ArrayList;

/**
 * Class to Build ArrayLists
 *
 * @param <T> the type
 */
public class RArrayListBuilder<T> {
    private final ArrayList<T> list = new ArrayList<>();

    /**
     * Return the built list
     *
     * @return the list
     */
    public ArrayList<T> build() {
        return list;
    }

    /**
     * Method to add item to list
     *
     * @param item item to add
     * @return this
     */
    RArrayListBuilder<T> add(T item) {
        list.add(item);
        return this;
    }

    /**
     * Method to add items to list
     *
     * @param items items to add
     * @return this
     */
    @SafeVarargs
    public final RArrayListBuilder<T> add(T... items) {
        for (T t : items) {
            add(t);
        }
        return this;
    }
}
