package com.mindskip.xzs.service.impl;

import com.mindskip.xzs.configuration.property.QnConfig;
import com.mindskip.xzs.configuration.property.SystemConfig;
import com.mindskip.xzs.service.FileUpload;
import com.google.gson.Gson;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.Region;
import com.qiniu.storage.UploadManager;
import com.qiniu.storage.model.DefaultPutRet;
import com.qiniu.util.Auth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

@Service
public class FileUploadImpl implements FileUpload {
    private final Logger logger = LoggerFactory.getLogger(FileUpload.class);
    private final SystemConfig systemConfig;


    @Autowired
    public FileUploadImpl(SystemConfig systemConfig) {
        this.systemConfig = systemConfig;
    }

    // @Override
    public String uploadFile_ul_back(InputStream inputStream, long size, String extName) {
        QnConfig qnConfig = systemConfig.getQn();
        Configuration cfg = new Configuration(Region.region2());
        UploadManager uploadManager = new UploadManager(cfg);
        Auth auth = Auth.create(qnConfig.getAccessKey(), qnConfig.getSecretKey());
        String upToken = auth.uploadToken(qnConfig.getBucket());
        try {
            Response response = uploadManager.put(inputStream, null, upToken, null, null);
            DefaultPutRet putRet = new Gson().fromJson(response.bodyString(), DefaultPutRet.class);
            return qnConfig.getUrl() + "/" + putRet.key;
        } catch (QiniuException ex) {
            logger.error(ex.getMessage(), ex);
        }
        return null;
    }
    @Override
    public String uploadFile(InputStream inputStream, long size, String extName) {
        QnConfig qnConfig = systemConfig.getQn();
        Configuration cfg = new Configuration(Region.region2());
        UploadManager uploadManager = new UploadManager(cfg);
        Auth auth = Auth.create(qnConfig.getAccessKey(), qnConfig.getSecretKey());
        String upToken = auth.uploadToken(qnConfig.getBucket());
        try {
            //目标路径
            String filePath = "D:"+ File.separator+"imgs"+File.separator;
            File file = new File(filePath);
            //如果文件目录不存在，就执行创建
            if(!file.isDirectory()){
                file.mkdirs();
            }

            //SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHss");
            //目标文件名称
            //String targetName =sdf.format(new Date()) + ".jpg";
            String targetName =extName;
            System.out.println(targetName);
            //创建目标文件
            File targetFile = new File(filePath + targetName);
            FileOutputStream fos = new FileOutputStream(targetFile);

            //读取本地文件
            //File localFile = new File("E:"+File.separator+"1.jpg");
            //获取本地文件输入流
            //InputStream stream = new FileInputStream(localFile);

            //写入目标文件
            byte[] buffer = new byte[1024*1024];
            int byteRead = 0;
            while((byteRead=inputStream.read(buffer))!=-1){
                fos.write(buffer, 0, byteRead);
                fos.flush();
            }
            fos.close();
            inputStream.close();
      return "http://localhost:8000/images/" + targetName;
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        }
        return null;
    }
}
