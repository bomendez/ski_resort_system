package EndpointClasses;

public class Statistics {
    private String url;
    private String operation;
    private int mean;
    private int max;

    public Statistics() {
    }

    public String getUrl() {
        return this.url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getOperation() {
        return this.operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public int getMean() {
        return this.mean;
    }

    public void setMean(int mean) {
        this.mean = mean;
    }

    public int getMax() {
        return this.max;
    }

    public void setMax(int max) {
        this.max = max;
    }
}
