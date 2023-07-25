package com.kwai.upload.demo.util;

import android.view.DragEvent;
import android.view.MotionEvent;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by 周智慧 on 2022/11/30.
 */

public class StringUtil {
    private static Pattern PATTERN_DIGIT = Pattern.compile("[+-]?[0-9]+(.[0-9]+)?");
    public static String getDateStr(long times) {
        int day = 0; // (int) (times / 1000 / 60 / 60 / 24);
        // int dayRemainder = (int) (times - (day * 1000 * 60 * 60 * 24));

        int hour = 0; // (int) (times / 1000 / 60 / 60);
        // int hourRemainder = (int) (times - (hour * 1000 * 60 * 60));

        int minute = (int) (times / 1000 / 60);
        int minuteRemainder = (int) (times - (minute * 1000 * 60));

        int second = minuteRemainder / 1000;

        String timeStr = "00";
        if (day != 0) {
            timeStr = timeStr + day + "天";
        }
        if (hour != 0) {
            timeStr = timeStr + hour + ":";
        }
        if (minute != 0) {
            if (minute < 10) {
                timeStr = "0";
            } else {
                timeStr = "";
            }
            timeStr = timeStr + minute;
        }
        timeStr = timeStr + ":";
        if (second != 0) {
            if (second < 10) {
                timeStr = timeStr + "0";
            } else {
                timeStr = timeStr + "";
            }
            timeStr = timeStr + second;
        } else {
            timeStr = timeStr + "00";
        }
        return timeStr;
    }

    public static boolean isEmpty(String s) {
        return s == null || s.trim().length() == 0;
    }

    public static boolean isDigit(String s) {
        Matcher m = PATTERN_DIGIT.matcher(s);
        return m.matches();
        // if (isEmpty(s)) {
        //     return false;
        // }
        // // 判断是否为空，如果为空则返回false
        // // 通过 length() 方法计算cs传入进来的字符串的长度，并将字符串长度存放到sz中
        // final int sz = s.length();
        // // 通过字符串长度循环
        // for (int i = 0; i < sz; i++) {
        //     // 判断每一个字符是否为数字，如果其中有一个字符不满足，则返回false
        //     if (!Character.isDigit(s.charAt(i))) {
        //         return false;
        //     }
        // }
        // // 验证全部通过则返回true
        // return true;
    }

    public static boolean equals(String a, String b) {
        return a != null && a.equals(b);
    }

    public static String getDragAction(int action) {
        // https://juejin.cn/post/7062961027578757156 setOnDragListener startDrag startDragAndDrop
        //DragEvent.ACTION_DRAG_STARTED:表示拖动已经开始
        //DragEvent.ACTION_DRAG_ENTERED:表示拖动阴影已经进入目标View
        //DragEvent.ACTION_DRAG_LOCATION：拖动阴影在目标View边界内移动时会多次响应此事件
        //DragEvent.ACTION_DRAG_EXITED：表示拖动阴影离开了目标View的边界
        //DragEvent.ACTION_DROP：表示拖动阴影被释放
        //DragEvent.ACTION_DRAG_ENDED：表示拖放操作即将结束，在此处需要通过调用event.getResult()的返回值来判断拖放操作是否成功
        if (action == DragEvent.ACTION_DRAG_STARTED) {
            return "ACTION_DRAG_STARTED";
        } else if (action == DragEvent.ACTION_DRAG_ENTERED) {
            return "ACTION_DRAG_ENTERED";
        } else if (action == DragEvent.ACTION_DRAG_LOCATION) {
            return "ACTION_DRAG_LOCATION";
        } else if (action == DragEvent.ACTION_DROP) {
            return "ACTION_DROP";
        } else if (action == DragEvent.ACTION_DRAG_EXITED) {
            return "ACTION_DRAG_EXITED";
        } else if (action == DragEvent.ACTION_DRAG_ENDED) {
            return "ACTION_DRAG_ENDED";
        }
        return String.valueOf(action);
    }

    public static String getTouchAction(int action) {
        if (action == MotionEvent.ACTION_DOWN) {
            return "ACTION_DOWN";
        } else if (action == MotionEvent.ACTION_MOVE) {
            return "ACTION_MOVE";
        } else if (action == MotionEvent.ACTION_UP) {
            return "ACTION_UP";
        } else if (action == MotionEvent.ACTION_CANCEL) {
            return "ACTION_CANCEL";
        } else if (action == MotionEvent.ACTION_POINTER_DOWN) {
            return "ACTION_POINTER_DOWN";
        } else if (action == MotionEvent.ACTION_POINTER_UP) {
            return "ACTION_POINTER_UP";
        } else if (action == MotionEvent.ACTION_HOVER_MOVE) {
            return "ACTION_HOVER_MOVE";
        } else if (action == MotionEvent.ACTION_SCROLL) {
            return "ACTION_SCROLL";
        } else if (action == MotionEvent.ACTION_HOVER_ENTER) {
            return "ACTION_HOVER_ENTER";
        } else if (action == MotionEvent.ACTION_HOVER_EXIT) {
            return "ACTION_HOVER_EXIT";
        }
        return String.valueOf(action);
    }
}
