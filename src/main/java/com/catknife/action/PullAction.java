package com.catknife.action;

import com.catknife.util.MeleeHelper;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

/**
 * 钩拉：将目标拉向自己
 */
public class PullAction {

    public static boolean pull(LivingEntity user, float range, float damage) {
        var targets = MeleeHelper.findTargetsInCone(user, range, 30f);
        if (targets.isEmpty()) {
            return false;
        }
        LivingEntity target = targets.getFirst();
        float dmg = MeleeHelper.calculateDamage(user, damage);
        MeleeHelper.applyDamage(user, target, -0.5f, dmg);

        Vec3 pull = user.position().subtract(target.position()).normalize().scale(1.5);
        target.setDeltaMovement(pull.x(), 0.3, pull.z());
        target.hurtMarked = true;

        if (user instanceof Player player) {
            player.causeFoodExhaustion(0.06F);
        }
        return true;
    }
}