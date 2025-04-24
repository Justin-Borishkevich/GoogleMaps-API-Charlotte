package com.example.assignment13.models;

import java.util.List;

public class PathResponse {
    private String title;
    private List<Point> path;

    public PathResponse() {
    }

    public PathResponse(String title, List<Point> path) {
        this.title = title;
        this.path = path;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<Point> getPath() {
        return path;
    }

    public void setPoints(List<Point> points) {
        this.path = points;
    }

    @Override
    public String toString() {
        return "PathResponse{" +
                "title='" + title + '\'' +
                ", paths=" + path +
                '}';
    }
}
