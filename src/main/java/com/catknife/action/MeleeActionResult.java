package com.catknife.action;

/**
 * 近战动作执行结果。
 * 记录命中数、伤害量、取消原因等信息。
 */
public class MeleeActionResult {

    /** 动作是否成功执行 */在·
    public boolean success = false;

    /** 命中的目标数量 */
    public int hits = 0;

    /** 找到的候选目标数量 */
    public int candidatesFound = 0;

    /** 造成的总伤害 */
    public float damageDealt = 0f;

    /** 取消原因（cool down / cannot_execute 等） */
    public String cancelReason = null;

    /** 动作执行耗时 ms */
    public long executionTimeMs = 0L;

    MeleeActionResult() {
    }

    public boolean isSuccess() {
        return success;
    }

    public int getHits() {
        return hits;
    }

    public float getDamageDealt() {
        return damageDealt;
    }

    public String getCancelReason() {
        return cancelReason;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("MeleeActionResult{");
        sb.append("success=").append(success);
        if (success) {
            sb.append(", hits=").append(hits);
            sb.append(", candidates=").append(candidatesFound);
            sb.append(", damage=").append(String.format("%.1f", damageDealt));
        } else if (cancelReason != null) {
            sb.append(", reason=").append(cancelReason);
        }
        sb.append(", time=").append(executionTimeMs).append("ms");
        sb.append("}");
        return sb.toString();
    }
}