/*
 * Copyright 2015 Peng Wan <phylame@163.com>
 *
 * This file is part of PAT Core.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package pw.pat.ixin;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import pw.pat.gaf.Settings;

import java.awt.Font;
import java.awt.Color;
import java.awt.Point;
import java.awt.Dimension;

/**
 * Settings for GUI applications.
 */
public class ISettings extends Settings {
    private static Log LOG = LogFactory.getLog(ISettings.class);

    public ISettings() {
        super();
    }

    public ISettings(boolean loading) {
        super(loading);
    }

    public ISettings(boolean loading, String baseName) {
        super(loading, baseName);
    }

    public static String toString(Font font) {
        StringBuilder builder = new StringBuilder(font.getFamily());
        builder.append("-");
        switch (font.getStyle()) {
            case Font.PLAIN:
                builder.append("PLAIN");
                break;
            case Font.BOLD:
                builder.append("BOLD");
                break;
            case Font.ITALIC:
                builder.append("ITALIC");
                break;
            case Font.BOLD|Font.ITALIC:
                builder.append("BOLDITALIC");
                break;
        }
        builder.append("-").append(font.getSize());
        return builder.toString();
    }

    public static String toString(Color color) {
        String str = String.format("%X", color.getRGB());
        return "#"+str.substring(2);
    }

    public static String toString(Dimension dimension) {
        return dimension.getWidth() + "-" + dimension.getHeight();
    }

    public static String toString(Point point) {
        return ((int) point.getX()) + "-" + ((int) point.getY());
    }

    public Font getFont(String key, Font defaultValue) {
        String str = getProperty(key);
        if (isEmpty(str)) {
            return defaultValue;
        }
        return Font.decode(str);
    }

    public void setFont(String key, Font font, String comment) {
        setProperty(key, toString(font), comment);
    }

    public Color getColor(String key, Color defaultValue) {
        String str = getProperty(key);
        if (isEmpty(str)) {
            return defaultValue;
        }
        try {
            return Color.decode(str);
        } catch (NumberFormatException e) {
            LOG.debug("invalid color format: "+str, e);
            return defaultValue;
        }
    }

    public void setColor(String key, Color color, String comment) {
        setProperty(key, toString(color), comment);
    }

    public Point getPoint(String key, Point defaultValue) {
        String str = getProperty(key);
        if (isEmpty(str)) {
            return defaultValue;
        }
        String[] parts = str.split("-");
        if (parts.length < 2) {
            LOG.debug("invalid point format: "+str+", require X-Y");
            return defaultValue;
        }
        try {
            return new Point(Integer.decode(parts[0].trim()), Integer.decode(parts[1].trim()));
        } catch (NumberFormatException e) {
            LOG.debug("invalid point format: "+str+", require X-Y", e);
            return defaultValue;
        }
    }

    public void setPoint(String key, Point point, String comment) {
        setProperty(key, toString(point), comment);
    }

    public Dimension getDimension(String key, Dimension defaultValue) {
        String str = getProperty(key);
        if (isEmpty(str)) {
            return defaultValue;
        }
        String[] parts = str.split("-");
        if (parts.length < 2) {
            LOG.debug("invalid dimension format: "+str+", require WIDTH-HEIGHT");
            return defaultValue;
        }
        try {
            return new Dimension(Integer.decode(parts[0].trim()), Integer.decode(parts[1].trim()));
        } catch (NumberFormatException e) {
            LOG.debug("invalid dimension format: "+str+", require WIDTH-HEIGHT", e);
            return defaultValue;
        }
    }

    public void setDimension(String key, Dimension dimension, String comment) {
        setProperty(key, toString(dimension), comment);
    }
}
