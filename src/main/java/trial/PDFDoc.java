/**
 * Author: Hicham B.I.
 * Date: 08/03/15
 * Time: 08:52
 */

package trial;

import org.apache.batik.bridge.*;
import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.dom.svg.SAXSVGDocumentFactory;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.svggen.SVGGeneratorContext;
import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.batik.transcoder.SVGAbstractTranscoder;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.util.XMLResourceDescriptor;
import org.apache.fop.svg.PDFTranscoder;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.svg.SVGDocument;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.io.*;

public class PDFDoc {

    private SVGGraphics2D svgGraphics2D;
    private SAXSVGDocumentFactory factory;
    private BridgeContext ctx;
    private GVTBuilder builder;

    private float documentWidth;
    private float documentHeight;

    public PDFDoc(float width, float height) {
        documentWidth = width;
        documentHeight = height;
        initialize();
    }

    public void addTextLine(String text, Font font, float xPosition, float yPosition) {
        svgGraphics2D.setFont(font);
        svgGraphics2D.drawString(text, xPosition, yPosition);
    }

    public void addSvgImage(File svgFile, float xPosition, float yPosition) throws IOException {
        SVGDocument svgDocument = factory.createSVGDocument(svgFile.toURL().toString());
        GraphicsNode mapGraphics = builder.build(ctx, svgDocument);

        AffineTransform transformer = new AffineTransform();
        transformer.translate(xPosition, yPosition);
        mapGraphics.setTransform(transformer);
        mapGraphics.paint(svgGraphics2D);
    }

    public ByteArrayOutputStream getPDFStream() throws IOException, TranscoderException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        Writer out = new OutputStreamWriter(outputStream, "UTF-8");
        svgGraphics2D.stream(out, true);

        String svg = new String(outputStream.toByteArray(), "UTF-8");

        return transcode(svg);
    }

    private void initialize() {
        // Get a DOMImplementation
        DOMImplementation domImpl = GenericDOMImplementation.getDOMImplementation();
        String svgNamespaceURI = "http://www.w3.org/2000/svg";

        // Create an instance of org.w3c.dom.Document
        Document document = domImpl.createDocument(svgNamespaceURI, "svg", null);

        // Create an instance of the SVG Generator
//        svgGraphics2D = new SVGGraphics2D(document);

        SVGGeneratorContext sVGGeneratorContext = SVGGeneratorContext.createDefault(document);
        sVGGeneratorContext.setExtensionHandler(new GradientExtensionHandler());
        svgGraphics2D  = new SVGGraphics2D(sVGGeneratorContext, false);


        svgGraphics2D.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_GASP);

        String parser = XMLResourceDescriptor.getXMLParserClassName();
        factory = new SAXSVGDocumentFactory(parser);

        UserAgent userAgent = new UserAgentAdapter();
        DocumentLoader loader = new DocumentLoader(userAgent);
        ctx = new BridgeContext(userAgent, loader);
        ctx.setDynamicState(BridgeContext.DYNAMIC);

        builder = new GVTBuilder();
    }

    private ByteArrayOutputStream transcode(String svg) throws TranscoderException {
        TranscoderInput input = new TranscoderInput(new StringReader(svg));
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        TranscoderOutput transOutput = new TranscoderOutput(output);
        SVGAbstractTranscoder transcoder = new PDFTranscoder();

        transcoder.addTranscodingHint(SVGAbstractTranscoder.KEY_WIDTH, documentWidth);
        transcoder.addTranscodingHint(SVGAbstractTranscoder.KEY_HEIGHT, documentHeight);
        transcoder.transcode(input, transOutput);
        return output;
    }
}
