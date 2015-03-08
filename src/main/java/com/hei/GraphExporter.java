package com.hei;

import org.apache.batik.gvt.renderer.ImageRenderer;
import org.apache.batik.transcoder.Transcoder;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.io.RandomAccessBuffer;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.edit.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.xobject.PDPixelMap;
import org.apache.pdfbox.pdmodel.graphics.xobject.PDXObjectImage;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;

public class GraphExporter {

    PDDocument doIt(String s, String message, String svg) throws IOException, COSVisitorException, TranscoderException {
        PDDocument pdDocument = new PDDocument();
        PDPage pdfPage = new PDPage();
        pdDocument.addPage(pdfPage);

        PDFont font = PDType1Font.HELVETICA;
        PDFont fontBold = PDType1Font.HELVETICA_BOLD;
        int fontSize = 10;

        ByteArrayOutputStream graphOutputStream = getGraphOutputStream(svg);
        BufferedImage read = ImageIO.read(new ByteArrayInputStream(graphOutputStream.toByteArray()));
        PDXObjectImage graphImage = new PDPixelMap(pdDocument, read);

        try (PDPageContentStream contentStream = new PDPageContentStream(pdDocument, pdfPage, true, true)) {

            createTextLine(contentStream, "Entity", fontBold, fontSize, 50, 750);
            createTextLine(contentStream, s, font, fontSize, 50, 740);
            createTextLine(contentStream, message, font, fontSize, 50, 730);

            createTextLine(contentStream, "Other Parameters", fontBold, fontSize, 50, 710);
            createTextLine(contentStream, "Currency : EUR", font, fontSize, 50, 700);
            createTextLine(contentStream, "Benchmark Type : 1. Master Configuration", font, fontSize, 50, 690);

            createTextLine(contentStream, "Date & Periods", fontBold, fontSize, 50, 670);
            createTextLine(contentStream, "End Date : 23/02/2015", font, fontSize, 50, 660);

//            contentStream.drawXObject(graphImage, 50, 180, 600, 450);

            RandomAccessBuffer storage = new RandomAccessBuffer();
            storage.read(graphOutputStream.toByteArray(), 0, 8192);
            COSStream cosStream = new COSStream(storage);

            contentStream.close();
            return pdDocument;
        }
    }

    public String stringFromFile(File file) throws IOException {
        StringBuffer buf = new StringBuffer();
        BufferedReader in = new BufferedReader(new FileReader(file));

        try {
            String line = null;
            while ((line = in.readLine()) != null) {
                buf.append(line);
            }
        }
        finally {
            in.close();
        }
        return buf.toString();
    }

    private void createTextLine(PDPageContentStream contentStream, String text, PDFont font, int fontSize, int xPosition, int yPosition) throws IOException {
        contentStream.beginText();
        contentStream.setFont(font, fontSize);
        contentStream.moveTextPositionByAmount(xPosition, yPosition);
        contentStream.drawString(text);
        contentStream.endText();
    }

    private ByteArrayOutputStream getGraphOutputStream(String svg) throws TranscoderException, IOException {
        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {


            TranscoderInput svgInput = new TranscoderInput(new FileReader(svg));
            TranscoderOutput transcoderOutput = new TranscoderOutput(output);

            Transcoder transcoder = new PNGTranscoder() {
                @Override
                protected ImageRenderer createRenderer() {
                    ImageRenderer r = super.createRenderer();

                    RenderingHints rh = r.getRenderingHints();

                    rh.add(new RenderingHints(RenderingHints.KEY_ALPHA_INTERPOLATION,
                            RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY));
                    rh.add(new RenderingHints(RenderingHints.KEY_INTERPOLATION,
                            RenderingHints.VALUE_INTERPOLATION_BICUBIC));

                    rh.add(new RenderingHints(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON));

                    rh.add(new RenderingHints(RenderingHints.KEY_COLOR_RENDERING,
                            RenderingHints.VALUE_COLOR_RENDER_QUALITY));
                    rh.add(new RenderingHints(RenderingHints.KEY_DITHERING,
                            RenderingHints.VALUE_DITHER_DISABLE));

                    rh.add(new RenderingHints(RenderingHints.KEY_RENDERING,
                            RenderingHints.VALUE_RENDER_QUALITY));

                    rh.add(new RenderingHints(RenderingHints.KEY_STROKE_CONTROL,
                            RenderingHints.VALUE_STROKE_PURE));

                    rh.add(new RenderingHints(RenderingHints.KEY_FRACTIONALMETRICS,
                            RenderingHints.VALUE_FRACTIONALMETRICS_ON));
                    rh.add(new RenderingHints(RenderingHints.KEY_TEXT_ANTIALIASING,
                            RenderingHints.VALUE_TEXT_ANTIALIAS_OFF));

                    r.setRenderingHints(rh);
                    return r;
                }
            };

            transcoder.transcode(svgInput, transcoderOutput);
            output.flush();
            return output;
        }
    }

    public void createPdf(String svg, String result) throws TranscoderException {
        FileOutputStream fileOut = null;
        PDDocument pdfDocument = null;
        try {
            File resultFile = new File(result);
            fileOut = new FileOutputStream(resultFile);

            pdfDocument = doIt("Portfolio : The Best portfolio ever created", "Benchmark : The Best benchmark ever created", svg);
            pdfDocument.save(resultFile);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fileOut.flush();
                fileOut.close();
                pdfDocument.close();
            } catch (Exception e) {/* trap */}
        }
    }
}
