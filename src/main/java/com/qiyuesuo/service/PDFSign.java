package com.qiyuesuo.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.qiyuesuo.client.ApaHttpsUtil;
import com.qiyuesuo.client.HttpRequestor;
import com.qiyuesuo.client.UploadFile;
import com.qiyuesuo.constant.TokenAndSecret;
import com.qiyuesuo.util.MD5;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Map;


import static com.itextpdf.text.BaseColor.BLACK;

/**
 *
 * @author SLX
 * @date 2018/11/21 16:19
 */
public class PDFSign {

    private static final Logger logger = LoggerFactory.getLogger(PDFSign.class);

    /**
     * 生成PDF文件
     * @param fileName 文件名
     * @throws Exception
     */
    public void createPDF(String fileName) throws Exception {
        Document document = new Document();

        PdfWriter.getInstance(document,new FileOutputStream(fileName));

        //生成字体
        BaseFont bfChinese = BaseFont.createFont("STSongStd-Light", "UniGB-UCS2-H", false);

        //标题字体
        Font f30 = new Font(bfChinese, 30, Font.NORMAL, BLACK);
        // 正文字体
        Font f12 = new Font(bfChinese, 12, Font.NORMAL, BLACK);
        Font f6 = new Font(bfChinese, 6, Font.NORMAL, BLACK);
        Font f8 = new Font(bfChinese, 8, Font.NORMAL, BLACK);

        document.open();

        // 标题

        Paragraph p1 = new Paragraph("采购单", f30);
        p1.setAlignment(1);
        p1.setAlignment(Element.ALIGN_CENTER);
        document.add(p1);

        // 换行
        document.add(new Chunk("\n"));



        // 添加table实例
        PdfPTable table = new PdfPTable(3);
        table.setWidthPercentage(100);
        table.setHorizontalAlignment(PdfPTable.ALIGN_LEFT);

        PdfPCell cell = new PdfPCell();
        cell.setFixedHeight(30);
        cell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);

        // 表格标题
        cell.setPhrase(new Paragraph("产品", f8));
        table.addCell(cell);
        cell.setPhrase(new Paragraph("数量", f8));
        table.addCell(cell);
        cell.setPhrase(new Paragraph("价格", f8));
        table.addCell(cell);


        // 表格数据
        PdfPCell row01 = new PdfPCell();
        row01.setFixedHeight(30);
        row01.setHorizontalAlignment(PdfPCell.LEFT);
        row01.setPhrase(new Paragraph("打印机", f8));
        table.addCell(row01);
        row01.setPhrase(new Paragraph("1", f8));
        table.addCell(row01);
        row01.setPhrase(new Paragraph("50000.00", f8));
        table.addCell(row01);

        // 表格数据
        PdfPCell row02 = new PdfPCell();
        row02.setFixedHeight(30);
        row02.setHorizontalAlignment(PdfPCell.LEFT);
        row02.setPhrase(new Paragraph("传真机", f8));
        table.addCell(row02);
        row02.setPhrase(new Paragraph("2", f8));
        table.addCell(row02);
        row02.setPhrase(new Paragraph("1100.00", f8));
        table.addCell(row02);
        // 表格数据
        PdfPCell row03 = new PdfPCell();
        row03.setFixedHeight(30);
        row03.setHorizontalAlignment(PdfPCell.LEFT);
        row03.setPhrase(new Paragraph("交换机", f8));
        table.addCell(row03);
        row03.setPhrase(new Paragraph("10", f8));
        table.addCell(row03);
        row03.setPhrase(new Paragraph("5000.00", f8));
        table.addCell(row03);

        document.add(table);
        // 换行
        document.add(new Chunk("\n"));
        Paragraph p = new Paragraph("公司盖章", f12);
        p.setAlignment(1);
        p.setAlignment(Element.ALIGN_RIGHT);
        document.add(p);

        document.add(new Chunk("\n"));
        Paragraph p3 = new Paragraph("abcd", f12);
        p3.setAlignment(1);
        p3.setAlignment(Element.ALIGN_RIGHT);
        document.add(p3);

        document.close();

        logger.info("文件生成成功！");

    }


    /**
     * 生成合同
     * @param map 参数集合
     * @return  合同Id
     */
    public String createByFile(Map<String,String> map){


        String token = TokenAndSecret.token; //token
        String secret = TokenAndSecret.secret; //secret
        Long timestamp1 = System.currentTimeMillis(); //时间戳
        String timestamp = Long.toString(timestamp1);
        String tst = token + secret + timestamp;
        String signature = MD5.toMD5(tst);


        //发送post请求 生成合同
        String s = ApaHttpsUtil.doPost("https://openapi.qiyuesuo.me/remote/contract/createbyfile",map, signature, token, timestamp);

        //获取docmentId
        String[] str1 = s.split(",");
        String str2 =  str1[2];
        String[] str3 = str2.split(":");
        String doc4 = str3[1];
        String docmentId = doc4.substring(1,doc4.length()-1);

        logger.info("合同生成成功！合同Id为"+docmentId);

        return docmentId;

    }

    /**
     * 通过原生JDK生成合同
     * @param file  生成合同所用文件 格式PDF
     * @param params 合同参数集合
     * @return 合同Id
     */
    public String createByJDK(File file, Map<String, String> params){

        String Jstr = UploadFile.uploadFile("https://openapi.qiyuesuo.me/remote/contract/createbyfile", file, params);

        JSONObject jsonObject = JSON.parseObject(Jstr);

        String documentId = jsonObject.getString("documentId");

        logger.info("合同生成成功！合同Id为"+documentId);

        return documentId;

    }


    /**
     * 合同签署
     * @param map 签署所需参数
     * @throws Exception
     */
    public void sign(Map<String,String> map) throws Exception {

        String token = TokenAndSecret.token; //token
        String secret = TokenAndSecret.secret; //secret
        Long timestamp1 = System.currentTimeMillis(); //时间戳
        String timestamp = Long.toString(timestamp1);
        String tst = token + secret + timestamp;
        String signature = MD5.toMD5(tst);

        //String s1 = ApaHttpsUtil.doPost("https://openapi.qiyuesuo.me/remote/signbyplatform", map, signature, token, timestamp);
        HttpRequestor httpRequestor = new HttpRequestor();
        String s = httpRequestor.doPost("https://openapi.qiyuesuo.me/remote/signbyplatform", map);

        logger.info("合同签署成功！"+s);

    }


    /**
     * 原生JDK签署合同
     * @param map  签署合同所需参数
     * @throws Exception
     */
    public void signByJDK(Map<String,String> map) throws Exception {

        String msg = UploadFile.uploadFile("https://openapi.qiyuesuo.me/remote/signbyplatform", null, map);

        logger.info("合同签署成功！");

    }

}
