package com.hei;

import com.itextpdf.text.DocumentException;
import org.apache.batik.transcoder.TranscoderException;

import java.io.IOException;

/**
 Author: Hicham B.I.
 Date: 06/03/15
 Time: 23:46
 */

public class TestExporter {

    public static final String SVG_FILE_PATH = "text.svg";
    public static final String RESULT = "result.pdf";

    public static void main(String ... arg) throws IOException, DocumentException, TranscoderException {
        ItextExporter exporter = new ItextExporter();
//        GraphExporter exporter = new GraphExporter();
        exporter.createPdf(SVG_FILE_PATH, RESULT);
    }

}
