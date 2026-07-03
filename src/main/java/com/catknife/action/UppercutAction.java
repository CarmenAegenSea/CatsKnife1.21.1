package com.catknife.action;

import com.catknife.util.MeleeHelper;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

/**
 * 上挑：击中目标时附加向上速度
 */
public class UppercutAction {

    public static boolean uppercut(LivingEntity user, float range, float damage, float launchPower) {
        var targets = MeleeHelper.findTargetsInCone(user, range, 30f);
        if (targets.isEmpty()) {
            return false;
        }
        LivingEntity target = targets.getFirst();
        float dmg = MeleeHelper.calculateDamage(user, damage);
        MeleeHelper.applyDamage(user, target, 0.3f, dmg);
        target.setDeltaMovement(target.getDeltaMovement().x(), launchPower, target.getDeltaMovement().z());
        target.hurtMarked = true;
        if (user instanceof Player player) {
            player.causeFoodExhaustion(0.06F);
        }
        return true;
    }
}