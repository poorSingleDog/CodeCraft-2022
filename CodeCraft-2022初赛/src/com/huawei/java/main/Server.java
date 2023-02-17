package com.huawei.java.main;

import java.util.*;

public class Server {
    public String id;           //id
    public int num;             //编号
    public int band_width;      //最大带宽
    public int clientNum=0;       //客户数
    public List<Client> clientList;     //客户列表
    public int rest;            //剩余带宽
    public int priority=Integer.MIN_VALUE;        //优先级
    public int sacrificeNum;        //牺牲次数
    public int burden=0;            //相连客户需求和
    public int upper_limit=0;         //每刻的上限
    public Boolean received=false;
    public Set<Integer> sacri_times;    //牺牲时刻
    public Map<Integer,Integer> time_rest;
    public List<Integer> used;      //每个时刻用了多少
    public Boolean isDead=false;      //是否不与任何客户相连
    public Queue<Integer> queue;     //存储总时刻数5%的牺牲时刻

    public Server(){
        this.clientList=new ArrayList<>();
        this.queue=new LinkedList<>();
        this.used=new ArrayList<>();
        this.sacri_times=new HashSet<>();
        this.time_rest=new HashMap<>();
    }
    public static Server[] init(String path){      //path="data",初始化所有边缘节点的最大带宽、id、序号、剩余带宽、牺牲次数，没有初始化关于边缘节点的部分

        String[][] info=Readfile.readcsv(path+"/site_bandwidth.csv");
        Server[] servers=new Server[info.length-1];
        for(int i=1;i<info.length;i++){
            servers[i-1]=new Server();
            servers[i-1].num=i-1;
            servers[i-1].id=info[i][0];
            servers[i-1].band_width=Integer.valueOf(info[i][1]);
            servers[i-1].rest=Integer.valueOf(info[i][1]);
            servers[i-1].sacrificeNum=0;
        }
        return servers;
    }

    public static int[][] getGragh(Server[] servers,Client[] clients,String path){
        String[][] info=Readfile.readcsv(path+"/qos.csv");
        int config=Readfile.readini(path+"/config.ini");
        String[] c=new String[info[0].length-1];
        String[] s=new String[info.length-1];
        Map<String,Integer> cc=new HashMap<>();
        Map<String,Integer> ss=new HashMap<>();
        for(int i=1;i<info[0].length;i++){
            c[i-1]=info[0][i];
        }
        for(int i=1;i<info.length;i++){
            s[i-1]=info[i][0];
        }
        for(Client client:clients){
            cc.put(client.id,client.num);
        }
        for(Server server:servers){
            ss.put(server.id,server.num);
        }
        int[][] old=new int[info.length-1][info[0].length-1];
        for(int i=1;i<info.length;i++){
            for(int j=1;j<info[0].length;j++){
                if(Integer.valueOf(info[i][j])<config){
                    old[i-1][j-1]=1;
                }else {
                    old[i-1][j-1]=0;
                }
            }
        }
        int[][] fresh=new int[info.length-1][info[0].length-1];
        for(int i=0;i<old.length;i++){
            for(int j=0;j<old[0].length;j++){
                if(old[i][j]==1){
                    fresh[ss.get(s[i])][cc.get(c[j])]=1;
                }
            }
        }
        return fresh;
    }
//    public static int[][] getGragh(String path){       //path="data"
//        String[][] info=Readfile.readcsv(path+"/qos.csv");
//        int[][] gragh=new int[info.length-1][info[0].length-1];
//        for(int i=1;i<info.length;i++){
//            for(int j=1;j<info[0].length;j++){
//                if(Integer.valueOf(info[i][j])<Readfile.readini(path+"/config.ini")){
//                    gragh[i-1][j-1]=1;
//                }else {
//                    gragh[i-1][j-1]=0;
//                }
//            }
//        }
//        return gragh;
//    }
    public static Server[] getClientInfo(Server[] servers,Client[] clients,String path){     //给本来一波servers加上客户信息,path="data"
        int[][] gragh=getGragh(servers,clients,path);
        for(int i=0;i<gragh.length;i++){
            for(int j=0;j<gragh[0].length;j++){
                if(gragh[i][j]==1) {
                    servers[i].clientNum++;
                    servers[i].clientList.add(clients[j]);
                }
            }
        }
        for(int i=0;i<servers.length;i++){
            if(servers[i].clientNum==0)
                servers[i].isDead=true;
        }

        return servers;
    }
    public static Server[] abandon(Server[] servers){       //遗弃自闭的边缘节点
        int emptyNum=0;
        for(Server s:servers){
            if(s.clientNum==0)
                emptyNum++;
        }
        Server[] newServers=new Server[servers.length-emptyNum];
        int i=-1;
        for(Server s:servers){
            if(s.clientNum!=0){
                newServers[++i]=s;
            }
        }
        return newServers;
    }


    public static void main(String[] args) {
//        String path="data";
//        Server[] servers1=Server.init(path);
//        Client[] clients1=Client.init(path);
//        int[][] a=GetGragh(servers1,clients1,path);
//        int[][] b=getGragh(path);
//        for(int i=0;i<a.length;i++){
//            for(int j=0;j<a[0].length;j++) {
//                if(a[i][j]!=b[i][j]) {
//                    System.out.println("wrong");
//                    return;
//                }
//            }
//        }
//        System.out.println("right");

    }


}
