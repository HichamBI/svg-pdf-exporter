package com.hei.com.bisam;

import org.apache.batik.transcoder.TranscoderException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class PDFReportCreator {

    public static void main(String... args) throws IOException, TranscoderException {
        PDFDocument pdfDoc = new PDFDocument();

        pdfDoc.addTitleLine("Entity");
        pdfDoc.addTextLine("Benchmark : MCI Euro");
        pdfDoc.addTextLine("Approval Date : 15/09/2015");

        pdfDoc.addTitleLine("Other Parameters");
        pdfDoc.addTextLine("Benchmark Type : Master Configuration");
        pdfDoc.addTextLine("Currency : EUR");

        File file = new File("gradient.svg");
        pdfDoc.addSvgImage(file);

        ByteArrayOutputStream pdfPageStream = pdfDoc.getDocumentStream();
        File temp = new File("result.pdf");

        try (FileOutputStream fileOut = new FileOutputStream(temp)) {
            fileOut.write(pdfPageStream.toByteArray());
        }
    }
}
