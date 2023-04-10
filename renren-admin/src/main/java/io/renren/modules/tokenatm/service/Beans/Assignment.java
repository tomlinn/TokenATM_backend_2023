package io.renren.modules.tokenatm.service.Beans;

public class Assignment {

    private String id;
    private String name;
    private String dueDate;
    private double maxPoints;

    public Assignment(String id, String name, String dueDate, double maxPoints) {
        this.id = id;
        this.name = name;
        this.dueDate = dueDate;
        this.maxPoints = maxPoints;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDueDate() {
        return dueDate;
    }

    public double getMaxPoints() {
        return maxPoints;
    }
}
