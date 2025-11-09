package stud.g01.utils;
import java.lang.System;
import java.util.*;
import stud.g01.pdb.SQlitePDB;

public class PreProcess {
    private static int size = 4;//不相交数据模式A*处理15puzzle
    private static int k = 5; // 每个模式的元素个数
    private static String[] submodel;//存储3个子模式

    public PreProcess(){
        submodel = new String[3];
    }

    //检查状态合法性，按照我们的定义，应该存在5个空格作为数据库中标识状态的字符串标识
    private static boolean check_valid(String str){
        int count = 0;
        for(int i = 0;i < str.length();i++){
            if(str.charAt(i) == ' ')count++;
        }
        if(count == 5)return true;
        else return false;
    }

    private static int compute_cost(int No, String str){
        if(check_valid(str)||!(No>=1&&No<=3)){
            System.out.println("非合法的数据库约定状态信息");
            return -1;
        }
        int cost = 0;
        int h1 = 0, h2 = 0, h3 = 0;//子模式启发式函数值
        // 按空格分割字符串
        String[] numbers = str.split(" ");
        if (numbers.length != 5) {
            System.out.println("输入的字符串不符合预期的格式");
            return -1;
        }
        //逐个处理字符
        switch (No){
            case 1:{//子模式1（1~5）
                int th = 1;//记录这是第几个数字

                for (String number : numbers) {
                    int num = Integer.parseInt(number);
                    //转换为棋盘上的坐标，从1开始索引
                    int x = num/4 + 1;
                    int y = num%4;
                    switch (th){
                        case 1: {
                            h1 += Math.abs(x-1)+Math.abs(y-1);
                            break;
                        }
                        case 2:{
                            h1 += Math.abs(x-1)+Math.abs(y-2);
                            break;
                        }
                        case 3:{
                            h1 += Math.abs(x-1)+Math.abs(y-3);
                            break;
                        }
                        case 4:{
                            h1 += Math.abs(x-1)+Math.abs(y-4);
                        }
                        case 5:{
                            h1 += Math.abs(x-2)+Math.abs(y-1);
                        }
                    }
                    th++;
                }
                break;
            }
            case 2:{//子模式2（6~10）
                int th = 1;//记录这是第几个数字
                for (String number : numbers) {
                    int num = Integer.parseInt(number);
                    //转换为棋盘上的坐标，从1开始索引
                    int x = num/4 + 1;
                    int y = num%4;
                    switch (th){
                        case 1: {
                            h2 += Math.abs(x-2)+Math.abs(y-2);
                            break;
                        }
                        case 2:{
                            h2 += Math.abs(x-2)+Math.abs(y-3);
                            break;
                        }
                        case 3:{
                            h2 += Math.abs(x-2)+Math.abs(y-4);
                            break;
                        }
                        case 4:{
                            h2 += Math.abs(x-3)+Math.abs(y-1);
                            break;
                        }
                        case 5:{
                            h2 += Math.abs(x-3)+Math.abs(y-2);
                        }
                    }
                    th++;
                }
                break;
            }
            case 3:{//子模式3（11~15）
                int th = 1;//记录这是第几个数字
                for (String number : numbers) {
                    int num = Integer.parseInt(number);
                    //转换为棋盘上的坐标，从1开始索引
                    int x = num/4 + 1;
                    int y = num%4;
                    switch (th){
                        case 1: {
                            h3 += Math.abs(x-3)+Math.abs(y-3);
                            break;
                        }
                        case 2:{
                            h3 += Math.abs(x-3)+Math.abs(y-4);
                            break;
                        }
                        case 3:{
                            h3 += Math.abs(x-4)+Math.abs(y-1);
                            break;
                        }
                        case 4:{
                            h3 += Math.abs(x-4)+Math.abs(y-2);
                            break;
                        }
                        case 5:{
                            h3 += Math.abs(x-4)+Math.abs(y-3);
                        }
                    }
                    th++;
                }
                break;
            }
        }

        if(No == 1)cost = h1;
        else if(No == 2)cost = h2;
        else cost = h3;

        return cost;
    }

    //处理状态转换，将表示棋盘的状态转换为子模式的状态
    private void trasform_state(String boardstate){
        //待补充...，将boardstate处理一下，得到submodel的三个值
    }

    static void dfs(List<Integer> path, boolean[] used) {
        if (path.size() == k) {
            // 输出当前排列的坐标
            StringBuilder bd = new StringBuilder();
            for (int e : path){
                bd.append(e).append(" ");
            }
            for (int i = 0; i < 3; i++){
                submodel[i] = bd.toString();
                int cost = compute_cost(i + 1, submodel[i]);
                // 插入数据库
                InsertPatternId(i + 1, submodel[i], cost);
            }
        }

        int N = size * size;
        for (int i = 0; i < N; i++) {
            if (!used[i]) {
                used[i] = true;
                path.add(i);
                dfs(path, used);
                path.removeLast();
                used[i] = false;
            }
        }
    }

    //整个预处理单独作为一个util使用
    public static void main(String[] args) {
        //遍历棋盘所有可能的状态...
        //for(...):trasform_state(boardstate)，计算三个子模式的值；
        //for(i:0~2):int cost += compute_cost(i+1,submodel[i]);//计算各个子模式的cost
        //调用数据库连接接口，存入数据，等ljh完成后，进一步补充
        List<Integer> path = new ArrayList<>();
        boolean[] used = new boolean[size * size];
        dfs(path, used);
    }
}
