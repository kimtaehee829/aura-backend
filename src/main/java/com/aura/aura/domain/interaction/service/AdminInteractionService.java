package com.aura.aura.domain.interaction.service;

import com.aura.aura.domain.interaction.entity.InteractionEvent;
import com.aura.aura.domain.interaction.repository.InteractionEventRepository;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminInteractionService {

    private final InteractionEventRepository interactionEventRepository;
    private final Storage storage;

    @Value("${gcp.assets-bucket-name}")
    private String bucketName;

    private static final String CSV_HEADER_SUMMARY = "총 이벤트 수,평균 체류 시간(ms),평균 회전 각도(도),제스처 완료율(%),최고 인기 부위,최고 인기 부위 횟수,최소 인기 부위,최소 인기 부위 횟수\n";
    private static final String CSV_HEADER_DATA = "세션 ID,진행 단계(Phase),대상 종류,대상 부위,상품 ID,제스처,체류 시간(ms),회전 각도(도),성공 여부,발생 시각\n";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public String generateAndUploadWeeklyReport() {
        byte[] excelBytes = generateWeeklyExcelReport();

        String objectPath = "reports/" + getWeeklyFileName();
        
        BlobInfo blobInfo = BlobInfo.newBuilder(bucketName, objectPath)
                .setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                .build();
                
        storage.create(blobInfo, excelBytes);
        
        return "https://storage.googleapis.com/" + bucketName + "/" + objectPath;
    }

    private byte[] generateWeeklyExcelReport() {
        LocalDate today = LocalDate.now();
        LocalDate lastMonday = today.with(TemporalAdjusters.previous(DayOfWeek.MONDAY)).minusWeeks(1);
        if (today.getDayOfWeek() == DayOfWeek.MONDAY) {
            lastMonday = today.minusWeeks(1);
        } else {
            lastMonday = today.with(TemporalAdjusters.previous(DayOfWeek.MONDAY));
        }
        
        LocalDate lastSunday = lastMonday.plusDays(6);

        LocalDateTime startOfLastWeek = lastMonday.atStartOfDay();
        LocalDateTime endOfLastWeek = lastSunday.atTime(LocalTime.MAX);

        List<InteractionEvent> events = interactionEventRepository.findByOccurredAtBetweenOrderByOccurredAtAsc(startOfLastWeek, endOfLastWeek);

        try (org.apache.poi.xssf.usermodel.XSSFWorkbook workbook = new org.apache.poi.xssf.usermodel.XSSFWorkbook()) {
            org.apache.poi.xssf.usermodel.XSSFSheet sheet = workbook.createSheet("Weekly Interactions");

            org.apache.poi.xssf.usermodel.XSSFCellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFillForegroundColor(org.apache.poi.ss.usermodel.IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(org.apache.poi.ss.usermodel.FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(org.apache.poi.ss.usermodel.HorizontalAlignment.LEFT);

            org.apache.poi.xssf.usermodel.XSSFCellStyle dataStyle = workbook.createCellStyle();
            dataStyle.setAlignment(org.apache.poi.ss.usermodel.HorizontalAlignment.LEFT);

            int rowIndex = 0;
            org.apache.poi.ss.usermodel.Row summaryHeaderRow = sheet.createRow(rowIndex++);
            String[] summaryHeaders = {"총 이벤트 수", "평균 체류 시간(ms)", "평균 회전 각도(도)", "제스처 완료율(%)", "최고 인기 부위", "최고 인기 부위 횟수", "최소 인기 부위", "최소 인기 부위 횟수"};
            for (int i = 0; i < summaryHeaders.length; i++) {
                org.apache.poi.ss.usermodel.Cell cell = summaryHeaderRow.createCell(i);
                cell.setCellValue(summaryHeaders[i]);
                cell.setCellStyle(headerStyle);
            }

            org.apache.poi.ss.usermodel.Row summaryDataRow = sheet.createRow(rowIndex++);
            if (events.isEmpty()) {
                summaryDataRow.createCell(0).setCellValue(0);
                summaryDataRow.createCell(1).setCellValue(0);
                summaryDataRow.createCell(2).setCellValue(0);
                summaryDataRow.createCell(3).setCellValue("0.0%");
                summaryDataRow.createCell(4).setCellValue("-");
                summaryDataRow.createCell(5).setCellValue("-");
                summaryDataRow.createCell(6).setCellValue("-");
                summaryDataRow.createCell(7).setCellValue("-");
                for (int i=0; i<8; i++) summaryDataRow.getCell(i).setCellStyle(dataStyle);
            } else {
                int totalEvents = events.size();
                double avgDwell = events.stream().mapToInt(InteractionEvent::getDwellMs).average().orElse(0.0);
                List<Integer> rotations = events.stream().filter(e -> e.getRotationDegrees() != null).map(InteractionEvent::getRotationDegrees).toList();
                double avgRotation = rotations.stream().mapToInt(Integer::intValue).average().orElse(0.0);
                long completedCount = events.stream().filter(InteractionEvent::getIsCompleted).count();
                double completionRate = ((double) completedCount / totalEvents) * 100.0;
                Map<String, Long> partCounts = events.stream().filter(e -> e.getTargetPart() != null && !e.getTargetPart().trim().isEmpty()).collect(Collectors.groupingBy(InteractionEvent::getTargetPart, Collectors.counting()));
                String mostPopularPart = "-";
                long mostPopularCount = 0;
                String leastPopularPart = "-";
                long leastPopularCount = 0;
                if (!partCounts.isEmpty()) {
                    Map.Entry<String, Long> maxEntry = partCounts.entrySet().stream().max(Map.Entry.comparingByValue()).orElse(null);
                    if (maxEntry != null) { mostPopularPart = maxEntry.getKey(); mostPopularCount = maxEntry.getValue(); }
                    Map.Entry<String, Long> minEntry = partCounts.entrySet().stream().min(Map.Entry.comparingByValue()).orElse(null);
                    if (minEntry != null) { leastPopularPart = minEntry.getKey(); leastPopularCount = minEntry.getValue(); }
                }
                
                summaryDataRow.createCell(0).setCellValue(totalEvents);
                summaryDataRow.createCell(1).setCellValue(Math.round(avgDwell));
                summaryDataRow.createCell(2).setCellValue(Math.round(avgRotation));
                summaryDataRow.createCell(3).setCellValue(String.format("%.1f%%", completionRate));
                summaryDataRow.createCell(4).setCellValue(mostPopularPart);
                summaryDataRow.createCell(5).setCellValue(mostPopularCount);
                summaryDataRow.createCell(6).setCellValue(leastPopularPart);
                summaryDataRow.createCell(7).setCellValue(leastPopularCount);
                for (int i=0; i<8; i++) summaryDataRow.getCell(i).setCellStyle(dataStyle);
            }

            rowIndex++; // 빈 줄 추가
            
            org.apache.poi.ss.usermodel.Row dataHeaderRow = sheet.createRow(rowIndex++);
            String[] dataHeaders = {"세션 ID", "진행 단계(Phase)", "대상 종류", "대상 부위", "상품 ID", "제스처", "체류 시간(ms)", "회전 각도(도)", "성공 여부", "발생 시각"};
            for (int i = 0; i < dataHeaders.length; i++) {
                org.apache.poi.ss.usermodel.Cell cell = dataHeaderRow.createCell(i);
                cell.setCellValue(dataHeaders[i]);
                cell.setCellStyle(headerStyle);
            }

            for (InteractionEvent event : events) {
                org.apache.poi.ss.usermodel.Row row = sheet.createRow(rowIndex++);
                row.createCell(0).setCellValue(event.getSession().getPublicId() != null ? event.getSession().getPublicId() : "");
                row.createCell(1).setCellValue(event.getPhase() != null ? event.getPhase() : "");
                row.createCell(2).setCellValue(event.getTargetType() != null ? event.getTargetType() : "");
                row.createCell(3).setCellValue(event.getTargetPart() != null ? event.getTargetPart() : "");
                
                Long productId = null;
                if (event.getTargetProduct() != null) {
                    productId = event.getTargetProduct().getId();
                } else if ("BAG_PART".equals(event.getTargetType()) && event.getSession().getBagProduct() != null) {
                    productId = event.getSession().getBagProduct().getId();
                }
                if (productId != null) row.createCell(4).setCellValue(productId);
                else row.createCell(4).setCellValue("");
                
                row.createCell(5).setCellValue(event.getGesture() != null ? event.getGesture() : "");
                row.createCell(6).setCellValue(event.getDwellMs() != null ? event.getDwellMs() : 0);
                row.createCell(7).setCellValue(event.getRotationDegrees() != null ? event.getRotationDegrees() : 0);
                row.createCell(8).setCellValue(event.getIsCompleted() != null && event.getIsCompleted() ? "O" : "X");
                row.createCell(9).setCellValue(event.getOccurredAt() != null ? event.getOccurredAt().format(DATE_FORMATTER) : "");
                for (int i=0; i<10; i++) row.getCell(i).setCellStyle(dataStyle);
            }

            for (int i = 0; i < 10; i++) {
                sheet.autoSizeColumn(i);
            }

            java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
            workbook.write(bos);
            return bos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Excel creation failed", e);
        }
    }

    public String getWeeklyFileName() {
        LocalDate today = LocalDate.now();
        LocalDate lastMonday;
        if (today.getDayOfWeek() == DayOfWeek.MONDAY) {
            lastMonday = today.minusWeeks(1);
        } else {
            lastMonday = today.with(TemporalAdjusters.previous(DayOfWeek.MONDAY));
        }
        LocalDate lastSunday = lastMonday.plusDays(6);

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyyMMdd");
        return "interactions_weekly_" + lastMonday.format(fmt) + "_" + lastSunday.format(fmt) + ".xlsx";
    }
}
