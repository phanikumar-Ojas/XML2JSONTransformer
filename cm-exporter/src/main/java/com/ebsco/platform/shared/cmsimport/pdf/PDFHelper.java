package com.ebsco.platform.shared.cmsimport.pdf;

import com.lowagie.text.pdf.BaseFont;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;

public class PDFHelper {


    public static String htmlToXhtml(String html) {
        Document document = Jsoup.parse(html);
        document.outputSettings().syntax(Document.OutputSettings.Syntax.xml)
                .prettyPrint(false)
                .charset("UTF-8");
        return document.html();
    }

    public static void xhtmlToPdf(String xhtml, String outFileName) {
        try (OutputStream outputStream = Files.newOutputStream(new File(outFileName).toPath())) {
            ITextRenderer iTextRenderer = new ITextRenderer();
            iTextRenderer.setDocumentFromString(xhtml);
            addFonts(iTextRenderer);
            iTextRenderer.layout();
            iTextRenderer.createPDF(outputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    private static void addFonts(ITextRenderer iTextRenderer) throws IOException {
        File folder = new File("cm-exporter/src/main/resources/fonts");
        File[] filesInFolder = folder.listFiles();
        for (File file : filesInFolder) {
            if (file.isFile()) {
                iTextRenderer.getFontResolver().addFont(file.getPath(), BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);
            }
        }
    }
}