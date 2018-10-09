package com.blogspot.debukkitsblog;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class TestSerializableObject implements Serializable {
    private List<Integer> data = new LinkedList<>();
    public TestSerializableObject(){
        Random r = new Random();
        for(int i = 0; i < 20; i++){
            data.add(r.nextInt());
        }
    }
    public List<Integer> getData() {
        return data;
    }
}
