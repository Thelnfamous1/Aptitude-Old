package com.infamous.aptitude.common.behavior.util;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.infamous.aptitude.Aptitude;
import com.infamous.aptitude.common.behavior.BehaviorType;
import com.infamous.aptitude.common.behavior.BehaviorTypes;
import com.mojang.datafixers.util.Pair;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.hoglin.Hoglin;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.npc.InventoryCarrier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;
import java.util.function.Function;

public class BehaviorHelper {

    public static <U> MemoryModuleType<U> parseMemoryTypeOrDefault(JsonObject jsonObject, String memberName, MemoryModuleType<U> defaultMemory){
        return jsonObject.has(memberName) ? parseMemoryType(jsonObject, memberName) : defaultMemory;
    }

    public static <U> MemoryModuleType<U> parseMemoryType(JsonObject jsonObject, String memberName) {
        String type = GsonHelper.getAsString(jsonObject, memberName, "");
        return parseMemoryTypeString(type);
    }

    public static <U> MemoryModuleType<U> parseMemoryType(JsonElement jsonElement) {
        String type = jsonElement.getAsString();
        return parseMemoryTypeString(type);
    }

    public static <U> MemoryModuleType<U> parseMemoryTypeString(String type) {
        ResourceLocation location = new ResourceLocation(type);
        if(!ForgeRegistries.MEMORY_MODULE_TYPES.containsKey(location)) throw new JsonParseException("Invalid MemoryModuleType: " + type);
        MemoryModuleType<?> memoryType = ForgeRegistries.MEMORY_MODULE_TYPES.getValue(location);
        if (memoryType == null) throw new JsonParseException("Invalid MemoryModuleType: " + type);
        return (MemoryModuleType<U>) memoryType;
    }

    public static SensorType<?> parseSensorType(JsonElement jsonElement) {
        String type = jsonElement.getAsString();
        ResourceLocation location = new ResourceLocation(type);
        if(!ForgeRegistries.SENSOR_TYPES.containsKey(location)) throw new JsonParseException("Invalid SensorType: " + type);
        SensorType<?> sensorType = ForgeRegistries.SENSOR_TYPES.getValue(location);
        if(sensorType == null) throw new JsonParseException("Invalid SensorType: " + type);
        return sensorType;
    }

