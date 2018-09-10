package com.mmall.util;
import java.io.File;
import java.io.FileReader;
import java.io.LineNumberReader;
import java.io.IOException;
/**
 * @author zez
 *
 * 源文件代码行统计工具Java版 :)
 */
public class Clog {
    //多文件代码统计汇总结果
    private int gcommentLineNum = 0;
    private int gcodeAndCommentLinNum = 0;
    private int gblankLineNum = 0;
    private int gtotalLineNum = 0;
    private int gblankAndCommentLinNum = 0;
    //单个文件统计信息
    private LineNumberReader lReader;
    private int commentLineNum = 0;
    private int codeAndCommentLinNum = 0;
    private int blankLineNum = 0;
    private int totalLineNum = 0;
    private int blankAndCommentLinNum = 0;

    //	private int logicLineNum = 0;
//初始化
    private void init() {
        lReader = null;
        commentLineNum = 0;
        codeAndCommentLinNum = 0;
        blankLineNum = 0;
        totalLineNum = 0;
        blankAndCommentLinNum = 0;
//	logicLineNum = 0;
    }

    /**
     * 判断文件类型,并取得此文件
     *
     * @param fileName
     * @return file, if not a source code file, return NULL .
     */
    private File getSourceFile(String fileName) {
        if (fileName == null || fileName.trim().length() == 0) {
            System.out.println("\nThe file name /* is null !\n");
            return null;
        }
        File file = new File(fileName);
//	如果是目录,返回此目录
        if (file.isDirectory()) {
            return file;
        }
//文件是否存在
        if (!file.exists()) {
            System.out.println("\nThe file " + fileName
                    + " is don't exists !\n");
            return null;
        }
/**
 * 判断是否是.c .cpp .java文件
 */
        if (fileName.indexOf('.') > 0) {
            String fileExt = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
            if (!fileExt.equals("c") && !fileExt.equals("cpp") && !fileExt.equals("h")
                    && !fileExt.equals("java") && !fileExt.equals("hpp")) {
                System.out.println("\nThe file " + fileName
                        + " is not a C or C++ or JAVA source file !\n");
                return null;
            }
        }
//文件大小是否大于64k,不过大于64k仍可以计算
        if (file.length() > 65535) {
            System.out.println("\nThe file " + fileName
                    + " is too large than 64k ,but It can work :)!\n");
        }
//文件大小如果小于3个字节,返回空
        if (file.length() < 3) {
            System.out.println("\nThe file " + fileName
                    + " has no content \n");
            return null;
        }
        return file;
    }

    /**
     * 打开文件
     *
     * @param file
     * @throws Exception
     */
    private void openFile(File file) throws Exception {
        try {
            lReader = new LineNumberReader(new FileReader(file));
        } catch (Exception e) {
            throw e;
        }
    }

    /***************************************************************************
     * 行数计算主函数 算法: 循环每次读取一行,分几种情况进行计算 1.空行 2.//开头 肯定为注释行 3.//在代码后面,
     * 按代码行算,并处理//在字符串中情况 4.以/*开头的情况,调用专门块注释计算方法 5./* 在代码后面情况,处理同上
     *
     */
    private void countLineNum() throws Exception {
        try {
            while (true) { //未到文件尾
                String str = lReader.readLine(); //取得一行代码
                totalLineNum++; //总行数加一
                if (str == null) { //判断是否为文件尾
                    totalLineNum--;
                    return;
                } else { //否则进行计算
                    str = str.trim(); //去两头空格
//	countLogicLineNum(str);
                    if (str.length() == 0) { //如果为空,则为空行,空行数加一
                        blankLineNum++;
                    } else if (str.startsWith("//")) { //如果是以//开头,为注释行,即使此行有//注释,但非开头,则要按代码行算
                        commentLineNum++;
                    } else if (str.indexOf("//") > 0 && isNotInStr(str, "//")) { // //在行中,并判断不是在字符串中
                        codeAndCommentLinNum++;
                    } else if (str.indexOf("/*") >= 0) {
//如果/*在行中,判断是否是在""内 ,否则为注释行
                        if (isNotInStr(str, "/*")) {
                            countCommentLin(str);//计算/**/ 内的注释行数
                        }
                    }
                }
            }
        } catch (IOException e) {
            throw new Exception("文件读取时出错 !\n");
        }
    }

    /**
     * 块注释计算方法 判断是否块注释结束 否则读下一行 循环
     */
    private void countCommentLin(String str) throws Exception {
        try {
            commentLineNum++;
//处理 /*some comment */ 即只一行的情况,并处理 /*/ 这种情况
            if ((str.substring(str.indexOf("/*") + 2).indexOf("*/") >= 0)) {
                //是否有代码
                if (str.length() > str.indexOf("*/") - str.indexOf("/*")) {
                    codeAndCommentLinNum++;
                    commentLineNum--;
                    //	countLogicLineNum(str);
                }
//处理 /*xxx*/ some code /*  这种情况
                if (str.lastIndexOf("*/") > str.lastIndexOf("/*") + 1) {
//	countLogicLineNum(str);
                    return;
                }
            }
            //一般情况, some code /* some comment
            if (str.indexOf("/*") > 0) {
                codeAndCommentLinNum++;
                commentLineNum--;
            }
            do {
                str = lReader.readLine(); //不是注释尾,取下一行代码
                totalLineNum++;
//如果到文件尾,返回
                if (str == null) {
                    totalLineNum--;
                    return;
                }
                str = str.trim(); //去空格
//是否到注释尾,如果后面还有代码,按混合行算
                if (str.indexOf("*/") >= 0) {
                    commentLineNum++;
                    if (str.length() > str.indexOf("*/") + 2) {
                        codeAndCommentLinNum++;
                        commentLineNum--;
//	countLogicLineNum(str);
                    }
                    return;
                } else {
                    if (str.length() == 0) { //是空行
                        blankLineNum++; //空行加一
                        blankAndCommentLinNum++; //注释中空行加一
                    } else {
                        commentLineNum++; //注释加一
                    }
                }
            } while (true);
        } catch (IOException e) {
            throw new Exception("文件读取时出错 !\n");
        }
    }

    /*
     * 判断 某字符 是否是字符串中,特别注释字符. 算法 : 字符串中是否有注释符号 如没有,返回 false 字符串中有 "
     * ,如没有,返回true 注释符号在""之前,返回true;否则,继续 str = str的非\"的"后面部分 循环
     */
    private boolean isNotInStr(String str, String subStr) {
        while (str.indexOf(subStr) >= 0) {
            if (str.indexOf('"') >= 0) {
                if (str.indexOf('"') > str.indexOf(subStr)) {
                    return true;
                } else {
                    str = str.substring(str.indexOf('"') + 1);
                    while (str.indexOf('\\') >= 0
                            && str.indexOf('"') == str.indexOf('\\') + 1) {
                        str = str.substring(str.indexOf('"') + 1);
                    }
                    str = str.substring(str.indexOf('"') + 1);
                }
            } else {
                return true;
            }
        }
        return false;
    }
}