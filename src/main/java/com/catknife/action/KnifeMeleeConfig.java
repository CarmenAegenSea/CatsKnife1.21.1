package com.catknife.action;

import com.google.gson.annotations.SerializedName;

/**
 * 刀类近战配置 — 合并 TACZ 的 GunMeleeData + GunDefaultMeleeData。
 */
public class KnifeMeleeConfig {

    @SerializedName("distance")
    private float distance = 2.5f;

    @SerializedName("cooldown")
    private float cooldown = 0.3f;

    @SerializedName("animation_type")
    private String animationType = "melee_slash";

    @SerializedName("range_angle")
    private float rangeAngle = 45f;

    @SerializedName("damage")
    private float damage = 5f;

    @SerializedName("knockback")
    private float knockback = 0.4f;

    @SerializedName("prep_time")
    private float prepTime = 0.05f;

    @SerializedName("sweep_angle")
    private float sweepAngle = 90f;

    @SerializedName("hit_count")
    private int hitCount = 1;

    public float getDistance() { return distance; }
    public float getCooldown() { return cooldown; }
    public String getAnimationType() { return animationType; }
    public float getRangeAngle() { return rangeAngle; }
    public float getDamage() { return damage; }
    public float getKnockback() { return knockback; }
    public float getPrepTime() { return prepTime; }
    public float getSweepAngle() { return sweepAngle; }
    public int getHitCount() { return hitCount; }

    public KnifeMeleeConfig setDistance(float v) { distance = v; return this; }
    public KnifeMeleeConfig setCooldown(float v) { cooldown = v; return this; }
    public KnifeMeleeConfig setDamage(float v) { damage = v; return this; }
    public KnifeMeleeConfig setKnockback(float v) { knockback = v; return this; }
    public KnifeMeleeConfig setRangeAngle(float v) { rangeAngle = v; return this; }
    public KnifeMeleeConfig setSweepAngle(float v) { sweepAngle = v; return this; }
    public KnifeMeleeConfig setHitCount(int v) { hitCount = v; return this; }
}