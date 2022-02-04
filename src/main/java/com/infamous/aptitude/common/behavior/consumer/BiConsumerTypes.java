package com.infamous.aptitude.common.behavior.consumer;

import com.google.gson.JsonObject;
import com.infamous.aptitude.Aptitude;
import com.infamous.aptitude.common.behavior.util.RangedAttackHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.CrossbowAttackMob;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class BiConsumerTypes {

    private static final DeferredRegister<BiConsumerType<?>> BICONSUMER_TYPES = DeferredRegister.create((Class<BiConsumerType<?>>)(Class)BiConsumerType.class, Aptitude.MOD_ID);

    public static Supplier<IForgeRegistry<BiConsumerType<?>>> BICONSUMER_TYPE_REGISTRY = BICONSUMER_TYPES.makeRegistry("biconsumer_types", () ->
            new RegistryBuilder<BiConsumerType<?>>().setMaxID(Integer.MAX_VALUE - 1).onAdd((owner, stage, id, obj, old) ->
                    Aptitude.LOGGER.info("BiConsumerType Added: " + obj.getRegistryName().toString() + " ")
            ).setDefaultKey(new ResourceLocation(Aptitude.MOD_ID, "nothing"))
    );

    public static final RegistryObject<BiConsumerType<BiConsumer<?, ?>>> NOTHING = register("nothing",
            jsonObject -> {
                return (o, o1) -> {};
            });


    public static final RegistryObject<BiConsumerType<BiConsumer<LivingEntity, Boolean>>> ENTITY_SET_CHARGING_CROSSBOW = register("entity_set_charging_crossbow",
            jsonObject -> {
                return (le, b) -> {
                    if(le instanceof CrossbowAttackMob crossbowAttackMob){
                        crossbowAttackMob.setChargingCrossbow(b);
                    }
                };
            });


    public static final RegistryObject<BiConsumerType<BiConsumer<LivingEntity, LivingEntity>>> ENTITY_PERFORM_CROSSBOW_ATTACK = register("entity_perform_crossbow_attack",
            jsonObject -> {
                float shootingPower = GsonHelper.getAsFloat(jsonObject, "shooting_power", 1.6F);
                int baseInaccuracy = GsonHelper.getAsInt(jsonObject, "base_inaccuracy", 14);
                int difficultyScale = GsonHelper.getAsInt(jsonObject, "difficulty_scale", 4);
                return (shooter, target) -> {
                    InteractionHand weaponHoldingHand = ProjectileUtil.getWeaponHoldingHand(shooter, item -> item instanceof CrossbowItem);
                    ItemStack itemInHand = shooter.getItemInHand(weaponHoldingHand);
                    if (shooter.isHolding(is -> is.getItem() instanceof CrossbowItem)) {
                        RangedAttackHelper.peformCrossbowShooting(shooter.level, shooter, target, weaponHoldingHand, itemInHand, shootingPower, (float)(baseInaccuracy - shooter.level.getDifficulty().getId() * difficultyScale));
                    }

                    shooter.setNoActionTime(0); // onCrossbowAttackPerformed
                };
            });


    private static <U extends BiConsumer<?, ?>> RegistryObject<BiConsumerType<U>> register(String name, Function<JsonObject, U> jsonFactory) {
        return BICONSUMER_TYPES.register(name, () -> new BiConsumerType<>(jsonFactory));
    }

    public static void register(IEventBus bus){
        BICONSUMER_TYPES.register(bus);
    }

    public static BiConsumerType<?> getBiConsumerType(ResourceLocation bctLocation) {
        BiConsumerType<?> value = BICONSUMER_TYPE_REGISTRY.get().getValue(bctLocation);
        Aptitude.LOGGER.info("Attempting to get biconsumer type {}, got {}", bctLocation, value.getRegistryName());
        return value;
    }
}
