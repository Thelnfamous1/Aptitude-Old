package com.infamous.aptitude.common.behavior.predicates;

import java.util.function.BiPredicate;
import java.util.function.Predicate;

public class BiPredicateGivenFirst<T, U> implements Predicate<U> {
    private final T first;
    private final BiPredicate<T, U> biPredicate;

    public BiPredicateGivenFirst(T first, BiPredicate<T, U> biPredicate){
     this.first = first;
     this.biPredicate = biPredicate;
    }

    @Override
    public boolean test(U u) {
        return this.biPredicate.test(this.first, u);
    }
}
