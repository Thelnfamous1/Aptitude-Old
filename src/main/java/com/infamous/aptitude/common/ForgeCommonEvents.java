package com.infamous.aptitude.common;

import com.infamous.aptitude.Aptitude;
import com.infamous.aptitude.common.behavior.BrainManager;
import com.infamous.aptitude.common.behavior.util.BrainHelper;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Pig;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Aptitude.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ForgeCommonEvents {

    @SubscribeEvent
    public static void onReloadListener(AddReloadListenerEvent event){
        Aptitude.brainManager = new BrainManager();
        event.addListener(Aptitude.brainManager);
    }

    @SubscribeEvent
    public static void onEntityJoinWorld(EntityJoinWorldEvent event){
        if(event.getWorld().isClientSide){
            return;
        }

        if(event.getEntity() instanceof Mob mob && mob.getType() == EntityType.PIG){
            event.getWorld().getServer().addTickable(new TickTask(1, () -> {
                mob.goalSelector.removeAllGoals();
                mob.targetSelector.removeAllGoals();
                BrainHelper.remakeBrain(mob, (ServerLevel) event.getWorld());
            }));
        }
    }
}
