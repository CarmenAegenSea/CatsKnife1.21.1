package com.catknife.action;

import com.catknife.util.MeleeHelper;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

/**
 * 处决：对低血量目标造成额外伤害
 */
public class ExecuteAction {

    public static boolean execute(LivingEntity user, float range, float damage, float threshold, float multiplier) {
        var targets = MeleeHelper.findTargetsInCone(user, range, 25f);
        if (targets.isEmpty()) {
            return false;
        }
        LivingEntity target = targets.getFirst();
        float dmg = MeleeHelper.calculateDamage(user, damage);

        float maxHp = target.getMaxHealth();
        float hp = target.getHealth();
        if (hp / maxHp <= threshold) {
            dmg *= multiplier;
        }

        MeleeHelper.applyDamage(user, target, 0.6f, dmg);
        if (user instanceof Player player) {
            player.causeFoodExhaustion(0.06F);
        }
        return true;
    }
}