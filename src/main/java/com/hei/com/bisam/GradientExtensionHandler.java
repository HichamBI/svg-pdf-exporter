package com.hei.com.bisam;

import org.apache.batik.ext.awt.LinearGradientPaint;
import org.apache.batik.ext.awt.MultipleGradientPaint;
import org.apache.batik.ext.awt.RadialGradientPaint;
import org.apache.batik.svggen.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

import static org.apache.batik.util.SVGConstants.*;

class GradientExtensionHandler extends DefaultExtensionHandler {

    @Override
    public SVGPaintDescriptor handlePaint(Paint paint, SVGGeneratorContext generatorCtx) {

        if (paint instanceof LinearGradientPaint)
            return getLinearGradientPaintDescriptor((LinearGradientPaint) paint, generatorCtx);
        else if (paint instanceof RadialGradientPaint) {
            return getRadialGradientPaintDescriptor((RadialGradientPaint) paint, generatorCtx);
        }

        return super.handlePaint(paint, generatorCtx);
    }

    private SVGPaintDescriptor getRadialGradientPaintDescriptor(RadialGradientPaint paint, SVGGeneratorContext generatorCtx) {
        RadialGradientPaint gradient = paint;

        // Create a new SVG 'radialGradient' element to represent the LinearGradientPaint being used.
        Element grad = generatorCtx.getDOMFactory().createElementNS(SVG_NAMESPACE_URI, SVG_RADIAL_GRADIENT_TAG);

        // Create and set unique XML id
        String id = generatorCtx.getIDGenerator().generateID("gradient");
        grad.setAttributeNS(null, SVGSyntax.SVG_ID_ATTRIBUTE, id);

        // Set x,y pairs
        Point2D centerPt = gradient.getCenterPoint();
        grad.setAttributeNS(null, SVGSyntax.SVG_CX_ATTRIBUTE, String.valueOf(centerPt.getX()));
        grad.setAttributeNS(null, SVGSyntax.SVG_CY_ATTRIBUTE, String.valueOf(centerPt.getY()));

        Point2D focusPt = gradient.getFocusPoint();
        grad.setAttributeNS(null, SVGSyntax.SVG_FX_ATTRIBUTE, String.valueOf(focusPt.getX()));
        grad.setAttributeNS(null, SVGSyntax.SVG_FY_ATTRIBUTE, String.valueOf(focusPt.getY()));

        grad.setAttributeNS(null, SVGSyntax.SVG_R_ATTRIBUTE, String.valueOf(gradient.getRadius()));

        setMultipleGradientPaintAttributes(generatorCtx, gradient, grad);

        return new SVGPaintDescriptor("url(#" + id + ")", SVG_OPAQUE_VALUE, grad);
    }

    private SVGPaintDescriptor getLinearGradientPaintDescriptor(LinearGradientPaint paint, SVGGeneratorContext generatorCtx) {
        LinearGradientPaint gradient = paint;

        // Create a new SVG 'linearGradient' element to represent the LinearGradientPaint being used.
        String id = generatorCtx.getIDGenerator().generateID("gradient");
        Document doc = generatorCtx.getDOMFactory();
        Element grad = doc.createElementNS
                (SVG_NAMESPACE_URI,
                        SVG_LINEAR_GRADIENT_TAG);

        // Set the relevant attributes on the 'linearGradient' element.
        grad.setAttributeNS(null, SVG_ID_ATTRIBUTE, id);
        Point2D pt = gradient.getStartPoint();
        grad.setAttributeNS(null, "x1", String.valueOf(pt.getX()));
        grad.setAttributeNS(null, "y1", String.valueOf(pt.getY()));

        pt = gradient.getEndPoint();
        grad.setAttributeNS(null, "x2", String.valueOf(pt.getX()));
        grad.setAttributeNS(null, "y2", String.valueOf(pt.getY()));

        setMultipleGradientPaintAttributes(generatorCtx, gradient, grad);

        return new SVGPaintDescriptor("url(#" + id + ")", SVG_OPAQUE_VALUE, grad);
    }

    private void setMultipleGradientPaintAttributes(SVGGeneratorContext generatorCtx, MultipleGradientPaint gradient, Element grad) {
        // Set cycle method
        if (gradient.getCycleMethod().equals(MultipleGradientPaint.REFLECT)) {
            grad.setAttributeNS(null, SVG_SPREAD_METHOD_ATTRIBUTE, SVG_REFLECT_VALUE);
        } else if (gradient.getCycleMethod().equals(MultipleGradientPaint.REPEAT)) {
            grad.setAttributeNS(null, SVG_SPREAD_METHOD_ATTRIBUTE, SVG_REPEAT_VALUE);
        }

        // Set color space
        if (gradient.getColorSpace().equals(MultipleGradientPaint.LINEAR_RGB)) {
            grad.setAttributeNS(null, SVG_COLOR_INTERPOLATION_ATTRIBUTE, SVG_LINEAR_RGB_VALUE);
        } else if (gradient.getColorSpace().equals(MultipleGradientPaint.SRGB)) {
            grad.setAttributeNS(null, SVG_COLOR_INTERPOLATION_ATTRIBUTE, SVG_SRGB_VALUE);
        }

        // Set transform matrix if not identity
        AffineTransform tf = gradient.getTransform();
        if (!tf.isIdentity()) {
            String matrix = "matrix(" +
                    tf.getScaleX() + " " + tf.getShearX() + " " + tf.getTranslateX() + " " +
                    tf.getScaleY() + " " + tf.getShearY() + " " + tf.getTranslateY() + ")";
            grad.setAttribute(SVG_TRANSFORM_ATTRIBUTE, matrix);
        }

        // Convert gradient stops
        Color[] colors = gradient.getColors();
        float[] fracs = gradient.getFractions();

        for (int i = 0; i < colors.length; i++) {
            Element stop = generatorCtx.getDOMFactory().createElementNS(SVG_NAMESPACE_URI, SVG_STOP_TAG);
            SVGPaintDescriptor pd = SVGColor.toSVG(colors[i], generatorCtx);

            stop.setAttribute(SVG_OFFSET_ATTRIBUTE, (int) (fracs[i] * 100.0f) + "%");
            stop.setAttribute(SVG_STOP_COLOR_ATTRIBUTE, pd.getPaintValue());

            if (colors[i].getAlpha() != 255) {
                stop.setAttribute(SVG_STOP_OPACITY_ATTRIBUTE, pd.getOpacityValue());
            }

            grad.appendChild(stop);
        }
    }
}
