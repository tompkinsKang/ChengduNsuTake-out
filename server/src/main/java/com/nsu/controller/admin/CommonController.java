package com.nsu.controller.admin;
/*
  Date:2025/3/13
  Time:12:25
  @author llh 
 */

import com.nsu.constant.MessageConstant;
import com.nsu.result.Result;
import com.nsu.utils.AliOssUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@RestController // 返回的是json数据
@RequestMapping("admin/common")
@Api(tags = "上传相关接口")
@Slf4j // 日志注解，可以直接使用log打印日志
public class CommonController {

    @Autowired
    AliOssUtil aliOssUtil;

    // 文档中返回值的data是String类型，所以泛型是String，返回的是图片的路径
    // MultipartFile是SpringBoot中的文件上传类
    @PostMapping("/upload")
    @ApiOperation("上传图片")
    public Result<String> upload(MultipartFile file){
        log.info("文件上传：{}", file); // 打印日志，{}是占位符，file是占位符的值
        // 调用阿里云工具类上传文件，参数是文件的字节数组和文件名
        try {
            // 截取原始文件名的后缀
            String originalFilename = file.getOriginalFilename();
                // 从最后一个.开始截取
                String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
                // 生成唯一的文件名
                String objectName = UUID.randomUUID()+extension;

                // 调用阿里云工具类上传文件，参数是文件的字节数组和文件名
            String filePath = aliOssUtil.upload(file.getBytes(), objectName);
            return Result.success(filePath);
        } catch (IOException e) {
            log.info("文件上传失败：{}", e);
        }
        return Result.error(MessageConstant.UPLOAD_FAILED);
    }
}

