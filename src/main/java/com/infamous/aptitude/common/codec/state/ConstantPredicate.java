package com.infamous.aptitude.common.codec.state;

import com.mojang.serialization.Codec;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Supplier;

/**
 * Predicates that always return either true or false
 **/
public class ConstantPredicate extends StatePredicate {
    public static final Codec<ConstantPredicate> ALWAYS_FALSE_CODEC = Codec.unit(new ConstantPredicate(StatePredicates.ALWAYS_FALSE, false));
    public static final Codec<ConstantPredicate> ALWAYS_TRUE_CODEC = Codec.unit(new ConstantPredicate(StatePredicates.ALWAYS_TRUE, true));
    private final boolean value;

    public ConstantPredicate(Supplier<StatePredicateSerializer<ConstantPredicate>> serializer, boolean value) {
        super(serializer);
        this.value = value;
    }

    @Override
    public boolean test(BlockState state) {
        return this.value;
    }
}
