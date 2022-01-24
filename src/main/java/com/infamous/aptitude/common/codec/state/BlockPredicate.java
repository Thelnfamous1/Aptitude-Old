package com.infamous.aptitude.common.codec.state;

import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Predicate that returns true if a state belongs to a given block instance
 **/
public class BlockPredicate extends StatePredicate {
    public static final Codec<BlockPredicate> CODEC = Registry.BLOCK.byNameCodec().xmap(BlockPredicate::new, BlockPredicate::getBlock).fieldOf("block").codec();

    private final Block block;

    public Block getBlock() {
        return this.block;
    }

    public BlockPredicate(Block block) {
        super(StatePredicates.BLOCK_SERIALIZER);
        this.block = block;
    }

    @Override
    public boolean test(BlockState state) {
        return state.is(this.block);
    }
}
