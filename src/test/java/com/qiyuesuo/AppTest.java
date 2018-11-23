package com.qiyuesuo;



import com.qiyuesuo.service.PDFSign;
import org.junit.Test;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Unit test for simple App.
 */
public class AppTest {

    /**
     * 测试生成PDF同时完成远程签署
     * @throws
     */
    @Test
    public void createAgreement() throws Exception {


        PDFSign pdfSign = new PDFSign();


        //生成PDF
        pdfSign.createPDF("D://table1.pdf");


        //生成合同参数
        Map<String,String> map01 = new HashMap<String,String>();
        map01.put("subject","测试合同"); //必要参数
        map01.put("file","D://table1.pdf"); //必要参数
        map01.put("docName","测试哦");

        //生成合同 同时返回合同Id
        String documentId = pdfSign.createByFile(map01);

        //签章参数
        Map<String,String> map02 = new HashMap<String,String>();
        map02.put("documentId",documentId); //必要参数
        map02.put("visible","true"); //必要参数
        map02.put("sealId","2384277523477803569"); //visible为true时必须
        map02.put("location","{\"offsetX\":0.7,\"offsetY\":0.5,\"page\":1}");

        //完成签署
        pdfSign.sign(map02);

    }

    /**
     * 使用JDK生成合同
     * @throws Exception
     */
    @Test
    public void upload() throws Exception {

        PDFSign pdfSign = new PDFSign();

        //生成PDF
        pdfSign.createPDF("D://table2.pdf");


        //生成合同 同时返回合同Id
        Map<String,String> map01 = new HashMap<String,String>();
        map01.put("subject","asdf"); //必要参数
        map01.put("docName","测试哦");

        //生成合同 同时返回合同Id
        File file = new File("D://table2.pdf");
        String documentId = pdfSign.createByJDK(file, map01);

        //签章参数
        Map<String,String> map02 = new HashMap<String,String>();
        map02.put("documentId",documentId); //必要参数
        map02.put("visible","true"); //必要参数
        map02.put("sealId","2384277523477803569"); //visible为true时必须
        map02.put("location","{\"offsetX\":0.7,\"offsetY\":0.5,\"page\":1}");

        //完成签署
        pdfSign.signByJDK(map02);
    }

}
