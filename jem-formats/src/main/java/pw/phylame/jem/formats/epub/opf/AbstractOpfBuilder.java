/*
 * Copyright 2014-2015 Peng Wan <phylame@163.com>
 *
 * This file is part of Jem.
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

package pw.phylame.jem.formats.epub.opf;

import org.dom4j.Element;

/**
 * Implements manifest, spine, guide methods.
 */
public abstract class AbstractOpfBuilder implements OpfBuilder {
    protected Element manifestElement = null;
    protected Element spineElement = null;
    protected Element guideElement = null;

    protected String coverHref = null;

    @Override
    public Element addManifestItem(String id, String href, String mediaType) {
        assert manifestElement != null;
        Element item = manifestElement.addElement("item");
        item.addAttribute("id", id);
        item.addAttribute("href", href);
        item.addAttribute("media-type", mediaType);
        return item;
    }

    @Override
    public Element addSpineItem(String idref, boolean linear, String properties) {
        assert spineElement != null;
        Element item = spineElement.addElement("itemref");
        item.addAttribute("idref", idref);
        if (!linear) {
            item.addAttribute("linear", "no");
        }
        if (properties != null) {
            item.addAttribute("properties", properties);
        }
        return item;

    }

    @Override
    public Element addGuideItem(String href, String type, String title) {
        assert guideElement != null;
        Element item = guideElement.addElement("reference");
        item.addAttribute("href", href);
        item.addAttribute("type", type);
        item.addAttribute("title", title);
        return item;
    }

    @Override
    public String getCover() {
        return coverHref;
    }
}
