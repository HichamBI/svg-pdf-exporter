package exporter;

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

public class PDFDocument {
    private static final int LINE_HEIGHT = 12;
    private final Font plainTextFont;
    private final Font boldTextFont;
    private SVGGraphics2D svgGraphics2D;
    private SAXSVGDocumentFactory factory;
    private BridgeContext ctx;
    private GVTBuilder builder;
    private float yPosition;
    private float xPosition;
    private float documentWidth;

    public PDFDocument() {
        initializeDocument();

        this.yPosition = 30f;
        this.xPosition = 15f;

        this.plainTextFont = new Font("Helvetica", Font.PLAIN, LINE_HEIGHT);
        this.boldTextFont = plainTextFont.deriveFont(Font.BOLD);
    }

    public void addTextLine(String text) {
        addTextCell(text, 0);
        carriageReturn();
    }

    public void addTextCell(String text, float x) {
        svgGraphics2D.setFont(plainTextFont);
        svgGraphics2D.drawString(text, xPosition + x, yPosition);
    }

    public void addTitleLine(String text) {
        svgGraphics2D.setFont(boldTextFont);
        svgGraphics2D.drawString(text, xPosition, yPosition);
        carriageReturn();
    }

    public void carriageReturn() {
        yPosition = yPosition + LINE_HEIGHT;
    }

    public void addSvgImage(File svg) throws IOException {
        SVGDocument svgDocument = factory.createSVGDocument(svg.toURL().toString());
        GraphicsNode mapGraphics = builder.build(ctx, svgDocument);
        AffineTransform transformer = new AffineTransform();
        transformer.translate(0, yPosition);
        mapGraphics.setTransform(transformer);
        mapGraphics.paint(svgGraphics2D);

        yPosition = (float) (yPosition + mapGraphics.getBounds().getHeight());

        documentWidth = (float) mapGraphics.getBounds().getWidth();
    }

    public ByteArrayOutputStream getDocumentStream() throws IOException, TranscoderException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Writer out = new OutputStreamWriter(outputStream, "UTF-8");
        svgGraphics2D.stream(out, true);
        svgGraphics2D.dispose();

        svgGraphics2D.getTransform();
        String svg = new String(outputStream.toByteArray(), "UTF-8");
        return transcodeToPDFStream(svg);
    }

    private void initializeDocument() {
        // Get a DOMImplementation
        DOMImplementation domImpl = GenericDOMImplementation.getDOMImplementation();
        String svgNamespaceURI = "http://www.w3.org/2000/svg";

        // Create an instance of org.w3c.dom.Document
        Document document = domImpl.createDocument(svgNamespaceURI, "svg", null);

        // Create an instance of the SVG Generator
        SVGGeneratorContext sVGGeneratorContext = SVGGeneratorContext.createDefault(document);
        sVGGeneratorContext.setExtensionHandler(new GradientExtensionHandler());
        svgGraphics2D = new SVGGraphics2D(sVGGeneratorContext, false);

        svgGraphics2D.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_GASP);

        // Create SVG parser
        String parser = XMLResourceDescriptor.getXMLParserClassName();
        factory = new SAXSVGDocumentFactory(parser);
        UserAgent userAgent = new UserAgentAdapter();
        DocumentLoader loader = new DocumentLoader(userAgent);
        ctx = new BridgeContext(userAgent, loader);
        ctx.setDynamicState(BridgeContext.STATIC);
        builder = new GVTBuilder();
    }

    private ByteArrayOutputStream transcodeToPDFStream(String svg) throws TranscoderException {
        TranscoderInput input = new TranscoderInput(new StringReader(svg));
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        TranscoderOutput transOutput = new TranscoderOutput(output);
        SVGAbstractTranscoder transcoder = new PDFTranscoder();
        transcoder.addTranscodingHint(SVGAbstractTranscoder.KEY_WIDTH, documentWidth);
        transcoder.addTranscodingHint(SVGAbstractTranscoder.KEY_HEIGHT, yPosition);
        transcoder.transcode(input, transOutput);
        return output;
    }
}
