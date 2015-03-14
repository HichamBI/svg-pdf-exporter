/**
 Author: Hicham B.I.
 Date: 06/03/15
 Time: 23:46
 */
package trial;

import com.itextpdf.text.DocumentException;
import org.apache.batik.transcoder.TranscoderException;

import java.io.IOException;

public class TestExporter {

    public static final String SVG_FILE_PATH = "text.svg";
    public static final String RESULT = "result.pdf";

    public static void main(String ... arg) throws IOException, DocumentException, TranscoderException {
        ItextExporter exporter = new ItextExporter();
//        PDFBoxExporter exporter = new PDFBoxExporter();
        exporter.createPdf(SVG_FILE_PATH, RESULT);
    }

}
