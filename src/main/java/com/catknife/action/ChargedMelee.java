package com.catknife.action;

import com.google.gson.annotations.SerializedName;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * 蓄力攻击 — 按住时蓄力，释放时根据蓄力时间提升伤害和范围。
 */
public class ChargedMelee extends MeleeAction {

    public static final int MAX_CHARGE_LEVEL = 5;
    public static final long CHARGE_PER_LEVEL_MS = 300L;

    @SerializedName("charge_multiplier")
    protected float chargeMultiplier = 0.5f;

    @SerializedName("max_charge_range_boost")
    protected float maxChargeRangeBoost = 1.5f;

    @SerializedName("range_per_level")
    protected float rangePerLevel = 0.15f;

    @SerializedName("knockback_per_level")
    protected float knockbackPerLevel = 0.1f;

    @SerializedName("exhaustion_per_level")
    protected float exhaustionPerLevel = 0.04f;

    @SerializedName("release_animation")
    protected String releaseAnimation = "charge_release";

    private long chargeStartTime = -1L;
    private int chargeLevel = 0;
    private boolean isCharging = false;
    private int totalChargedAttacks = 0;

    public ChargedMelee() {
        this.name = "charged";
        this.cooldown = 0.6f;
        this.damage = 6f;
        this.range = 3.0f;
    }

    // ── 蓄力控制 ─────────────────────────────────────────

    /**
     * 开始蓄力。
     */
    public void startCharge() {
        if (isCharging) {
            return;
        }
        isCharging = true;
        chargeStartTime = System.currentTimeMillis();
        chargeLevel = 0;
    }

    /**
     * 释放蓄力攻击。
     */
    public MeleeActionResult release(LivingEntity user) {
        if (!isCharging) {
            MeleeActionResult result = new MeleeActionResult();
            result.success = false;
            result.cancelReason = "not_charging";
            return result;
        }
        updateChargeLevel();
        isCharging = false;

        MeleeActionResult result = execute(user);
        if (result.success) {
            totalChargedAttacks++;
        }
        return result;
    }

    /**
     * 取消蓄力。
     */
    public void cancelCharge() {
        isCharging = false;
        chargeStartTime = -1L;
        chargeLevel = 0;
    }

    /**
     * 更新当前蓄力等级。
     */
    public int updateChargeLevel() {
        if (!isCharging || chargeStartTime < 0) {
            chargeLevel = 0;
            return 0;
        }
        long elapsed = System.currentTimeMillis() - chargeStartTime;
        chargeLevel = (int) (elapsed / CHARGE_PER_LEVEL_MS);
        if (chargeLevel > MAX_CHARGE_LEVEL) {
            chargeLevel = MAX_CHARGE_LEVEL;
        }
        return chargeLevel;
    }

    /**
     * 获取当前蓄力进度 0~1。
     */
    public float getChargeProgress() {
        if (!isCharging) {
            return 0f;
        }
        updateChargeLevel();
        return (float) chargeLevel / MAX_CHARGE_LEVEL;
    }

    @Override
    protected boolean canExecute(LivingEntity user) {
        updateChargeLevel();
        if (!super.canExecute(user)) {
            return false;
        }
        return true;
    }

    @Override
    protected List<LivingEntity> findTargets(LivingEntity user) {
        float actualRange = range + chargeLevel * rangePerLevel;
        if (chargeLevel >= MAX_CHARGE_LEVEL) {
            actualRange += maxChargeRangeBoost;
        }
        float actualAngle = rangeAngle + chargeLevel * 3f;
        return findTargetsInCone(user, actualRange, actualAngle);
    }

    @Override
    protected void onStart(LivingEntity user) {
        float actualKnockback = knockback + chargeLevel * knockbackPerLevel;
        if (chargeLevel >= MAX_CHARGE_LEVEL) {
            actualKnockback *= 2f;
        }
        knockback = actualKnockback;

        float actualExhaustion = exhaustion + chargeLevel * exhaustionPerLevel;
        exhaustion = Math.min(actualExhaustion, 0.5f);
    }

    @Override
    protected void onHit(LivingEntity user, LivingEntity target) {
        if (chargeLevel >= 3) {
            target.setDeltaMovement(target.getDeltaMovement().x(), 0.4, target.getDeltaMovement().z());
            target.hurtMarked = true;
        }
        if (chargeLevel >= MAX_CHARGE_LEVEL) {
            target.igniteForSeconds(3);
        }
    }

    @Override
    protected void onEnd(LivingEntity user, MeleeActionResult result) {
        float actualDamage = damage + chargeLevel * damage * chargeMultiplier;
        if (chargeLevel >= MAX_CHARGE_LEVEL) {
            actualDamage *= 1.5f;
        }
        this.damage = actualDamage;
        result.damageDealt = actualDamage;
    }

    @Override
    protected float calculateDamage(LivingEntity user) {
        float base = super.calculateDamage(user);
        return base + chargeLevel * base * chargeMultiplier;
    }

    // ── Getter ─────────────────────────────────────────

    public boolean isCharging() {
        return isCharging;
    }

    public int getChargeLevel() {
        updateChargeLevel();
        return chargeLevel;
    }

    public float getChargeMultiplier() {
        return chargeMultiplier;
    }

    public float getMaxChargeRangeBoost() {
        return maxChargeRangeBoost;
    }

    public long getChargeStartTime() {
        return chargeStartTime;
    }

    public int getTotalChargedAttacks() {
        return totalChargedAttacks;
    }

    public String getReleaseAnimation() {
        return releaseAnimation;
    }

    public long getChargeElapsed() {
        if (!isCharging || chargeStartTime < 0) {
            return 0L;
        }
        return System.currentTimeMillis() - chargeStartTime;
    }

    // ── Builder ─────────────────────────────────────────

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final ChargedMelee action = new ChargedMelee();

        public Builder name(String v) { action.name = v; return this; }
        public Builder damage(float v) { action.damage = v; return this; }
        public Builder range(float v) { action.range = v; return this; }
        public Builder rangeAngle(float v) { action.rangeAngle = v; return this; }
        public Builder cooldown(float v) { action.cooldown = v; return this; }
        public Builder knockback(float v) { action.knockback = v; return this; }
        public Builder chargeMultiplier(float v) { action.chargeMultiplier = v; return this; }
        public Builder maxChargeRangeBoost(float v) { action.maxChargeRangeBoost = v; return this; }
        public Builder rangePerLevel(float v) { action.rangePerLevel = v; return this; }
        public Builder knockbackPerLevel(float v) { action.knockbackPerLevel = v; return this; }
        public Builder exhaustionPerLevel(float v) { action.exhaustionPerLevel = v; return this; }
        public Builder releaseAnimation(String v) { action.releaseAnimation = v; return this; }

        public ChargedMelee build() {
            if (action.chargeMultiplier <= 0) {
                action.chargeMultiplier = 0.5f;
            }
            return action;
        }
    }
}