package com.aimlab.service;

import com.aimlab.dto.TrainingReportDTO;
import com.aimlab.entity.ShootingRecord;
import com.aimlab.mapper.ShootingRecordMapper;
import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * PDF生成服务类
 */
@Service
public class PdfGenerationService {

    @Autowired
    private ShootingRecordMapper shootingRecordMapper;
    
    /**
     * 生成训练报告PDF
     * 
     * @param reportDTO 训练报告DTO
     * @return PDF文件的字节数组
     */
    public byte[] generateTrainingReportPdf(TrainingReportDTO reportDTO) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf, PageSize.A4);
            
            // 加载支持中文的字体
            PdfFont font = PdfFontFactory.createFont("STSong-Light", "UniGB-UCS2-H", PdfFontFactory.EmbeddingStrategy.PREFER_EMBEDDED);
            document.setFont(font);
            
            // 添加文档标题
            Paragraph title = new Paragraph("训练报告 - " + reportDTO.getSessionName())
                    .setFontSize(20)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER);
            document.add(title);
            
            // 添加分隔线
            document.add(new Paragraph("").setHeight(20));
            
            // 添加摘要信息
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            
            Table summaryTable = new Table(UnitValue.createPercentArray(new float[]{30, 70}))
                    .setWidth(UnitValue.createPercentValue(100));
            
            addSummaryRow(summaryTable, "训练ID", reportDTO.getId().toString());
            addSummaryRow(summaryTable, "训练名称", reportDTO.getSessionName());
            addSummaryRow(summaryTable, "开始时间", reportDTO.getStartTime() != null ? 
                    reportDTO.getStartTime().format(formatter) : "未记录");
            addSummaryRow(summaryTable, "结束时间", reportDTO.getEndTime() != null ? 
                    reportDTO.getEndTime().format(formatter) : "未记录");
            addSummaryRow(summaryTable, "训练时长", reportDTO.getDurationMinutes() != null ? 
                    reportDTO.getDurationMinutes() + " 分钟" : "未记录");
            addSummaryRow(summaryTable, "总射击次数", reportDTO.getTotalShots().toString());
            addSummaryRow(summaryTable, "平均环数", reportDTO.getAverageScore().toString());
            addSummaryRow(summaryTable, "最高环数", reportDTO.getBestScore().toString());
            addSummaryRow(summaryTable, "最低环数", reportDTO.getWorstScore().toString());
            addSummaryRow(summaryTable, "稳定性指数", reportDTO.getStabilityIndex().toString());
            addSummaryRow(summaryTable, "射击频率", reportDTO.getShotsPerMinute() + " 发/分钟");
            
            if (reportDTO.getNotes() != null && !reportDTO.getNotes().isEmpty()) {
                addSummaryRow(summaryTable, "备注", reportDTO.getNotes());
            }
            
            document.add(summaryTable);
            
            // 添加分隔线
            document.add(new Paragraph("").setHeight(20));
            
            // 添加环数分布统计
            if (reportDTO.getScoreDistribution() != null && !reportDTO.getScoreDistribution().isEmpty()) {
                document.add(new Paragraph("环数分布统计").setFontSize(16).setBold());
                document.add(new Paragraph("").setHeight(10));
                
                Table distributionTable = new Table(UnitValue.createPercentArray(new float[]{50, 50}))
                        .setWidth(UnitValue.createPercentValue(100));
                
                distributionTable.addHeaderCell(createHeaderCell("环数"));
                distributionTable.addHeaderCell(createHeaderCell("次数"));
                
                reportDTO.getScoreDistribution().entrySet().stream()
                        .sorted((e1, e2) -> e2.getKey().compareTo(e1.getKey()))
                        .forEach(entry -> {
                            distributionTable.addCell(createCell(entry.getKey().toString()));
                            distributionTable.addCell(createCell(entry.getValue().toString()));
                        });
                
                document.add(distributionTable);
                document.add(new Paragraph("").setHeight(20));
            }
            
            // 添加射击记录表格
            document.add(new Paragraph("射击详细记录").setFontSize(16).setBold());
            document.add(new Paragraph("").setHeight(10));
            
            // 获取该训练场次的所有射击记录
            List<ShootingRecord> records = shootingRecordMapper.findByTrainingSessionId(reportDTO.getId());
            
            if (records != null && !records.isEmpty()) {
                Table recordsTable = new Table(UnitValue.createPercentArray(new float[]{15, 25, 20, 20, 20}))
                        .setWidth(UnitValue.createPercentValue(100));
                
                recordsTable.addHeaderCell(createHeaderCell("序号"));
                recordsTable.addHeaderCell(createHeaderCell("时间"));
                recordsTable.addHeaderCell(createHeaderCell("环数"));
                recordsTable.addHeaderCell(createHeaderCell("X坐标"));
                recordsTable.addHeaderCell(createHeaderCell("Y坐标"));
                
                int counter = 1;
                for (ShootingRecord record : records) {
                    recordsTable.addCell(createCell(String.valueOf(counter++)));
                    recordsTable.addCell(createCell(record.getShotAt().format(formatter)));
                    recordsTable.addCell(createCell(record.getScore().toString()));
                    recordsTable.addCell(createCell(record.getX().toString()));
                    recordsTable.addCell(createCell(record.getY().toString()));
                }
                
                document.add(recordsTable);
            } else {
                document.add(new Paragraph("无射击记录").setItalic());
            }
            
            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("生成PDF失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 添加摘要行
     */
    private void addSummaryRow(Table table, String label, String value) {
        Cell labelCell = new Cell().add(new Paragraph(label).setBold())
                .setBackgroundColor(ColorConstants.LIGHT_GRAY);
        Cell valueCell = new Cell().add(new Paragraph(value));
        table.addCell(labelCell);
        table.addCell(valueCell);
    }
    
    /**
     * 创建表头单元格
     */
    private Cell createHeaderCell(String text) {
        return new Cell().add(new Paragraph(text).setBold())
                .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                .setTextAlignment(TextAlignment.CENTER);
    }
    
    /**
     * 创建普通单元格
     */
    private Cell createCell(String text) {
        return new Cell().add(new Paragraph(text))
                .setTextAlignment(TextAlignment.CENTER);
    }
} 