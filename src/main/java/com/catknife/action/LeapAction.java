package com.catknife.action;

import com.catknife.util.MeleeHelper;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

/**
 * 跃斩：跳向目标并下劈，距离越远伤害越高
 */
public class LeapAction {

    public static boolean leap(LivingEntity user, float maxRange, float minDamage, float maxDamage) {
        var targets = MeleeHelper.findTargetsInCone(user, maxRange, 20f);
        if (targets.isEmpty()) {
            return false;
        }
        LivingEntity target = targets.getFirst();
        double dist = user.distanceToSqr(target);
        double ratio = Math.min(1, dist / (maxRange * maxRange));
        float dmg = (float) (minDamage + (maxDamage - minDamage) * ratio);
        dmg = MeleeHelper.calculateDamage(user, dmg);
        MeleeHelper.applyDamage(user, target, 0.8f, dmg);

        var look = user.getLookAngle();
        user.setDeltaMovement(look.x() * 0.4, 0.4, look.z() * 0.4);
        user.hurtMarked = true;

        if (user instanceof Player player) {
            player.causeFoodExhaustion(0.1F);
        }
        return true;
    }
}