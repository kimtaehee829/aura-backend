package com.aura.aura.domain.output.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.List;

@Slf4j
@Component
public class SoulTagImageGenerator {

    private final BufferedImage template;
    private final Font font;

    public SoulTagImageGenerator() {
        try (InputStream imgStream = new ClassPathResource("assets/soultag/template.png").getInputStream();
             InputStream fontStream = new ClassPathResource("assets/soultag/Pretendard-Regular.ttf").getInputStream()) {
            
            this.template = ImageIO.read(imgStream);
            this.font = Font.createFont(Font.TRUETYPE_FONT, fontStream).deriveFont(12f);
            
        } catch (Exception e) {
            log.error("SoulTagImageGenerator 에셋 초기화에 실패했습니다.", e);
            throw new RuntimeException("SoulTagImageGenerator 에셋 초기화에 실패했습니다.", e);
        }
    }

    private BufferedImage copyTemplate() {
        java.awt.image.ColorModel cm = template.getColorModel();
        boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
        java.awt.image.WritableRaster raster = template.copyData(null);
        return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
    }

    public byte[] generate(String bagName, List<String> auraCodes, String mood, String styling, String storeName, String date) {
        BufferedImage image = copyTemplate();
        Graphics2D g = image.createGraphics();

        try {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

            double scale = (double) template.getWidth() / 344.0;

            Font scaledFont = this.font.deriveFont((float) (12 * scale));
            g.setFont(scaledFont);
            g.setColor(Color.decode("#525252"));

            FontMetrics fm = g.getFontMetrics();
            int ascent = fm.getAscent();

            int baseX = (int) Math.round(108 * scale);
            
            g.drawString(bagName != null ? bagName : "Unknown", baseX, (int) Math.round(70 * scale) + ascent);
            g.drawString(mood != null ? mood : "Unknown", baseX, (int) Math.round(112 * scale) + ascent);
            
            String displayStyling = (styling != null && !styling.trim().isEmpty()) ? styling : "—";
            g.drawString(displayStyling, baseX, (int) Math.round(134 * scale) + ascent);
            
            g.drawString(storeName != null ? storeName : "Unknown", baseX, (int) Math.round(155 * scale) + ascent);
            g.drawString(date != null ? date : "Unknown", baseX, (int) Math.round(176 * scale) + ascent);

            int currentX = baseX;
            int circleY = (int) Math.round(91 * scale);
            int circleSize = (int) Math.round(10 * scale);
            int circleToTextSpacing = (int) Math.round(4 * scale);
            int textToNextCircleSpacing = (int) Math.round(6 * scale);

            int textY = circleY + (circleSize - fm.getHeight()) / 2 + fm.getAscent();

            for (String hex : auraCodes) {
                if (hex == null || hex.trim().isEmpty()) continue;
                String normalizedHex = hex.trim();
                if (!normalizedHex.startsWith("#")) {
                    normalizedHex = "#" + normalizedHex;
                }
                
                g.setColor(Color.decode(normalizedHex));
                g.fillOval(currentX, circleY, circleSize, circleSize);
                
                currentX += circleSize + circleToTextSpacing;
                
                g.setColor(Color.decode("#525252"));
                String hexText = hex.toUpperCase();
                g.drawString(hexText, currentX, textY);
                
                int textWidth = fm.stringWidth(hexText);
                currentX += textWidth + textToNextCircleSpacing;
            }

            try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                ImageIO.write(image, "png", baos);
                return baos.toByteArray();
            }

        } catch (Exception e) {
            log.error("Soul Tag 이미지 생성에 실패했습니다.", e);
            throw new RuntimeException("Soul Tag 이미지 생성에 실패했습니다.", e);
        } finally {
            g.dispose();
        }
    }
}