    public static MemoryStatus parseMemoryStatus(JsonObject elementObject, String memberName) {
        String value = GsonHelper.getAsString(elementObject, memberName, "");
        MemoryStatus memoryStatus;
        try{
            memoryStatus = MemoryStatus.valueOf(value.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e){
            throw new JsonParseException("Invalid MemoryStatus: " + value);
        }
        return memoryStatus;
    }

    public static <U extends Entity> EntityType<U> parseEntityType(JsonObject jsonObject, String memberName) {
        String entityTypeString = GsonHelper.getAsString(jsonObject, memberName, "");
        return parseEntityTypeString(entityTypeString);
    }

    public static <U extends Entity> EntityType<U> parseEntityTypeString(String entityTypeString) {
        ResourceLocation etLocation = new ResourceLocation(entityTypeString);
        if(!ForgeRegistries.ENTITIES.containsKey(etLocation)) throw new JsonParseException("Invalid EntityType: " + entityTypeString);
        EntityType<?> entityType = ForgeRegistries.ENTITIES.getValue(etLocation);
        if (entityType == null) throw new JsonParseException("Invalid EntityType: " + entityTypeString);
        return (EntityType<U>) entityType;
    }

    public static List<Pair<Behavior<?>, Integer>> parseWeightedBehaviors(JsonObject jsonObject, String memberName){
        List<Pair<Behavior<?>, Integer>> weightedBehaviors = new ArrayList<>();
        JsonArray behaviorArray = GsonHelper.getAsJsonArray(jsonObject, memberName);
        behaviorArray.forEach(je -> {
                    JsonObject elementObject = je.getAsJsonObject();
                    int weight = GsonHelper.getAsInt(elementObject, "weight", 0);
                    Behavior<?> behavior = parseBehavior(elementObject, "type");
                    Pair<Behavior<?>, Integer> pair = Pair.of(behavior, weight);
                    weightedBehaviors.add(pair);
                }
        );
        return weightedBehaviors;
    }

    public static Behavior<?> parseBehavior(JsonObject jsonObject, String memberName, String typeMemberName){
        JsonObject behaviorObject = GsonHelper.getAsJsonObject(jsonObject, memberName);
        return parseBehavior(behaviorObject, typeMemberName);
    }

    public static Behavior<?> parseBehavior(JsonObject behaviorObject, String typeMemberName){
        BehaviorType<?> behaviorType = parseBehaviorType(behaviorObject, typeMemberName);

        return behaviorType.fromJson(behaviorObject);
    }

    public static BehaviorType<?> parseBehaviorType(JsonObject jsonObject, String memberName) {
        String behaviorTypeString = GsonHelper.getAsString(jsonObject, memberName, "");
        ResourceLocation btLocation = new ResourceLocation(behaviorTypeString);
        BehaviorType<?> behaviorType = BehaviorTypes.getBehaviorType(btLocation);
        if(behaviorType == null) throw new JsonParseException("Invalid behavior type: " + behaviorTypeString);
        return behaviorType;
    }

    public static UniformInt parseUniformInt(JsonObject jsonObject, String memberName) {
        JsonObject interval = GsonHelper.getAsJsonObject(jsonObject, memberName);
        int minInclusive = GsonHelper.getAsInt(interval, "minInclusive", 0);
        int maxInclusive = GsonHelper.getAsInt(interval, "maxInclusive", 0);
        if(minInclusive > maxInclusive) throw new JsonParseException("maxInclusive must not be less than minInclusive");
        return UniformInt.of(minInclusive, maxInclusive);
    }

    public static float parseSpeedModifier(JsonObject jsonObject) {
        return GsonHelper.getAsFloat(jsonObject, "speedModifier", 1.0F);
    }

    public static Pair<Integer, Integer> parseBaseBehaviorDuration(JsonObject jsonObject){
        int minDuration = GsonHelper.getAsInt(jsonObject, "minDuration", 150);
        int maxDuration = GsonHelper.getAsInt(jsonObject, "maxDuration", 250);
        return Pair.of(minDuration, maxDuration);
    }

    public static Map<MemoryModuleType<?>, MemoryStatus> parseMemoriesToStatus(JsonElement addContextElement) {
        Map<MemoryModuleType<?>, MemoryStatus> memoriesToStatus = new HashMap<>();

        if(addContextElement.isJsonArray()){
            JsonArray addContextArray = addContextElement.getAsJsonArray();
            addContextArray.forEach(jsonElement -> {
                JsonObject elementAsObj = jsonElement.getAsJsonObject();
                buildMemoriesToStatus(memoriesToStatus, elementAsObj);
            });
        } else if(addContextElement.isJsonObject()){
            buildMemoriesToStatus(memoriesToStatus, addContextElement.getAsJsonObject());
        }
        return memoriesToStatus;
    }

    public static void buildMemoriesToStatus(Map<MemoryModuleType<?>, MemoryStatus> memoriesToStatus, JsonObject elementAsObj) {
        MemoryModuleType<?> memoryType = parseMemoryType(elementAsObj, "type");
        MemoryStatus memoryStatus = parseMemoryStatus(elementAsObj, "status");
        memoriesToStatus.put(memoryType, memoryStatus);
    }

    public static Activity parseActivity(JsonElement je) {
        String activityString = je.getAsString();
        return parseActivityString(activityString);
    }

    public static Activity parseActivity(JsonObject jsonObject, String typeMemberName) {
        String activityString = GsonHelper.getAsString(jsonObject, typeMemberName);
        return parseActivityString(activityString);
    }

    public static Activity parseActivityString(String activityString) {
        ResourceLocation activityLocation = new ResourceLocation(activityString);
        if(!ForgeRegistries.ACTIVITIES.containsKey(activityLocation)) throw new JsonParseException("Invalid Activity: " + activityString);
        Activity activity = ForgeRegistries.ACTIVITIES.getValue(activityLocation);
        if (activity == null) throw new JsonParseException("Invalid Activity: " + activityString);
        return activity;
    }

    public static SoundEvent parseSoundEventString(JsonObject jsonObject, String memberName){
        String soundTypeString = GsonHelper.getAsString(jsonObject, memberName, "");
        ResourceLocation seLocation = new ResourceLocation(soundTypeString);
        if(!ForgeRegistries.SOUND_EVENTS.containsKey(seLocation)) throw new JsonParseException("Invalid SoundEvent: " + soundTypeString);
        SoundEvent soundEvent = ForgeRegistries.SOUND_EVENTS.getValue(seLocation);
        if(soundEvent == null) throw new JsonParseException("Invalid SoundEvent: " + soundTypeString);
        return soundEvent;
    }

    public static ToolAction parseToolAction(JsonObject jsonObject) {
        String toolActionString = GsonHelper.getAsString(jsonObject, "tool_action");
        ToolAction toolAction = null;
        for(ToolAction ta : ToolAction.getActions()){
            if(ta.name().equals(toolActionString)){
                toolAction = ta;
                break;
            }
        }
        if(toolAction == null){
            Aptitude.LOGGER.error("Could not find ToolAction {}, creating new instance. If you notice a typo, correct your JSON file and relaunch.", toolActionString);
            toolAction = ToolAction.get(toolActionString);

        }
        return toolAction;
    }

    public static void putInInventory(LivingEntity livingEntity, ItemStack stack) {
        ItemStack addResult =
                livingEntity instanceof InventoryCarrier ic
                        && ic.getInventory() instanceof SimpleContainer sc ?
                        sc.addItem(stack) :
                        stack;
        throwItemsTowardRandomPos(livingEntity, Collections.singletonList(addResult));
    }

    public static void throwItems(LivingEntity livingEntity, List<ItemStack> items) {
        Optional<Player> player = livingEntity.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_PLAYER);
        if (player.isPresent()) {
            throwItemsTowardPlayer(livingEntity, player.get(), items);
        } else {
            throwItemsTowardRandomPos(livingEntity, items);
        }

    }

