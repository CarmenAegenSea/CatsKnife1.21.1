package com.catknife.action;

import com.catknife.util.MeleeHelper;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

/**
 * 背刺：从目标背后攻击造成额外伤害
 */
public class BackstabAction {
    public static boolean backstab(LivingEntity user, float range, float damage, float multiplier) {
        var targets = MeleeHelper.findTargetsInCone(user, range, 25f);
        if (targets.isEmpty()) {
            return false;
        }
        LivingEntity target = targets.getFirst();
        float dmg = MeleeHelper.calculateDamage(user, damage);

        var look = user.getLookAngle();
        var toTarget = target.position().subtract(user.position()).normalize();
        double dot = look.dot(toTarget);

        if (dot < -0.5) {
            dmg *= multiplier;
        }

        MeleeHelper.applyDamage(user, target, 0.5f, dmg);
        if (user instanceof Player player) {
            player.causeFoodExhaustion(0.06F);
        }
        return true;
    }
}