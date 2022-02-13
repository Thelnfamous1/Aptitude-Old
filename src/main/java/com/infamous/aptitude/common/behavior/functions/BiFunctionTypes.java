package com.infamous.aptitude.common.behavior.functions;

import com.google.gson.JsonObject;
import com.infamous.aptitude.Aptitude;
import com.infamous.aptitude.common.behavior.util.MeleeAttackHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;
import net.minecraftforge.registries.RegistryObject;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public class BiFunctionTypes {

    private static final DeferredRegister<BiFunctionType<?>> BIFUNCTION_TYPES = DeferredRegister.create((Class<BiFunctionType<?>>)(Class)BiFunctionType.class, Aptitude.MOD_ID);

    public static Supplier<IForgeRegistry<BiFunctionType<?>>> BIFUNCTION_TYPE_REGISTRY = BIFUNCTION_TYPES.makeRegistry("bifunction_types", () ->
            new RegistryBuilder<BiFunctionType<?>>().setMaxID(Integer.MAX_VALUE - 1).onAdd((owner, stage, id, obj, old) ->
                    Aptitude.LOGGER.info("BiFunctionType Added: " + obj.getRegistryName().toString() + " ")
            )
    );

    public static final RegistryObject<BiFunctionType<BiFunction<LivingEntity, LivingEntity, Double>>> ENTITY_MELEE_ATTACK_REACH_SQUARED = register("entity_melee_attack_reach_squared",
            jsonObject -> {
                return (attacker, target) -> {
                    float attackerWidthRed = GsonHelper.getAsFloat(jsonObject, "attacker_width_reduction", 0);
                    float adjAttackerWidthScale = GsonHelper.getAsFloat(jsonObject, "adjusted_attacker_width_scale", 2);
                    float adjAttackerWidthSqrAdd = GsonHelper.getAsFloat(jsonObject, "adjusted_attacker_width_squared_addition", 0);
                    float targetWidthScale = GsonHelper.getAsFloat(jsonObject, "target_width_scale", 1);
                    return MeleeAttackHelper.calculateMeleeAttackRangeSqr(attacker, target, attackerWidthRed, adjAttackerWidthScale, adjAttackerWidthSqrAdd, targetWidthScale);
                };
            });

    public static final RegistryObject<BiFunctionType<BiFunction<?, ?, ?>>> CUSTOM_BIFUNCTION = register("custom_bifunction",
            jsonObject -> {
                String locationString = GsonHelper.getAsString(jsonObject, "location");
                ResourceLocation location = new ResourceLocation(locationString);
                return Aptitude.customLogicManager.getBiFunction(location);
            });

    private static <U extends BiFunction<?, ?, ?>> RegistryObject<BiFunctionType<U>> register(String name, Function<JsonObject, U> jsonFactory) {
        return BIFUNCTION_TYPES.register(name, () -> new BiFunctionType<>(jsonFactory));
    }

    public static void register(IEventBus bus){
        BIFUNCTION_TYPES.register(bus);
    }

    public static BiFunctionType<?> getBiFunctionType(ResourceLocation bftLocation) {
        BiFunctionType<?> value = BIFUNCTION_TYPE_REGISTRY.get().getValue(bftLocation);
        if(value == null) Aptitude.LOGGER.error("Failed to get BiFunctionType {}", bftLocation);
        return value;
    }
}