    public static void throwItemsTowardRandomPos(LivingEntity livingEntity, List<ItemStack> items) {
        throwItemsTowardPos(livingEntity, items, getRandomNearbyPos(livingEntity));
    }

    public static void throwItemsTowardPlayer(LivingEntity livingEntity, Player player, List<ItemStack> items) {
        throwItemsTowardPos(livingEntity, items, player.position());
    }

    public static void throwItemsTowardPos(LivingEntity livingEntity, List<ItemStack> items, Vec3 pos) {
        if (!items.isEmpty()) {
            livingEntity.swing(InteractionHand.OFF_HAND);

            for(ItemStack itemstack : items) {
                BehaviorUtils.throwItem(livingEntity, itemstack, pos.add(0.0D, 1.0D, 0.0D));
            }
        }
    }

    public static Vec3 getRandomNearbyPos(LivingEntity livingEntity) {
        Vec3 randomNearbyPos = livingEntity instanceof PathfinderMob pathfinderMob? LandRandomPos.getPos(pathfinderMob, 4, 2) : null;
        return randomNearbyPos == null ? livingEntity.position() : randomNearbyPos;
    }

    public static List<ItemStack> getBarterResponseItems(LivingEntity livingEntity, ResourceLocation barteringLootLocation) {
        LootTable lootTable = livingEntity.level.getServer().getLootTables().get(barteringLootLocation);
        return lootTable.getRandomItems((new LootContext.Builder((ServerLevel)livingEntity.level)).withParameter(LootContextParams.THIS_ENTITY, livingEntity).withRandom(livingEntity.level.random).create(LootContextParamSets.PIGLIN_BARTER));
    }

    public static EquipmentSlot parseEquipmentSlotOrDefault(JsonObject jsonObject, String slotMemberName, EquipmentSlot defaultSlot){
        if(jsonObject.has(slotMemberName)) return parseEquipmentSlot(jsonObject, slotMemberName);
        return defaultSlot;
    }

    public static EquipmentSlot parseEquipmentSlot(JsonObject jsonObject, String slotMemberName) {
        String equipmentSlotString = GsonHelper.getAsString(jsonObject, slotMemberName);
        return EquipmentSlot.byName(equipmentSlotString);
    }

    public static Function<LivingEntity, Long> parseExpireTimeFunction(JsonObject jsonObject, String memberName, String typeMemberName) {
        if(jsonObject.has(memberName) && jsonObject.get(memberName).isJsonObject()){
            return FunctionHelper.parseFunction(jsonObject, memberName, typeMemberName);
        } else{
            long expireTime = GsonHelper.getAsLong(jsonObject, memberName, Long.MAX_VALUE);
            return livingEntity -> expireTime;
        }
    }

    public static ItemStack removeOneItemFromItemEntity(ItemEntity itemEntity) {
        ItemStack itemStack = itemEntity.getItem();
        ItemStack singleton = itemStack.split(1);
        if (itemStack.isEmpty()) {
            itemEntity.discard();
        } else {
            itemEntity.setItem(itemStack);
        }
        return singleton;
    }

    public static void stopWalking(LivingEntity livingEntity) {
        livingEntity.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
        if(livingEntity instanceof Mob mob) mob.getNavigation().stop();
    }

    public static void holdInOffhand(LivingEntity livingEntity, ItemStack stack) {
        if (!livingEntity.getOffhandItem().isEmpty()) {
            livingEntity.spawnAtLocation(livingEntity.getItemBySlot(EquipmentSlot.OFFHAND));
        }

        livingEntity.setItemSlot(EquipmentSlot.OFFHAND, stack);
        if(livingEntity instanceof Mob mob){
            mob.setGuaranteedDrop(EquipmentSlot.OFFHAND);
            mob.setPersistenceRequired();
        }
    }

