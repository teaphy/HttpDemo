package com.teaphy.okhttptest.retrofit.bean;

/**
 * Created by Administrator
 * on 2016/6/13.
 */
public class Score {
    String subject;
    float score;

    public Score() {
    }

    public Score(String subject, float score) {
        this.score = score;
        this.subject = subject;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public float getScore() {
        return score;
    }

    public void setScore(float score) {
        this.score = score;
    }

    @Override
    public String toString() {
        return "Score{" +
                "subject='" + subject + '\'' +
                ", score=" + score +
                '}';
    }
}
