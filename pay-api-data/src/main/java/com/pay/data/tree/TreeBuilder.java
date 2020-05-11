package com.pay.data.tree;

import com.pay.common.enums.MenuLevel;

import java.util.ArrayList;
import java.util.List;

public class TreeBuilder {


    public static List<MenuNode> buildMenuTree(List<MenuNode> treeMenuNodes) {
        List<MenuNode> trees = new ArrayList<>();
        for (MenuNode treeMenuNode : treeMenuNodes) {
            if (treeMenuNode.getPid().equals(treeMenuNode.getId())) {
                trees.add(treeMenuNode);
            }
            for (MenuNode it : treeMenuNodes) {
                if (!it.getPid().equals(it.getId()) && it.getPid().equals(treeMenuNode.getId())) {
                    if (treeMenuNode.getChildren() == null && !treeMenuNode.getMenuLevel().equals(MenuLevel.BUTTON)) {
                        treeMenuNode.setChildren(new ArrayList<>());
                    }
                    treeMenuNode.getChildren().add(it);
                }
            }
        }
        return trees;
    }

}
