package com.huawei.java.main;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;

public class Readfile {
    public static String[][] readcsv(String path){
        String line;
        String cvsSplitBy = ",";

        try(LineNumberReader lineNumberReader=new LineNumberReader(new FileReader(path))){
            lineNumberReader.skip(Long.MAX_VALUE);
            int n=lineNumberReader.getLineNumber();
            String[][] res=new String[n][];            //不知道几列
            try (BufferedReader br = new BufferedReader(new FileReader(path))) {
                int i=0;
                while ((line = br.readLine()) != null) {
                    // use comma as separator
                    String[] item = line.split(cvsSplitBy);
                    res[i]=item;
                    i++;

                }
                return res;

            } catch (IOException e) {
                e.printStackTrace();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
    public static int readini(String path) {
        String line;
        String temp_qos="";
        StringBuffer qos_constraint=new StringBuffer();
        Boolean k=false;
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            int i = 0;
            while ((line = br.readLine()) != null) {
                // use comma as separator
                if (i == 1) {
                    temp_qos = line;
                    break;
                }
                i++;
            }
            for(int j=0;j<temp_qos.length();j++){

                if(k==true){
                    qos_constraint.append(temp_qos.charAt(j));
                }
                if(temp_qos.charAt(j)=='=')
                    k=true;

            }
            return Integer.valueOf(String.valueOf(qos_constraint));
        }catch (Exception e){
            e.printStackTrace();
        }
        return 0;
    }

    public static void main(String[] args) {
        String[][] s=readcsv("data/demand.csv");
        System.out.println(s.length+"+"+s[0].length+"+"+s[1][10]);
        int c=readini("data/config.ini");
        System.out.println(c);
    }
}
