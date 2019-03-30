package tech.aayush.pinger;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.content.Context.WIFI_SERVICE;

public class RegexFormat {

    public int getHostIP(String str, Context context){
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int ipAddress = wifiInfo.getIpAddress();
        return ipAddress;
    }

    public String getRemoteName(String str){
        return regexChecker("(www.)?([A-Za-z0-9]+\\.)+[A-Za-z]{2,4}", str);
    }

    public String getRemoteAddr(String str){
        return regexChecker("[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}", str);
    }

    public String getNumberOfPacketsSent(String str){
        return regexChecker("\\d+ packets", str);
    }

    public String getNumberOfPacketsReceived(String str){
        return regexChecker("\\d+ received", str);
    }

    public String getRoundTripTime(String str){
        //return regexChecker("[0-9]+(.[0-9]+)?", str);
        String result = regexChecker("\\[([0-9]{10})\\]PING", str);
        result = result.substring(0, result.indexOf("P"));
        return result;
    }

    public String[] getMinMaxAvg(String str){
        String a[] = new String[4];
        String data = regexChecker("[0-9]+(.[0-9]+)?/[0-9]+(.[0-9]+)?/[0-9]+(.[0-9]+)?/[0-9]+(.[0-9]+)?", str);
        Log.d("MinMaxD", data);

        if(!data.isEmpty()){
            a[0] = data.substring(0,data.indexOf("/"));
            a[1] = data.substring(ordinalIndexOf(data, "/", 0)+1, ordinalIndexOf(data, "/", 1));
            a[2] = data.substring(ordinalIndexOf(data, "/", 1)+1, ordinalIndexOf(data, "/", 2));
            a[3] = data.substring(data.lastIndexOf("/")+1);
            return a;
        }

        return a;
    }



    public ArrayList<String> countICMP(String str){
        return regexChecker2("icmp_seq=\\d+", str);
    }

    public ArrayList<String> timeOFEachIcm(String str){
        return regexChecker2("time=\\d+", str);
    }

    public static String regexChecker(String theRegex, String str2Check){
        Pattern checkRegex = Pattern.compile(theRegex);
        Matcher regexMatcher = checkRegex.matcher(str2Check);

        if(regexMatcher.find()){
            if(regexMatcher.group().length() != 0){
                return regexMatcher.group().trim();
            }
        }

        return "";
    }

    public static ArrayList<String> regexChecker2(String theRegex, String str2Check) {
        Pattern checkRegex = Pattern.compile(theRegex);
        Matcher regexMatcher = checkRegex.matcher(str2Check);
        ArrayList<String> seq = new ArrayList<>();
        /*String a[] = new String[20];
        int i =0;*/


        while(regexMatcher.find()) {
            if(regexMatcher.group().length() != 0) {
                String text = regexMatcher.group().trim();
                text = text.substring(text.indexOf("=") + 1);
                seq.add(text);
                /*a[i] = regexMatcher.group().trim();
                a[i] = a[i].substring(a[i].indexOf("=") + 1);
                i = i+1;*/
            }
        }

        return seq;

    }


    public static int ordinalIndexOf(String str, String substr, int n) {
        int pos = -1;
        do {
            pos = str.indexOf(substr, pos + 1);
        } while (n-- > 0 && pos != -1);
        return pos;
    }

}
