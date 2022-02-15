package com.infamous.aptitude.common.interaction;

import com.infamous.aptitude.Aptitude;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;

@FunctionalInterface
public interface MobInteraction {
    MobInteraction DEFAULT = (mob, player, hand) -> InteractionResult.PASS;

    InteractionResult interact(Mob mob, Player player, InteractionHand hand);

    enum WrapMode{
        ALL("all"),
        PRE_ONLY("pre"),
        POST_ONLY("post"),
        NONE("none");

        private final String id;

        WrapMode(String id){
            this.id = id;
        }

        static WrapMode fromId(String id){
            if(ALL.id.equals(id)){
                return ALL;
            } else if(PRE_ONLY.id.equals(id)){
                return PRE_ONLY;
            } else if(POST_ONLY.id.equals(id)){
                return POST_ONLY;
            }
            if(!id.equals(NONE.id)) Aptitude.LOGGER.error("Invalid WrapMode id {}, defaulting to NONE", id);
            return NONE;
        }
    }
}
