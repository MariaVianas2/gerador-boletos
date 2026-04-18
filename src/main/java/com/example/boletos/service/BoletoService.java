package com.example.boletos.service;

import com.example.boletos.builder.BoletoBuilder;
import com.example.boletos.model.Boleto;
import com.example.boletos.model.BoletoRequest;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.BarcodeInter25;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;

@Service
public class BoletoService {

    private final BoletoDirector director;
    private final BoletoBuilder bradescoBuilder;
    private final BoletoBuilder itauBuilder;
    private final BoletoBuilder nubankBuilder;

    public BoletoService(
            BoletoDirector director,
            @Qualifier("bradescoBuilder") BoletoBuilder bradescoBuilder,
            @Qualifier("itauBuilder") BoletoBuilder itauBuilder,
            @Qualifier("nubankBuilder") BoletoBuilder nubankBuilder
    ) {
        this.director = director;
        this.bradescoBuilder = bradescoBuilder;
        this.itauBuilder = itauBuilder;
        this.nubankBuilder = nubankBuilder;
    }

    public Boleto gerarBoleto(BoletoRequest request) {
        String banco = request.getBanco().toLowerCase();

        return switch (banco) {
            case "bradesco" -> director.construir(bradescoBuilder, request);
            case "itau" -> director.construir(itauBuilder, request);
            case "nubank" -> director.construir(nubankBuilder, request);
            default -> throw new IllegalArgumentException("Banco invalido. Use bradesco, itau ou nubank.");
        };
    }

    public byte[] gerarPdf(Boleto boleto) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Document document = new Document(PageSize.A4, 24, 24, 24, 24);
            PdfWriter writer = PdfWriter.getInstance(document, out);
            document.open();

            String banco = boleto.getBanco() == null ? "" : boleto.getBanco().toLowerCase();

