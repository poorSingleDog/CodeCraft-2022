package com.huawei.java.main;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SingleClientStrategy {
    public Map<String,Integer> item;

    public SingleClientStrategy(){
        item=new HashMap<>();

    }

    public static void writeDataToFile(SingleClientStrategy[][] output,String path,Client[] clients) throws IOException {
        //文件目录
        File writefile;
        BufferedWriter bw;
        boolean append = true;              //  是否追加
//        String path = "D:\\ocr.txt";
        writefile = new File(path);
        if (writefile.exists() == false)   // 判断文件是否存在，不存在则生成
        {
            try {
                writefile.createNewFile();
                writefile = new File(path);
            } catch (IOException e) {
                // TODO 自动生成的 catch 块
                e.printStackTrace();
            }
        } else {        // 存在先删除，再创建
            writefile.delete();
            try {
                writefile.createNewFile();
                writefile = new File(path);
            } catch (IOException e) {
                // TODO 自动生成的 catch 块
                e.printStackTrace();
            }
        }
        try {
            FileWriter fw = new FileWriter(writefile, append);
            bw = new BufferedWriter(fw);
            for(int i=0;i<output.length;i++){
                for(int j=0;j<output[0].length;j++){
                    bw.write(clients[j].id+":");
                    int count=0;
                    for(Map.Entry<String ,Integer> entry:output[i][j].item.entrySet()){
                        if(count++!=0){
                            bw.write(",");
                        }
                        bw.write("<"+entry.getKey()+","+entry.getValue()+">");
                    }
                    if(i!=(output.length-1)||j!=(output[0].length-1))
                        bw.write("\r\n");
                    else
                        System.out.println("not ln");
                }
            }
//            bw.write(str);
            bw.flush();
            bw.close();

        } catch (IOException e) {
            // TODO 自动生成的 catch 块
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {



    }
}
