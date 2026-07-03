package com.catknife.action;

import com.google.gson.annotations.SerializedName;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * 连击动作 — 多段攻击链。
 * <p>
 * 每次调用 execute() 推进一段连击，段数用完后重置。
 * 每段可以有不同的伤害、距离和击退。
 * 如果在连击窗口内未执行，连击自动重置。
 */
public class ComboMelee extends MeleeAction {

    /**
     * 连击段配置。
     */
    public static class ComboStage {
        @SerializedName("damage")
        public float damage = 4f;

        @SerializedName("range")
        public float range = 2.5f;

        @SerializedName("knockback")
        public float knockback = 0.5f;

        @SerializedName("range_angle")
        public float rangeAngle = 45f;

        @SerializedName("animation")
        public String animation = "combo_1";

        public ComboStage() {
        }

        public ComboStage(float damage, float range, float knockback, float rangeAngle, String animation) {
            this.damage = damage;
            this.range = range;
            this.knockback = knockback;
            this.rangeAngle = rangeAngle;
            this.animation = animation;
        }
    }

    // ── 字段 ─────────────────────────────────────────────

    @SerializedName("stages")
    protected List<ComboStage> stages = new ArrayList<>();

    @SerializedName("combo_window")
    protected long comboWindowMs = 800L;

    @SerializedName("reset_on_miss")
    protected boolean resetOnMiss = true;

    @SerializedName("final_stage_multiplier")
    protected float finalStageMultiplier = 2.0f;

    private int currentStage = 0;
    private long lastComboTime = 0L;
    private int totalComboHits = 0;

    // ── 构造 ─────────────────────────────────────────────

    public ComboMelee() {
        this.name = "combo";
        this.cooldown = 0.1f;
        this.damage = 3f;

        stages.add(new ComboStage(3f, 2.0f, 0.3f, 40f, "combo_1"));
        stages.add(new ComboStage(4f, 2.3f, 0.4f, 45f, "combo_2"));
        stages.add(new ComboStage(6f, 2.8f, 0.7f, 50f, "combo_3"));
    }

    // ── 模板方法重写 ──────────────────────────────────────

    @Override
    protected boolean canExecute(LivingEntity user) {
        checkComboTimeout();
        if (!super.canExecute(user)) {
            return false;
        }
        if (stages.isEmpty()) {
            return false;
        }
        return true;
    }

    @Override
    protected List<LivingEntity> findTargets(LivingEntity user) {
        ComboStage stage = getCurrentStage();
        if (stage == null) {
            return new ArrayList<>();
        }
        return findTargetsInCone(user, stage.range, stage.rangeAngle);
    }

    @Override
    protected void onStart(LivingEntity user) {
        lastComboTime = System.currentTimeMillis();
    }

    @Override
    protected void onHit(LivingEntity user, LivingEntity target) {
        totalComboHits++;
        if (user instanceof Player) {
            Player p = (Player) user;
            p.setDeltaMovement(p.getDeltaMovement().x(), 0.1, p.getDeltaMovement().z());
            p.hurtMarked = true;
        }
    }

    @Override
    protected void onEnd(LivingEntity user, MeleeActionResult result) {
        if (result.success) {
            advanceStage();
        } else if (resetOnMiss) {
            resetCombo();
        }
        result.damageDealt = damage;
    }

    // ── 连击管理 ─────────────────────────────────────────

    /**
     * 获取当前段配置。
     */
    public ComboStage getCurrentStage() {
        if (stages.isEmpty()) {
            return null;
        }
        if (currentStage >= stages.size()) {
            currentStage = stages.size() - 1;
        }
        ComboStage stage = stages.get(currentStage);

        // 动态覆盖当前属性
        this.damage = stage.damage;
        this.range = stage.range;
        this.knockback = stage.knockback;
        this.rangeAngle = stage.rangeAngle;

        if (isFinalStage()) {
            this.damage = stage.damage * finalStageMultiplier;
            this.knockback = stage.knockback * 1.5f;
        }

        return stage;
    }

    /**
     * 推进到下一段。
     */
    public void advanceStage() {
        if (currentStage < stages.size() - 1) {
            currentStage++;
        }
    }

    /**
     * 重置连击。
     */
    public void resetCombo() {
        currentStage = 0;
        totalComboHits = 0;
    }

    /**
     * 检查连击窗口是否超时，超时则重置。
     */
    public void checkComboTimeout() {
        long now = System.currentTimeMillis();
        if (currentStage > 0 && (now - lastComboTime) > comboWindowMs) {
            resetCombo();
        }
    }

    /**
     * 是否是最后一段。
     */
    public boolean isFinalStage() {
        return currentStage == stages.size() - 1;
    }

    /**
     * 当前段数（从 0 开始）。
     */
    public int getCurrentStageIndex() {
        return currentStage;
    }

    /**
     * 总段数。
     */
    public int getTotalStages() {
        return stages.size();
    }

    /**
     * 连击总命中数。
     */
    public int getTotalComboHits() {
        return totalComboHits;
    }

    /**
     * 连击窗口剩余时间 ms，已超时返回 0。
     */
    public long getRemainingWindowTime() {
        long elapsed = System.currentTimeMillis() - lastComboTime;
        return Math.max(0, comboWindowMs - elapsed);
    }

    /**
     * 连击进度 0~1。
     */
    public float getComboProgress() {
        if (stages.isEmpty()) {
            return 0f;
        }
        return (float) currentStage / (stages.size() - 1);
    }

    // ── Getter ─────────────────────────────────────────

    public long getComboWindowMs() {
        return comboWindowMs;
    }

    public boolean isResetOnMiss() {
        return resetOnMiss;
    }

    public float getFinalStageMultiplier() {
        return finalStageMultiplier;
    }

    public List<ComboStage> getStages() {
        return new ArrayList<>(stages);
    }

    // ── Builder ─────────────────────────────────────────

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final ComboMelee action = new ComboMelee();

        public Builder name(String v) { action.name = v; return this; }
        public Builder cooldown(float v) { action.cooldown = v; return this; }
        public Builder exhaustion(float v) { action.exhaustion = v; return this; }
        public Builder comboWindow(long ms) { action.comboWindowMs = ms; return this; }
        public Builder resetOnMiss(boolean v) { action.resetOnMiss = v; return this; }
        public Builder finalStageMultiplier(float v) { action.finalStageMultiplier = v; return this; }

        public Builder addStage(float damage, float range, float knockback, float angle, String anim) {
            action.stages.add(new ComboStage(damage, range, knockback, angle, anim));
            return this;
        }

        public Builder addStage(ComboStage stage) {
            action.stages.add(stage);
            return this;
        }

        public ComboMelee build() {
            if (action.stages.isEmpty()) {
                action.stages.add(new ComboStage(3f, 2f, 0.3f, 40f, "combo_1"));
                action.stages.add(new ComboStage(5f, 2.5f, 0.5f, 45f, "combo_2"));
            }
            return action;
        }
    }
}