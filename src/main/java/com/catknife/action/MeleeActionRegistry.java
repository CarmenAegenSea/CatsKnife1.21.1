package com.catknife.action;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * 近战动作注册中心。
 * <p>
 * 动作可以通过代码注册，也可以从 JSON 文件反序列化注册。
 * 提供按名称查找、批量执行、统计等功能。
 */
public class MeleeActionRegistry {

    private static MeleeActionRegistry instance;

    private final Map<String, MeleeAction> actions = new HashMap<>();
    private final Map<String, Class<? extends MeleeAction>> typeMap = new HashMap<>();

    private int totalExecutions = 0;
    private int totalHits = 0;

    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    public MeleeActionRegistry() {
        registerBuiltinTypes();
    }


    public static MeleeActionRegistry getInstance() {
        if (instance == null) {
            instance = new MeleeActionRegistry();
        }
        return instance;
    }

    public static void setInstance(MeleeActionRegistry registry) {
        instance = registry;
    }

    private void registerBuiltinTypes() {
        typeMap.put("slash", SlashMelee.class);
        typeMap.put("combo", ComboMelee.class);
    }

    /**
     * 注册一个动作。如果同名已存在则覆盖。
     */
    public void register(MeleeAction action) {
        if (action == null) {
            return;
        }
        String key = action.getName();
        if (key == null || key.isEmpty()) {
            return;
        }
        actions.put(key, action);
    }

    /**
     * 批量注册。
     */
    public void registerAll(MeleeAction... actions) {
        for (MeleeAction action : actions) {
            register(action);
        }
    }

    /**
     * 反注册。
     */
    public MeleeAction unregister(String name) {
        return actions.remove(name);
    }

    /**
     * 按名称获取动作。
     */
    public MeleeAction get(String name) {
        return actions.get(name);
    }

    /**
     * 是否存在。
     */
    public boolean has(String name) {
        return actions.containsKey(name);
    }

    /**
     * 获取所有注册的动作名称。
     */
    public Collection<String> getActionNames() {
        return actions.keySet();
    }

    /**
     * 获取所有注册的动作。
     */
    public Collection<MeleeAction> getActions() {
        return actions.values();
    }

    /**
     * 注册总数。
     */
    public int size() {
        return actions.size();
    }

    /**
     * 按名称执行动作。
     *
     * @param name 动作名称
     * @param user 攻击者
     * @return 执行结果，未找到动作返回失败结果
     */
    public MeleeActionResult execute(String name, net.minecraft.world.entity.LivingEntity user) {
        MeleeAction action = get(name);
        if (action == null) {
            MeleeActionResult result = new MeleeActionResult();
            result.success = false;
            result.cancelReason = "not_found";
            return result;
        }
        return execute(action, user);
    }

    /**
     * 执行动作并统计。
     */
    public MeleeActionResult execute(MeleeAction action, net.minecraft.world.entity.LivingEntity user) {
        MeleeActionResult result = action.execute(user);
        totalExecutions++;
        totalHits += result.hits;
        return result;
    }

    /**
     * 从 JSON 对象加载动作配置。
     */
    public MeleeAction loadFromJson(JsonObject json) {
        if (json == null) {
            return null;
        }

        JsonElement typeElem = json.get("type");
        if (typeElem == null || !typeElem.isJsonPrimitive()) {
            return null;
        }
        String type = typeElem.getAsString();

        Class<? extends MeleeAction> clazz = typeMap.get(type);
        if (clazz == null) {
            return null;
        }

        MeleeAction action = GSON.fromJson(json, clazz);
        if (action != null) {
            register(action);
        }
        return action;
    }

    /**
     * 从 JSON 文件加载。
     */
    public MeleeAction loadFromFile(File file) {
        if (file == null || !file.exists() || !file.isFile()) {
            return null;
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            JsonObject json = GSON.fromJson(reader, JsonObject.class);
            return loadFromJson(json);
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * 从 JSON 字符串加载。
     */
    public MeleeAction loadFromString(String jsonStr) {
        try {
            JsonObject json = GSON.fromJson(jsonStr, JsonObject.class);
            return loadFromJson(json);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 注册自定义类型映射（用于 JSON 反序列化）。
     */
    public void registerType(String typeName, Class<? extends MeleeAction> clazz) {
        if (typeName != null && clazz != null) {
            typeMap.put(typeName, clazz);
        }
    }

    /**
     * 清除所有注册。
     */
    public void clear() {
        actions.clear();
    }

    /**
     * 获取总执行次数。
     */
    public int getTotalExecutions() {
        return totalExecutions;
    }

    /**
     * 获取总命中次数。
     */
    public int getTotalHits() {
        return totalHits;
    }

    /**
     * 重置统计。
     */
    public void resetStatistics() {
        totalExecutions = 0;
        totalHits = 0;
    }

    /**
     * 生成统计报告。
     */
    public String getStatisticsReport() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== MeleeActionRegistry Report ===\n");
        sb.append("Registered actions: ").append(actions.size()).append("\n");
        sb.append("Total executions: ").append(totalExecutions).append("\n");
        sb.append("Total hits: ").append(totalHits).append("\n");
        sb.append("Hit ratio: ");
        if (totalExecutions > 0) {
            sb.append(String.format("%.1f%%", (totalHits * 100f / totalExecutions)));
        } else {
            sb.append("N/A");
        }
        sb.append("\n");
        sb.append("Actions:\n");
        for (Map.Entry<String, MeleeAction> entry : actions.entrySet()) {
            MeleeAction action = entry.getValue();
            sb.append("  - ").append(entry.getKey());
            sb.append(" (dmg=").append(action.getDamage());
            sb.append(", range=").append(action.getRange());
            sb.append(", cd=").append(action.getCooldown()).append(")\n");
        }
        sb.append("===================================");
        return sb.toString();
    }
}