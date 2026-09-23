package tfgirls.project.javarts.Model;

import tfgirls.project.javarts.Model.Building.Building;

import java.util.UUID;

/**
 * 游戏里的一个人，有唯一编号，被分配了房子和工作。
 *
 * ===== 改动：新加人物状态系统（数据层）=====
 * 在原来的「房子/工作」基础上，新加：
 *   - consecutiveWorkDays：连续干了几天活
 *   - fatigue：累不累（0~FATIGUE_MAX）
 *   - movementSpeed：走路速度（累了就变慢）
 * 连续干活超过 FATIGUE_THRESHOLD_DAYS 天就会越来越累；
 * 累到一定程度后，走路速度和干活效率（getEfficiency）都会下降，
 * 这样产出也就跟着少了。
 */
public class People {

    // ===== 累不累 / 状态常量 =====
    public static final int FATIGUE_PER_DAY = 25;            // 每干一天活累多少
    public static final int FATIGUE_RECOVERY_PER_DAY = 25;   // 每休息一天恢复多少
    public static final int FATIGUE_MAX = 100;               // 最累能累到多少
    public static final int FATIGUE_THRESHOLD = 50;          // 累到这个数就算「很累了」
    public static final double FATIGUED_MOVEMENT_SPEED = 0.5; // 累了走路速度变慢的倍率
    public static final double FATIGUED_EFFICIENCY = 0.5;     // 累了干活效率变低的倍率

    private UUID id;
    private Building house;
    private Building job;

    // ===== 改动：性别（渲染小人图片时用，男女居民 / 男女工人） =====
    public enum Gender {
        MALE,
        FEMALE
    }

    private final Gender gender;

    // ===== 改动：累不累 / 状态字段 =====
    private int consecutiveWorkDays = 0;
    private int fatigue = 0;
    private double movementSpeed = 1.0;

    public People() {
        id = UUID.randomUUID();
        // 随机性别，一半男一半女
        gender = (Math.random() < 0.5) ? Gender.MALE : Gender.FEMALE;
    }

    /** @return 这个人的性别（男 / 女） */
    public Gender getGender() {
        return gender;
    }

    public Building getHouse() {
        return house;
    }

    public Building getJobPlace() {
        return job;
    }


    /**
     * @param house 把哪个建筑分配给这个人住
     */
    public void affectHouse(Building house) {
        this.house = house;
    }

    /**
     * @param job 把哪个建筑分配给这个人上班
     */
    public void affectJobPlace(Building job) {
        this.job = job;
    }

    public UUID getId() {
        return id;
    }

    // ===== 改动：累不累 / 状态相关方法 =====

    /**
     * 过一天，更新这个人的疲劳状态。
     有活干 -> 连续干活天数 +1、更累了；
     没活干（休息）-> 连续干活天数清零、疲劳恢复。
     */
    public void advanceDay() {
        if (job != null) {
            consecutiveWorkDays++;
            fatigue = Math.min(FATIGUE_MAX, fatigue + FATIGUE_PER_DAY);
        } else {
            consecutiveWorkDays = 0;
            fatigue = Math.max(0, fatigue - FATIGUE_RECOVERY_PER_DAY);
        }
        updateMovementSpeed();
    }

    private void updateMovementSpeed() {
        this.movementSpeed = isFatigued() ? FATIGUED_MOVEMENT_SPEED : 1.0;
    }

    /** @return 是不是太累了（累到这个数就触发了） */
    public boolean isFatigued() {
        return fatigue >= FATIGUE_THRESHOLD;
    }

    /** @return 当前累不累的程度（0~100） */
    public int getFatigue() {
        return fatigue;
    }

    /** @return 连续干了几天活 */
    public int getConsecutiveWorkDays() {
        return consecutiveWorkDays;
    }

    /** @return 当前走路速度倍率（累了就变成 0.5） */
    public double getMovementSpeed() {
        return movementSpeed;
    }

    /**
     * @return 这个人的干活效率倍率。累了是 0.5，不累是 1.0。
     *         算建筑产出的时候按工人效率来算，
     *         跟 GameManager 的饿肚子效率乘在一起。
     */
    public double getEfficiency() {
        return isFatigued() ? FATIGUED_EFFICIENCY : 1.0;
    }
}
