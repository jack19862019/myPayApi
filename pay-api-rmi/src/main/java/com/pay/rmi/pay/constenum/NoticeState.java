package com.pay.rmi.pay.constenum;

import java.util.HashMap;
import java.util.Map;

public enum NoticeState {

    init(0),succ(1), fail(-1);

    private static final Map<Integer,String> descMap = new HashMap<>(8);

    static{
        descMap.put(init.id(), "初始化");
        descMap.put(succ.id(), "成功");
        descMap.put(fail.id(), "失败");
    }

    private int id;

    private NoticeState(int id) {
        this.id = id;
    }

    public int id() {
        return id;
    }

}
