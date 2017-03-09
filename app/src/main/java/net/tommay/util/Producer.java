package net.tommay.util;

// XXX get() should probably be timed out by a higher level instead of
// timing itself out.

import java.util.concurrent.TimeoutException;

public interface Producer<T> {
    /**
     * Returns a T out of nowhere.
     */
    T get() throws InterruptedException, TimeoutException;
}
