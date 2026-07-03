package com.catknife.action;

import com.catknife.util.MeleeHelper;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

/**
 * 突进：向前冲刺一段距离，沿途造成伤害
 */
public class LungeAction {

    public static boolean lunge(LivingEntity user, float range, float damage, float knockback) {
        Vec3 look = user.getLookAngle();
        float dmg = MeleeHelper.calculateDamage(user, damage);
        int hits = 0;

        for (LivingEntity target : user.level().getEntitiesOfClass(LivingEntity.class,
                user.getBoundingBox().inflate(range))) {
            if (target == user || !target.isAlive() || target.isSpectator()) {
                continue;
            }
            var vec = target.position().subtract(user.position());
            double dist = vec.length();
            if (dist > range) {
                continue;
            }
            vec = vec.normalize();
            if (look.dot(vec) < 0.3) {
                continue;
            }
            MeleeHelper.applyDamage(user, target, knockback, dmg);
            hits++;
        }

        user.setDeltaMovement(look.x() * 0.6, 0.2, look.z() * 0.6);
        user.hurtMarked = true;

        if (user instanceof Player player) {
            player.causeFoodExhaustion(0.1F);
        }
        return hits > 0;
    }
}