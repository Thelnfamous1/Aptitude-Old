package com.infamous.aptitude.common.codec.state;

import com.infamous.aptitude.common.codec.Dispatcher;
import com.mojang.serialization.Codec;

// we need a unique class object to make the forge registry, so we extend Dispatcher here
public class StatePredicateSerializer<P extends StatePredicate> extends Dispatcher<StatePredicateSerializer<?>, P> {
    public StatePredicateSerializer(Codec<P> subCodec) {
        super(subCodec);
    }
}
