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

package pw.phylame.jem.formats.pmab.writer;

import pw.phylame.jem.core.Book;
import pw.phylame.jem.formats.pmab.PmabConfig;

import java.util.zip.ZipOutputStream;

/**
 * Created by Peng Wan on 2015-3-17.
 */
public final class WriterV3 {
    public static void writePBM(Book book, ZipOutputStream zipOut, PmabConfig config) {
        System.out.println("PBM 3.0");
    }

    public static void writePBC(Book book, ZipOutputStream zipOut, PmabConfig config) {
        System.out.println("PBC 3.0");
    }
}
