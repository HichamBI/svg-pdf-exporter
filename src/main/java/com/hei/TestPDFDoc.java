/**
 Created by IntelliJ IDEA.
 Author: Hicham B.I.
 Date: 08/03/15
 Time: 10:36
 */

package com.hei;

import com.itextpdf.text.DocumentException;
import org.apache.batik.transcoder.TranscoderException;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class TestPDFDoc {

    public static void main(String... arg) throws IOException, DocumentException, TranscoderException {
        PDFDoc pdfDoc = new PDFDoc(600f, 588f);

        Font fontBold = new Font("Arial", Font.BOLD, 13);
        Font fontNormal = new Font("Arial", Font.PLAIN, 13);

        pdfDoc.addTextLine("Entity", fontBold, 15, 40);
        pdfDoc.addTextLine("Benchmark : MCI Euro", fontNormal, 15, 53);
        pdfDoc.addTextLine("Approval Date : 15/09/2015", fontNormal, 15, 66);

        pdfDoc.addTextLine("Other Parameters", fontBold, 15, 92);
        pdfDoc.addTextLine("Benchmark Type : Master Configuration", fontNormal, 15, 105);
        pdfDoc.addTextLine("Currency : EUR", fontNormal, 15, 118);

        File file = new File("gradient.svg");
        pdfDoc.addSvgImage(file, 0, 131);

        ByteArrayOutputStream pdfPageStream = pdfDoc.getPDFStream();
        File temp = new File("result1.pdf");

        try(FileOutputStream fileOut = new FileOutputStream(temp)) {
            fileOut.write(pdfPageStream.toByteArray());
        }
    }
}
