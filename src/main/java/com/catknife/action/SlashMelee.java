package com.catknife.action;

import com.google.gson.annotations.SerializedName;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/**
 * 横斩动作 — 扇形范围横扫
 */
public class SlashMelee extends MeleeAction {

    @SerializedName("sweep_angle")
    protected float sweepAngle = 90f;

    @SerializedName("hit_count")
    protected int hitCount = 3;

    @SerializedName("damage_falloff")
    protected float damageFalloff = 0.7f;

    @SerializedName("ignore_armor")
    protected boolean ignoreArmor = false;

    @SerializedName("particle_enabled")
    protected boolean particleEnabled = true;

    @SerializedName("sound_type")
    protected String soundType = "slash";

    private int currentHitIndex = 0;
    private float accumulatedDamage = 0f;

    public SlashMelee() {
        this.name = "slash";
        this.rangeAngle = 60f;
    }

    @Override
    protected List<LivingEntity> findTargets(LivingEntity user) {
        return findTargetsInCone(user, range, rangeAngle);
    }

    @Override
    protected boolean canHit(LivingEntity user, LivingEntity target) {
        if (!super.canHit(user, target)) {
            return false;
        }
        if (currentHitIndex >= hitCount) {
            return false;
        }
        return true;
    }

    @Override
    protected void onStart(LivingEntity user) {
        currentHitIndex = 0;
        accumulatedDamage = 0f;
    }

    @Override
    protected void onHit(LivingEntity user, LivingEntity target) {
        currentHitIndex++;
        float actualDamage = calculateDamage(user);
        if (currentHitIndex > 1) {
            float falloff = (float) Math.pow(damageFalloff, currentHitIndex - 1);
            actualDamage *= falloff;
        }
        accumulatedDamage += actualDamage;

        if (target instanceof Player) {
            Player p = (Player) target;
            p.setDeltaMovement(p.getDeltaMovement().x(), 0.15, p.getDeltaMovement().z());
            p.hurtMarked = true;
        }

        Vec3 look = user.getLookAngle();
        float yaw = (float) Math.toRadians(user.getYRot());
        float spread = (currentHitIndex - 1) * 0.15f;
        float knockAngle = (currentHitIndex % 2 == 0) ? 1 : -1;
        target.knockback(knockback + spread,
                (float) Math.sin(yaw) + knockAngle * 0.2f,
                (float) -Math.cos(yaw) + knockAngle * 0.2f);
    }

    @Override
    protected void onEnd(LivingEntity user, MeleeActionResult result) {
        result.damageDealt = accumulatedDamage;
    }

    /**
     * 是否还能继续命中。
     */
    public boolean canHitMore() {
        return currentHitIndex < hitCount;
    }

    /**
     * 当前连击次数。
     */
    public int getCurrentHitIndex() {
        return currentHitIndex;
    }

    /**
     * 重置连击计数。
     */
    public void resetCombo() {
        currentHitIndex = 0;
        accumulatedDamage = 0f;
    }

    /**
     * 获取本次横扫累积的总伤害。
     */
    public float getAccumulatedDamage() {
        return accumulatedDamage;
    }

    public float getSweepAngle() {
        return sweepAngle;
    }

    public int getHitCount() {
        return hitCount;
    }

    public float getDamageFalloff() {
        return damageFalloff;
    }

    public boolean isIgnoreArmor() {
        return ignoreArmor;
    }

    public boolean isParticleEnabled() {
        return particleEnabled;
    }

    public String getSoundType() {
        return soundType;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final SlashMelee action = new SlashMelee();

        public Builder name(String v) { action.name = v; return this; }
        public Builder damage(float v) { action.damage = v; return this; }
        public Builder range(float v) { action.range = v; return this; }
        public Builder rangeAngle(float v) { action.rangeAngle = v; return this; }
        public Builder cooldown(float v) { action.cooldown = v; return this; }
        public Builder knockback(float v) { action.knockback = v; return this; }
        public Builder sweepAngle(float v) { action.sweepAngle = v; return this; }
        public Builder hitCount(int v) { action.hitCount = v; return this; }
        public Builder damageFalloff(float v) { action.damageFalloff = v; return this; }
        public Builder ignoreArmor(boolean v) { action.ignoreArmor = v; return this; }
        public Builder particleEnabled(boolean v) { action.particleEnabled = v; return this; }
        public Builder soundType(String v) { action.soundType = v; return this; }
        public Builder exhaustion(float v) { action.exhaustion = v; return this; }

        public SlashMelee build() {
            if (action.sweepAngle <= 0) {
                action.sweepAngle = 90f;
            }
            if (action.hitCount <= 0) {
                action.hitCount = 1;
            }
            return action;
        }
    }
}