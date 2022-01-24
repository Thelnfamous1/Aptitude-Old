package com.infamous.aptitude.common.codec.state;

import java.util.function.Predicate;
import java.util.function.Supplier;

import com.infamous.aptitude.common.codec.Dispatchable;

import net.minecraft.world.level.block.state.BlockState;

// StatePredicate is the base class for the objects we'll deserialize from jsons
public abstract class StatePredicate extends Dispatchable<StatePredicateSerializer<?>> implements Predicate<BlockState>
{
	public StatePredicate(Supplier<? extends StatePredicateSerializer<? extends StatePredicate>> serializerGetter)
	{
		super(serializerGetter);
	}
	
	/**
	 * @param state A blockstate to test
	 * @return true if the state passes the test
	 */
	@Override
	public abstract boolean test(BlockState state);

}