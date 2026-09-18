package com.example.microfinance_loan_system.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.util.Map;
import java.util.Set;

/**
 * Generates a styled PDF credit report for a client using OpenPDF.
 */
@Service
public class CreditReportPdfService {

    @Autowired
    private ClientService clientService;

    public byte[] generateCreditReportPdf(Long clientId) {
        Map<String, String> data = clientService.getCreditReport(clientId);

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 50, 50, 60, 60);
            PdfWriter.getInstance(document, out);
            document.open();

            // ── Fonts ──────────────────────────────────────────────────────────
            Font titleFont   = new Font(Font.HELVETICA, 22, Font.BOLD,   new Color(255, 255, 255));
            Font subFont     = new Font(Font.HELVETICA, 11, Font.NORMAL, new Color(200, 220, 255));
            Font headingFont = new Font(Font.HELVETICA, 11, Font.BOLD,   new Color(30, 50, 90));
            Font labelFont   = new Font(Font.HELVETICA, 10, Font.NORMAL, new Color(80, 95, 120));
            Font valueFont   = new Font(Font.HELVETICA, 10, Font.BOLD,   new Color(15, 25, 50));
            Font scoreFont   = new Font(Font.HELVETICA, 36, Font.BOLD,   new Color(255, 255, 255));
            Font footerFont  = new Font(Font.HELVETICA,  8, Font.ITALIC, new Color(130, 140, 160));
            Font sectionFont = new Font(Font.HELVETICA, 10, Font.BOLD,   new Color(255, 255, 255));

            // ── Header Banner ─────────────────────────────────────────────────
            PdfPTable header = new PdfPTable(1);
            header.setWidthPercentage(100);

            PdfPCell headerCell = new PdfPCell();
            headerCell.setBackgroundColor(new Color(15, 32, 80));
            headerCell.setPadding(24);
            headerCell.setBorder(Rectangle.NO_BORDER);

            Paragraph org = new Paragraph("MicroFin Loan System", subFont);
            org.setSpacingAfter(4);
            Paragraph title = new Paragraph("Credit Report", titleFont);
            title.setSpacingAfter(4);

            String clientName = data.getOrDefault("client_name", "Client");
            String clientIdStr = data.getOrDefault("client_id", "");
            String generated   = data.getOrDefault("report_generated_on", "");

            Paragraph meta = new Paragraph(
                    clientName + "  |  ID " + clientIdStr + "  |  Generated: " + generated, subFont);

            headerCell.addElement(org);
            headerCell.addElement(title);
            headerCell.addElement(meta);
            header.addCell(headerCell);
            document.add(header);
            document.add(new Paragraph(" "));

            // ── CIBIL Score Banner ─────────────────────────────────────────────
            String scoreStr = data.getOrDefault("cibil_score", "720");
            int score;
            try { score = Integer.parseInt(scoreStr); } catch (NumberFormatException e) { score = 720; }

            Color scoreBg = score >= 750
                    ? new Color(16, 120, 60)
                    : score >= 650
                    ? new Color(160, 100, 0)
                    : new Color(180, 30, 30);

            PdfPTable scoreBanner = new PdfPTable(2);
            scoreBanner.setWidthPercentage(100);
            scoreBanner.setWidths(new float[]{1f, 3f});

            PdfPCell scoreNumCell = new PdfPCell();
            scoreNumCell.setBackgroundColor(scoreBg);
            scoreNumCell.setBorder(Rectangle.NO_BORDER);
            scoreNumCell.setPadding(20);
            scoreNumCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            scoreNumCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            Paragraph scoreLabel = new Paragraph("CIBIL SCORE\n", sectionFont);
            Paragraph scoreNum   = new Paragraph(scoreStr, scoreFont);
            scoreLabel.setAlignment(Element.ALIGN_CENTER);
            scoreNum.setAlignment(Element.ALIGN_CENTER);
            scoreNumCell.addElement(scoreLabel);
            scoreNumCell.addElement(scoreNum);

