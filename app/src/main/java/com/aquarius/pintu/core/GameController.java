package com.aquarius.pintu.core;

import com.aquarius.pintu.entity.ImageItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by aquarius on 2017/6/6.
 */
public class GameController {

    /**
     * 生成有解的随机的Item
     */
    public static void elementsGenerator(List<ImageItem> itemList, int column) {
        resetListElementsOrder(itemList);
        int blankPosition = getBlankItemPosition(itemList);

        List<Integer> data = new ArrayList<Integer>();
        for (int i = 0; i < itemList.size(); i++) {
            data.add(itemList.get(i).getIndex());
        }
        // 判断生成是否有解
        if (canSolve(data, blankPosition, column)) {
            return;
        } else {
            elementsGenerator(itemList, column);
        }
    }

    /**
     * 该数据是否有解
     */
    public static boolean canSolve(List<Integer> data, int blankPosition, int column) {

        // 可行性原则
        if (data.size() % 2 == 1) {
            return getInversions(data) % 2 == 0;
        } else {
            // 从底往上数,空格位于奇数行
            if (((blankPosition - 1) / column) % 2 == 1) {
                return getInversions(data) % 2 == 0;
            } else {
                // 从底往上数,空位位于偶数行
                return getInversions(data) % 2 == 1;
            }
        }
    }

    /**
     * 计算倒置和算法
     */
    public static int getInversions(List<Integer> data) {
        int inversions = 0;
        int inversionCount = 0;
        for (int i = 0; i < data.size(); i++) {
            for (int j = i + 1; j < data.size(); j++) {
                int index = data.get(i);
                if (data.get(j) != 0 && data.get(j) < index) {
                    inversionCount++;
                }
            }
            inversions += inversionCount;
            inversionCount = 0;
        }
        return inversions;
    }

    public static boolean isMoveable(int clickPosition, int blankCurrentPosition, int column) {
        // 不同行 相差为column
        if (Math.abs(blankCurrentPosition - clickPosition) == column) {
            return true;
        }
        // 相同行 相差为1
        if ((blankCurrentPosition / column == clickPosition / column) &&
                Math.abs(blankCurrentPosition - clickPosition) == 1) {
            return true;
        }
        return false;
    }


    public static boolean isSuccess(List<ImageItem> itemList) {
        if (itemList == null) {
            return false;
        }

        // 如果空白格不是位于最后一个 则一定是还没有拼图成功
        int blankIndex = getBlankItemPosition(itemList);
        int expectIndex = itemList.size() - 1;
        if (blankIndex != expectIndex) {
            return false;
        }else {
            for (int i = 0 ; i < itemList.size() -1 ; i++) {
               if(i != itemList.get(i).getIndex()) {
                   return false;
               }
            }
        }
        return true;
    }

    public static int getBlankItemPosition(List<ImageItem> itemList) {
        if (itemList == null) {
            return -1;
        }

        int position  = 0;
        for (ImageItem item : itemList) {
            if (item.isBlank() == true) {
                break;
            }
            position++;
        }
        return position;
    }

    /** 打乱列表中元素的顺序 */
    public static <T> void resetListElementsOrder(List<T> list) {
        if (list == null || list.size() < 2) {
            return;
        }

        /*
        Collections.sort(list, new Comparator<T>() {
            @Override
            public int compare(T o1, T o2) {
                int seed = (int) (Math.random() * 10);  // 用随机数的值，求余数
                return seed % 2 == 0 ? 1 : -1;
            }
        });
        */

        Collections.shuffle(list);  //洗牌算法 -> 实现乱序
    }

    private int minValue(int a, int b, int c, int d) {
        int min = a;
        if (min > b) {
            min = b;
        } else if (min > c) {
            min = c;
        } else if (min > d) {
            min = d;
        }
        return min;

    }
}
