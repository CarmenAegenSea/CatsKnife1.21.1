package com.catknife.action;

import com.google.gson.annotations.SerializedName;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * 空中攻击 — 在空中时行为不同
 */
public class AerialMelee extends MeleeAction {

    @SerializedName("ground_pound_radius")
    protected float groundPoundRadius = 4.0f;

    @SerializedName("ground_pound_damage")
    protected float groundPoundDamage = 8.0f;

    @SerializedName("ground_pound_knockback")
    protected float groundPoundKnockback = 1.5f;
3
    @SerializedName("fall_speed")
    protected double fallSpeed = 1.8;

    @SerializedName("aerial_angle")
    protected float aerialAngle = 120f;

    @SerializedName("aerial_range")
    protected float aerialRange = 3.5f;

    @SerializedName("aerial_damage_multiplier")
    protected float aerialDamageMultiplier = 1.3f;

    @SerializedName("require_airborne")
    protected boolean requireAirborne = false;

    @SerializedName("land_animation")
    protected String landAnimation = "ground_pound";

    private boolean wasAirborne = false;
    private boolean didGroundPound = false;
    private int aerialHits = 0;

    public AerialMelee() {
        this.name = "aerial";
        this.range = 2.5f;
        this.damage = 4f;
        this.cooldown = 0.3f;
    }

    @Override
    protected boolean canExecute(LivingEntity user) {
        if (!super.canExecute(user)) {
            return false;
        }
        if (requireAirborne && user.onGround()) {
            return false;
        }
        return true;
    }

    @Override
    protected List<LivingEntity> findTargets(LivingEntity user) {
        if (user.onGround()) {
            return findTargetsInCone(user, range, rangeAngle);
        } else {
            return findTargetsInSphere(user, aerialRange);
        }
    }

    @Override
    protected void onStart(LivingEntity user) {
        wasAirborne = !user.onGround();
        didGroundPound = false;
    }

    @Override
    protected void onHit(LivingEntity user, LivingEntity target) {
        if (wasAirborne) {
            aerialHits++;
            if (aerialHits <= 3) {
                target.setDeltaMovement(target.getDeltaMovement().x(), -0.3, target.getDeltaMovement().z());
                target.hurtMarked = true;
            }
        }
    }

    @Override
    protected void onEnd(LivingEntity user, MeleeActionResult result) {
        if (wasAirborne) {
            user.setDeltaMovement(user.getDeltaMovement().x(), -fallSpeed, user.getDeltaMovement().z());
            user.hurtMarked = true;
            result.damageDealt = damage * aerialDamageMultiplier * (1 + 0.2f * aerialHits);
        } else {
            result.damageDealt = damage;
        }
    }

    /**
     * 落地震击。应在玩家接触地面时调用此方法。
     */
    public MeleeActionResult doGroundPound(LivingEntity user) {
        MeleeActionResult result = new MeleeActionResult();
        if (didGroundPound) {
            result.success = false;
            result.cancelReason = "already_pounded";
            return result;
        }
        didGroundPound = true;

        float dmg = calculateDamage(user) * groundPoundDamage / damage;
        int hits = 0;
        List<LivingEntity> targets = findTargetsInSphere(user, groundPoundRadius);
        for (LivingEntity target : targets) {
            if (target == user || !target.isAlive() || target.isSpectator()) {
                continue;
            }
            double dist = target.distanceToSqr(user);
            float falloff = 1.0f - (float) (Math.sqrt(dist) / groundPoundRadius);
            if (falloff <= 0) {
                continue;
            }
            float actualDmg = dmg * falloff;
            float kb = groundPoundKnockback * falloff;
            target.knockback(kb,
                    (float) Math.sin(Math.toRadians(user.getYRot())),
                    (float) -Math.cos(Math.toRadians(user.getYRot())));
            if (user instanceof Player) {
                target.hurt(user.damageSources().playerAttack((Player) user), actualDmg);
            } else {
                target.hurt(user.damageSources().mobAttack(user), actualDmg);
            }
            hits++;
            result.damageDealt += actualDmg;
        }

        result.success = hits > 0;
        result.hits = hits;

        for (int i = 0; i < 8; i++) {
            double px = user.getX() + (user.getRandom().nextDouble() - 0.5) * groundPoundRadius * 2;
            double pz = user.getZ() + (user.getRandom().nextDouble() - 0.5) * groundPoundRadius * 2;
            double py = user.getY();
            user.level().addParticle(
                    net.minecraft.core.particles.ParticleTypes.EXPLOSION,
                    px, py, pz, 0, 0.5, 0);
        }

        if (user instanceof Player) {
            ((Player) user).causeFoodExhaustion(exhaustion * 1.5f);
        }
        return result;
    }

    // ── Getter ─────────────────────────────────────────

    public boolean isWasAirborne() {
        return wasAirborne;
    }

    public boolean isDidGroundPound() {
        return didGroundPound;
    }

    public int getAerialHits() {
        return aerialHits;
    }

    public float getGroundPoundRadius() {
        return groundPoundRadius;
    }

    public float getGroundPoundDamage() {
        return groundPoundDamage;
    }

    public float getAerialRange() {
        return aerialRange;
    }

    public float getAerialDamageMultiplier() {
        return aerialDamageMultiplier;
    }

    public boolean isRequireAirborne() {
        return requireAirborne;
    }

    // ── Builder ─────────────────────────────────────────

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final AerialMelee action = new AerialMelee();

        public Builder name(String v) { action.name = v; return this; }
        public Builder damage(float v) { action.damage = v; return this; }
        public Builder range(float v) { action.range = v; return this; }
        public Builder cooldown(float v) { action.cooldown = v; return this; }
        public Builder knockback(float v) { action.knockback = v; return this; }
        public Builder groundPoundRadius(float v) { action.groundPoundRadius = v; return this; }
        public Builder groundPoundDamage(float v) { action.groundPoundDamage = v; return this; }
        public Builder groundPoundKnockback(float v) { action.groundPoundKnockback = v; return this; }
        public Builder fallSpeed(double v) { action.fallSpeed = v; return this; }
        public Builder aerialRange(float v) { action.aerialRange = v; return this; }
        public Builder aerialDamageMultiplier(float v) { action.aerialDamageMultiplier = v; return this; }
        public Builder requireAirborne(boolean v) { action.requireAirborne = v; return this; }
        public Builder landAnimation(String v) { action.landAnimation = v; return this; }

        public AerialMelee build() {
            return action;
        }
    }
}