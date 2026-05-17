package com.yyyouth.service.aichat.agent.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 计算器工具类
 *
 * @author yyyouth zg
 * @date 2026-05-07
 *
 * 提供基础数学运算能力，供 AI 代理在需要精确计算时调用
 */
public class CalculatorTools {

    /**
     * 加法运算
     */
    @Tool(description = "计算两个数字的和")
    public BigDecimal add(
        @ToolParam(description = "第一个加数") BigDecimal a,
        @ToolParam(description = "第二个加数") BigDecimal b) {
        return a.add(b);
    }

    /**
     * 减法运算
     */
    @Tool(description = "计算两个数字的差（第一个数减去第二个数）")
    public BigDecimal subtract(
        @ToolParam(description = "被减数") BigDecimal a,
        @ToolParam(description = "减数") BigDecimal b) {
        return a.subtract(b);
    }

    /**
     * 乘法运算
     */
    @Tool(description = "计算两个数字的乘积")
    public BigDecimal multiply(
        @ToolParam(description = "第一个乘数") BigDecimal a,
        @ToolParam(description = "第二个乘数") BigDecimal b) {
        return a.multiply(b);
    }

    /**
     * 除法运算
     */
    @Tool(description = "计算两个数字的商（第一个数除以第二个数），默认保留10位小数")
    public BigDecimal divide(
        @ToolParam(description = "被除数") BigDecimal a,
        @ToolParam(description = "除数") BigDecimal b) {
        return a.divide(b, 10, RoundingMode.HALF_UP);
    }

    /**
     * 幂运算
     */
    @Tool(description = "计算一个数的指定次幂")
    public BigDecimal pow(
        @ToolParam(description = "底数") BigDecimal base,
        @ToolParam(description = "指数") int exponent) {
        return base.pow(exponent);
    }

    /**
     * 开平方运算
     */
    @Tool(description = "计算一个数的平方根")
    public double sqrt(
        @ToolParam(description = "被开方数，必须大于等于0") double value) {
        return Math.sqrt(value);
    }

    /**
     * 取模运算
     */
    @Tool(description = "计算两个数字的模（第一个数对第二个数取余）")
    public BigDecimal mod(
        @ToolParam(description = "被除数") BigDecimal a,
        @ToolParam(description = "除数") BigDecimal b) {
        return a.remainder(b);
    }

    /**
     * 绝对值
     */
    @Tool(description = "计算一个数字的绝对值")
    public BigDecimal abs(
        @ToolParam(description = "目标数字") BigDecimal value) {
        return value.abs();
    }

    /**
     * 四舍五入
     */
    @Tool(description = "将一个数字按指定小数位进行四舍五入")
    public BigDecimal round(
        @ToolParam(description = "目标数字") BigDecimal value,
        @ToolParam(description = "保留的小数位数") int scale) {
        return value.setScale(scale, RoundingMode.HALF_UP);
    }

    /**
     * 计算平均值
     */
    @Tool(description = "计算一组数字的平均值")
    public BigDecimal average(
        @ToolParam(description = "数字数组") double[] numbers) {
        if (numbers == null || numbers.length == 0) {
            throw new IllegalArgumentException("数字数组不能为空");
        }
        double sum = 0;
        for (double num : numbers) {
            sum += num;
        }
        return BigDecimal.valueOf(sum / numbers.length);
    }

    /**
     * 计算最大值
     */
    @Tool(description = "找出一组数字中的最大值")
    public double max(
        @ToolParam(description = "数字数组") double[] numbers) {
        if (numbers == null || numbers.length == 0) {
            throw new IllegalArgumentException("数字数组不能为空");
        }
        double max = numbers[0];
        for (int i = 1; i < numbers.length; i++) {
            if (numbers[i] > max) {
                max = numbers[i];
            }
        }
        return max;
    }

    /**
     * 计算最小值
     */
    @Tool(description = "找出一组数字中的最小值")
    public double min(
        @ToolParam(description = "数字数组") double[] numbers) {
        if (numbers == null || numbers.length == 0) {
            throw new IllegalArgumentException("数字数组不能为空");
        }
        double min = numbers[0];
        for (int i = 1; i < numbers.length; i++) {
            if (numbers[i] < min) {
                min = numbers[i];
            }
        }
        return min;
    }
}
