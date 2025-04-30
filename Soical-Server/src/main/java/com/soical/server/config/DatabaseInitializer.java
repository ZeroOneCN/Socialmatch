package com.soical.server.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * 数据库初始化器，用于应用启动时执行数据库修复脚本
 */
@Slf4j
@Component
public class DatabaseInitializer implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;
    private final DataSource dataSource;

    @Autowired
    public DatabaseInitializer(JdbcTemplate jdbcTemplate, DataSource dataSource) {
        this.jdbcTemplate = jdbcTemplate;
        this.dataSource = dataSource;
    }

    @Override
    public void run(String... args) throws Exception {
        log.info("检查和修复数据库表结构...");
        
        // 先执行SQL修复脚本
        executeSqlFixScript();
        
        // 检查聊天消息表结构，确保与实体类匹配
        fixChatMessageTable();
        
        // 检查用户资料表中是否有重复用户ID
        checkForDuplicateProfiles();
        
        log.info("数据库初始化完成");
    }
    
    /**
     * 执行SQL修复脚本
     */
    private void executeSqlFixScript() {
        try {
            log.info("执行SQL修复脚本...");
            
            // 尝试加载SQL修复脚本
            ClassPathResource resource = new ClassPathResource("db/fix/message-fix.sql");
            
            if (resource.exists()) {
                // 读取脚本内容
                String sqlScript;
                try (Reader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)) {
                    sqlScript = FileCopyUtils.copyToString(reader);
                }
                
                if (sqlScript != null && !sqlScript.trim().isEmpty()) {
                    log.info("找到SQL修复脚本，开始执行...");
                    
                    // 按分号分割SQL语句
                    String[] sqlStatements = sqlScript.split(";");
                    int executedCount = 0;
                    
                    for (String sql : sqlStatements) {
                        String trimmedSql = sql.trim();
                        if (!trimmedSql.isEmpty()) {
                            try {
                                jdbcTemplate.execute(trimmedSql);
                                executedCount++;
                                log.debug("执行SQL语句成功: {}", trimmedSql.substring(0, Math.min(100, trimmedSql.length())) + "...");
                            } catch (DataAccessException e) {
                                log.warn("执行SQL语句时出错: {}", e.getMessage());
                            }
                        }
                    }
                    
                    log.info("SQL修复脚本执行完成，共执行 {} 条SQL语句", executedCount);
                } else {
                    log.info("SQL修复脚本为空，跳过执行");
                }
            } else {
                log.info("未找到SQL修复脚本，跳过执行");
            }
        } catch (IOException e) {
            log.error("读取SQL修复脚本时出错", e);
        } catch (Exception e) {
            log.error("执行SQL修复脚本时出错", e);
        }
    }
    
    /**
     * 修复聊天消息表结构
     */
    private void fixChatMessageTable() {
        try {
            log.info("检查t_chat_message表结构...");
            
            // 检查表是否存在
            if (!tableExists("t_chat_message")) {
                log.info("t_chat_message表不存在，无需修复");
                return;
            }
            
            // 检查列是否符合实体类定义
            checkAndFixColumn("t_chat_message", "receiver_id", "BIGINT", "接收者ID");
            checkTypeAndStatusColumns();
            
            log.info("t_chat_message表结构检查完成");
        } catch (Exception e) {
            log.error("修复聊天消息表结构时出错", e);
        }
    }
    
    /**
     * 检查表是否存在
     */
    private boolean tableExists(String tableName) {
        try (Connection conn = dataSource.getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();
            ResultSet rs = metaData.getTables(null, null, tableName, new String[]{"TABLE"});
            return rs.next();
        } catch (SQLException e) {
            log.error("检查表是否存在时出错", e);
            return false;
        }
    }
    
    /**
     * 检查并修复列
     */
    private void checkAndFixColumn(String tableName, String columnName, String dataType, String comment) {
        try {
            // 检查列是否存在
            String checkSql = "SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS " +
                    "WHERE TABLE_NAME = ? AND COLUMN_NAME = ?";
            Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class, tableName, columnName);
            
            if (count != null && count == 0) {
                log.info("添加缺失的列: {}.{}", tableName, columnName);
                String alterSql = String.format("ALTER TABLE %s ADD COLUMN %s %s NOT NULL COMMENT '%s'", 
                        tableName, columnName, dataType, comment);
                jdbcTemplate.execute(alterSql);
                
                // 如果是receiver_id列，需要从会话表中填充数据
                if ("receiver_id".equals(columnName)) {
                    updateReceiverIdFromConversation();
                }
            } else {
                log.info("列已存在: {}.{}", tableName, columnName);
            }
        } catch (Exception e) {
            log.error("检查并修复列时出错: {}.{}", tableName, columnName, e);
        }
    }
    
    /**
     * 从会话表中更新receiver_id
     */
    private void updateReceiverIdFromConversation() {
        try {
            String updateSql = "UPDATE t_chat_message m " +
                    "JOIN t_conversation c ON m.conversation_id = c.conversation_id " +
                    "SET m.receiver_id = " +
                    "    CASE " +
                    "        WHEN m.sender_id = c.user_a_id THEN c.user_b_id " +
                    "        ELSE c.user_a_id " +
                    "    END " +
                    "WHERE m.receiver_id = 0 OR m.receiver_id IS NULL";
            int rowsAffected = jdbcTemplate.update(updateSql);
            log.info("已更新 {} 条消息的接收者ID", rowsAffected);
        } catch (Exception e) {
            log.error("更新接收者ID时出错", e);
        }
    }
    
    /**
     * 检查并修复type和status列
     */
    private void checkTypeAndStatusColumns() {
        try {
            // 检查type列是否存在
            String checkTypeSql = "SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS " +
                    "WHERE TABLE_NAME = 't_chat_message' AND COLUMN_NAME = 'type'";
            Integer typeCount = jdbcTemplate.queryForObject(checkTypeSql, Integer.class);
            
            // 检查message_type列是否存在
            String checkMessageTypeSql = "SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS " +
                    "WHERE TABLE_NAME = 't_chat_message' AND COLUMN_NAME = 'message_type'";
            Integer messageTypeCount = jdbcTemplate.queryForObject(checkMessageTypeSql, Integer.class);
            
            // 如果type存在但message_type不存在，则重命名
            if (typeCount != null && typeCount > 0 && (messageTypeCount == null || messageTypeCount == 0)) {
                log.info("将type列重命名为message_type");
                jdbcTemplate.execute("ALTER TABLE t_chat_message CHANGE COLUMN type message_type INT DEFAULT 1 COMMENT '消息类型：1-文本，2-图片'");
            }
            
            // 检查status列是否存在
            String checkStatusSql = "SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS " +
                    "WHERE TABLE_NAME = 't_chat_message' AND COLUMN_NAME = 'status'";
            Integer statusCount = jdbcTemplate.queryForObject(checkStatusSql, Integer.class);
            
            // 检查is_read列是否存在
            String checkIsReadSql = "SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS " +
                    "WHERE TABLE_NAME = 't_chat_message' AND COLUMN_NAME = 'is_read'";
            Integer isReadCount = jdbcTemplate.queryForObject(checkIsReadSql, Integer.class);
            
            // 如果status存在但is_read不存在，则重命名
            if (statusCount != null && statusCount > 0 && (isReadCount == null || isReadCount == 0)) {
                log.info("将status列重命名为is_read");
                jdbcTemplate.execute("ALTER TABLE t_chat_message CHANGE COLUMN status is_read BOOLEAN DEFAULT FALSE COMMENT '是否已读'");
            }
        } catch (Exception e) {
            log.error("检查并修复type和status列时出错", e);
        }
    }
    
    /**
     * 检查用户资料表中是否有重复用户ID
     */
    private void checkForDuplicateProfiles() {
        try {
            log.info("检查用户资料表中是否有重复用户ID...");
            
            String checkSql = "SELECT user_id, COUNT(*) AS count FROM t_user_profile GROUP BY user_id HAVING COUNT(*) > 1";
            List<Map<String, Object>> duplicates = jdbcTemplate.queryForList(checkSql);
            
            if (!duplicates.isEmpty()) {
                log.warn("发现 {} 个重复的用户资料", duplicates.size());
                
                for (Map<String, Object> duplicate : duplicates) {
                    Long userId = ((Number) duplicate.get("user_id")).longValue();
                    int count = ((Number) duplicate.get("count")).intValue();
                    
                    log.warn("用户ID {} 有 {} 个资料记录", userId, count);
                    
                    // 保留最新的一条记录，删除其他记录
                    String deleteSql = "DELETE FROM t_user_profile " +
                            "WHERE user_id = ? AND profile_id NOT IN (" +
                            "SELECT profile_id FROM (" +
                            "SELECT MAX(profile_id) AS profile_id FROM t_user_profile WHERE user_id = ?" +
                            ") AS temp)";
                    
                    int rowsDeleted = jdbcTemplate.update(deleteSql, userId, userId);
                    log.info("为用户ID {} 删除了 {} 条重复记录", userId, rowsDeleted);
                }
            } else {
                log.info("用户资料表中没有重复的用户ID");
            }
        } catch (Exception e) {
            log.error("检查重复用户资料时出错", e);
        }
    }
} 