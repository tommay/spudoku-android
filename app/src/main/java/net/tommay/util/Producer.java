package net.tommay.util;

public interface Producer<T> {
    /**
     * Returns a T out of nowhere.
     */
    T get();
}
