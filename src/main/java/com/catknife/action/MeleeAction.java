package com.catknife.action;

import com.google.gson.annotations.SerializedName;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * 近战动作抽象基类 — 模板方法模式实现执行流程。
 * <p>
 * 子类只需实现 {@link #findTargets(LivingEntity)} 定义目标筛选逻辑，
 * 其余伤害计算、冷却管理、体力消耗由基类统一处理。
 */
public abstract class MeleeAction {

    @SerializedName("name")
    protected String name = "melee";

    @SerializedName("damage")
    protected float damage = 4.0f;

    @SerializedName("range")
    protected float range = 2.5f;

    @SerializedName("range_angle")
    protected float rangeAngle = 45.0f;

    @SerializedName("cooldown")
    protected float cooldown = 0.4f;

    @SerializedName("knockback")
    protected float knockback = 0.5f;

    @SerializedName("prep_time")
    protected float prepTime = 0.05f;

    @SerializedName("exhaustion")
    protected float exhaustion = 0.08f;

    protected long lastExecuteTime = 0L;
    protected int totalHits = 0;

    protected MeleeAction() {
    }

    /**
     * 执行一次动作。子类不应重写此方法。
     *
     * @param user 攻击者
     * @return 动作结果，包含命中信息和伤害
     */
    public MeleeActionResult execute(LivingEntity user) {
        MeleeActionResult result = new MeleeActionResult();

        if (!canExecute(user)) {
            result.success = false;
            result.cancelReason = "cannot_execute";
            return result;
        }

        long now = System.currentTimeMillis();
        long elapsed = now - lastExecuteTime;
        if (elapsed < cooldown * 1000L) {
            result.success = false;
            result.cancelReason = "cooldown";
            return result;
        }
        lastExecuteTime = now;

        onStart(user);

        List<LivingEntity> targets = findTargets(user);
        result.candidatesFound = targets.size();

        float finalDamage = calculateDamage(user);
        result.damageDealt = finalDamage;

        for (LivingEntity target : targets) {
            if (!canHit(user, target)) {
                continue;
            }
            applyDamage(user, target, finalDamage);
            onHit(user, target);
            result.hits++;
            totalHits++;
        }

        result.success = result.hits > 0;

        if (user instanceof Player) {
            Player player = (Player) user;
            player.causeFoodExhaustion(exhaustion);
        }

        onEnd(user, result);
        return result;
    }

    /**
     * 查找目标实体列表。
     * 每个子类定义自己的目标筛选逻辑（锥形、球形、直线等）。
     */
    protected abstract List<LivingEntity> findTargets(LivingEntity user);

    /**
     * 执行前的检查。
     */
    protected boolean canExecute(LivingEntity user) {
        return user != null && user.isAlive();
    }

    /**
     * 对单个目标的命中检查。
     */
    protected boolean canHit(LivingEntity user, LivingEntity target) {
        if (target == user) {
            return false;
        }
        if (!target.isAlive() || target.isSpectator()) {
            return false;
        }
        return user.hasLineOfSight(target);
    }

    /**
     * 动作开始时的回调。
     */
    protected void onStart(LivingEntity user) {
    }

    /**
     * 命中单个目标时的回调。
     */
    protected void onHit(LivingEntity user, LivingEntity target) {
    }

    /**
     * 动作结束时的回调。
     */
    protected void onEnd(LivingEntity user, MeleeActionResult result) {
    }

    /**
     * 计算含玩家攻击属性的最终伤害。
     */
    protected float calculateDamage(LivingEntity user) {
        var attr = user.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attr == null) {
            return damage;
        }
        double old = attr.getBaseValue();
        ResourceLocation id = ResourceLocation.withDefaultNamespace("melee_action_dmg");
        AttributeModifier modifier = new AttributeModifier(id, damage,
                AttributeModifier.Operation.ADD_VALUE);
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
    protected void applyDamage(LivingEntity user, LivingEntity target, float finalDamage) {
        target.knockback(knockback,
                (float) Math.sin(Math.toRadians(user.getYRot())),
                (float) -Math.cos(Math.toRadians(user.getYRot())));
        if (user instanceof Player) {
            Player player = (Player) user;
            target.hurt(user.damageSources().playerAttack(player), finalDamage);
        } else {
            target.hurt(user.damageSources().mobAttack(user), finalDamage);
        }
    }

    /**
     * 锥形范围内查找实体。
     */
    protected List<LivingEntity> findTargetsInCone(LivingEntity user, float range, float angle) {
        float xRot = (float) Math.toRadians(-user.getXRot());
        float yRot = (float) Math.toRadians(-user.getYRot());
        Vec3 eyeVec = new Vec3(0, 0, 1).xRot(xRot).yRot(yRot).normalize().scale(range);
        Vec3 centre = user.getEyePosition().subtract(eyeVec);

        List<LivingEntity> list = user.level().getEntitiesOfClass(
                LivingEntity.class, user.getBoundingBox().inflate(range + 2));
        list.removeIf(e -> e == user || !e.isAlive() || e.isSpectator());

        List<LivingEntity> result = new java.util.ArrayList<>();
        for (LivingEntity target : list) {
            Vec3 toTarget = target.getEyePosition().subtract(centre);
            double dist = toTarget.length();
            if (dist < range || dist > range + 3) {
                continue;
            }
            double deg = Math.toDegrees(Math.acos(toTarget.dot(eyeVec) / (dist * range)));
            if (deg > angle / 2) {
                continue;
            }
            result.add(target);
        }
        return result;
    }

    /**
     * 球形范围内查找实体。
     */
    protected List<LivingEntity> findTargetsInSphere(LivingEntity user, float radius) {
        List<LivingEntity> list = user.level().getEntitiesOfClass(
                LivingEntity.class, user.getBoundingBox().inflate(radius));
        list.removeIf(e -> e == user || !e.isAlive() || e.isSpectator());
        List<LivingEntity> result = new java.util.ArrayList<>();
        for (LivingEntity target : list) {
            if (target.distanceToSqr(user) <= radius * radius) {
                result.add(target);
            }
        }
        return result;
    }

    public String getName() { return name; }
    public float getDamage() { return damage; }
    public float getRange() { return range; }
    public float getRangeAngle() { return rangeAngle; }
    public float getCooldown() { return cooldown; }
    public float getKnockback() { return knockback; }
    public float getPrepTime() { return prepTime; }
    public float getExhaustion() { return exhaustion; }
    public int getTotalHits() { return totalHits; }
}