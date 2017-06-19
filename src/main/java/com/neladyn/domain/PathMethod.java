package com.neladyn.domain;

import org.eclipse.jetty.http.HttpMethod;

public class PathMethod {
    private String path;
    private HttpMethod method;

    public PathMethod() {
    }

    public PathMethod(String path, HttpMethod method) {
        this.path = path;
        this.method = method;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public HttpMethod getMethod() {
        return method;
    }

    public void setMethod(HttpMethod method) {
        this.method = method;
    }

    @Override
    public String toString() {
        return "PathMethod{" +
                "path='" + path + '\'' +
                ", method=" + method +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PathMethod that = (PathMethod) o;

        if (path != null ? !path.equals(that.path) : that.path != null) return false;
        return method == that.method;
    }

    @Override
    public int hashCode() {
        int result = path != null ? path.hashCode() : 0;
        result = 31 * result + (method != null ? method.hashCode() : 0);
        return result;
    }
}
