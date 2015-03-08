package com.hei;

import com.itextpdf.awt.PdfGraphics2D;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfTemplate;
import com.itextpdf.text.pdf.PdfWriter;
import org.apache.batik.bridge.*;
import org.apache.batik.dom.svg.SAXSVGDocumentFactory;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.util.XMLResourceDescriptor;
import org.w3c.dom.svg.SVGDocument;

import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class ItextExporter {

    /**
     * The resulting PDF.
     */

    /**
     * The SVG document factory.
     */
    protected SAXSVGDocumentFactory factory;
    /**
     * The SVG bridge context.
     */
    protected BridgeContext ctx;
    /**
     * The GVT builder
     */
    protected GVTBuilder builder;

    /**
     * Creates an SvgToPdf object.
     */
    public ItextExporter() {
        String parser = XMLResourceDescriptor.getXMLParserClassName();
        factory = new SAXSVGDocumentFactory(parser);

        UserAgent userAgent = new UserAgentAdapter();
        DocumentLoader loader = new DocumentLoader(userAgent);
        ctx = new BridgeContext(userAgent, loader);
        ctx.setDynamicState(BridgeContext.DYNAMIC);

        builder = new GVTBuilder();
    }

    public void drawSvg(PdfTemplate map, String resource) throws IOException {
        Graphics2D g2d = new PdfGraphics2D(map, 600, 450);
        SVGDocument city = factory.createSVGDocument(new File(resource).toURL().toString());
        GraphicsNode mapGraphics = builder.build(ctx, city);
        mapGraphics.paint(g2d);
        g2d.dispose();
    }

    public void createPdf(String svg, String result) throws IOException, DocumentException {
        File resultFile = new File(result);
        try (FileOutputStream fileOut = new FileOutputStream(resultFile)) {

            // step 1
            Document document = new Document(new Rectangle(600, 550));
            // step 2
            PdfWriter writer = PdfWriter.getInstance(document, fileOut);
            // step 3
            document.open();

            document.add(new Paragraph("Other Parameters"));
            document.add(new Paragraph("Currency : EUR"));
            document.add(new Paragraph("Benchmark Type : 1. Master Configuration"));
            // step 4
            PdfContentByte cb = writer.getDirectContent();
            PdfTemplate map = cb.createTemplate(600, 450);
            drawSvg(map, svg);
            cb.addTemplate(map, 0, 0);

            // step 5
            document.close();
        }
    }

}