            switch (banco) {
                case "itau" -> gerarPdfItau(document, writer, boleto);
                case "bradesco" -> gerarPdfBradesco(document, writer, boleto);
                case "nubank" -> gerarPdfNubank(document, writer, boleto);
                default -> gerarPdfPadrao(document, writer, boleto, "Banco");
            }

            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar PDF do boleto", e);
        }
    }

    private void gerarPdfNubank(Document document, PdfWriter writer, Boleto boleto) throws Exception {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        // Configurações de Cores e Fontes (Identidade Nu)
        Color roxoNubank = new Color(130, 10, 209);
        Font font6 = FontFactory.getFont(FontFactory.HELVETICA, 6, Color.BLACK);
        Font font7 = FontFactory.getFont(FontFactory.HELVETICA, 7, Color.BLACK);
        Font font8 = FontFactory.getFont(FontFactory.HELVETICA, 8, Color.BLACK);
        Font font8Bold = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, Color.BLACK);
        Font font10Bold = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.BLACK);
        Font font14Bold = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, Color.BLACK);


        PdfPTable header = new PdfPTable(3);
        header.setWidthPercentage(100);
        header.setWidths(new float[]{1.5f, 1.0f, 7.5f});


        Image logoNu = Image.getInstance(new ClassPathResource("static/nubank-logo.png").getURL());
        logoNu.scaleToFit(50, 20);
        PdfPCell cellLogo = new PdfPCell(logoNu);
        cellLogo.setBorder(Rectangle.BOTTOM);
        cellLogo.setPaddingBottom(5);
        header.addCell(cellLogo);


        PdfPCell cellCod = new PdfPCell(new Phrase("260-7", font14Bold));
        cellCod.setHorizontalAlignment(Element.ALIGN_CENTER);
        cellCod.setVerticalAlignment(Element.ALIGN_BOTTOM);
        cellCod.setBorder(Rectangle.BOTTOM | Rectangle.LEFT | Rectangle.RIGHT);
        header.addCell(cellCod);


        PdfPCell cellLinha = new PdfPCell(new Phrase(boleto.getLinhaDigitavel(), font10Bold));
        cellLinha.setHorizontalAlignment(Element.ALIGN_RIGHT);
        cellLinha.setVerticalAlignment(Element.ALIGN_BOTTOM);
        cellLinha.setBorder(Rectangle.BOTTOM);
        header.addCell(cellLinha);
        document.add(header);


        PdfPTable linha1 = new PdfPTable(2);
        linha1.setWidthPercentage(100);
        linha1.setWidths(new float[]{7.5f, 2.5f});
        linha1.addCell(celulaCampo("Local de Pagamento", "Em qualquer banco até o vencimento", font6, font8));
        linha1.addCell(celulaCampo("Vencimento", boleto.getDataVencimento().format(formatter), font6, font8Bold));
        document.add(linha1);


        PdfPTable linha2 = new PdfPTable(3);
        linha2.setWidthPercentage(100);
        linha2.setWidths(new float[]{5.0f, 2.5f, 2.5f});
        linha2.addCell(celulaCampo("Beneficiário", "NU PAGAMENTOS S/A", font6, font8));
        linha2.addCell(celulaCampo("CNPJ/CPF", "18.236.120/0001-58", font6, font8));
        linha2.addCell(celulaCampo("Agência / Código do Beneficiário", boleto.getAgencia() + " / " + boleto.getConta(), font6, font8));
        document.add(linha2);


        PdfPTable linha3 = new PdfPTable(6);
        linha3.setWidthPercentage(100);
        linha3.setWidths(new float[]{1.4f, 1.6f, 1.2f, 1.0f, 1.6f, 3.2f});
        linha3.addCell(celulaCampo("Data do documento", boleto.getDataDocumento().format(formatter), font6, font7));
        linha3.addCell(celulaCampo("Nº do documento", boleto.getNumeroDocumento(), font6, font7));
        linha3.addCell(celulaCampo("Espécie doc.", "DV", font6, font7));
        linha3.addCell(celulaCampo("Aceite", "N", font6, font7));
        linha3.addCell(celulaCampo("Data Processamento", boleto.getDataDocumento().format(formatter), font6, font7));
        linha3.addCell(celulaCampo("Nosso Número / Cód. do Documento", boleto.getNossoNumero(), font6, font7));
        document.add(linha3);


        PdfPTable linha4 = new PdfPTable(5);
        linha4.setWidthPercentage(100);
        linha4.setWidths(new float[]{1.8f, 1.5f, 1.5f, 1.5f, 3.7f});
        linha4.addCell(celulaCampo("Uso do Banco", "", font6, font7));
        linha4.addCell(celulaCampo("Carteira", "00", font6, font7));
        linha4.addCell(celulaCampo("Espécie Moeda", "R$", font6, font7));
        linha4.addCell(celulaCampo("Quantidade Moeda", "", font6, font7));
        linha4.addCell(celulaCampo("Valor Moeda", "", font6, font7));
        document.add(linha4);


        PdfPTable corpo = new PdfPTable(2);
        corpo.setWidthPercentage(100);
        corpo.setWidths(new float[]{7.2f, 2.8f});


        PdfPCell colEsquerda = new PdfPCell();
        colEsquerda.setPadding(0);
        PdfPTable tabInst = new PdfPTable(1);
        tabInst.setWidthPercentage(100);

        String txtInst = "Sr. Caixa:\n\n1) Não aceitar pagamento em cheque;\n2) Não aceitar mais de um pagamento com o mesmo boleto;\n3) Em caso de vencimento no fim de semana ou feriado, aceitar o pagamento até o primeiro dia útil após o vencimento.";
        tabInst.addCell(celulaBlocoInstrucoes("Instruções", txtInst, font6, font7));


        String endBenef = "NU PAGAMENTOS S/A\nRua Capote Valente 39, Pinheiros 05409000 - São Paulo - SP";
        tabInst.addCell(celulaCampo("Beneficiário", endBenef, font6, font7));

        colEsquerda.addElement(tabInst);
        corpo.addCell(colEsquerda);


        PdfPCell colDireita = new PdfPCell();
        colDireita.setPadding(0);
        PdfPTable tabVal = new PdfPTable(1);
        tabVal.setWidthPercentage(100);
        tabVal.addCell(celulaCampo("(=) Valor do Documento", "R$ " + boleto.getValor(), font6, font8Bold));
        tabVal.addCell(celulaCampo("(-) Desconto / Abatimento", "0,00", font6, font7));
        tabVal.addCell(celulaCampo("(-) Outras Deduções", "0,00", font6, font7));
        tabVal.addCell(celulaCampo("(+) Mora / Multa", "0,00", font6, font7));
        tabVal.addCell(celulaCampo("(+) Outros Acréscimos", "0,00", font6, font7));
        tabVal.addCell(celulaCampo("(=) Valor Cobrado", "R$ " + boleto.getValor(), font6, font8Bold));

        colDireita.addElement(tabVal);
        corpo.addCell(colDireita);
        document.add(corpo);


        PdfPTable tabPagador = new PdfPTable(1);
        tabPagador.setWidthPercentage(100);


        String infoPagador = boleto.getPagador() + "\n" +
                (boleto.getLogradouroPagador() != null ? boleto.getLogradouroPagador() : "") + "\n" +
                (boleto.getCepPagador() != null ? boleto.getCepPagador() : "") + " " + (boleto.getCidadePagador() != null ? boleto.getCidadePagador() : "");

        PdfPCell cellPag = celulaCampo("Pagador", infoPagador, font6, font8);
        cellPag.setBorder(Rectangle.LEFT | Rectangle.RIGHT | Rectangle.BOTTOM);
        cellPag.setPaddingBottom(8f);
        tabPagador.addCell(cellPag);
        document.add(tabPagador);


        PdfPTable rodape = new PdfPTable(2);
        rodape.setWidthPercentage(100);
        rodape.addCell(celulaRodapeTexto("Código de Baixa", font6));
        rodape.addCell(celulaRodapeTexto("Autenticação Mecânica - FICHA DE COMPENSAÇÃO", font6));
        document.add(rodape);

        document.add(new Paragraph(" ")); // Espaço para o código de barras

        PdfContentByte cb = writer.getDirectContent();
        BarcodeInter25 barcode = new BarcodeInter25();
        barcode.setCode(boleto.getCodigoBarras());
        barcode.setChecksumText(false);
        barcode.setFont(null);

        Image imgBar = barcode.createImageWithBarcode(cb, null, null);
        imgBar.scaleAbsolute(460, 48);
        imgBar.setAlignment(Element.ALIGN_CENTER);
        document.add(imgBar);
    }

    private void gerarPdfBradesco(Document document, PdfWriter writer, Boleto boleto) throws Exception {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        Font font6 = FontFactory.getFont(FontFactory.HELVETICA, 6, Color.BLACK);
        Font font7 = FontFactory.getFont(FontFactory.HELVETICA, 7, Color.BLACK);
        Font font7Bold = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 7, Color.BLACK);
        Font font8 = FontFactory.getFont(FontFactory.HELVETICA, 8, Color.BLACK);
        Font font8Bold = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, Color.BLACK);
        Font font10Bold = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.BLACK);
        Font font14Bold = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, Color.BLACK);


        Image logoBradesco = Image.getInstance(new ClassPathResource("static/bradesco-logo.png").getURL());
        logoBradesco.scaleToFit(120, 60);
        logoBradesco.setAlignment(Element.ALIGN_LEFT);
        document.add(logoBradesco);


        PdfPTable topo = new PdfPTable(4);
        topo.setWidthPercentage(100);
        topo.setWidths(new float[]{1.6f, 2.2f, 1.1f, 5.1f});

        PdfPCell logo = new PdfPCell(new Phrase("", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Color.WHITE)));
        logo.setBackgroundColor(new Color(255, 255, 255)); // Vermelho Bradesco
        logo.setHorizontalAlignment(Element.ALIGN_CENTER);
        logo.setVerticalAlignment(Element.ALIGN_MIDDLE);
        logo.setFixedHeight(24f);
        logo.setBorder(Rectangle.BOTTOM);
        topo.addCell(logo);

        PdfPCell nomeBanco = new PdfPCell(new Phrase("Banco Bradesco S.A.", font7Bold));
        nomeBanco.setVerticalAlignment(Element.ALIGN_MIDDLE);
        nomeBanco.setBorder(Rectangle.BOTTOM);
        topo.addCell(nomeBanco);

        PdfPCell codigoBanco = new PdfPCell(new Phrase("237-2", font14Bold));
        codigoBanco.setHorizontalAlignment(Element.ALIGN_CENTER);
        codigoBanco.setVerticalAlignment(Element.ALIGN_MIDDLE);
        codigoBanco.setBorder(Rectangle.BOTTOM);
        topo.addCell(codigoBanco);

        PdfPCell linha = new PdfPCell(new Phrase(boleto.getLinhaDigitavel(), font10Bold));
        linha.setHorizontalAlignment(Element.ALIGN_RIGHT);
        linha.setVerticalAlignment(Element.ALIGN_MIDDLE);
        linha.setBorder(Rectangle.BOTTOM);
        topo.addCell(linha);

        document.add(topo);


        PdfPTable linha1 = new PdfPTable(2);
        linha1.setWidthPercentage(100);
        linha1.setWidths(new float[]{7.5f, 2.5f});
        linha1.addCell(celulaCampo("Local de Pagamento", "PAGÁVEL PREFERENCIALMENTE NAS AGÊNCIAS DO BRADESCO", font6, font8));
        linha1.addCell(celulaCampo("Vencimento", boleto.getDataVencimento().format(formatter), font6, font8Bold));
        document.add(linha1);

        PdfPTable linha2 = new PdfPTable(2);
        linha2.setWidthPercentage(100);
        linha2.setWidths(new float[]{7.5f, 2.5f});
        linha2.addCell(celulaCampo("Beneficiário", boleto.getBeneficiario(), font6, font8));
        linha2.addCell(celulaCampo("Agência / Código Beneficiário", boleto.getAgencia() + " / " + boleto.getConta(), font6, font8));
        document.add(linha2);

        PdfPTable linha3 = new PdfPTable(6);
        linha3.setWidthPercentage(100);
        linha3.setWidths(new float[]{1.4f, 1.6f, 1.2f, 1f, 1.6f, 2.2f});
        linha3.addCell(celulaCampo("Data do documento", boleto.getDataDocumento().format(formatter), font6, font7));
        linha3.addCell(celulaCampo("Nº do documento", boleto.getNumeroDocumento(), font6, font7));
        linha3.addCell(celulaCampo("Espécie doc.", "DM", font6, font7));
        linha3.addCell(celulaCampo("Aceite", "N", font6, font7));
        linha3.addCell(celulaCampo("Data do processamento", boleto.getDataDocumento().format(formatter), font6, font7));
        linha3.addCell(celulaCampo("Nosso Número", boleto.getNossoNumero(), font6, font7));
        document.add(linha3);

        PdfPTable corpo = new PdfPTable(2);
        corpo.setWidthPercentage(100);
        corpo.setWidths(new float[]{7.2f, 2.8f});

        PdfPCell esquerda = new PdfPCell();
        esquerda.setPadding(0);
        PdfPTable tabelaEsquerda = new PdfPTable(1);
        tabelaEsquerda.setWidthPercentage(100);
        tabelaEsquerda.addCell(celulaBlocoInstrucoes("Instruções", boleto.getInstrucoes(), font6, font7));
        tabelaEsquerda.addCell(celulaCampo("Pagador", boleto.getPagador(), font6, font8));
        tabelaEsquerda.addCell(celulaCampo("Documento do pagador", boleto.getDocumentoPagador(), font6, font7));
        esquerda.addElement(tabelaEsquerda);
        corpo.addCell(esquerda);

        PdfPCell direita = new PdfPCell();
        direita.setPadding(0);
        PdfPTable tabelaDireita = new PdfPTable(1);
        tabelaDireita.setWidthPercentage(100);
        tabelaDireita.addCell(celulaCampo("(=) Valor do Documento", "R$ " + boleto.getValor().setScale(2, RoundingMode.HALF_UP), font6, font8Bold));
        tabelaDireita.addCell(celulaCampo("(-) Desconto", "", font6, font7));
        tabelaDireita.addCell(celulaCampo("(+) Mora / Multa", "", font6, font7));
        tabelaDireita.addCell(celulaCampo("(=) Valor Cobrado", "R$ " + boleto.getValor().setScale(2, RoundingMode.HALF_UP), font6, font8Bold));
        direita.addElement(tabelaDireita);
        corpo.addCell(direita);
        document.add(corpo);

        document.add(new Paragraph(" "));
        adicionarBarraSeparadora(document);
        document.add(new Paragraph(" "));

        // Código de barras
        PdfContentByte cb = writer.getDirectContent();
        BarcodeInter25 barcode = new BarcodeInter25();
        barcode.setCode(boleto.getCodigoBarras());
        barcode.setChecksumText(false);
        barcode.setFont(null);
        Image imagemCodigo = barcode.createImageWithBarcode(cb, null, null);
        imagemCodigo.scaleAbsolute(470, 48);
        imagemCodigo.setAlignment(Element.ALIGN_CENTER);
        document.add(imagemCodigo);
    }
    private void gerarPdfItau(Document document, PdfWriter writer, Boleto boleto) throws Exception {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        Font font6 = FontFactory.getFont(FontFactory.HELVETICA, 6, Color.BLACK);
        Font font7 = FontFactory.getFont(FontFactory.HELVETICA, 7, Color.BLACK);
        Font font7Bold = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 7, Color.BLACK);
        Font font8 = FontFactory.getFont(FontFactory.HELVETICA, 8, Color.BLACK);
        Font font8Bold = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, Color.BLACK);
        Font font10Bold = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.BLACK);
        Font font14Bold = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, Color.BLACK);


        Image logoItau = Image.getInstance(new ClassPathResource("static/itau-logo.png").getURL());
        logoItau.scaleToFit(120, 60);
        logoItau.setAlignment(Element.ALIGN_LEFT);

        document.add(logoItau);


        PdfPTable topo = new PdfPTable(4);
        topo.setWidthPercentage(100);
        topo.setWidths(new float[]{1.6f, 2.2f, 1.1f, 5.1f});

        PdfPCell logo = new PdfPCell(new Phrase(""));
        logo.setBackgroundColor(new Color(255, 255, 255));
        logo.setHorizontalAlignment(Element.ALIGN_CENTER);
        logo.setVerticalAlignment(Element.ALIGN_MIDDLE);
        logo.setFixedHeight(24f);
        logo.setBorder(Rectangle.BOTTOM);
        topo.addCell(logo);

        PdfPCell nomeBanco = new PdfPCell(new Phrase("Banco Itaú S.A.", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, Color.BLACK))); // Fonte maior
        nomeBanco.setVerticalAlignment(Element.ALIGN_MIDDLE);
        nomeBanco.setBorder(Rectangle.BOTTOM);
        topo.addCell(nomeBanco);

        PdfPCell codigoBanco = new PdfPCell(new Phrase("341-7", font14Bold));
        codigoBanco.setHorizontalAlignment(Element.ALIGN_CENTER);
        codigoBanco.setVerticalAlignment(Element.ALIGN_MIDDLE);
        codigoBanco.setBorder(Rectangle.BOTTOM);
        topo.addCell(codigoBanco);

        PdfPCell linha = new PdfPCell(new Phrase(boleto.getLinhaDigitavel(), font10Bold));
        linha.setHorizontalAlignment(Element.ALIGN_RIGHT);
        linha.setVerticalAlignment(Element.ALIGN_MIDDLE);
        linha.setBorder(Rectangle.BOTTOM);
        topo.addCell(linha);

        document.add(topo);


        PdfPTable linha1 = new PdfPTable(2);
        linha1.setWidthPercentage(100);
        linha1.setWidths(new float[]{7.5f, 2.5f});
        linha1.addCell(celulaCampo("Local de Pagamento", "ATE O VENCIMENTO PAGUE PREFERENCIALMENTE NO ITAU OU BANERJ.\nAPOS O VENCIMENTO PAGUE SOMENTE NO ITAU OU BANERJ.", font6, font8));
        linha1.addCell(celulaCampo("Vencimento", boleto.getDataVencimento().format(formatter), font6, font8Bold));
        document.add(linha1);


        PdfPTable linha2 = new PdfPTable(2);
        linha2.setWidthPercentage(100);
        linha2.setWidths(new float[]{7.5f, 2.5f});
        linha2.addCell(celulaCampo("Cedente", boleto.getBeneficiario(), font6, font8));
        linha2.addCell(celulaCampo("Agência / Código Cedente", boleto.getAgencia() + "/" + boleto.getConta(), font6, font8));
        document.add(linha2);


        PdfPTable linha3 = new PdfPTable(6);
        linha3.setWidthPercentage(100);
        linha3.setWidths(new float[]{1.4f, 1.6f, 1.2f, 1f, 1.6f, 2.2f});
        linha3.addCell(celulaCampo("Data do documento", boleto.getDataDocumento().format(formatter), font6, font7));
        linha3.addCell(celulaCampo("Nº do documento", boleto.getNumeroDocumento(), font6, font7));
        linha3.addCell(celulaCampo("Espécie doc.", boleto.getEspecieDocumento(), font6, font7));
        linha3.addCell(celulaCampo("Aceite", "N", font6, font7));
        linha3.addCell(celulaCampo("Data do processamento", boleto.getDataDocumento().format(formatter), font6, font7));
        linha3.addCell(celulaCampo("Nosso Número", boleto.getNossoNumero(), font6, font7));
        document.add(linha3);


        PdfPTable corpo = new PdfPTable(2);
        corpo.setWidthPercentage(100);
        corpo.setWidths(new float[]{7.2f, 2.8f});

        PdfPCell esquerda = new PdfPCell();
        esquerda.setPadding(0);

        PdfPTable tabelaEsquerda = new PdfPTable(1);
        tabelaEsquerda.setWidthPercentage(100);
        tabelaEsquerda.addCell(celulaBlocoInstrucoes("Instruções (Todas informações deste boleto são de exclusiva responsabilidade do cedente)",
                boleto.getInstrucoes() == null || boleto.getInstrucoes().isBlank()
                        ? "Não receber após o vencimento."
                        : boleto.getInstrucoes(),
                font6, font7));
        tabelaEsquerda.addCell(celulaCampo("Sacado", boleto.getPagador(), font6, font8));
        tabelaEsquerda.addCell(celulaCampo("Documento do pagador", boleto.getDocumentoPagador(), font6, font7));

        esquerda.addElement(tabelaEsquerda);
        corpo.addCell(esquerda);

        PdfPCell direita = new PdfPCell();
        direita.setPadding(0);

        PdfPTable tabelaDireita = new PdfPTable(1);
        tabelaDireita.setWidthPercentage(100);
        tabelaDireita.addCell(celulaCampo("(=) Valor do Documento", "R$ " + boleto.getValor().setScale(2, RoundingMode.HALF_UP), font6, font8Bold));
        tabelaDireita.addCell(celulaCampo("(-) Desconto / Abatimento", "", font6, font7));
        tabelaDireita.addCell(celulaCampo("(-) Outras Deduções", "", font6, font7));
        tabelaDireita.addCell(celulaCampo("(+) Mora / Multa", "", font6, font7));
        tabelaDireita.addCell(celulaCampo("(=) Valor Cobrado", "R$ " + boleto.getValor().setScale(2, RoundingMode.HALF_UP), font6, font8Bold));

        direita.addElement(tabelaDireita);
        corpo.addCell(direita);

        document.add(corpo);

        document.add(new Paragraph(" "));
        adicionarBarraSeparadora(document);
        document.add(new Paragraph(" "));

        PdfPTable rodapeInfo = new PdfPTable(3);
        rodapeInfo.setWidthPercentage(100);
        rodapeInfo.setWidths(new float[]{2.5f, 2.5f, 5f});

        rodapeInfo.addCell(celulaRodapeTexto("Sacador / Avalista", font6));
        rodapeInfo.addCell(celulaRodapeTexto("Código de Baixa", font6));
        rodapeInfo.addCell(celulaRodapeTexto("Autenticação Mecânica / FICHA DE COMPENSAÇÃO", font6));

        document.add(rodapeInfo);

        document.add(new Paragraph(" "));

        PdfContentByte cb = writer.getDirectContent();
        BarcodeInter25 barcode = new BarcodeInter25();

        String codigo = boleto.getCodigoBarras();
        if (codigo.length() % 2 != 0) {
            codigo = "0" + codigo;
        }

        barcode.setCode(codigo);
        barcode.setChecksumText(false);
        barcode.setFont(null);

        Image imagemCodigo = barcode.createImageWithBarcode(cb, null, null);
        imagemCodigo.scaleAbsolute(470, 48);
        imagemCodigo.setAlignment(Element.ALIGN_CENTER);
        document.add(imagemCodigo);
    }


    private void gerarPdfPadrao(Document document, PdfWriter writer, Boleto boleto, String nomeBanco) throws Exception {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        Paragraph titulo = new Paragraph(
                "BOLETO BANCÁRIO - " + nomeBanco.toUpperCase(),
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16)
        );
        titulo.setAlignment(Element.ALIGN_CENTER);
        document.add(titulo);
        document.add(new Paragraph(" "));

        PdfPTable header = new PdfPTable(3);
        header.setWidthPercentage(100);
        header.setWidths(new float[]{3f, 1f, 5f});

        addHeaderCell(header, boleto.getNomeBanco(), 12, true);
        addHeaderCell(header, boleto.getCodigoBanco(), 12, true);
        addHeaderCell(header, boleto.getLinhaDigitavel(), 11, false);
        document.add(header);

        document.add(new Paragraph(" "));

        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{1.4f, 2.6f});

        addInfoCell(table, "Beneficiário");
        addValueCell(table, boleto.getBeneficiario());

        addInfoCell(table, "Documento do beneficiário");
        addValueCell(table, boleto.getDocumentoBeneficiario());

        addInfoCell(table, "Pagador");
        addValueCell(table, boleto.getPagador());

        addInfoCell(table, "Documento do pagador");
        addValueCell(table, boleto.getDocumentoPagador());

        addInfoCell(table, "Agência / Conta");
        addValueCell(table, boleto.getAgencia() + " / " + boleto.getConta());

        addInfoCell(table, "Carteira");
        addValueCell(table, boleto.getCarteira());

        addInfoCell(table, "Nosso número");
        addValueCell(table, boleto.getNossoNumero());

        addInfoCell(table, "Número do documento");
        addValueCell(table, boleto.getNumeroDocumento());

        addInfoCell(table, "Espécie do documento");
        addValueCell(table, boleto.getEspecieDocumento());

        addInfoCell(table, "Data do documento");
        addValueCell(table, boleto.getDataDocumento().format(formatter));

        addInfoCell(table, "Data de vencimento");
        addValueCell(table, boleto.getDataVencimento().format(formatter));

        addInfoCell(table, "Valor do documento");
        addValueCell(table, "R$ " + boleto.getValor().setScale(2, RoundingMode.HALF_UP));

        addInfoCell(table, "Instruções");
        addValueCell(table, boleto.getInstrucoes());

        addInfoCell(table, "Código de barras");
        addValueCell(table, boleto.getCodigoBarras());

        document.add(table);
        document.add(new Paragraph(" "));

        PdfContentByte cb = writer.getDirectContent();
        BarcodeInter25 barcode = new BarcodeInter25();

        String codigo = boleto.getCodigoBarras();
        if (codigo.length() % 2 != 0) {
            codigo = "0" + codigo;
        }

        barcode.setCode(codigo);
        barcode.setChecksumText(false);
        barcode.setFont(null);

        Image imagemCodigo = barcode.createImageWithBarcode(cb, null, null);
        imagemCodigo.scalePercent(120);
        imagemCodigo.setAlignment(Element.ALIGN_CENTER);
        document.add(imagemCodigo);
    }

    private PdfPCell celulaCampo(String titulo, String valor, Font tituloFont, Font valorFont) {
        PdfPCell cell = new PdfPCell();
        cell.setPadding(3);
        cell.setBorder(Rectangle.BOX);

        Paragraph p1 = new Paragraph(titulo, tituloFont);
        Paragraph p2 = new Paragraph(valor == null ? "" : valor, valorFont);

        cell.addElement(p1);
        cell.addElement(p2);

        return cell;
    }

    private PdfPCell celulaBlocoInstrucoes(String titulo, String valor, Font tituloFont, Font valorFont) {
        PdfPCell cell = new PdfPCell();
        cell.setPadding(3);
        cell.setBorder(Rectangle.BOX);
        cell.setMinimumHeight(95f);

        Paragraph p1 = new Paragraph(titulo, tituloFont);
        Paragraph p2 = new Paragraph(valor == null ? "" : valor, valorFont);

        cell.addElement(p1);
        cell.addElement(p2);

        return cell;
    }

    private PdfPCell celulaRodapeTexto(String texto, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(texto, font));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPaddingTop(2);
        cell.setPaddingBottom(2);
        return cell;
    }

    private void adicionarBarraSeparadora(Document document) throws Exception {
        PdfPTable linha = new PdfPTable(1);
        linha.setWidthPercentage(100);

        PdfPCell cell = new PdfPCell(new Phrase(""));
        cell.setBorder(Rectangle.TOP);
        cell.setFixedHeight(1f);

        linha.addCell(cell);
        document.add(linha);
    }

    private void addHeaderCell(PdfPTable table, String text, int size, boolean bold) {
        PdfPCell cell = new PdfPCell(
                new Phrase(
                        text,
                        FontFactory.getFont(
                                bold ? FontFactory.HELVETICA_BOLD : FontFactory.HELVETICA,
                                size
                        )
                )
        );
        cell.setBorder(Rectangle.BOX);
        cell.setPadding(6);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(cell);
    }

    private void addInfoCell(PdfPTable table, String text) {
        PdfPCell cell = new PdfPCell(
                new Phrase(text, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10))
        );
        cell.setPadding(6);
        table.addCell(cell);
    }

    private void addValueCell(PdfPTable table, String text) {
        PdfPCell cell = new PdfPCell(
                new Phrase(text == null ? "" : text, FontFactory.getFont(FontFactory.HELVETICA, 10))
        );
        cell.setPadding(6);
        table.addCell(cell);
    }
}