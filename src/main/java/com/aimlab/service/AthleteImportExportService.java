package com.aimlab.service;

import com.aimlab.dto.ExportFile;
import com.aimlab.dto.ImportResult;
import com.aimlab.entity.Athlete;
import com.aimlab.entity.User;
import com.aimlab.mapper.AthleteMapper;
import com.aimlab.mapper.UserMapper;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 运动员档案导入导出服务
 */
@Service
public class AthleteImportExportService {

    private static final String[] HEADERS = {
            "userId", "username", "name", "gender", "birthDate", "level", "approvalStatus", "createdAt", "updatedAt"
    };

    @Autowired
    private AthleteMapper athleteMapper;

    @Autowired
    private UserMapper userMapper;

    /**
     * 导出运动员档案
     */
    public ExportFile exportAthletes(String format) {
        List<Athlete> athletes = athleteMapper.findAll();
        Map<Long, User> userMap = userMapper.findAll().stream()
                .collect(Collectors.toMap(User::getId, u -> u));

        if ("xlsx".equalsIgnoreCase(format)) {
            return exportAsExcel(athletes, userMap);
        }
        return exportAsCsv(athletes, userMap);
    }

    private ExportFile exportAsCsv(List<Athlete> athletes, Map<Long, User> userMap) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.join(",", HEADERS)).append("\n");
        for (Athlete athlete : athletes) {
            User user = userMap.get(athlete.getUserId());
            sb.append(athlete.getUserId()).append(",");
            sb.append(user != null ? safe(user.getUsername()) : "").append(",");
            sb.append(safe(athlete.getName())).append(",");
            sb.append(safe(athlete.getGender())).append(",");
            sb.append(athlete.getBirthDate() != null ? athlete.getBirthDate() : "").append(",");
            sb.append(safe(athlete.getLevel())).append(",");
            sb.append(safe(athlete.getApprovalStatus())).append(",");
            sb.append(athlete.getCreatedAt() != null ? athlete.getCreatedAt() : "").append(",");
            sb.append(athlete.getUpdatedAt() != null ? athlete.getUpdatedAt() : "").append("\n");
        }
        byte[] data = sb.toString().getBytes(StandardCharsets.UTF_8);
        return new ExportFile("athletes.csv", "text/csv;charset=UTF-8", data);
    }

    private ExportFile exportAsExcel(List<Athlete> athletes, Map<Long, User> userMap) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Athletes");
            Row header = sheet.createRow(0);
            for (int i = 0; i < HEADERS.length; i++) {
                header.createCell(i).setCellValue(HEADERS[i]);
            }

            int rowIndex = 1;
            for (Athlete athlete : athletes) {
                Row row = sheet.createRow(rowIndex++);
                int col = 0;
                User user = userMap.get(athlete.getUserId());
                row.createCell(col++).setCellValue(athlete.getUserId());
                row.createCell(col++).setCellValue(user != null ? user.getUsername() : "");
                row.createCell(col++).setCellValue(athlete.getName());
                row.createCell(col++).setCellValue(athlete.getGender());
                row.createCell(col++).setCellValue(athlete.getBirthDate() != null ? athlete.getBirthDate().toString() : "");
                row.createCell(col++).setCellValue(athlete.getLevel());
                row.createCell(col++).setCellValue(athlete.getApprovalStatus());
                row.createCell(col++).setCellValue(athlete.getCreatedAt() != null ? athlete.getCreatedAt().toString() : "");
                row.createCell(col).setCellValue(athlete.getUpdatedAt() != null ? athlete.getUpdatedAt().toString() : "");
            }

            workbook.write(bos);
            return new ExportFile("athletes.xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", bos.toByteArray());
        } catch (Exception e) {
            throw new RuntimeException("导出Excel失败: " + e.getMessage(), e);
        }
    }

    /**
     * 导入运动员档案
     */
    @Transactional
    public ImportResult importAthletes(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        String format = originalFilename != null ? originalFilename.substring(originalFilename.lastIndexOf('.') + 1) : "csv";
        format = format.toLowerCase(Locale.ROOT);
        try (InputStream is = file.getInputStream()) {
            if ("xlsx".equals(format) || "xls".equals(format)) {
                return importFromExcel(is);
            }
            return importFromCsv(is);
        } catch (Exception e) {
            throw new RuntimeException("导入失败: " + e.getMessage(), e);
        }
    }

    private ImportResult importFromCsv(InputStream inputStream) throws Exception {
        ImportResult result = new ImportResult();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            boolean firstLine = true;
            int rowNum = 0;
            while ((line = reader.readLine()) != null) {
                rowNum++;
                if (firstLine) {
                    firstLine = false;
                    continue;
                }
                if (line.trim().isEmpty()) {
                    continue;
                }
                String[] parts = line.split(",", -1);
                processRow(parts, rowNum, result);
            }
        }
        return result;
    }

    private ImportResult importFromExcel(InputStream inputStream) throws Exception {
        ImportResult result = new ImportResult();
        try (Workbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            int rowNum = 0;
            for (Row row : sheet) {
                rowNum++;
                if (rowNum == 1) {
                    continue; // skip header
                }
                if (isRowEmpty(row)) {
                    continue;
                }
                String[] parts = new String[HEADERS.length];
                for (int i = 0; i < HEADERS.length; i++) {
                    Cell cell = row.getCell(i);
                    parts[i] = cell == null ? "" : cell.toString();
                }
                processRow(parts, rowNum, result);
            }
        }
        return result;
    }

    private void processRow(String[] parts, int rowNum, ImportResult result) {
        try {
            if (parts.length < 6) {
                throw new RuntimeException("列数不足");
            }
            Long userId = Long.parseLong(parts[0].trim());
            User user = userMapper.findById(userId);
            if (user == null) {
                throw new RuntimeException("用户ID不存在: " + userId);
            }

            String name = parts[2].trim();
            if (name.isEmpty()) {
                throw new RuntimeException("姓名不能为空");
            }

            String gender = valueOrDefault(parts, 3, "UNKNOWN").toUpperCase(Locale.ROOT);
            String level = valueOrDefault(parts, 5, null);
            String approvalStatus = valueOrDefault(parts, 6, "PENDING").toUpperCase(Locale.ROOT);
            LocalDate birthDate = null;
            if (parts.length > 4 && !parts[4].trim().isEmpty()) {
                birthDate = LocalDate.parse(parts[4].trim());
            }

            Athlete existing = athleteMapper.findByUserId(userId);
            LocalDateTime now = LocalDateTime.now();
            if (existing == null) {
                Athlete athlete = new Athlete();
                athlete.setUserId(userId);
                athlete.setName(name);
                athlete.setGender(gender);
                athlete.setBirthDate(birthDate);
                athlete.setLevel(level);
                athlete.setApprovalStatus(approvalStatus);
                athlete.setCreatedAt(now);
                athlete.setUpdatedAt(now);
                athleteMapper.insert(athlete);
            } else {
                existing.setName(name);
                existing.setGender(gender);
                existing.setBirthDate(birthDate);
                existing.setLevel(level);
                existing.setApprovalStatus(approvalStatus);
                existing.setUpdatedAt(now);
                athleteMapper.update(existing);
            }

            result.incrementSuccess();
        } catch (Exception e) {
            result.addError("第" + rowNum + "行: " + e.getMessage());
        }
    }

    private boolean isRowEmpty(Row row) {
        if (row == null) {
            return true;
        }
        for (int c = row.getFirstCellNum(); c < row.getLastCellNum(); c++) {
            Cell cell = row.getCell(c);
            if (cell != null && cell.toString().trim().length() > 0) {
                return false;
            }
        }
        return true;
    }

    private String safe(String value) {
        if (value == null) {
            return "";
        }
        String escaped = value.replace("\"", "\"\"");
        if (escaped.contains(",") || escaped.contains("\"")) {
            return "\"" + escaped + "\"";
        }
        return escaped;
    }

    private String valueOrDefault(String[] parts, int index, String defaultValue) {
        if (index >= parts.length) {
            return defaultValue;
        }
        String value = parts[index].trim();
        return value.isEmpty() ? defaultValue : value;
    }
}
