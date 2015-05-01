package me.zhilong.app;

import java.io.File;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;

/**
 * 类App.java的实现描述：根据DNA源文件,建立索引，进行缓存，然后搜索玩
 * 
 * @author 正纬 2015年5月1日 下午11:55:50
 * @version 1.4
 */
public class App {
    // 输入流
    private static Scanner                         scanner         = new Scanner(System.in);
    // 判断是否继续搜索
    private static boolean                         flag            = true;
    // 缓存DNA数据的索引
    private static Map<String, ArrayList<Integer>> dna_index_cache = new HashMap<String, ArrayList<Integer>>();
    // 当前索引行数
    private static int                             current_line    = 1;
    // 当前行DNA数据
    private static String                          current_dna_data;
    // 截止行
    private static int                             dead_line       = -1;
    // DNA分片大小
    private static int                             k;
    // 遍历进行索引时的游标
    private static int                             cursor          = 0;
    // DNA序列源文件
    private static String                          dna_data_file;
    // 查询结果保存的位置
    private static String                          result_file;
    // 子串的总数
    private static long                            mer_count;
    // 重复的子串总数
    private static long                            repeat_count;
    // 当前百分数
    private static String                          current_percent = "0.00%";

    // ------------- 计量参数 ------------
    // 最大内存
    private static final long                      MAX_MEM         = Runtime.getRuntime().maxMemory() / 1024 / 1024;
    // 时间 —— 起始时间
    private static long                            startTime;
    // 时间 -- 结束时间
    private static long                            stopTime;
    // 内存 —— 起始内存
    private static long                            startMem;
    // 内存 -- 结束内存
    private static long                            stopMem;

    public static void main(String[] args) {

        System.out.println(">>> JVM MaxMemory : " + MAX_MEM + "MB\n");

        try {

            // 读取DNA数据文件
            System.out.println(">> Input data file path:");
            dna_data_file = scanner.nextLine().trim();
            File dataFile = new File(dna_data_file);

            if (dataFile.exists()) {
                result_file = dna_data_file.substring(0, dna_data_file.lastIndexOf(File.separator)) + "/search_result";
            } else {
                System.out.println(">> No that file.");
                return;
            }

            // 读取DNA分片的大小
            System.out.println(">> Input K:");
            k = scanner.nextInt();

            // 读取准备处理的行数
            System.out.println(">> Enter the number of rows ready for processing");
            dead_line = scanner.nextInt();

            if (dead_line <= 0) {
                System.out.println(">> The number is less than zero");
                return;
            }

            System.out.print(">> Indexing... " + current_percent);

            // 清除上一行的 \n
            scanner.nextLine();

            // 使用 Apache common 的工具类遍历数据文件
            LineIterator iterator = FileUtils.lineIterator(dataFile, "UTF-8");

            startMem = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
            startTime = System.currentTimeMillis();

            while (iterator.hasNext()) {

                // 隔一行读一行，没办法源文件太逗比 
                iterator.next();
                // 读取DNA数据
                current_dna_data = iterator.next();

                for (cursor = 1; cursor <= 101 - k; cursor++) {
                    // 统计总数
                    mer_count++;

                    String subDna = current_dna_data.substring(cursor - 1, cursor - 1 + k);
                    if (dna_index_cache.get(subDna) == null) {
                        dna_index_cache.put(subDna, new ArrayList<Integer>() {
                            private static final long serialVersionUID = -6287991922699429774L;
                            {
                                this.add(current_line * 100 + cursor);
                            }
                        });
                    } else {
                        // 统计重复的因子
                        repeat_count++;

                        ArrayList<Integer> temp = dna_index_cache.get(subDna);
                        temp.add(current_line * 100 + cursor);
                        dna_index_cache.put(subDna, temp);
                    }
                }

                // 设置统计进度
                drawProcess(current_line, dead_line, 2);

                // 设置一个开关，可以仅跑部分测试数据
                if (dead_line == current_line) {
                    break;
                }
                current_line++;
            }

            stopTime = System.currentTimeMillis();
            stopMem = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

            System.out.println("\n>>>> Index finished, using times : " + (stopTime - startTime) + "ms, mems : "
                    + (stopMem - startMem) / 1024 / 1024 + "MB, repetition : " + getPercent(repeat_count, mer_count, 5)
                    + " (Measurement datas are only for reference.) <<<<\n");

            while (flag) {

                // 获取需要查找的子串
                System.out.println(">> Please enter the word to search:");
                String k_mer = scanner.nextLine().trim().toUpperCase();

                if (k_mer.equals("") || k_mer == null) {
                    flag = false;
                    return;
                } else {

                    System.out.println(">> Searching...");

                    List<Integer> result = dna_index_cache.get(k_mer);
                    if (result != null) {

                        String fileContent = "| ";

                        for (int key : result) {
                            fileContent += (key / 100 + ", " + key % 100 + ", " + (key % 100 + k) + " | ");
                        }

                        // 结果文件加上当前时间戳
                        String temp_result = result_file + "-" + System.currentTimeMillis();

                        System.out.println(">>> Search finished, Saving to file...");

                        //写入结果到文本文件 
                        FileUtils.writeStringToFile(new File(temp_result), fileContent, "UTF-8");

                        System.out.println("We found [" + result.size() + "] results.\nResults are saved in : "
                                + temp_result + "\n");
                    } else {
                        System.out.println(">> No result.\n");
                    }
                }
            }
        } catch (Exception e) {
            System.out.println(">> We have a problem." + e.toString());
        } finally {
            scanner.close();
        }
    }

    /**
     * 画出百分比
     * 
     * @param numerator
     * @param denominator
     * @param precision
     */
    public static void drawProcess(double numerator, double denominator, int precision) {
        for (int i = 0; i < current_percent.length(); i++) {
            System.out.print("\b");
        }
        current_percent = getPercent(current_line, dead_line, 2);
        System.out.print(current_percent);
    }

    /**
     * 获取当前百分数
     * 
     * @param numerator 分子
     * @param denominator 分母
     * @return
     */
    public static String getPercent(double numerator, double denominator, int precision) {
        double result = numerator / denominator;
        NumberFormat nf = NumberFormat.getPercentInstance();
        nf.setMinimumFractionDigits(precision);
        return nf.format(result);
    }
}
