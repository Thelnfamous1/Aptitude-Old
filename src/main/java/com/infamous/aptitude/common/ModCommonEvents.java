package com.infamous.aptitude.common;

import com.infamous.aptitude.Aptitude;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Aptitude.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModCommonEvents {

    @SubscribeEvent
    public static void onEntityAttributeModification(EntityAttributeModificationEvent event){
        // Piglin-like
        event.add(EntityType.ZOMBIE, Attributes.MAX_HEALTH, 16.0D);
        event.add(EntityType.ZOMBIE, Attributes.MOVEMENT_SPEED, (double)0.35F);
        event.add(EntityType.ZOMBIE, Attributes.ATTACK_DAMAGE, 5.0D);

        // Piglin Brute-like
        event.add(EntityType.HUSK, Attributes.MAX_HEALTH, 50.0D);
        event.add(EntityType.HUSK, Attributes.MOVEMENT_SPEED, (double)0.35F);
        event.add(EntityType.HUSK, Attributes.ATTACK_DAMAGE, 7.0D);

        // Hoglin-like
        event.add(EntityType.PIG, Attributes.MAX_HEALTH, 40.0D);
        event.add(EntityType.PIG, Attributes.MOVEMENT_SPEED, (double)0.3F);
        event.add(EntityType.PIG, Attributes.KNOCKBACK_RESISTANCE, (double)0.6F);
        event.add(EntityType.PIG, Attributes.ATTACK_KNOCKBACK, 1.0D);
        event.add(EntityType.PIG, Attributes.ATTACK_DAMAGE, 6.0D);

        // Zoglin-like
        event.add(EntityType.COW, Attributes.MAX_HEALTH, 40.0D);
        event.add(EntityType.COW, Attributes.MOVEMENT_SPEED, (double)0.3F);
        event.add(EntityType.COW, Attributes.KNOCKBACK_RESISTANCE, (double)0.6F);
        event.add(EntityType.COW, Attributes.ATTACK_KNOCKBACK, 1.0D);
        event.add(EntityType.COW, Attributes.ATTACK_DAMAGE, 6.0D);
    }
}
