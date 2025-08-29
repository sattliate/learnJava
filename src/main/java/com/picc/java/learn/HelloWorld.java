package com.picc.java.learn;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class HelloWorld {
    // 定义一个数据类来存储预约信息
    static class AppointmentData {
        private int isAppointment;
        private String data;

        public AppointmentData(int isAppointment, String data) {
            this.isAppointment = isAppointment;
            this.data = data;
        }

        public int getIsAppointment() {
            return isAppointment;
        }

        public String getData() {
            return data;
        }

        @Override
        public String toString() {
            return "AppointmentData{isAppointment=" + isAppointment + ", data='" + data + "'}";
        }
    }

    /**
     * 主方法 - 演示预约数据的过滤操作
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        // 创建示例数据
        List<AppointmentData> originalList = new ArrayList<>();
        originalList.add(new AppointmentData(1, "预约1"));
        originalList.add(new AppointmentData(2, "预约2"));
        originalList.add(new AppointmentData(1, "预约3"));
        originalList.add(new AppointmentData(2, "预约4"));

        System.out.println("原始数据:");
        originalList.forEach(System.out::println);

        // 假设这是前端传入的参数
        int filterParam = 1;

        if (filterParam == 1) {
            // 查找不为1的数据
            List<AppointmentData> nonMatchingList = originalList.stream()
                    .filter(data -> data.getIsAppointment() != 1)
                    .collect(Collectors.toList());

            if (!nonMatchingList.isEmpty()) {
                System.out.println("\n找到不匹配的数据 (isAppointment != 1):");
                nonMatchingList.forEach(data -> 
                    System.out.println("isAppointment: " + data.getIsAppointment() + ", data: " + data.getData())
                );
            } else {
                System.out.println("\n没有找到不匹配的数据，所有数据的isAppointment都是1");
            }
        } else {
            // 原有的过滤逻辑
            List<AppointmentData> filteredList = filterByAppointment(originalList, filterParam);
            System.out.println("\n过滤参数: " + filterParam);
            System.out.println("过滤后的结果:");
            filteredList.forEach(data -> 
                System.out.println("isAppointment: " + data.getIsAppointment() + ", data: " + data.getData())
            );
        }
    }

    /**
     * 根据预约值过滤数据
     * @param list 原始数据列表
     * @param appointmentValue 要过滤的预约值
     * @return 过滤后的数据列表
     */
    private static List<AppointmentData> filterByAppointment(List<AppointmentData> list, int appointmentValue) {
        return list.stream()
                .filter(data -> data.getIsAppointment() == appointmentValue)
                .collect(Collectors.toList());
    }
}
