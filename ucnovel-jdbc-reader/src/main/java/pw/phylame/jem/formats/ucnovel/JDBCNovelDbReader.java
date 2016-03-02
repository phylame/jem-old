/*
 * Copyright 2016 Peng Wan <phylame@163.com>
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

package pw.phylame.jem.formats.ucnovel;

import pw.phylame.jem.formats.util.ParserException;

import java.util.ArrayList;
import java.util.Date;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.DriverManager;

public class JDBCNovelDbReader implements NovelDbReader {
    private static final String DRIVER_NAME = "org.sqlite.JDBC";

    private Connection connection;

    private void openConnection(String dbPath) throws ParserException {
        String url = "jdbc:sqlite:" + dbPath;

        try {
            Class.forName(DRIVER_NAME);
        } catch (ClassNotFoundException e) {
            throw new ParserException("Not found SQLite driver: " + DRIVER_NAME, e);
        }
        try {
            connection = DriverManager.getConnection(url);
        } catch (SQLException e) {
            throw new ParserException("Cannot create connection to SQLite: " + url, e);
        }
    }

    @Override
    public void init(String dbPath) throws ParserException {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                throw new ParserException("Cannot close previous connection", e);
            }
        }
        openConnection(dbPath);
    }

    @Override
    public String[] fetchNovels() throws ParserException {
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(SQL_FETCH_NOVELS)) {
            ArrayList<String> novelIDs = new ArrayList<>();
            while (rs.next()) {
                novelIDs.add(rs.getString("book_id"));
            }
            return novelIDs.toArray(new String[novelIDs.size()]);
        } catch (SQLException e) {
            throw new ParserException("Failed to fetch novel list", e);
        }
    }

    private NovelInfo parseInfo(ResultSet rs) throws SQLException {
        NovelInfo info = new NovelInfo();
        info.name = rs.getString(NOVEL_NAME);
        info.author = rs.getString(NOVEL_AUTHOR);
        info.expireTime = new Date(rs.getLong(NOVEL_EXPIRE_TIME));
        info.updateTime = new Date(rs.getLong(NOVEL_UPDATE_TIME));
        info.table = rs.getString(NOVEL_TABLE_NAME);
        return info;
    }

    @Override
    public NovelInfo fetchInfo(String novelId) throws ParserException {
        String sql = String.format(SQL_FETCH_INFO, novelId);
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            return rs.next() ? parseInfo(rs) : null;
        } catch (SQLException e) {
            throw new ParserException("Failed to fetch novel information", e);
        }
    }

    private ChapterItem parseItem(ResultSet rs) throws SQLException {
        ChapterItem item = new ChapterItem();
        item.id = rs.getInt(CHAPTER_ID);
        item.title = rs.getString(CHAPTER_NAME);
        item.startIndex = rs.getLong(CHAPTER_INDEX_START);
        item.endIndex = rs.getLong(CHAPTER_INDEX_END);
        item.isNew = rs.getInt(CHAPTER_IS_NEW) != 0;
        item.itemIndex = rs.getInt(CHAPTER_INDEX);
        item.offlinePath = rs.getString(CHAPTER_OFFLINE_PATH);
        item.cdnURL = rs.getString(CHAPTER_CDN_URL);
        item.contentKey = rs.getString(CHAPTER_CONTENT_KEY);
        item.updateTime = new Date(rs.getLong(CHAPTER_UPDATE_TIME));
        return item;
    }

    @Override
    public void fetchChapters(ChapterWatcher watcher, String novelTable) throws ParserException {
        String sql = String.format(SQL_FETCH_CHAPTER, novelTable);
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                watcher.watch(parseItem(rs));
            }
        } catch (SQLException e) {
            throw new ParserException("Failed to fetch novel chapters", e);
        }
    }

    @Override
    public void close() throws IOException {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                throw new IOException("Cannot close SQLite connection", e);
            }
        }
    }
}
