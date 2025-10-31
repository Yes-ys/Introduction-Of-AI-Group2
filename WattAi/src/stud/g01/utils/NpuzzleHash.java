package stud.g01.utils;
import stud.g01.problem.npuzzle.PuzzleBoard;

import java.util.HashMap;
import java.util.Map;

//预留一个类，后续可能要在hash上做优化
public class NpuzzleHash {
    private Map<String, Boolean> map;

    public NpuzzleHash(){
        map = new HashMap<>();
    }

    public boolean check(String boradstr) {
        if(map.get(boradstr)){
            map.put(boradstr,true);
            return false;
        }else return true;
    }

}
