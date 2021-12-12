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
        event.add(EntityType.HORSE, Attributes.ATTACK_DAMAGE, 6.0D);
        event.add(EntityType.DONKEY, Attributes.ATTACK_DAMAGE, 6.0D);
        event.add(EntityType.MULE, Attributes.ATTACK_DAMAGE, 6.0D);
        event.add(EntityType.SKELETON_HORSE, Attributes.ATTACK_DAMAGE, 6.0D);
        event.add(EntityType.ZOMBIE_HORSE, Attributes.ATTACK_DAMAGE, 6.0D);

        event.add(EntityType.LLAMA, Attributes.ATTACK_DAMAGE, 4.0D);
        event.add(EntityType.TRADER_LLAMA, Attributes.ATTACK_DAMAGE, 4.0D);

        event.add(EntityType.HORSE, Attributes.ATTACK_KNOCKBACK, 1.5D);
        event.add(EntityType.DONKEY, Attributes.ATTACK_KNOCKBACK, 1.5D);
        event.add(EntityType.MULE, Attributes.ATTACK_KNOCKBACK, 1.5D);
        event.add(EntityType.SKELETON_HORSE, Attributes.ATTACK_KNOCKBACK, 1.5D);
        event.add(EntityType.ZOMBIE_HORSE, Attributes.ATTACK_KNOCKBACK, 1.5D);

        event.add(EntityType.LLAMA, Attributes.ATTACK_KNOCKBACK, 1.0D);
        event.add(EntityType.TRADER_LLAMA, Attributes.ATTACK_KNOCKBACK, 1.0D);
    }
}
