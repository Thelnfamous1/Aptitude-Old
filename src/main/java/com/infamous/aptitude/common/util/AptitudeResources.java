package com.infamous.aptitude.common.util;

import com.infamous.aptitude.Aptitude;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.Tags;

public class AptitudeResources {
    public static final Tags.IOptionalNamedTag<Item> DOLPHINS_EAT = ItemTags.createOptional(new ResourceLocation(Aptitude.MOD_ID, "dolphins_eat"));
    public static final Tags.IOptionalNamedTag<Item> HORSES_EAT = ItemTags.createOptional(new ResourceLocation(Aptitude.MOD_ID, "horses_eat"));
    public static final Tags.IOptionalNamedTag<Item> LLAMAS_EAT = ItemTags.createOptional(new ResourceLocation(Aptitude.MOD_ID, "llamas_eat"));

    public static final Tags.IOptionalNamedTag<Item> APPLE_EQUIVALENTS = ItemTags.createOptional(new ResourceLocation(Aptitude.MOD_ID, "apple_equivalents"));
    public static final Tags.IOptionalNamedTag<Item> GOLDEN_APPLE_EQUIVALENTS = ItemTags.createOptional(new ResourceLocation(Aptitude.MOD_ID, "golden_apple_equivalents"));
    public static final Tags.IOptionalNamedTag<Item> GOLDEN_CARROT_EQUIVALENTS = ItemTags.createOptional(new ResourceLocation(Aptitude.MOD_ID, "golden_carrot_equivalents"));
    public static final Tags.IOptionalNamedTag<Item> HAY_EQUIVALENTS = ItemTags.createOptional(new ResourceLocation(Aptitude.MOD_ID, "hay_equivalents"));
    public static final Tags.IOptionalNamedTag<Item> SUGAR_EQUIVALENTS = ItemTags.createOptional(new ResourceLocation(Aptitude.MOD_ID, "sugar_equivalents"));
    public static final Tags.IOptionalNamedTag<Item> WHEAT_EQUIVALENTS = ItemTags.createOptional(new ResourceLocation(Aptitude.MOD_ID, "wheat_equivalents"));

    public static final Tags.IOptionalNamedTag<EntityType<?>> DOLPHINS_HUNT = EntityTypeTags.createOptional(new ResourceLocation(Aptitude.MOD_ID, "dolphins_hunt"));
}
