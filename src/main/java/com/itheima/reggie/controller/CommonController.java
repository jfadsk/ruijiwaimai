package com.itheima.reggie.controller;

import com.itheima.reggie.common.R;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.UUID;

/**
 * 文件上传
 */
@RestController
@RequestMapping("/common")
public class CommonController {

    @Value("${reggie.path}")
    private String basePath;

    @PostMapping("/upload")
    public R<String> upload(MultipartFile file) {
        // 获取原始的文件名 abc.jpg
        String filename = file.getOriginalFilename();
        // 获取的后缀
        String suffix = filename.substring(filename.lastIndexOf("."));
        // 通过UUID生成一个文件名防止文件重复
        filename = UUID.randomUUID().toString() + suffix;
        // 将其保存到本地
        File dir = new File(basePath);

        // 目录不存在时创建
        if (dir.exists()) {
            dir.mkdirs();
        }

        // 将临时文件储存到其本地
        try {
            file.transferTo(new File(basePath + filename));
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 返回文件名称
        return R.success(filename);
    }

    @GetMapping("/download")
    public void download(String name, HttpServletResponse response) {
        try {
            // 从本地磁盘进行读取
            FileInputStream fileInputStream = new FileInputStream(new File(basePath + name));
            // 从response中获取输出流对象
            ServletOutputStream outputStream = response.getOutputStream();
            response.setContentType("image/jpeg");
            // 进行读取
            int len = 0;
            byte[] bytes = new byte[1024];
            // 当bytes没有数据的时候 返回-1
            while((len = fileInputStream.read(bytes)) != -1) {
                outputStream.write(bytes, 0, len);
                outputStream.flush();
            }
            outputStream.close();
            fileInputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