            PdfPCell riskCell = new PdfPCell();
            riskCell.setBackgroundColor(new Color(235, 240, 255));
            riskCell.setBorder(Rectangle.NO_BORDER);
            riskCell.setPadding(20);
            riskCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            String risk  = data.getOrDefault("risk_classification", "—");
            String limit = data.getOrDefault("maximum_eligible_loan_limit", "—");
            String rate  = data.getOrDefault("recommended_annual_interest_rate", "—");
            Paragraph riskPara = new Paragraph();
            riskPara.add(new Chunk("Risk Classification:  ", headingFont));
            riskPara.add(new Chunk(risk + "\n", valueFont));
            riskPara.add(new Chunk("Max Eligible Limit:   ", headingFont));
            riskPara.add(new Chunk(limit + "\n", valueFont));
            riskPara.add(new Chunk("Recommended Rate:     ", headingFont));
            riskPara.add(new Chunk(rate, valueFont));
            riskCell.addElement(riskPara);

            scoreBanner.addCell(scoreNumCell);
            scoreBanner.addCell(riskCell);
            document.add(scoreBanner);
            document.add(new Paragraph(" "));

            // ── Section heading ───────────────────────────────────────────────
            PdfPTable sectionHeader = new PdfPTable(1);
            sectionHeader.setWidthPercentage(100);
            PdfPCell secCell = new PdfPCell(
                    new Phrase("Report Details", new Font(Font.HELVETICA, 10, Font.BOLD, Color.WHITE)));
            secCell.setBackgroundColor(new Color(30, 55, 115));
            secCell.setPadding(8);
            secCell.setBorder(Rectangle.NO_BORDER);
            sectionHeader.addCell(secCell);
            document.add(sectionHeader);

            // ── Detailed Fields Table ─────────────────────────────────────────
            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{2f, 3f});

            // Fields already shown prominently in banner — skip from table
            Set<String> skip = Set.of(
                "cibil_score", "risk_classification",
                "maximum_eligible_loan_limit", "recommended_annual_interest_rate",
                "client_name", "client_id", "report_generated_on"
            );

            boolean altRow = false;
            for (Map.Entry<String, String> entry : data.entrySet()) {
                if (skip.contains(entry.getKey())) continue;

                Color rowBg = altRow ? new Color(245, 248, 255) : Color.WHITE;
                altRow = !altRow;

                String label = entry.getKey().replace("_", " ");
                label = Character.toUpperCase(label.charAt(0)) + label.substring(1);

                PdfPCell lCell = new PdfPCell(new Phrase(label, labelFont));
                lCell.setBorder(Rectangle.BOTTOM);
                lCell.setBorderColor(new Color(220, 225, 240));
                lCell.setPadding(9);
                lCell.setBackgroundColor(rowBg);

                PdfPCell vCell = new PdfPCell(new Phrase(entry.getValue(), valueFont));
                vCell.setBorder(Rectangle.BOTTOM);
                vCell.setBorderColor(new Color(220, 225, 240));
                vCell.setPadding(9);
                vCell.setBackgroundColor(rowBg);

                table.addCell(lCell);
                table.addCell(vCell);
            }
            document.add(table);
            document.add(new Paragraph(" "));

            // ── Disclaimer ────────────────────────────────────────────────────
            Paragraph disclaimer = new Paragraph(
                "DISCLAIMER: This credit report is generated for internal reference purposes by MicroFin Loan System. " +
                "The CIBIL score reflected here is a composite score computed by the platform and may differ from " +
                "official TransUnion CIBIL scores. This document does not constitute an official credit bureau report.",
                footerFont
            );
            disclaimer.setAlignment(Element.ALIGN_CENTER);
            document.add(disclaimer);

            document.close();
            return out.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Failed to generate credit report PDF for client #" + clientId, e);
        }
    }
}
