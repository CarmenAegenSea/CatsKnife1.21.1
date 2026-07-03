package com.catknife.action;

import com.catknife.util.MeleeHelper;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

/**
 * 出血：攻击后给目标施加凋零效果
 */
public class BleedAction {

    public static boolean bleed(LivingEntity user, float range, float damage, int durationTicks, int amplifier) {
        var targets = MeleeHelper.findTargetsInCone(user, range, 30f);
        if (targets.isEmpty()) {
            return false;
        }
        LivingEntity target = targets.getFirst();
        float dmg = MeleeHelper.calculateDamage(user, damage);
        MeleeHelper.applyDamage(user, target, 0.3f, dmg);
        target.addEffect(new MobEffectInstance(MobEffects.WITHER, durationTicks, amplifier));
        if (user instanceof Player player) {
            player.causeFoodExhaustion(0.05F);
        }
        return true;
    }
}