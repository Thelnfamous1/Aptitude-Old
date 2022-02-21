package com.infamous.aptitude.common;

import com.infamous.aptitude.Aptitude;
import com.infamous.aptitude.common.behavior.util.SelectorHelper;
import com.infamous.aptitude.common.manager.base.BaseAIHelper;
import com.infamous.aptitude.common.manager.brain.BrainManager;
import com.infamous.aptitude.common.behavior.util.BrainHelper;
import com.infamous.aptitude.common.manager.custom.CustomLogicManager;
import com.infamous.aptitude.common.manager.selector.SelectorManager;
import com.infamous.aptitude.common.manager.base.BaseAIManager;
import com.infamous.aptitude.common.util.ReflectionHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.control.SmoothSwimmingLookControl;
import net.minecraft.world.entity.ai.control.SmoothSwimmingMoveControl;
import net.minecraft.world.entity.ai.navigation.WaterBoundPathNavigation;
import net.minecraft.world.entity.animal.Turtle;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.hoglin.Hoglin;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.pathfinder.AmphibiousNodeEvaluator;
import net.minecraft.world.level.pathfinder.PathFinder;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Aptitude.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ForgeCommonEvents {

    @SubscribeEvent
    public static void onReloadListener(AddReloadListenerEvent event){
        Aptitude.customLogicManager = new CustomLogicManager();
        Aptitude.brainManager = new BrainManager();
        Aptitude.selectorManager = new SelectorManager();
        Aptitude.baseAIManager = new BaseAIManager();
        event.addListener(Aptitude.customLogicManager);
        event.addListener(Aptitude.brainManager);
        event.addListener(Aptitude.selectorManager);
        event.addListener(Aptitude.baseAIManager);
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
                    mobSpecificSpawnStuff(le);
                }));
                if(!event.loadedFromDisk()){
                    server.tell(new TickTask(server.getTickCount() + 1, () -> {
                        BaseAIHelper.firstSpawn(le);
                    }));
                }
            }
        }
    }

    // TODO: Make data-driven
    private static void mobSpecificSpawnStuff(LivingEntity le) {
        if(le instanceof Turtle turtle){
            ReflectionHelper.setMobLookControl(turtle, new SmoothSwimmingLookControl(turtle, 20));
            ReflectionHelper.setMobMoveControl(turtle, new SmoothSwimmingMoveControl(turtle, 85, 10, 0.1F, 0.5F, false));
            ReflectionHelper.setMobNavigation(turtle, new WaterBoundPathNavigation(turtle, turtle.level){
                @Override
                protected boolean canUpdatePath() {
                    return true;
                }

                @Override
                protected PathFinder createPathFinder(int maxVisitedNodes) {
                    this.nodeEvaluator = new AmphibiousNodeEvaluator(false);
                    return new PathFinder(this.nodeEvaluator, maxVisitedNodes);
                }

                @Override
                public boolean isStableDestination(BlockPos blockPos) {
                    return !this.level.getBlockState(blockPos.below()).isAir();
                }
            });
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

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event){
        Player player = event.getPlayer();
        InteractionHand hand = event.getHand();
        Entity entity = event.getTarget();
        if(entity instanceof Mob mob){
            if(BaseAIHelper.hasBaseAIFile(mob)){
                event.setCanceled(true);
                InteractionResult interactResult = BaseAIHelper.interact(mob, player, hand);
                event.setCancellationResult(interactResult);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onAttacked(LivingAttackEvent event){
        if(!event.isCanceled()){
            LivingEntity victim = event.getEntityLiving();

            if(victim.level.isClientSide) return;

            Entity sourceEntity = event.getSource().getEntity();
            if(sourceEntity instanceof LivingEntity attacker){
                if(BaseAIHelper.hasBaseAIFile(attacker)){
                    BaseAIHelper.attacked(attacker, victim);
                }
                if(BaseAIHelper.hasBaseAIFile(victim)) {
                    BaseAIHelper.attackedBy(victim, attacker);
                }
            }
        }
    }

}
