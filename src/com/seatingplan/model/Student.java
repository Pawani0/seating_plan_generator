package com.seatingplan.model;

/**
 * Represents a student with academic details.
 */
public class Student {
    private String name;
    private String rollNo;
    private int year;
    private int semester;
    private String branch;

    public Student() {
    }

    public Student(String name, String rollNo, int year, int semester, String branch) {
        this.name = name;
        this.rollNo = rollNo;
        this.year = year;
        this.semester = semester;
        this.branch = branch;
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRollNo() {
        return rollNo;
    }

    public void setRollNo(String rollNo) {
        this.rollNo = rollNo;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getSemester() {
        return semester;
    }

    public void setSemester(int semester) {
        this.semester = semester;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    /**
     * Returns a unique key combining year and semester for grouping.
     */
    public String getYearSemKey() {
        return year + "-" + semester;
    }

    @Override
    public String toString() {
        return rollNo + " - " + name + " (" + branch + ", Y" + year + "S" + semester + ")";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Student student = (Student) obj;
        return rollNo != null && rollNo.equals(student.rollNo);
    }

    @Override
    public int hashCode() {
        return rollNo != null ? rollNo.hashCode() : 0;
    }
}
