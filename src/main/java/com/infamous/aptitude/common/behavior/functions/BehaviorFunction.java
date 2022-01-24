package com.infamous.aptitude.common.behavior.functions;

import com.infamous.aptitude.common.codec.Dispatchable;

import java.util.function.Function;
import java.util.function.Supplier;

public abstract class BehaviorFunction<T, R> extends Dispatchable<BehaviorFunctionSerializer<?>> implements Function<T, R> {

    public BehaviorFunction(Supplier<? extends BehaviorFunctionSerializer<?>> dispatcherGetter) {
        super(dispatcherGetter);
    }

    @Override
    public abstract R apply(T t);
}
