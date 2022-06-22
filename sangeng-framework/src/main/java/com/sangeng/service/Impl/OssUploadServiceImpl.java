package com.sangeng.service.Impl;

import com.google.gson.Gson;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.Region;
import com.qiniu.storage.UploadManager;
import com.qiniu.storage.model.DefaultPutRet;
import com.qiniu.util.Auth;
import com.sangeng.domain.ResponseResult;
import com.sangeng.enums.AppHttpCodeEnum;
import com.sangeng.exception.SystemException;
import com.sangeng.service.UploadService;
import com.sangeng.utils.PathUtils;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileInputStream;
import java.io.InputStream;

@Service
@Data
@ConfigurationProperties(prefix = "oss")
public class OssUploadServiceImpl implements UploadService {
    @Override
    public ResponseResult uploadImg(MultipartFile img) {
        //判断文件类型
        //获取原始文件名
        String originalFilename = img.getOriginalFilename();
        //对原始文件进行判断
        if(!originalFilename.endsWith(".png")&&!originalFilename.endsWith(".jpg")){
            throw new SystemException(AppHttpCodeEnum.FILE_TYPE_ERROR);
        }
        //如果判断通过上传文件到OSS
        String filePath = PathUtils.generateFilePath(originalFilename);
        String url = uploadOss(img,filePath);
        return ResponseResult.okResult(url);
    }

    private  String accesskey;
    private  String sercetKey;
    private  String bucket;

    private String uploadOss(MultipartFile imgFile, String filePath){
            //构造一个带指定 Region 对象的配置类
            Configuration cfg = new Configuration(Region.autoRegion());
            //...其他参数参考类注释
            UploadManager uploadManager = new UploadManager(cfg);
            //默认不指定key的情况下，以文件内容的hash值作为文件名
            String key = "sg.jpg";
            try {
                InputStream inputStream = imgFile.getInputStream();
                Auth auth = Auth.create(accesskey, sercetKey);
                String upToken = auth.uploadToken(bucket);
                try {
                    Response response = uploadManager.put(inputStream,key,upToken,null, null);
                    //解析上传成功的结果
                    DefaultPutRet putRet = new Gson().fromJson(response.bodyString(), DefaultPutRet.class);
                    System.out.println(putRet.key);
                    System.out.println(putRet.hash);
                    return "sss";
                } catch (QiniuException ex) {
                    Response r = ex.response;
                    System.err.println(r.toString());
                    try {
                        System.err.println(r.bodyString());
                    } catch (QiniuException ex2) {
                        //ignore
                    }
                }
            } catch (Exception ex) {
                //ignore
            }
            return "www";
        }
}
