package com.alj.controller;

import com.alj.config.WebConfig;
import com.alj.enums.ResponseCodeEnum;
import com.alj.exception.BusinessException;
import com.alj.util.Constants;
import com.alj.util.StringTools;
import com.alj.vo.ResponseVO;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/file")
public class FileController extends ABaseController{
    private static final Logger logger = LoggerFactory.getLogger(FileController.class);
    @Resource
    private WebConfig webConfig;


    /**
     * @description: 上传图片
     * @author alj
     * @date 2024/4/10 14:57
     * @version 1.0
     */

    @RequestMapping("/uploadImage")
    public ResponseVO uploadImage(MultipartFile file){
        if (file==null){
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        String fileName = file.getOriginalFilename();
        String fileExName = StringTools.getFileSuffix(fileName);
        if (!ArrayUtils.contains(Constants.IMAGE_SUFFIX,fileExName)){
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        String path=copyFile(file);
        Map<String,String> fileMap=new HashMap<>();
        fileMap.put("fileName",path);
        return getSuccessResponseVO(fileMap);
    }
    private String copyFile(MultipartFile file){
        try {
            String uploadDir = "D:/java/bbsforalj/images/"; // 指定文件保存的目录
            String fileName = file.getOriginalFilename(); // 获取原始文件名
            String fileExName = fileName.substring(fileName.lastIndexOf(".")); // 获取文件扩展名
            String fileRealName = StringTools.getRandomString(15) + fileExName; // 生成新的文件名
            Path copyLocation = Paths.get(uploadDir + File.separator + fileRealName); // 确定文件的保存路径
            Files.copy(file.getInputStream(), copyLocation); // 将文件从临时位置复制到最终位置
            return  fileRealName; // 返回文件的保存路径
        } catch (Exception e) {
            e.printStackTrace();
            // 处理异常，例如可以抛出自定义异常或返回错误信息
            throw new BusinessException("Could not store file. Error: " + e.getMessage());
        }
    }

    /**
     * @description: 读取封面
     * @author alj
     * @date 2024/4/16 20:31
     * @version 1.0
     */
    @RequestMapping ("/getImage/{imageFolder}/{imageName}")
    public void readCover(HttpServletResponse response,  @PathVariable("imageFolder") String imageFolder,@PathVariable("imageName")String imageName) {
        String uploadDir = "D:/java/bbsforalj/images/"; // 这应该是你的基础图片存储目录
        String filePath = uploadDir  +imageFolder+ "/" + imageName;
        System.out.println(filePath);
        File imageFile = new File(filePath);
        if (imageFile.exists()) {
            String mimeType = URLConnection.guessContentTypeFromName(imageFile.getName());
            if (mimeType == null) {
                // 如果无法从文件名推断MIME类型，则默认使用
                mimeType = "application/octet-stream";
            }

            response.setContentType(mimeType);
            response.setContentLength((int) imageFile.length());
            long cacheAge = 60 * 60 * 24 * 30; // 例如, 缓存30天
            response.setHeader("Cache-Control", "max-age=" + cacheAge);

            try (FileInputStream in = new FileInputStream(imageFile);
                 OutputStream out = response.getOutputStream()) {

                byte[] buf = new byte[1024];
                int count;
                while ((count = in.read(buf)) >= 0) {
                    out.write(buf, 0, count);
                }
            } catch (Exception e) {
                // 处理读取或写入错误
                e.printStackTrace();
            }
        } else {
            // 如果文件不存在，设置HTTP状态码为404
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
    }
    /**
     * @description: 获取正文图片
     * @author alj
     * @date 2024/4/10 16:16
     * @version 1.0
     */
    @RequestMapping ("/getImage/{imageName}")
    public void readImage(HttpServletResponse response,  @PathVariable("imageName")String imageName) {
        String uploadDir = "D:/java/bbsforalj/images/"; // 这应该是你的基础图片存储目录
        String filePath = uploadDir  + "/" + imageName;
        System.out.println(filePath);
        File imageFile = new File(filePath);
        if (imageFile.exists()) {
            String mimeType = URLConnection.guessContentTypeFromName(imageFile.getName());
            if (mimeType == null) {
                // 如果无法从文件名推断MIME类型，则默认使用
                mimeType = "application/octet-stream";
            }

            response.setContentType(mimeType);
            response.setContentLength((int) imageFile.length());
            long cacheAge = 60 * 60 * 24 * 30; // 例如, 缓存30天
            response.setHeader("Cache-Control", "max-age=" + cacheAge);

            try (FileInputStream in = new FileInputStream(imageFile);
                 OutputStream out = response.getOutputStream()) {

                byte[] buf = new byte[1024];
                int count;
                while ((count = in.read(buf)) >= 0) {
                    out.write(buf, 0, count);
                }
            } catch (Exception e) {
                // 处理读取或写入错误
                e.printStackTrace();
            }
        } else {
            // 如果文件不存在，设置HTTP状态码为404
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
    }

    @RequestMapping("/getAvatar/{userId}")
    public void getAvatar(HttpServletResponse response, @PathVariable("userId") String userId) {
        String basePath = "D:/java/bbsforalj/avatar/";
        String userAvatarPath = basePath + userId + ".png"; // 假设头像都是PNG格式
        String defaultAvatarPath = basePath + "default.png";
        // 首先尝试用户的头像，如果不存在，使用默认头像
        File avatarFile = new File(userAvatarPath);
        if (!avatarFile.exists()) {
            avatarFile = new File(defaultAvatarPath); // 使用默认头像
        }

        // 确定MIME类型
        String mimeType = URLConnection.guessContentTypeFromName(avatarFile.getName());
        if (mimeType == null) {
            mimeType = "image/png"; // 如果无法确定，则假设为image/png
        }
        response.setContentType(mimeType);

        // 将头像文件写入响应
        try (FileInputStream in = new FileInputStream(avatarFile);
             OutputStream out = response.getOutputStream()) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }


    }