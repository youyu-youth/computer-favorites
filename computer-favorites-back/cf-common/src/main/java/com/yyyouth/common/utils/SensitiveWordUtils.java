package com.yyyouth.common.utils;

import com.github.houbb.sensitive.word.bs.SensitiveWordBs;
import com.github.houbb.sensitive.word.support.allow.WordAllows;
import com.github.houbb.sensitive.word.support.deny.WordDenys;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * 敏感词检测工具类
 * 基于 sensitive-word 框架实现
 * 
 * @author yyyouth zg
 * @date 2025-12-12
 */
@Slf4j
public class SensitiveWordUtils {

    /**
     * 敏感词检测实例（单例）
     */
    private static volatile SensitiveWordBs sensitiveWordBs;

    /**
     * 获取敏感词检测实例
     * 使用双重检查锁定确保线程安全
     */
    private static SensitiveWordBs getInstance() {
        if (sensitiveWordBs == null) {
            synchronized (SensitiveWordUtils.class) {
                if (sensitiveWordBs == null) {
                    sensitiveWordBs = SensitiveWordBs.newInstance()
                            // 忽略大小写
                            .ignoreCase(true)
                            // 忽略全角半角
                            .ignoreWidth(true)
                            // 忽略数字样式
                            .ignoreNumStyle(true)
                            // 忽略繁简体
                            .ignoreChineseStyle(true)
                            // 忽略英文样式
                            .ignoreEnglishStyle(true)
                            // 忽略重复词
                            .ignoreRepeat(true)
                            // 启用数字检测（手机号等）
                            .enableNumCheck(true)
                            // 数字检测长度（11位手机号）
                            .numCheckLen(11)
                            // 启用邮箱检测
                            .enableEmailCheck(true)
                            // 启用URL检测
                            .enableUrlCheck(true)
                            // 使用系统默认词库
                            .wordDeny(WordDenys.defaults())
                            .wordAllow(WordAllows.defaults())
                            .init();
                }
            }
        }
        return sensitiveWordBs;
    }

    /**
     * 判断文本是否包含敏感词
     * 
     * @param text 待检测文本
     * @return true-包含敏感词，false-不包含
     */
    public static boolean contains(String text) {
        if (StringUtils.isEmpty(text)) {
            return false;
        }
        return getInstance().contains(text);
    }

    /**
     * 获取文本中的第一个敏感词
     * 
     * @param text 待检测文本
     * @return 第一个敏感词，如果没有则返回null
     */
    public static String findFirst(String text) {
        if (StringUtils.isEmpty(text)) {
            return null;
        }
        return getInstance().findFirst(text);
    }

    /**
     * 获取文本中的所有敏感词
     * 
     * @param text 待检测文本
     * @return 敏感词列表
     */
    public static List<String> findAll(String text) {
        if (StringUtils.isEmpty(text)) {
            return List.of();
        }
        return getInstance().findAll(text);
    }

    /**
     * 替换文本中的敏感词（使用*替换）
     * 
     * @param text 待处理文本
     * @return 替换后的文本
     */
    public static String replace(String text) {
        if (StringUtils.isEmpty(text)) {
            return text;
        }
        return getInstance().replace(text);
    }

    /**
     * 替换文本中的敏感词（使用指定字符替换）
     * 
     * @param text 待处理文本
     * @param replaceChar 替换字符
     * @return 替换后的文本
     */
    public static String replace(String text, char replaceChar) {
        if (StringUtils.isEmpty(text)) {
            return text;
        }
        // 使用字符串构建替换后的文本
        String result = text;
        List<String> sensitiveWords = getInstance().findAll(text);
        for (String word : sensitiveWords) {
            String replacement = String.valueOf(replaceChar).repeat(word.length());
            result = result.replace(word, replacement);
        }
        return result;
    }

    /**
     * 动态添加敏感词
     * 
     * @param word 敏感词
     */
    public static void addWord(String word) {
        if (StringUtils.isNotEmpty(word)) {
            getInstance().addWord(word);
            log.info("动态添加敏感词: {}", word);
        }
    }

    /**
     * 动态添加敏感词（批量）
     * 
     * @param words 敏感词列表
     */
    public static void addWords(List<String> words) {
        if (words != null && !words.isEmpty()) {
            getInstance().addWord(words);
            log.info("动态批量添加敏感词，数量: {}", words.size());
        }
    }

    /**
     * 动态删除敏感词
     * 
     * @param word 敏感词
     */
    public static void removeWord(String word) {
        if (StringUtils.isNotEmpty(word)) {
            getInstance().removeWord(word);
            log.info("动态删除敏感词: {}", word);
        }
    }

    /**
     * 动态添加白名单词
     * 
     * @param word 白名单词
     */
    public static void addWordAllow(String word) {
        if (StringUtils.isNotEmpty(word)) {
            getInstance().addWordAllow(word);
            log.info("动态添加白名单词: {}", word);
        }
    }

    /**
     * 动态删除白名单词
     * 
     * @param word 白名单词
     */
    public static void removeWordAllow(String word) {
        if (StringUtils.isNotEmpty(word)) {
            getInstance().removeWordAllow(word);
            log.info("动态删除白名单词: {}", word);
        }
    }
}
