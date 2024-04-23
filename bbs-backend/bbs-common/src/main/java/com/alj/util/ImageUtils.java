package com.alj.util;


import com.alj.config.AppConfig;
import com.alj.util.Constants;
import com.alj.enums.DateTimePatternEnum;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class ImageUtils {

    @Resource
    private AppConfig appConfig;

    public String resetImageHtml(String html) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM");
        String month = LocalDate.now().format(formatter);
        List<String> imageList = getImageList(html);
        for (String img : imageList) {
            resetImage(img, month);
        }
        return month;
    }


    private String resetImage(String imagePath, String month) {
        if (StringTools.isEmpty(imagePath) || !imagePath.contains(Constants.FILE_FOLDER_TEMP_2)) {
            return imagePath;
        }
        imagePath = imagePath.replace(Constants.READ_IMAGE_PATH, "");
        if (StringTools.isEmpty(month)) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM");
             month = LocalDate.now().format(formatter);
        }
        String imageFileName = month + "/" + imagePath.substring(imagePath.lastIndexOf("/") + 1);

        // 使用之前定义的路径常量
        File targetFile = new File(Constants.PROJECT_FOLDER + Constants.FILE_FOLDER_FILE + Constants.FILE_FOLDER_IMAGE + imageFileName);
        try {
            FileUtils.copyFile(new File(Constants.PROJECT_FOLDER + Constants.FILE_FOLDER_FILE + imagePath), targetFile);
        } catch (IOException e) {
            e.printStackTrace();
            return imagePath;
        }
        return imageFileName;
    }

    private List<String> getImageList(String content) {
        List<String> imageList = new ArrayList<>();
        String regEx_img = "(<img.*src\\s*=\\s*(.*?)[^>]*?>)";
        Pattern p_image = Pattern.compile(regEx_img, Pattern.CASE_INSENSITIVE);
        Matcher m_image = p_image.matcher(content);
        while (m_image.find()) {
            String img = m_image.group();
            Matcher m = Pattern.compile("src\\s*=\\s*\"?(.*?)(\"|>|\\s+)")
                    .matcher(img);
            while (m.find()) {
                String imageUrl = m.group(1);
                imageList.add(imageUrl);
            }
        }
        return imageList;
    }

    /**
     * @description: 压缩图片
     * @author alj
     * @date 2024/4/11 15:48
     * @version 1.0
     */
    public  static Boolean createThumbnail(File file,int thumbnailWidth,int Height,File targetFile){
        try {
            BufferedImage src= ImageIO.read(file);
            int sorceW=src.getWidth();
            int sorceH= src.getHeight();
            if (sorceW<=thumbnailWidth){
                return false;
            }
            int height = sorceH;
            if (sorceW>thumbnailWidth){
                height=thumbnailWidth*sorceH/sorceW;
            }else{
                thumbnailWidth=sorceW;
                height=sorceH;
            }
            BufferedImage dst = new BufferedImage(thumbnailWidth,height,BufferedImage.TYPE_INT_RGB);
            Image scaleImage = src.getScaledInstance(thumbnailWidth,height,Image.SCALE_SMOOTH);
            Graphics2D g = dst.createGraphics();
            g.drawImage(scaleImage,0,0,thumbnailWidth,height,null);
            g.dispose();
            int resultH=dst.getHeight();
            if (resultH>Height){
                resultH=Height;
                dst.getSubimage(0,0,thumbnailWidth,resultH);
            }
            ImageIO.write(dst,"JPEG",targetFile);
            return true;
        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }
}
