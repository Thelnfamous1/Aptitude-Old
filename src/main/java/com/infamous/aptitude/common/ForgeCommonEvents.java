package com.infamous.aptitude.common;

import com.infamous.aptitude.Aptitude;
import com.infamous.aptitude.common.behavior.BrainManager;
import com.infamous.aptitude.common.behavior.util.BrainHelper;
import com.infamous.aptitude.common.util.AptitudeResources;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.util.GoalUtils;
import net.minecraft.world.entity.monster.Zombie;
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

        if(event.getEntity() instanceof Mob mob && hasBrainFile(mob)){
            if(mob instanceof Zombie zombie) {
                zombie.setCanPickUpLoot(true);
                if (GoalUtils.hasGroundPathNavigation(zombie)) {
                    ((GroundPathNavigation)zombie.getNavigation()).setCanOpenDoors(true);
                }
            }
            ServerLevel serverLevel = (ServerLevel) event.getWorld();
            MinecraftServer server = serverLevel.getServer();
            server.tell(new TickTask(server.getTickCount() + 1, () -> BrainHelper.clearAIAndRemakeBrain(mob, serverLevel)));
        }
    }

    @SubscribeEvent
    public static void onCustomServerAiStepEvent(CustomServerAiStepEvent event){
        Mob mob = event.getMob();

        if(hasBrainFile(mob)){
            if(shouldTickBrain(mob)){
                BrainHelper.getBrainCast(mob).tick((ServerLevel) mob.level, mob);
                BrainHelper.updateActivity(mob);
            }
        }
    }

    private static boolean shouldTickBrain(Mob mob) {
        return !mob.getType().is(AptitudeResources.EXCLUDE_BRAIN_TICK);
    }

    private static boolean hasBrainFile(Mob mob) {
        ResourceLocation etLocation = mob.getType().getRegistryName();
        return Aptitude.brainManager.hasBrainFile(etLocation);
    }

}
