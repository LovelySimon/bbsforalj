package com.alj.util;

import com.alj.config.AppConfig;
import com.alj.dto.FileUploadDto;
import com.alj.enums.FileUpLoadTypeEnum;
import com.alj.exception.BusinessException;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * @description: 上传图片工具类
 * @author alj
 * @date 2024/4/11 15:53
 * @version 1.0
 */

@Component
public class FileUtil {
    @Resource
    private AppConfig appConfig;

    private static final String BASE_PATH = "D:/java/bbsforalj/";
    private static final int MAX_ORIGINAL_NAME_LENGTH = 200;
    private static final int TRUNCATED_NAME_LENGTH = 190;
    private static final int THUMBNAIL_WIDTH = 200;
    private static final int THUMBNAIL_HEIGHT = 200;

    public FileUploadDto uploadFile2local(MultipartFile file, String folder, FileUpLoadTypeEnum typeEnum){
        try {
            FileUploadDto fileUploadDto = new FileUploadDto();
            String originalName = file.getOriginalFilename();
            String fileSuffix = StringTools.getFileSuffix(originalName);

            // Use MAX_ORIGINAL_NAME_LENGTH and TRUNCATED_NAME_LENGTH constants
            if (originalName.length() > MAX_ORIGINAL_NAME_LENGTH) {
                originalName = StringTools.getFileName(originalName).substring(0, TRUNCATED_NAME_LENGTH) + fileSuffix;
            }

            if (!ArrayUtils.contains(typeEnum.getSuffixArray(), fileSuffix)) {
                throw new BusinessException("文件类型不正确");
            }

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM");
            String month = LocalDate.now().format(formatter);
            File targetFileFolder;
            String fileName = StringTools.getRandomString(15) + fileSuffix;
            if (typeEnum==FileUpLoadTypeEnum.AVATAR){
                fileName=folder+fileSuffix;
            }
            File targetFile;
            String localPath;

            if (typeEnum == FileUpLoadTypeEnum.AVATAR) {
                // Use BASE_PATH constant for avatar upload path
                targetFileFolder = new File(BASE_PATH + "avatar/");
                targetFile = new File(targetFileFolder, fileName);
                localPath = "avatar/"+fileName;
            } else {
                targetFileFolder = new File(BASE_PATH + folder + month + "/");
                targetFile = new File(targetFileFolder, fileName);
                localPath = month + "/" + fileName;
            }

            if (!targetFileFolder.exists()) {
                targetFileFolder.mkdirs(); // Use mkdirs() to create parent directories as needed
            }

            file.transferTo(targetFile);

            // Use THUMBNAIL_WIDTH and THUMBNAIL_HEIGHT constants
            if (typeEnum == FileUpLoadTypeEnum.COMMENT_IMAGE) {
                String thumbFile = targetFile.getName().replace(".", "_.");
                File thumb = new File(targetFile.getParent(), thumbFile);
                Boolean thumbnailCreated = ImageUtils.createThumbnail(targetFile, THUMBNAIL_WIDTH, THUMBNAIL_HEIGHT, thumb);
                if (!thumbnailCreated) {
                    org.apache.commons.io.FileUtils.copyFile(targetFile, thumb);
                }
            } else if (typeEnum == FileUpLoadTypeEnum.AVATAR || typeEnum == FileUpLoadTypeEnum.ARTICLE_COVER) {
                ImageUtils.createThumbnail(targetFile, THUMBNAIL_WIDTH, THUMBNAIL_HEIGHT, targetFile);
            }

            fileUploadDto.setLocalPath(localPath);
            fileUploadDto.setOriginalFileName(originalName);
            return fileUploadDto;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException("上传文件失败");
        }
    }
}