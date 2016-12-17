package net.tommay.util;

import net.tommay.util.ProducerException;

public interface Producer<T> {
    /**
     * Returns a T out of nowhere.
     */
    T get() throws ProducerException;
}
