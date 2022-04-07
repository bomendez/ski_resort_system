package EndpointClasses;

public class Resorts {
    private int resortID;
    private int seasonID;
    private int dayID;
    private int year;

    public Resorts() {
    }

    public int getResortID() {
        return this.resortID;
    }

    public void setResortID(int resortID) {
        this.resortID = resortID;
    }

    public int getSeasonID() {
        return this.seasonID;
    }

    public void setSeasonID(int seasonID) {
        this.seasonID = seasonID;
    }

    public int getDayID() {
        return this.dayID;
    }

    public void setDayID(int dayID) {
        this.dayID = dayID;
    }

    public int getYear() { return this.year; }

    public void setYear(int year) { this.year = year; }
}
