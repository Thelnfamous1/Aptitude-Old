package com.infamous.aptitude.common.behavior.predicates;

import java.util.function.BiPredicate;
import java.util.function.Predicate;

public class BiPredicateGivenSecond<T, U> implements Predicate<T> {
    private final U second;
    private final BiPredicate<T, U> biPredicate;

    public BiPredicateGivenSecond(U second, BiPredicate<T, U> biPredicate){
     this.second = second;
     this.biPredicate = biPredicate;
    }

    @Override
    public boolean test(T t) {
        return this.biPredicate.test(t, this.second);
    }
}
