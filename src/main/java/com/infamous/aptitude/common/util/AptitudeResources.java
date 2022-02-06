package com.infamous.aptitude.common.util;

import com.infamous.aptitude.Aptitude;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.Tags;

public class AptitudeResources {
    public static final Tags.IOptionalNamedTag<EntityType<?>> EXCLUDE_BRAIN_TICK = EntityTypeTags.createOptional(new ResourceLocation(Aptitude.MOD_ID, "exclude_brain_tick"));

}
