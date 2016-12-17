package net.tommay.util;

public interface AOTState<T> {
    T get ();
    void put (T val);
}
