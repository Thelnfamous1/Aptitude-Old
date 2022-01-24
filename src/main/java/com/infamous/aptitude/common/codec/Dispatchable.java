package com.infamous.aptitude.common.codec;

import java.util.function.Supplier;

/**
 * Base class for the dispatched objects
 * Instances of subclasses of this can be deserialized from jsons, etc
 */
public abstract class Dispatchable<DTYPE> {
    private final Supplier<? extends DTYPE> dispatcherGetter;

    public DTYPE getDispatcher() {
        return this.dispatcherGetter.get();
    }

    public Dispatchable(Supplier<? extends DTYPE> dispatcherGetter) {
        this.dispatcherGetter = dispatcherGetter;
    }
}
