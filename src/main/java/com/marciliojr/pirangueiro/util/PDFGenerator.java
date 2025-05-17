package com.marciliojr.pirangueiro.util;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import com.marciliojr.pirangueiro.dto.ReceitaDTO;
import com.marciliojr.pirangueiro.dto.DespesaDTO;
import com.marciliojr.pirangueiro.dto.LimiteGastosDTO;
import org.springframework.stereotype.Component;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
public class PDFGenerator {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public byte[] gerarPDFReceitas(List<ReceitaDTO> receitas, String titulo) throws DocumentException {
        Document document = new Document(PageSize.A4);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            adicionarTitulo(document, titulo);
            adicionarCabecalhoReceitas(document);
            adicionarConteudoReceitas(document, receitas);
            adicionarRodape(document);

        } finally {
            document.close();
        }

        return out.toByteArray();
    }

    public byte[] gerarPDFDespesas(List<DespesaDTO> despesas, String titulo) throws DocumentException {
        Document document = new Document(PageSize.A4);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            adicionarTitulo(document, titulo);
            adicionarCabecalhoDespesas(document);
            adicionarConteudoDespesas(document, despesas);
            adicionarRodape(document);

        } finally {
            document.close();
        }

        return out.toByteArray();
    }

    public byte[] gerarPDFLimitesGastos(List<LimiteGastosDTO> limites, String titulo) throws DocumentException {
        Document document = new Document(PageSize.A4);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            adicionarTitulo(document, titulo);
            adicionarCabecalhoLimites(document);
            adicionarConteudoLimites(document, limites);
            adicionarRodape(document);

        } finally {
            document.close();
        }

        return out.toByteArray();
    }

    private void adicionarTitulo(Document document, String titulo) throws DocumentException {
        Font font = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
        Paragraph paragraph = new Paragraph(titulo, font);
        paragraph.setAlignment(Element.ALIGN_CENTER);
        paragraph.setSpacingAfter(20);
        document.add(paragraph);
    }

    private void adicionarCabecalhoReceitas(Document document) throws DocumentException {
        PdfPTable table = new PdfPTable(5);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{1, 3, 2, 2, 2});

        adicionarCelula(table, "ID", true);
        adicionarCelula(table, "Descrição", true);
        adicionarCelula(table, "Valor", true);
        adicionarCelula(table, "Data", true);
        adicionarCelula(table, "Categoria", true);

        document.add(table);
    }

    private void adicionarConteudoReceitas(Document document, List<ReceitaDTO> receitas) throws DocumentException {
        PdfPTable table = new PdfPTable(5);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{1, 3, 2, 2, 2});

        for (ReceitaDTO receita : receitas) {
            adicionarCelula(table, receita.getId().toString(), false);
            adicionarCelula(table, receita.getDescricao(), false);
            adicionarCelula(table, String.format("R$ %.2f", receita.getValor()), false);
            adicionarCelula(table, receita.getData().format(formatter), false);
            adicionarCelula(table, receita.getCategoria().getNome(), false);
        }

        document.add(table);
    }

    private void adicionarCabecalhoDespesas(Document document) throws DocumentException {
        PdfPTable table = new PdfPTable(6);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{1, 3, 2, 2, 2, 2});

        adicionarCelula(table, "ID", true);
        adicionarCelula(table, "Descrição", true);
        adicionarCelula(table, "Valor", true);
        adicionarCelula(table, "Data", true);
        adicionarCelula(table, "Categoria", true);
        adicionarCelula(table, "Cartão", true);

        document.add(table);
    }

    private void adicionarConteudoDespesas(Document document, List<DespesaDTO> despesas) throws DocumentException {
        PdfPTable table = new PdfPTable(6);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{1, 3, 2, 2, 2, 2});

        for (DespesaDTO despesa : despesas) {
            adicionarCelula(table, despesa.getId().toString(), false);
            adicionarCelula(table, despesa.getDescricao(), false);
            adicionarCelula(table, String.format("R$ %.2f", despesa.getValor()), false);
            adicionarCelula(table, despesa.getData().format(formatter), false);
            adicionarCelula(table, despesa.getCategoria().getNome(), false);
            adicionarCelula(table, despesa.getCartao() != null ? despesa.getCartao().getNome() : "-", false);
        }

        document.add(table);
    }

    private void adicionarCabecalhoLimites(Document document) throws DocumentException {
        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{1, 3, 2, 2});

        adicionarCelula(table, "ID", true);
        adicionarCelula(table, "Descrição", true);
        adicionarCelula(table, "Valor", true);
        adicionarCelula(table, "Data", true);

        document.add(table);
    }

    private void adicionarConteudoLimites(Document document, List<LimiteGastosDTO> limites) throws DocumentException {
        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{1, 3, 2, 2});

        for (LimiteGastosDTO limite : limites) {
            adicionarCelula(table, limite.getId().toString(), false);
            adicionarCelula(table, limite.getDescricao(), false);
            adicionarCelula(table, String.format("R$ %.2f", limite.getValor()), false);
            adicionarCelula(table, limite.getData().format(formatter), false);
        }

        document.add(table);
    }

    private void adicionarCelula(PdfPTable table, String texto, boolean negrito) {
        Font font = negrito ? FontFactory.getFont(FontFactory.HELVETICA_BOLD) : FontFactory.getFont(FontFactory.HELVETICA);
        PdfPCell cell = new PdfPCell(new Phrase(texto, font));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(5);
        table.addCell(cell);
    }

    private void adicionarRodape(Document document) throws DocumentException {
        Font font = FontFactory.getFont(FontFactory.HELVETICA, 10);
        Paragraph paragraph = new Paragraph("Relatório gerado em " + java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")), font);
        paragraph.setAlignment(Element.ALIGN_CENTER);
        paragraph.setSpacingBefore(20);
        document.add(paragraph);
    }
} 