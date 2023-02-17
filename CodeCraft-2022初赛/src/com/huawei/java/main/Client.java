package com.huawei.java.main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Client {
    public String id;                           //id
    public int num;                             //编号
    public Map<Integer,Integer>demand;        //时刻——需求，有时刻数个
    public Map<Integer,Integer>time_remain;     //时刻--余量
    public int ave_demand=0;
    public int remain=0;                          //剩余的需求
    public int serverNum=0;                       //连到的边缘的数量
    public int priority=Integer.MIN_VALUE;                        //优先级
    public List<Server> serverList;             //边缘节点列表
    public Boolean satisfied=false;                   //是否已经被满足
    public static int times;                    //时刻数
    public Client(){
        this.serverList=new ArrayList<>();
    }
    public static Client[] init(String path){   //path="data",没有初始化关于边缘节点的部分
        String[][] info=Readfile.readcsv(path+"/demand.csv");
        times=info.length-1;
        Client[] clients=new Client[info[0].length-1];
        for(int i=0;i<clients.length;i++)
            clients[i]=new Client();
        for(int i=0;i<clients.length;i++){
            clients[i].demand=new HashMap<>();              //初始化表
            clients[i].time_remain=new HashMap<>();
        }
        for(int i=1;i<info.length;i++) {      //一行行往下
            for(int j=1;j<info[0].length;j++){
                clients[j-1].id=info[0][j];
                clients[j-1].num=j-1;
            }
            for(int j=1;j<info[0].length;j++) {
                clients[j-1].demand.put(i - 1, Integer.valueOf(info[i][j])); //为第j-1个客户添加i-1时刻的需求
                clients[j-1].time_remain.put(i-1,Integer.valueOf(info[i][j]));
//                clients[j-1].remain=Integer.valueOf(info[i][j]);
            }

        }
        for(int i=1;i<info[0].length;i++){
            for(int j=1;j<info.length;j++){
                clients[i-1].ave_demand+=Integer.valueOf(info[j][i]);
            }
            clients[i-1].ave_demand=clients[i-1].ave_demand/Client.times;
        }

        return clients;
    }
    public static Client[] getServerInfo(Server[] servers,Client[] clients,String path) {
        int[][] gragh=Server.getGragh(servers, clients, path);
        for(int i=0;i<gragh.length;i++){
            for(int j=0;j<gragh[0].length;j++){
                if(gragh[i][j]==1){
                    clients[j].serverNum++;
                    clients[j].serverList.add(servers[i]);
                }
            }
        }
        return clients;
    }



}