    protected static void wasHurtBy(LivingEntity victim, LivingEntity attacker) {
        if (!(attacker instanceof Piglin)) {
            Brain<?> brain = victim.getBrain();
            if (attacker.getType() == EntityType.HOGLIN && hoglinsOutnumberPiglins(victim)) {
                setAvoidTargetAndDontHuntForAWhile(victim, attacker);
                broadcastRetreat(victim, attacker);
            } else {
                //maybeRetaliate(victim, attacker);
            }
        }
    }

    private static void broadcastRetreat(LivingEntity p_34930_, LivingEntity p_34931_) {
        getVisibleAdultPiglins(p_34930_).stream().filter((p_34985_) -> {
            return p_34985_ instanceof Piglin;
        }).forEach((p_34819_) -> {
            retreatFromNearestTarget((Piglin)p_34819_, p_34931_);
        });
    }

    private static void retreatFromNearestTarget(LivingEntity victim, LivingEntity attacker) {
        Brain<?> brain = victim.getBrain();
        LivingEntity nearestTarget = BehaviorUtils.getNearestTarget(victim, brain.getMemory(MemoryModuleType.AVOID_TARGET), attacker);
        nearestTarget = BehaviorUtils.getNearestTarget(victim, brain.getMemory(MemoryModuleType.ATTACK_TARGET), nearestTarget);
        setAvoidTargetAndDontHuntForAWhile(victim, nearestTarget);
    }

    private static boolean hoglinsOutnumberPiglins(LivingEntity p_35013_) {
        int i = p_35013_.getBrain().getMemory(MemoryModuleType.VISIBLE_ADULT_PIGLIN_COUNT).orElse(0) + 1;
        int j = p_35013_.getBrain().getMemory(MemoryModuleType.VISIBLE_ADULT_HOGLIN_COUNT).orElse(0);
        return j > i;
    }

    private static void setAvoidTargetAndDontHuntForAWhile(LivingEntity coward, LivingEntity target) {
        coward.getBrain().eraseMemory(MemoryModuleType.ANGRY_AT);
        coward.getBrain().eraseMemory(MemoryModuleType.ATTACK_TARGET);
        coward.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
        //coward.getBrain().setMemoryWithExpiry(MemoryModuleType.AVOID_TARGET, target, (long)RETREAT_DURATION.sample(coward.level.random));
        dontKillAnyMoreHoglinsForAWhile(coward);
    }

    public static Optional<LivingEntity> getAvoidTarget(LivingEntity p_34987_) {
        return p_34987_.getBrain().hasMemoryValue(MemoryModuleType.AVOID_TARGET) ? p_34987_.getBrain().getMemory(MemoryModuleType.AVOID_TARGET) : Optional.empty();
    }

    protected static void broadcastAngerTarget(LivingEntity p_34896_, LivingEntity p_34897_) {
        getAdultAllies(p_34896_).forEach((p_34890_) -> {
            //if (p_34897_.getType() != EntityType.HOGLIN || p_34890_.canHunt() && ((Hoglin)p_34897_).canBeHunted()) {
                //setAngerTargetIfCloserThanCurrent(p_34890_, p_34897_);
            //}
        });
    }

    protected static void setAngerTarget(LivingEntity attacker, LivingEntity target) {
        if (Sensor.isEntityAttackableIgnoringLineOfSight(attacker, target)) {
            attacker.getBrain().eraseMemory(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);
            attacker.getBrain().setMemoryWithExpiry(MemoryModuleType.ANGRY_AT, target.getUUID(), 600L);
            /*
            if (target.getType() == EntityType.HOGLIN && attacker.canHunt()) {
                dontKillAnyMoreHoglinsForAWhile(attacker);
            }
             */

            if (target.getType() == EntityType.PLAYER && attacker.level.getGameRules().getBoolean(GameRules.RULE_UNIVERSAL_ANGER)) {
                attacker.getBrain().setMemoryWithExpiry(MemoryModuleType.UNIVERSAL_ANGER, true, 600L);
            }
        }
    }

    private static Optional<LivingEntity> getAngerTarget(LivingEntity attacker) {
        return BehaviorUtils.getLivingEntityFromUUIDMemory(attacker, MemoryModuleType.ANGRY_AT);
    }

    protected static void dontKillAnyMoreHoglinsForAWhile(LivingEntity p_34923_) {
        //p_34923_.getBrain().setMemoryWithExpiry(MemoryModuleType.HUNTED_RECENTLY, true, (long)TIME_BETWEEN_HUNTS.sample(p_34923_.level.random));
    }

    private static List<AbstractPiglin> getVisibleAdultPiglins(LivingEntity p_35005_) {
        return p_35005_.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_ADULT_PIGLINS).orElse(ImmutableList.of());
    }

    private static List<AbstractPiglin> getAdultAllies(LivingEntity p_34961_) {
        return p_34961_.getBrain().getMemory(MemoryModuleType.NEARBY_ADULT_PIGLINS).orElse(ImmutableList.of());
    }
}
