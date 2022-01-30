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
        event.add(EntityType.PIG, Attributes.MAX_HEALTH, 40.0D);
        event.add(EntityType.PIG, Attributes.MOVEMENT_SPEED, (double)0.3F);
        event.add(EntityType.PIG, Attributes.KNOCKBACK_RESISTANCE, (double)0.6F);
        event.add(EntityType.PIG, Attributes.ATTACK_KNOCKBACK, 1.0D);
        event.add(EntityType.PIG, Attributes.ATTACK_DAMAGE, 6.0D);
    }
}
