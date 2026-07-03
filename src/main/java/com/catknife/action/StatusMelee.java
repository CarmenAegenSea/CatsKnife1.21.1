package com.catknife.action;

import com.google.gson.annotations.SerializedName;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * 状态攻击 — 命中后给目标施加药水效果
 * 支持多个效果同时施加，每个效果可单独配置时长和等级
 * 支持"命中时给自己加 buff"和"命中时给目标加 debuff"两种模式
 */
public class StatusMelee extends MeleeAction {
    public static class EffectConfig {
        @SerializedName("effect_id")
        public String effectId = "minecraft:slowness";

        @SerializedName("duration")
        public int duration = 100;

        @SerializedName("amplifier")
        public int amplifier = 0;

        @SerializedName("apply_to_self")
        public boolean applyToSelf = false;

        @SerializedName("chance")
        public float chance = 1.0f;

        @SerializedName("stackable")
        public boolean stackable = false;

        public EffectConfig() {
        }

        public EffectConfig(String effectId, int duration, int amplifier) {
            this.effectId = effectId;
            this.duration = duration;
            this.amplifier = amplifier;
        }
    }

    @SerializedName("effects")
    protected List<EffectConfig> effects = new ArrayList<>();

    @SerializedName("on_critical_effects")
    protected List<EffectConfig> criticalEffects = new ArrayList<>();

    @SerializedName("critical_chance")
    protected float criticalChance = 0.15f;

    @SerializedName("critical_damage_multiplier")
    protected float criticalDamageMultiplier = 1.5f;

    @SerializedName("effect_duration_reduction")
    protected float effectDurationReduction = 0f;

    private boolean lastHitWasCritical = false;

    public StatusMelee() {
        this.name = "status";
        this.damage = 3f;
        this.cooldown = 0.5f;

        effects.add(new EffectConfig("minecraft:slowness", 100, 0));
        effects.add(new EffectConfig("minecraft:weakness", 60, 0));
    }

    @Override
    protected List<LivingEntity> findTargets(LivingEntity user) {
        return findTargetsInCone(user, range, rangeAngle);
    }

    @Override
    protected void onHit(LivingEntity user, LivingEntity target) {
        lastHitWasCritical = user.getRandom().nextFloat() < criticalChance;
        float actualDamage = damage;
        if (lastHitWasCritical) {
            actualDamage *= criticalDamageMultiplier;
        }

        if (!user.level().isClientSide()) {
            List<EffectConfig> activeEffects = lastHitWasCritical ? criticalEffects : effects;
            if (activeEffects.isEmpty()) {
                activeEffects = effects;
            }

            for (EffectConfig cfg : activeEffects) {
                if (user.getRandom().nextFloat() > cfg.chance) {
                    continue;
                }
                int duration = cfg.duration;
                if (effectDurationReduction > 0 && !cfg.stackable) {
                    duration = (int) (duration * (1.0f - effectDurationReduction));
                }
                if (duration <= 0) {
                    continue;
                }

                Holder<MobEffect> holder = BuiltInRegistries.MOB_EFFECT.getHolder(
                        net.minecraft.resources.ResourceLocation.parse(cfg.effectId)).orElse(null);
                if (holder == null) {
                    continue;
                }
                MobEffectInstance instance = new MobEffectInstance(holder, duration, cfg.amplifier);

                if (cfg.applyToSelf) {
                    user.addEffect(instance);
                } else {
                    target.addEffect(instance);
                }
            }
        }

        if (lastHitWasCritical) {
            target.setDeltaMovement(target.getDeltaMovement().x(), 0.3, target.getDeltaMovement().z());
            target.hurtMarked = true;
        }
    }

    @Override
    protected void onEnd(LivingEntity user, MeleeActionResult result) {
        if (lastHitWasCritical) {
            result.damageDealt = damage * criticalDamageMultiplier;
        } else {
            result.damageDealt = damage;
        }
    }

    @Override
    protected float calculateDamage(LivingEntity user) {
        float base = super.calculateDamage(user);
        if (lastHitWasCritical) {
            return base * criticalDamageMultiplier;
        }
        return base;
    }

    // ── 公共方法 ─────────────────────────────────────────

    /**
     * 添加效果。
     */
    public void addEffect(String effectId, int duration, int amplifier) {
        effects.add(new EffectConfig(effectId, duration, amplifier));
    }

    /**
     * 添加暴击效果。
     */
    public void addCriticalEffect(String effectId, int duration, int amplifier) {
        criticalEffects.add(new EffectConfig(effectId, duration, amplifier));
    }

    /**
     * 添加给自己加 buff 的效果。
     */
    public void addSelfEffect(String effectId, int duration, int amplifier) {
        EffectConfig cfg = new EffectConfig(effectId, duration, amplifier);
        cfg.applyToSelf = true;
        effects.add(cfg);
    }

    /**
     * 最后一次命中是否为暴击。
     */
    public boolean wasLastHitCritical() {
        return lastHitWasCritical;
    }

    /**
     * 获取效果列表。
     */
    public List<EffectConfig> getEffects() {
        return new ArrayList<>(effects);
    }

    /**
     * 获取暴击效果列表。
     */
    public List<EffectConfig> getCriticalEffects() {
        return new ArrayList<>(criticalEffects);
    }

    // ── Getter ─────────────────────────────────────────

    public float getCriticalChance() {
        return criticalChance;
    }

    public float getCriticalDamageMultiplier() {
        return criticalDamageMultiplier;
    }

    // ── Builder ─────────────────────────────────────────

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final StatusMelee action = new StatusMelee();

        public Builder name(String v) { action.name = v; return this; }
        public Builder damage(float v) { action.damage = v; return this; }
        public Builder range(float v) { action.range = v; return this; }
        public Builder rangeAngle(float v) { action.rangeAngle = v; return this; }
        public Builder cooldown(float v) { action.cooldown = v; return this; }
        public Builder knockback(float v) { action.knockback = v; return this; }
        public Builder criticalChance(float v) { action.criticalChance = v; return this; }
        public Builder criticalDamageMultiplier(float v) { action.criticalDamageMultiplier = v; return this; }
        public Builder effectDurationReduction(float v) { action.effectDurationReduction = v; return this; }

        public Builder addEffect(String id, int duration, int amp) {
            action.effects.add(new EffectConfig(id, duration, amp));
            return this;
        }

        public Builder addCriticalEffect(String id, int duration, int amp) {
            action.criticalEffects.add(new EffectConfig(id, duration, amp));
            return this;
        }

        public Builder addSelfEffect(String id, int duration, int amp) {
            EffectConfig cfg = new EffectConfig(id, duration, amp);
            cfg.applyToSelf = true;
            action.effects.add(cfg);
            return this;
        }

        public StatusMelee build() {
            if (action.effects.isEmpty()) {
                action.effects.add(new EffectConfig("minecraft:slowness", 100, 0));
            }
            return action;
        }
    }
}