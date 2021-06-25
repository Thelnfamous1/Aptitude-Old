package com.infamous.aptitude.common.util;

import com.infamous.aptitude.Aptitude;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.ITag;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.Tags;

public class AptitudeResources {
    public static final Tags.IOptionalNamedTag<Item> DOLPHINS_EAT = ItemTags.createOptional(new ResourceLocation(Aptitude.MOD_ID, "dolphins_eat"));
    public static final Tags.IOptionalNamedTag<Item> HORSES_EAT = ItemTags.createOptional(new ResourceLocation(Aptitude.MOD_ID, "horses_eat"));
    public static final Tags.IOptionalNamedTag<Item> LLAMAS_EAT = ItemTags.createOptional(new ResourceLocation(Aptitude.MOD_ID, "llamas_eat"));
    public static final Tags.IOptionalNamedTag<Item> POLAR_BEARS_EAT = ItemTags.createOptional(new ResourceLocation(Aptitude.MOD_ID, "polar_bears_eat"));
    public static final Tags.IOptionalNamedTag<Item> OCELOTS_EAT = ItemTags.createOptional(new ResourceLocation(Aptitude.MOD_ID, "ocelots_eat"));
    public static final Tags.IOptionalNamedTag<Item> CATS_EAT = ItemTags.createOptional(new ResourceLocation(Aptitude.MOD_ID, "cats_eat"));
    public static final Tags.IOptionalNamedTag<Item> PARROTS_EAT = ItemTags.createOptional(new ResourceLocation(Aptitude.MOD_ID, "parrots_eat"));
    public static final Tags.IOptionalNamedTag<Item> COWS_EAT = ItemTags.createOptional(new ResourceLocation(Aptitude.MOD_ID, "cows_eat"));
    public static final Tags.IOptionalNamedTag<Item> PIGS_EAT = ItemTags.createOptional(new ResourceLocation(Aptitude.MOD_ID, "pigs_eat"));
    public static final Tags.IOptionalNamedTag<Item> CHICKENS_EAT = ItemTags.createOptional(new ResourceLocation(Aptitude.MOD_ID, "chickens_eat"));
    public static final Tags.IOptionalNamedTag<Item> RABBITS_EAT = ItemTags.createOptional(new ResourceLocation(Aptitude.MOD_ID, "rabbits_eat"));
    public static final Tags.IOptionalNamedTag<Item> SHEEP_EAT = ItemTags.createOptional(new ResourceLocation(Aptitude.MOD_ID, "sheep_eat"));
    public static final Tags.IOptionalNamedTag<Item> FOXES_EAT = ItemTags.createOptional(new ResourceLocation(Aptitude.MOD_ID, "foxes_eat"));
    public static final Tags.IOptionalNamedTag<Item> PANDAS_EAT = ItemTags.createOptional(new ResourceLocation(Aptitude.MOD_ID, "pandas_eat"));
    public static final Tags.IOptionalNamedTag<Item> TURTLES_EAT = ItemTags.createOptional(new ResourceLocation(Aptitude.MOD_ID, "turtles_eat"));
    public static final Tags.IOptionalNamedTag<Item> STRIDERS_EAT = ItemTags.createOptional(new ResourceLocation(Aptitude.MOD_ID, "striders_eat"));

    public static final Tags.IOptionalNamedTag<Item> PARROTS_CANNOT_EAT = ItemTags.createOptional(new ResourceLocation(Aptitude.MOD_ID, "parrots_cannot_eat"));

    public static final Tags.IOptionalNamedTag<Item> HORSES_BREED_WITH = ItemTags.createOptional(new ResourceLocation(Aptitude.MOD_ID, "horses_breed_with"));
    public static final Tags.IOptionalNamedTag<Item> LLAMAS_BREED_WITH = ItemTags.createOptional(new ResourceLocation(Aptitude.MOD_ID, "llamas_breed_with"));

    public static final Tags.IOptionalNamedTag<Item> APPLE_EQUIVALENTS = ItemTags.createOptional(new ResourceLocation(Aptitude.MOD_ID, "apple_equivalents"));
    public static final Tags.IOptionalNamedTag<Item> GOLDEN_APPLE_EQUIVALENTS = ItemTags.createOptional(new ResourceLocation(Aptitude.MOD_ID, "golden_apple_equivalents"));
    public static final Tags.IOptionalNamedTag<Item> GOLDEN_CARROT_EQUIVALENTS = ItemTags.createOptional(new ResourceLocation(Aptitude.MOD_ID, "golden_carrot_equivalents"));
    public static final Tags.IOptionalNamedTag<Item> HAY_EQUIVALENTS = ItemTags.createOptional(new ResourceLocation(Aptitude.MOD_ID, "hay_equivalents"));
    public static final Tags.IOptionalNamedTag<Item> SUGAR_EQUIVALENTS = ItemTags.createOptional(new ResourceLocation(Aptitude.MOD_ID, "sugar_equivalents"));
    public static final Tags.IOptionalNamedTag<Item> WHEAT_EQUIVALENTS = ItemTags.createOptional(new ResourceLocation(Aptitude.MOD_ID, "wheat_equivalents"));
    public static final Tags.IOptionalNamedTag<Item> CAKES = ItemTags.createOptional(new ResourceLocation(Aptitude.MOD_ID, "cakes"));

