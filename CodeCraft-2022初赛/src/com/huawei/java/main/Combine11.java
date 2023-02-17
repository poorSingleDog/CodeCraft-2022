package com.huawei.java.main;

import java.io.IOException;
import java.util.*;

public class Combine11 {            //暴力平均--301w
    public void run(String path,String out){
        Server[] servers1 = Server.init(path);
        Client[] clients1 = Client.init(path);
        Server[] servers = Server.getClientInfo(servers1, clients1, path);
        Client[] clients = Client.getServerInfo(servers1, clients1, path);

        int sacrificeMax = (int) (Client.times * 0.05);         //同一个边缘节点最大白嫖次数
        int[][] gragh = Server.getGragh(servers1, clients1, path);                    //存放每个客户的原始边缘节点列表
        int[] static_servernum = new int[clients.length];         //存放每个客户的原始线数
        for (int i = 0; i < clients.length; i++) {
            static_servernum[i] = clients[i].serverNum;
        }
        int[] static_clientnum = new int[servers.length];
        for (int i = 0; i < servers.length; i++) {
            static_clientnum[i] = servers[i].clientNum;
        }
        for (int i = 0; i < servers.length; i++) {
            for (int j = 0; j < Client.times; j++) {
                servers[i].time_rest.put(j, servers[i].band_width);  //初始化每个客户的<时刻-余量>表
            }
        }
        SingleClientStrategy[][] output = new SingleClientStrategy[Client.times][clients.length];     //存储输出结果
        for (int i = 0; i < output.length; i++) {
            for (int j = 0; j < output[0].length; j++) {
                output[i][j] = new SingleClientStrategy();
            }
        }
        //---------------------------------------------开始5%部分------------------------------------------------------------------------
//        Arrays.sort(servers, new Comparator<Server>() {
//            @Override
//            public int compare(Server o1, Server o2) {
//                return o1.clientNum-o2.clientNum;
//            }
//        });
        Arrays.sort(servers, new Comparator<Server>() {         //test:边缘节点按照容量降序排序
            @Override
            public int compare(Server o1, Server o2) {
                return o2.band_width - o1.band_width;
            }
        });
        for (int i = 0; i < servers.length; i++) {      //对每一列
            if (servers[i].isDead)
                continue;
            Map<Integer, Integer> t_b = new HashMap<>();
            for (int j = 0; j < Client.times; j++) {    //算每个时刻对该边缘节点的客户剩余需求和
                int sum = 0;
                for (Client c : servers[i].clientList) {
                    sum += c.time_remain.get(j);
                }
                t_b.put(j, sum);
            }
            List<Map.Entry<Integer, Integer>> T_B = new ArrayList<>(t_b.entrySet());
            Collections.sort(T_B, new Comparator<Map.Entry<Integer, Integer>>() {       //将一列根据相连客户需求和降序排序
                @Override
                public int compare(Map.Entry<Integer, Integer> o1, Map.Entry<Integer, Integer> o2) {
                    return o2.getValue() - o1.getValue();
                }
            });
            for (int j = 0; j < sacrificeMax; j++) {            //对这一列的前5%行进行分配,时刻=T_B.get(j).getKey()
                servers[i].sacri_times.add(T_B.get(j).getKey());
                servers[i].sacrificeNum++;
                Collections.sort(servers[i].clientList, new Comparator<Client>() {      //对该边缘下面的客户进行线数升序排序
                    @Override
                    public int compare(Client o1, Client o2) {
                        return o1.serverNum - o2.serverNum;
                    }
                });
                for (Client c : servers[i].clientList) {
                    if (c.time_remain.get(T_B.get(j).getKey()) == 0) {      //若在该时刻客户余量为0
                        continue;
                    } else {
                        if (servers[i].time_rest.get(T_B.get(j).getKey()) > c.time_remain.get(T_B.get(j).getKey())) {   //边缘余量>客户余量
                            int trans = c.time_remain.get(T_B.get(j).getKey());
                            servers[i].time_rest.put(T_B.get(j).getKey(), servers[i].time_rest.get(T_B.get(j).getKey()) - trans);
                            c.time_remain.put(T_B.get(j).getKey(), 0);
                            output[T_B.get(j).getKey()][c.num].item.put(servers[i].id, trans);
                        } else if (servers[i].time_rest.get(T_B.get(j).getKey()) <= c.time_remain.get(T_B.get(j).getKey())) { //边缘余量<=客户余量
                            int trans = servers[i].time_rest.get(T_B.get(j).getKey());
                            servers[i].time_rest.put(T_B.get(j).getKey(), 0);
                            c.time_remain.put(T_B.get(j).getKey(), c.time_remain.get(T_B.get(j).getKey()) - trans);
                            output[T_B.get(j).getKey()][c.num].item.put(servers[i].id, trans);
                            break;
                        }
                    }
                }
            }
        }
        //------------------------------------------------开始95%部分--------------------------------------------------------------------
        Arrays.sort(servers, new Comparator<Server>() {         //将servers排回原来顺序
            @Override
            public int compare(Server o1, Server o2) {
                return o1.num - o2.num;
            }
        });
        long t1=System.currentTimeMillis();
        for(int i=0;i<Client.times;i++){
            //每秒重置边缘节点的上限
            for(Server s:servers){
                s.upper_limit=0;
            }
            int sum_c_remain=0;
            for(int j=0;j<clients.length;j++){
                sum_c_remain+=clients[j].time_remain.get(i);
            }
            //计算活的边缘节点（全部边缘节点-用于5%的边缘节点-自闭边缘节点）
            List<Server> aliveS=new ArrayList<>();
            for(int j=0;j<servers.length;j++){
                if(servers[j].sacri_times.contains(i)||servers[j].isDead){
                    continue;
                }else {
                    aliveS.add(servers[j]);
                }
            }
            Collections.sort(aliveS, new Comparator<Server>() {     //给活边缘节点按容量升序排序
                @Override
                public int compare(Server o1, Server o2) {
                    return o1.band_width-o2.band_width;
                }
            });
            int upper=(int)((double)sum_c_remain/aliveS.size())+1;
            for(int j=0;j<aliveS.size();j++){                       //给活边缘节点设置上限
                if(aliveS.get(j).band_width<upper){         //容量《上限
                    aliveS.get(j).upper_limit=aliveS.get(j).band_width;
                    sum_c_remain-=aliveS.get(j).band_width;
                    upper=(int)((double)sum_c_remain/(aliveS.size()-j-1))+1;
                }else {                                     //容量》上限
                    aliveS.get(j).upper_limit=upper;
                }
            }
            Collections.sort(aliveS, new Comparator<Server>() {     //给活边缘节点按线数升序排序 (或者先比线数后比容量？)******
                @Override
                public int compare(Server o1, Server o2) {
                    return o1.clientNum-o2.clientNum;
                }
            });
            for(Server s:aliveS){
                Collections.sort(s.clientList, new Comparator<Client>() {   //给该边缘节点下的客户按线数升序排序
                    @Override
                    public int compare(Client o1, Client o2) {
                        return o1.serverNum-o2.serverNum;
                    }
                });
                for(Client c:s.clientList){
                    if(c.time_remain.get(i)==0){
                        continue;
                    }
                    if(s.upper_limit>c.time_remain.get(i)){     //客户满
                        int trans=c.time_remain.get(i);
                        c.time_remain.put(i,0);
                        s.time_rest.put(i,s.time_rest.get(i)-trans);
                        s.upper_limit-=trans;
                        output[i][c.num].item.put(s.id,trans);
                    }else if(s.upper_limit<=c.time_remain.get(i)){      //边缘满
                        int trans=s.upper_limit;
                        c.time_remain.put(i,c.time_remain.get(i)-trans);
                        s.time_rest.put(i,s.time_rest.get(i)-trans);
                        s.upper_limit=0;
                        output[i][c.num].item.put(s.id,trans);
                        break;
                    }
                }
            }

            for(Client c:clients){          //检查客户需求是否都被满足(是否要将client按线数升序排）
                if(c.time_remain.get(i)>0){
                    int[] start=new int[c.serverList.size()];
                    for(int k=0;k<start.length;k++){
                        start[k]=c.serverList.get(k).time_rest.get(i);
                    }
                    l:while (true){
                        for(int k=0;k<c.serverList.size();k++){
                            if(c.time_remain.get(i)==0){
                                break l;
                            }else {
                                if(c.serverList.get(k).time_rest.get(i)>0){
                                    c.serverList.get(k).time_rest.put(i,c.serverList.get(k).time_rest.get(i)-1);
                                    c.time_remain.put(i,c.time_remain.get(i)-1);
                                }else {
                                    continue ;
                                }
                            }
                        }
                    }
                    int[] trans=new int[c.serverList.size()];
                    for(int k=0;k<trans.length;k++){
                        trans[k]=start[k]-c.serverList.get(k).time_rest.get(i);
                    }
                    for(int k=0;k<trans.length;k++){
                        if(trans[k]>0){
                            output[i][c.num].item.put(c.serverList.get(k).id,output[i][c.num].item.getOrDefault(c.serverList.get(k).id,0)+trans[k]);
                        }
                    }
                }
            }


        }
        long t2=System.currentTimeMillis();

        for(int i=0;i<output.length;i++){
            for(int j=0;j<output[0].length;j++){
                System.out.print(clients[j].id+":");
                for(Map.Entry<String ,Integer> entry:output[i][j].item.entrySet()){
                    System.out.print("<"+entry.getKey()+","+entry.getValue()+">");
                }
                System.out.println();
            }
        }
        try {
            SingleClientStrategy.writeDataToFile(output, out, clients);
        }catch (IOException e){
            e.printStackTrace();
        }

//        for(Server s:servers){
//            System.out.println("cishu:"+s.sacrificeNum);
//        }
        System.out.println("分配和检查客户："+(t2-t1));
    }
}
