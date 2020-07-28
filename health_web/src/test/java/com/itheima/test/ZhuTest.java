package com.itheima.test;

public class ZhuTest {
    private Integer age;
    private Double score;

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }

    @Override
    public String toString() {
        return "ZhuTest{" +
                "age=" + age +
                ", score=" + score +
                '}';
    }

    public ZhuTest(Integer age, Double score) {
        this.age = age;
        this.score = score;
    }
}
