package com.meetyou.blackhand;

import org.dom4j.Attribute;
import org.dom4j.Element;

import java.util.List;

/**
 * Created by Linhh on 2018/1/2.
 */

public class BlackhandAndroidXMLParser {

    public void getNodes(Element node){
        System.out.println("--------------------");

        //当前节点的名称、文本内容和属性
        System.out.println("当前节点名称："+node.getName());//当前节点名称
        System.out.println("当前节点的内容："+node.getTextTrim());//当前节点名称
        List<Attribute> listAttr=node.attributes();//当前节点的所有属性的list
        for(Attribute attr:listAttr){//遍历当前节点的所有属性
            String name=attr.getName();//属性名称
            String value=attr.getValue();//属性的值
            System.out.println("属性名称："+name+"属性值："+value);
        }

        //递归遍历当前节点所有的子节点
        List<Element> listElement=node.elements();//所有一级子节点的list
        for(Element e:listElement){//遍历所有一级子节点
            this.getNodes(e);//递归
        }
    }
}
