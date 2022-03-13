public class Skiers {
    private Integer resortID;
    private String seasonID;
    private String dayID;
    private Integer skierID;
    private String time;
    private int liftID;
    private int waitTime;

    public Skiers() {
    }

    public Integer getResortID() {
        return this.resortID;
    }

    public void setResortID(Integer resortID) {
        this.resortID = resortID;
    }

    public String getSeasonID() {
        return this.seasonID;
    }

    public void setSeasonID(String seasonID) {
        this.seasonID = seasonID;
    }

    public String getDayID() {
        return this.dayID;
    }

    public void setDayID(String dayID) {
        this.dayID = dayID;
    }

    public Integer getSkierID() {
        return skierID;
    }

    public void setSkierID(Integer skierID) {
        this.skierID = skierID;
    }

    public String getTime() { return time; }

    public void setTime(String time) { this.time = time; }

    public int getLiftID() { return liftID; }

    public void setLiftID(int liftID) { this.liftID = liftID; }

    public int getWaitTime() { return waitTime; }

    public void setWaitTime(int waitTime) { this.waitTime = waitTime; }
}
