package com.itheima.test;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class SortTest {

    @Test
    public void sortTest() {
        Student student1 = new Student(18, "aa", 150, 2.1);
        Student student2 = new Student(18, "ab", 150, 2.2);
        Student student3 = new Student(18, "ab", 140, 2.1);
        Student student4 = new Student(19, "aa", 150, 2.1);
        Student student5 = new Student(20, "aa", 150, 2.0);
        Student student6 = new Student(20, "aa", 150, 2.3);

        List<Student> list = new ArrayList<>(Arrays.asList(student1, student2, student3, student4, student5, student6));
        List<Student> result = list.stream().sorted(Comparator.comparing(Student::getAge).thenComparing(Student::getScore).thenComparing(Student::getWeight)).collect(Collectors.toList());
        System.out.println(result);

    }

    @Test
    public void test() {
        ZhuTest zhuTest1 = new ZhuTest(18, 1.0);
        ZhuTest zhuTest2 = new ZhuTest(18, 2.0);
        ZhuTest zhuTest3 = new ZhuTest(18, 2.5);
        ZhuTest zhuTest4 = new ZhuTest(18, 3.0);
        ZhuTest zhuTest5 = new ZhuTest(18, 4.0);
        List<ZhuTest> list = new ArrayList<>(Arrays.asList(zhuTest1, zhuTest2, zhuTest4, zhuTest3, zhuTest5));
        System.out.println(list.stream().sorted(Comparator.comparingDouble(ZhuTest::getScore)).collect(Collectors.toList()));

    }
}
