package com.infamous.aptitude.common.codec.function;

import com.infamous.aptitude.common.codec.Dispatcher;
import com.mojang.serialization.Codec;

public class BehaviorFunctionSerializer<F extends BehaviorFunction<?, ?>> extends Dispatcher<BehaviorFunctionSerializer<?>, F> {
    public BehaviorFunctionSerializer(Codec<F> subCodec) {
        super(subCodec);
    }
}
