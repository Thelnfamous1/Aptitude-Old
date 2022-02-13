package com.infamous.aptitude.common;

import com.infamous.aptitude.Aptitude;
import com.infamous.aptitude.common.behavior.util.SelectorHelper;
import com.infamous.aptitude.common.manager.base.BaseAIHelper;
import com.infamous.aptitude.common.manager.brain.BrainManager;
import com.infamous.aptitude.common.behavior.util.BrainHelper;
import com.infamous.aptitude.common.manager.custom.CustomLogicManager;
import com.infamous.aptitude.common.manager.selector.SelectorManager;
import com.infamous.aptitude.common.manager.base.BaseAIManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.hoglin.Hoglin;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Aptitude.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ForgeCommonEvents {

    @SubscribeEvent
    public static void onReloadListener(AddReloadListenerEvent event){
        Aptitude.brainManager = new BrainManager();
        Aptitude.selectorManager = new SelectorManager();
        Aptitude.baseAIManager = new BaseAIManager();
        Aptitude.customLogicManager = new CustomLogicManager();
        event.addListener(Aptitude.brainManager);
        event.addListener(Aptitude.selectorManager);
        event.addListener(Aptitude.baseAIManager);
        event.addListener(Aptitude.customLogicManager);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onEntityJoinWorld(EntityJoinWorldEvent event){
        if(event.getWorld().isClientSide){
            return;
        }

        if(event.getEntity() instanceof AbstractPiglin piglin){
            piglin.setImmuneToZombification(true);
        } else if(event.getEntity() instanceof Hoglin hoglin){
            hoglin.setImmuneToZombification(true);
        }

        ServerLevel serverLevel = (ServerLevel) event.getWorld();
        MinecraftServer server = serverLevel.getServer();

        // mob-specific
        if(event.getEntity() instanceof Mob mob){
            if(SelectorHelper.hasSelectorFile(mob)){
                server.tell(new TickTask(server.getTickCount() + 1, () -> {
                    SelectorHelper.remakeSelectors(mob);
                }));
            }
        }

        // non-specific
        if(event.getEntity() instanceof LivingEntity le){
            if(BrainHelper.hasBrainFile(le)){
                server.tell(new TickTask(server.getTickCount() + 1, () -> {
                    BrainHelper.remakeBrain(le, serverLevel);
                }));
            }
            if(BaseAIHelper.hasBaseAIFile(le)){
                server.tell(new TickTask(server.getTickCount() + 1, () -> {
                    BaseAIHelper.addedToWorld(le);
                }));
                if(!event.loadedFromDisk()){
                    server.tell(new TickTask(server.getTickCount() + 1, () -> {
                        BaseAIHelper.firstSpawn(le);
                    }));
                }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onCustomServerAiStepEvent(CustomServerAiStepEvent event){
        Mob mob = event.getMob();

        if(BrainHelper.hasBrainFile(mob)){
            if(BrainHelper.shouldTickBrain(mob)){
                BrainHelper.getBrainCast(mob).tick((ServerLevel) mob.level, mob);
                BrainHelper.updateActivity(mob);
            }
        }
    }

    @SubscribeEvent
    public static void onMobPickUpLootEvent(MobPickUpLootEvent event){
        Mob mob = event.getMob();
        ItemEntity itemEntity = event.getItem();
        if(BaseAIHelper.hasBaseAIFile(mob)){
            event.setCanceled(true);
            if(BaseAIHelper.wantsToPickUp(mob, itemEntity.getItem())){
                BaseAIHelper.pickUpItem(mob, itemEntity);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onAttacked(LivingAttackEvent event){
        if(!event.isCanceled()){
            LivingEntity victim = event.getEntityLiving();
            Entity sourceEntity = event.getSource().getEntity();
            if(sourceEntity instanceof LivingEntity attacker && BaseAIHelper.hasBaseAIFile(victim)){
                BaseAIHelper.attackedBy(victim, attacker);
            }
        }
    }

}
