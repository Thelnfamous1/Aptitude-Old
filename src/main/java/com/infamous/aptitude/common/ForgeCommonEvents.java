package com.infamous.aptitude.common;

import com.infamous.aptitude.Aptitude;
import com.infamous.aptitude.common.behavior.util.SelectorHelper;
import com.infamous.aptitude.common.manager.base.BaseAIHelper;
import com.infamous.aptitude.common.manager.brain.BrainManager;
import com.infamous.aptitude.common.behavior.util.BrainHelper;
import com.infamous.aptitude.common.manager.selector.SelectorManager;
import com.infamous.aptitude.common.manager.base.BaseAIManager;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.TimeUtil;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.util.GoalUtils;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.monster.hoglin.Hoglin;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Aptitude.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ForgeCommonEvents {

    @SubscribeEvent
    public static void onReloadListener(AddReloadListenerEvent event){
        Aptitude.brainManager = new BrainManager();
        Aptitude.selectorManager = new SelectorManager();
        Aptitude.baseAIManager = new BaseAIManager();
        event.addListener(Aptitude.brainManager);
        event.addListener(Aptitude.selectorManager);
        event.addListener(Aptitude.baseAIManager);
    }

    @SubscribeEvent
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
        if(event.getEntity() instanceof Mob mob){
            if(SelectorHelper.hasSelectorFile(mob)){
                server.tell(new TickTask(server.getTickCount() + 1, () -> {
                    SelectorHelper.remakeSelectors(mob);
                }));
            }
        } else if(event.getEntity() instanceof LivingEntity le){
            if(BrainHelper.hasBrainFile(le)){
                server.tell(new TickTask(server.getTickCount() + 1, () -> {
                    BrainHelper.remakeBrain(le, serverLevel);
                }));
            }
            if(BaseAIHelper.hasBaseAIFile(le)){
                server.tell(new TickTask(server.getTickCount() + 1, () -> {
                    BaseAIHelper.finalizeSpawn(le);
                }));
            }
        }
    }

    // TODO: Make data-driven
    private static void performSpecificMobHandling(EntityJoinWorldEvent event) {
        Entity mob = event.getEntity();
        if(mob instanceof Zombie zombie) {
            zombie.setCanPickUpLoot(true);
            if (GoalUtils.hasGroundPathNavigation(zombie)) {
                ((GroundPathNavigation)zombie.getNavigation()).setCanOpenDoors(true);
            }
            if(!event.loadedFromDisk()){ // "finalize spawn"
                boolean baby = zombie.isBaby();
                if(zombie.getType() == EntityType.ZOMBIE){
                    if(!baby){
                        if(zombie.getRandom().nextFloat() < 0.5F){
                            zombie.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.CROSSBOW));
                        } else{
                            zombie.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.GOLDEN_SWORD));
                        }
                    }
                    int waitBeforeHunting = TimeUtil.rangeOfSeconds(30, 120).sample(zombie.level.random);
                    zombie.getBrain().setMemoryWithExpiry(MemoryModuleType.HUNTED_RECENTLY, true, (long)waitBeforeHunting);
                } else if(zombie.getType() == EntityType.HUSK){
                    if(!baby){
                        zombie.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.GOLDEN_AXE));
                    }
                    GlobalPos globalpos = GlobalPos.of(zombie.level.dimension(), zombie.blockPosition());
                    zombie.getBrain().setMemory(MemoryModuleType.HOME, globalpos);
                }
            }
        }
    }

    @SubscribeEvent
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

}