    public static final Tags.IOptionalNamedTag<EntityType<?>> DOLPHINS_HUNT = EntityTypeTags.createOptional(new ResourceLocation(Aptitude.MOD_ID, "dolphins_hunt"));
    public static final Tags.IOptionalNamedTag<EntityType<?>> POLAR_BEARS_HUNT = EntityTypeTags.createOptional(new ResourceLocation(Aptitude.MOD_ID, "polar_bears_hunt"));
    public static final Tags.IOptionalNamedTag<EntityType<?>> OCELOTS_HUNT = EntityTypeTags.createOptional(new ResourceLocation(Aptitude.MOD_ID, "ocelots_hunt"));
    public static final Tags.IOptionalNamedTag<EntityType<?>> CATS_HUNT = EntityTypeTags.createOptional(new ResourceLocation(Aptitude.MOD_ID, "cats_hunt"));
    public static final Tags.IOptionalNamedTag<EntityType<?>> FOXES_HUNT_ON_LAND = EntityTypeTags.createOptional(new ResourceLocation(Aptitude.MOD_ID, "foxes_hunt_on_land"));
    public static final Tags.IOptionalNamedTag<EntityType<?>> FOXES_HUNT_IN_WATER = EntityTypeTags.createOptional(new ResourceLocation(Aptitude.MOD_ID, "foxes_hunt_in_water"));
    public static final Tags.IOptionalNamedTag<EntityType<?>> WOLVES_HUNT = EntityTypeTags.createOptional(new ResourceLocation(Aptitude.MOD_ID, "wolves_hunt"));

    public static final Tags.IOptionalNamedTag<EntityType<?>> CATS_REPEL = EntityTypeTags.createOptional(new ResourceLocation(Aptitude.MOD_ID, "cats_repel"));
    public static final Tags.IOptionalNamedTag<EntityType<?>> OCELOTS_REPEL = EntityTypeTags.createOptional(new ResourceLocation(Aptitude.MOD_ID, "ocelots_repel"));
    public static final Tags.IOptionalNamedTag<EntityType<?>> LLAMAS_REPEL = EntityTypeTags.createOptional(new ResourceLocation(Aptitude.MOD_ID, "llamas_repel"));
    public static final Tags.IOptionalNamedTag<EntityType<?>> DONKEYS_REPEL = EntityTypeTags.createOptional(new ResourceLocation(Aptitude.MOD_ID, "donkeys_repel"));
    public static final Tags.IOptionalNamedTag<EntityType<?>> MULES_REPEL = EntityTypeTags.createOptional(new ResourceLocation(Aptitude.MOD_ID, "mules_repel"));
    public static final Tags.IOptionalNamedTag<EntityType<?>> WOLVES_REPEL = EntityTypeTags.createOptional(new ResourceLocation(Aptitude.MOD_ID, "wolves_repel"));

    public static final Tags.IOptionalNamedTag<EntityType<?>> FOXES_AVOID = EntityTypeTags.createOptional(new ResourceLocation(Aptitude.MOD_ID, "foxes_avoid"));
    public static final Tags.IOptionalNamedTag<EntityType<?>> CREEPERS_AVOID = EntityTypeTags.createOptional(new ResourceLocation(Aptitude.MOD_ID, "creepers_avoid"));
    public static final Tags.IOptionalNamedTag<EntityType<?>> SKELETONS_AVOID = EntityTypeTags.createOptional(new ResourceLocation(Aptitude.MOD_ID, "skeletons_avoid"));
    public static final Tags.IOptionalNamedTag<EntityType<?>> WOLVES_AVOID = EntityTypeTags.createOptional(new ResourceLocation(Aptitude.MOD_ID, "wolves_avoid"));
    public static final Tags.IOptionalNamedTag<EntityType<?>> RABBITS_AVOID = EntityTypeTags.createOptional(new ResourceLocation(Aptitude.MOD_ID, "rabbits_avoid"));

}
