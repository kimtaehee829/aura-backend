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

    @Value("${gcp.bucket-name}")
    private String bucketName;

    // CSV 헤더 상수
    private static final String CSV_HEADER_SUMMARY = "Total Events,Average Dwell Time (ms),Average Rotation Degrees,Completion Rate (%),Most Popular Part,Most Popular Part Count,Least Popular Part,Least Popular Part Count\n";
    private static final String CSV_HEADER_DATA = "Session ID,Phase,Target Type,Target Part,Product ID,Gesture,Dwell Time (ms),Rotation (degrees),Completed,Occurred At\n";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public String generateAndUploadWeeklyReport() {
        String csvContent = generateWeeklyCsvReport();
        byte[] csvBytes = csvContent.getBytes(StandardCharsets.UTF_8);

        String objectPath = "reports/weekly-interactions.csv";
        
        BlobInfo blobInfo = BlobInfo.newBuilder(bucketName, objectPath)
                .setContentType("text/csv; charset=UTF-8")
                .build();
                
        storage.create(blobInfo, csvBytes);
        
        return "https://storage.googleapis.com/" + bucketName + "/" + objectPath;
    }

    private String generateWeeklyCsvReport() {
        // 지난 주 월요일 00:00:00 부터 일요일 23:59:59 까지 계산
        LocalDate today = LocalDate.now();
        LocalDate lastMonday = today.with(TemporalAdjusters.previous(DayOfWeek.MONDAY)).minusWeeks(1);
        if (today.getDayOfWeek() == DayOfWeek.MONDAY) {
            // 오늘이 월요일이면 previous(MONDAY)는 지난주 월요일임, minusWeeks(1) 하면 지지난주가 됨
            // TemporalAdjusters.previous(MONDAY) from Monday gives previous Monday.
            lastMonday = today.minusWeeks(1);
        } else {
            lastMonday = today.with(TemporalAdjusters.previous(DayOfWeek.MONDAY));
        }
        
        LocalDate lastSunday = lastMonday.plusDays(6);

        LocalDateTime startOfLastWeek = lastMonday.atStartOfDay();
        LocalDateTime endOfLastWeek = lastSunday.atTime(LocalTime.MAX);

        List<InteractionEvent> events = interactionEventRepository.findByOccurredAtBetweenOrderByOccurredAtAsc(startOfLastWeek, endOfLastWeek);

        StringBuilder csvBuilder = new StringBuilder();
        
        // UTF-8 BOM for Excel compatibility
        csvBuilder.append('\ufeff');

        // 요약표 작성
        appendSummary(csvBuilder, events);
        
        csvBuilder.append("\n"); // 한 줄 띄우기

        // 원본 데이터 작성
        csvBuilder.append(CSV_HEADER_DATA);
        for (InteractionEvent event : events) {
            appendEventRow(csvBuilder, event);
        }

        return csvBuilder.toString();
    }

    private void appendSummary(StringBuilder builder, List<InteractionEvent> events) {
        if (events.isEmpty()) {
            builder.append(CSV_HEADER_SUMMARY);
            builder.append("0,0,0,0.0%,-,-,-,-\n");
            return;
        }

        int totalEvents = events.size();
        
        double avgDwell = events.stream()
                .mapToInt(InteractionEvent::getDwellMs)
                .average()
                .orElse(0.0);

        List<Integer> rotations = events.stream()
                .filter(e -> e.getRotationDegrees() != null)
                .map(InteractionEvent::getRotationDegrees)
                .toList();
        
        double avgRotation = rotations.stream()
                .mapToInt(Integer::intValue)
                .average()
                .orElse(0.0);

        long completedCount = events.stream()
                .filter(InteractionEvent::getIsCompleted)
                .count();
        double completionRate = ((double) completedCount / totalEvents) * 100.0;

        // 부위별 카운트 (targetPart가 null이 아니고 빈 문자열이 아닌 것만)
        Map<String, Long> partCounts = events.stream()
                .filter(e -> e.getTargetPart() != null && !e.getTargetPart().trim().isEmpty())
                .collect(Collectors.groupingBy(InteractionEvent::getTargetPart, Collectors.counting()));

        String mostPopularPart = "-";
        long mostPopularCount = 0;
        String leastPopularPart = "-";
        long leastPopularCount = 0;

        if (!partCounts.isEmpty()) {
            Map.Entry<String, Long> maxEntry = partCounts.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .orElse(null);
            if (maxEntry != null) {
                mostPopularPart = maxEntry.getKey();
                mostPopularCount = maxEntry.getValue();
            }

            Map.Entry<String, Long> minEntry = partCounts.entrySet().stream()
                    .min(Map.Entry.comparingByValue())
                    .orElse(null);
            if (minEntry != null) {
                leastPopularPart = minEntry.getKey();
                leastPopularCount = minEntry.getValue();
            }
        }

        builder.append(CSV_HEADER_SUMMARY);
        builder.append(String.format("%d,%.0f,%.0f,%.1f%%,%s,%d,%s,%d\n",
                totalEvents, avgDwell, avgRotation, completionRate,
                mostPopularPart, mostPopularCount, leastPopularPart, leastPopularCount));
    }

    private void appendEventRow(StringBuilder builder, InteractionEvent event) {
        builder.append(escapeCsv(event.getSession().getPublicId())).append(",");
        builder.append(escapeCsv(event.getPhase())).append(",");
        builder.append(escapeCsv(event.getTargetType())).append(",");
        builder.append(escapeCsv(event.getTargetPart())).append(",");
        builder.append(event.getTargetProduct() != null ? event.getTargetProduct().getId() : "").append(",");
        builder.append(escapeCsv(event.getGesture())).append(",");
        builder.append(event.getDwellMs()).append(",");
        builder.append(event.getRotationDegrees() != null ? event.getRotationDegrees() : "").append(",");
        builder.append(event.getIsCompleted() ? "O" : "X").append(",");
        builder.append(event.getOccurredAt().format(DATE_FORMATTER)).append("\n");
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        value = value.replace("\"", "\"\"");
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value + "\"";
        }
        return value;
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
        return "interactions_weekly_" + lastMonday.format(fmt) + "_" + lastSunday.format(fmt) + ".csv";
    }
}
