package me.zhilong.app;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;

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
    // 统计时间 —— 起始时间
    private static long                            startTime;
    // 统计实践 -- 结束时间
    private static long                            stopTime;
    // 遍历进行索引时的游标
    private static int                             cursor          = 0;

    public static void main(String[] args) {

        try {

            // 读取DNA数据文件
            System.out.println(">> Input data file path:");
            File dataFile = new File(scanner.nextLine());

            // 读取DNA分片的大小
            System.out.println(">> Input K:");
            k = scanner.nextInt();

            // 读取准备处理的行数
            System.out.println(">> Enter the number of rows ready for processing");
            dead_line = scanner.nextInt();

            // 清除上一行的 \n
            scanner.nextLine();

            // 使用 Apache common 的工具类遍历数据文件
            LineIterator iterator = FileUtils.lineIterator(dataFile, "UTF-8");

            startTime = System.currentTimeMillis();

            while (iterator.hasNext()) {

                // 隔一行读一行，没办法源文件太逗比 
                iterator.next();
                // 读取DNA数据
                current_dna_data = iterator.next();

                for (cursor = 1; cursor <= 101 - k; cursor++) {
                    String subDna = current_dna_data.substring(cursor - 1, cursor - 1 + k);
                    if (dna_index_cache.get(subDna) == null) {
                        dna_index_cache.put(subDna, new ArrayList<Integer>() {
                            private static final long serialVersionUID = -6287991922699429774L;
                            {
                                this.add(current_line * 100 + cursor);
                            }
                        });
                    } else {
                        ArrayList<Integer> temp = dna_index_cache.get(subDna);
                        temp.add(current_line * 100 + cursor);
                        dna_index_cache.put(subDna, temp);
                    }
                }

                // 设置一个开关，可以仅跑部分测试数据
                if (dead_line == current_line) {
                    break;
                }

                current_line++;
            }

            stopTime = System.currentTimeMillis();

            System.out.println("\n>>>> Index finished, using times : " + (stopTime - startTime) + "ms <<<<\n");

            while (flag) {

                // 获取需要查找的子串
                System.out.println(">> Input the key which you want to search.");
                String k_mer = scanner.nextLine();

                if (k_mer.equals("") || k_mer == null) {
                    flag = false;
                    return;
                } else {

                    startTime = System.currentTimeMillis();

                    List<Integer> result = dna_index_cache.get(k_mer);
                    if (result != null) {
                        System.out.print("There is [" + result.size() + "] matched, they are : [");
                        for (int key : result) {
                            System.out.print(key / 100 + "," + key % 100 + "," + (key % 100 + k) + " | ");
                        }
                        System.out.println("]");
                    } else {
                        System.out.println(">> No result.");
                    }

                    stopTime = System.currentTimeMillis();

                    System.out.println("\n>>>> Search finished, using times : " + (stopTime - startTime) + "ms <<<<\n");
                }
            }

        } catch (Exception e) {
            System.out.println(">> We have a problem.");
            System.out.println(e.toString());
        } finally {
            scanner.close();
        }
    }
}
