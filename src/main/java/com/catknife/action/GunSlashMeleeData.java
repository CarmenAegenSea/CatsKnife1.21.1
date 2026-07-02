package com.catknife.action;

import com.catknife.util.MeleeHelper;
import com.google.gson.annotations.SerializedName;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class GunSlashMeleeData {

    @SerializedName("animation_type")
    private String animationType = "melee_slash";

    @SerializedName("distance")
    private float distance = 2.5f;

    @SerializedName("range_angle")
    private float rangeAngle = 60f;

    @SerializedName("cooldown")
    private float cooldown = 0.4f;

    @SerializedName("damage")
    private float damage = 4f;

    @SerializedName("knockback")
    private float knockback = 0.5f;

    @SerializedName("prep")
    private float prepTime = 0.05f;

    @SerializedName("sweep_angle")
    private float sweepAngle = 90f;

    @SerializedName("hit_count")
    private int hitCount = 1;

    private long lastExecuteTime = 0L;

    public GunSlashMeleeData() {
    }

    public static boolean execute(LivingEntity user, ItemStack weapon) {
        return doSlash(user, new GunSlashMeleeData());
    }

    public boolean execute(LivingEntity user) {
        long now = System.currentTimeMillis();
        if (now - lastExecuteTime < cooldown * 1000F) {
            return false;
        }
        lastExecuteTime = now;
        return doSlash(user, this);
    }

    private static boolean doSlash(LivingEntity user, GunSlashMeleeData cfg) {
        if (user == null || !user.isAlive()) {
            return false;
        }
        List<LivingEntity> targets = MeleeHelper.findTargetsInCone(user, cfg.distance, cfg.rangeAngle);
        float dmg = MeleeHelper.calculateDamage(user, cfg.damage);
        int hits = 0;
        for (LivingEntity t : targets) {
            if (hits >= cfg.hitCount) {
                break;
            }
            MeleeHelper.applyDamage(user, t, cfg.knockback, dmg);
            cfg.onHit(user, t);
            hits++;
        }
        if (user instanceof Player player) {
            player.causeFoodExhaustion(0.08F);
        }
        cfg.onAfterSlash(user, hits);
        return hits > 0;
    }

    protected void onHit(LivingEntity user, LivingEntity target) {
    }

    protected void onAfterSlash(LivingEntity user, int hitCount) {
    }

    public String getAnimationType() { return animationType; }
    public float getDistance() { return distance; }
    public float getRangeAngle() { return rangeAngle; }
    public float getCooldown() { return cooldown; }
    public float getDamage() { return damage; }
    public float getKnockback() { return knockback; }
    public float getPrepTime() { return prepTime; }
    public float getSweepAngle() { return sweepAngle; }
    public int getHitCount() { return hitCount; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private final GunSlashMeleeData data = new GunSlashMeleeData();
        public Builder distance(float d) { data.distance = d; return this; }
        public Builder rangeAngle(float a) { data.rangeAngle = a; return this; }
        public Builder cooldown(float c) { data.cooldown = c; return this; }
        public Builder damage(float d) { data.damage = d; return this; }
        public Builder knockback(float k) { data.knockback = k; return this; }
        public Builder sweepAngle(float a) { data.sweepAngle = a; return this; }
        public Builder hitCount(int n) { data.hitCount = n; return this; }
        public GunSlashMeleeData build() { return data; }
    }
}