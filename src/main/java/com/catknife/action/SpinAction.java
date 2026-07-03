package com.catknife.action;

import com.catknife.util.MeleeHelper;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import java.util.List;

/**
 * 回旋：360° 横扫一圈，击退周围所有目标
 */
public class SpinAction {

    public static boolean spin(LivingEntity user, float range, float damage, float knockback) {
        float dmg = MeleeHelper.calculateDamage(user, damage);
        int hits = 0;
        for (LivingEntity target : user.level().getEntitiesOfClass(LivingEntity.class,
                user.getBoundingBox().inflate(range))) {
            if (target == user || !target.isAlive() || target.isSpectator()) {
                continue;
            }
            if (target.distanceToSqr(user) > range * range) {
                continue;
            }
            if (!user.hasLineOfSight(target)) {
                continue;
            }
            MeleeHelper.applyDamage(user, target, knockback, dmg);
            hits++;
        }
        if (user instanceof Player player) {
            player.causeFoodExhaustion(0.12F);
        }
        return hits > 0;
    }
}