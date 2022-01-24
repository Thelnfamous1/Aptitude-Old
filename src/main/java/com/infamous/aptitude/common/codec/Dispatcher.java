package com.infamous.aptitude.common.codec;

import com.mojang.serialization.Codec;
import net.minecraftforge.registries.ForgeRegistryEntry;
import net.minecraftforge.registries.IForgeRegistryEntry;

/**
 * Class for the dispatchers/serializers
 * Extend this to make your class useable with makeDispatchRegistry
 */
public abstract class Dispatcher<DTYPE extends IForgeRegistryEntry<DTYPE>, P> extends ForgeRegistryEntry<DTYPE> {
    private final Codec<P> subCodec;

    public Codec<P> getSubCodec() {
        return this.subCodec;
    }

    public Dispatcher(Codec<P> subCodec) {
        this.subCodec = subCodec;
    }
}
