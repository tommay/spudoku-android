package net.tommay.util;

public interface Callback<T> {
    /**
     * Performs this operation on the given argument.
     *
     * @param t the input argument
     */
    void call(T t);
}
