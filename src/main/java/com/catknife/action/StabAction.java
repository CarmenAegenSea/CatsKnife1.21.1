package com.catknife.action;

import com.catknife.util.MeleeHelper;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

/**
 * 最简单的刺击：只打正前方第一个目标，一次伤害一次击退
 */
public class StabAction {

    public static boolean stab(LivingEntity user, float range, float damage, float knockback) {
        var targets = MeleeHelper.findTargetsInCone(user, range, 20f);
        if (targets.isEmpty()) {
            return false;
        }
        LivingEntity target = targets.getFirst();
        float dmg = MeleeHelper.calculateDamage(user, damage);
        MeleeHelper.applyDamage(user, target, knockback, dmg);
        if (user instanceof Player player) {
            player.causeFoodExhaustion(0.04F);
        }
        return true;
    }
}