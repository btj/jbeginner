// Placed in the public domain, 2003.  Share and enjoy!
//
// This software is provided 'as-is', without any express or implied
// warranty. In no event will the authors be held liable for any
// damages arising from the use of this software.

import java.awt.*;

class SwingUtil {
    static void drawStringCentered(Graphics g, String s, int baseline, int left, int right) {
        int width = g.getFontMetrics().stringWidth(s);
        int maxWidth = right - left;
        if (maxWidth < width)
            drawStringBounded(g, s, baseline, left, right);
        else
            g.drawString(s, left + (maxWidth - width) / 2, baseline);
    }
    
    static void drawStringRightJustified(Graphics g, String s, int baseline, int left, int right) {
        int width = g.getFontMetrics().stringWidth(s);
        int maxWidth = right - left;
        if (maxWidth < width)
            drawStringBounded(g, s, baseline, left, right);
        else
            g.drawString(s, left + (maxWidth - width), baseline);
    }
    
    static void drawStringBounded(Graphics g, String text, int baseline, int left, int right) {
        FontMetrics metrics = g.getFontMetrics();
        int width = metrics.stringWidth(text);
        int maxWidth = right - left;
        String result;
        if (width <= maxWidth) {
            result = text;
        } else {
            result = "...";
            int knownGood = 0;
            int knownBad = text.length();
            while (knownGood + 1 < knownBad) {
                int k = (knownGood + 1 + knownBad) / 2;
                String s = text.substring(0, k) + "...";
                int w = metrics.stringWidth(s);
                if (w <= maxWidth) {
                    result = s;
                    knownGood = k;
                } else {
                    knownBad = k;
                }
            }
        }
        g.drawString(result, left, baseline);
    }
    
    static Color desaturate(Color color) {
        float[] hsb = new float[3];
        Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), hsb);
        float hue = hsb[0];
        float saturation = hsb[1];
        float brightness = hsb[2];
        return Color.getHSBColor(hue, saturation / 2, brightness);
    }

	static Font deriveFont(Font baseFont, int style) {
		return new Font(baseFont.getName(), style, baseFont.getSize());
	}

	static Font deriveBoldFont(Font baseFont) {
		return deriveFont(baseFont, Font.BOLD);
	}
}