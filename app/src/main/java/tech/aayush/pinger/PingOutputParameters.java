package tech.aayush.pinger;

import java.util.ArrayList;

public class PingOutputParameters {

    private String Remote_Name, Remote_Addr, Time, Xmt, Rcv, Min, Avg, Max, Mdev;
    private ArrayList<String> seq, rtt;

    public PingOutputParameters(String rn, String rd, String time, String xmt, String rcv, String min, String avg, String max, String mdev, ArrayList<String> seq, ArrayList<String> rtt){
        this.Remote_Name = rn;
        this.Remote_Addr = rd;
        this.Time = time;
        this.Xmt = xmt;
        this.Rcv = rcv;
        this.Min = min;
        this.Avg = avg;
        this.Max = max;
        this.Mdev = mdev;
        this.seq = seq;
        this.rtt = rtt;
    }

    public String getRemote_Name() {
        return Remote_Name;
    }

    public String getRemote_Addr() {
        return Remote_Addr;
    }

    public String getTime() {
        return Time;
    }

    public String getXmt() {
        return Xmt;
    }

    public String getRcv() {
        return Rcv;
    }

    public String getMin() {
        return Min;
    }

    public String getAvg() {
        return Avg;
    }

    public String getMax() {
        return Max;
    }

    public String getMdev() {
        return Mdev;
    }

    public ArrayList<String> getSeq() {
        return seq;
    }

    public ArrayList<String> getRtt() {
        return rtt;
    }
}
