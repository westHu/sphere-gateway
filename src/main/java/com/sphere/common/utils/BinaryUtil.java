package com.sphere.common.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * 二进制转换工具
 */
public class BinaryUtil {

    /**
     * 获取 二进制 中，出现 1 的位置（从右开始数）
     * 如：3 对应的二进制为 : 0011
     * 则，该方法返回 [1,2]
     *
     * @param number 十进制数
     * @return 出现 1 数字的位置
     */
    public static List<Integer> find1Cursor(int number) {
        if (number < 0)
            return new ArrayList<>();

        List<Integer> cursorList = new ArrayList<>();

        int cursor = 0;

        if (number == 0) {
            cursorList.add(cursor);
            return cursorList;
        }

        while (number != 0) {
            //移动坐标
            ++cursor;
            //如果低位二进制有 1 值，则将坐标保存到数组中
            if ((number & 1) == 1) {
                cursorList.add(cursor);
            }

            number >>= 1;
        }
        return cursorList;
    }

    /**
     * 将数组里面的数字对应至二进制 1 的位置（从右开始数）
     * 如：[1,3] 代表，二进制数中，第1的和第三的位置为 1，其他位置为 0，即：0101
     * 则 [1,3] 将会被转换为十进制数字：5
     *
     * @param numberList 数字列表
     * @return 二进制填充 1 后对应的十进制
     */
    public static int convert2Binary(List<Integer> numberList) {
        int number = 0;
        for (Integer cursor : numberList) {
            if (cursor == 0)
                number += 0;

            number += 1 << (cursor - 1);
        }
        return number;
    }
}
