package com.catknife.util;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/**
 * 近战辅助工具 — 重写 TACZ 锥形检测和伤害逻辑。
 */
public class MeleeHelper {

    /**
     * 在玩家视角锥形范围内查找目标实体。
     *
     * @param user        攻击者
     * @param range       最大距离
     * @param rangeAngle  锥形半角（度）
     * @return 范围内且无阻挡的实体列表
     */
    public static List<LivingEntity> findTargetsInCone(LivingEntity user, double range, float rangeAngle) {
        List<LivingEntity> results = new ArrayList<>();
        float xRot = (float) Math.toRadians(-user.getXRot());
        float yRot = (float) Math.toRadians(-user.getYRot());
        Vec3 eyeVec = new Vec3(0, 0, 1).xRot(xRot).yRot(yRot).normalize().scale(range);
        Vec3 centre = user.getEyePosition().subtract(eyeVec);

        for (LivingEntity target : user.level().getEntitiesOfClass(LivingEntity.class,
                user.getBoundingBox().inflate(range + 2))) {
            if (target == user || !target.isAlive() || target.isSpectator()) {
                continue;
            }
            Vec3 toTarget = target.getEyePosition().subtract(centre);
            double dist = toTarget.length();
            if (dist < range || dist > range + 3) {
                continue;
            }
            double angle = Math.toDegrees(Math.acos(toTarget.dot(eyeVec) / (dist * range)));
            if (angle > rangeAngle / 2) {
                continue;
            }
            if (user.hasLineOfSight(target)) {
                results.add(target);
            }
        }
        return results;
    }

    /**
     * 计算最终伤害（含玩家攻击属性加成，和 TACZ 一致）。
     */
    public static float calculateDamage(LivingEntity user, float baseDamage) {
        var attr = user.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attr == null) {
            return baseDamage;
        }
        double old = attr.getBaseValue();
        var modifier = new AttributeModifier(
                ResourceLocation.withDefaultNamespace("melee_dmg"),
                baseDamage, AttributeModifier.Operation.ADD_VALUE);
        try {
            attr.setBaseValue(0);
            attr.addTransientModifier(modifier);
            return (float) attr.getValue();
        } finally {
            attr.setBaseValue(old);
            attr.removeModifier(modifier);
        }
    }

    /**
     * 应用伤害和击退。
     */
    public static void applyDamage(LivingEntity user, LivingEntity target,
                                    float knockback, float damage) {
        target.knockback(knockback,
                Math.sin(Math.toRadians(user.getYRot())),
                -Math.cos(Math.toRadians(user.getYRot())));
        if (user instanceof Player player) {
            target.hurt(user.damageSources().playerAttack(player), damage);
        } else {
            target.hurt(user.damageSources().mobAttack(user), damage);
        }
    }

    /**
     * 完整的一次近战攻击：查找目标 → 计算伤害 → 应用伤害 → 返回命中数。
     */
    public static int doMelee(LivingEntity user, double range, float rangeAngle,
                               float baseDamage, float knockback) {
        List<LivingEntity> targets = findTargetsInCone(user, range, rangeAngle);
        float finalDamage = calculateDamage(user, baseDamage);
        for (LivingEntity t : targets) {
            applyDamage(user, t, knockback, finalDamage);
        }
        if (user instanceof Player player) {
            player.causeFoodExhaustion(0.08F);
        }
        return targets.size();
    }
}