package io.github.smagical.bot.plugin.util;

import io.github.smagical.bot.plugin.datasource.model.TgBotInfo;
import io.github.smagical.bot.plugin.datasource.model.TgGroup;
import io.github.smagical.bot.plugin.datasource.model.TgMessage;
import io.github.smagical.bot.plugin.datasource.model.TgSpider;
import io.github.smagical.bot.plugin.handler.PageHelper;
import io.github.smagical.bot.plugin.handler.message.MessageContentHandler;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.io.*;
import java.sql.*;
import java.util.List;

@Slf4j
public class DbUtil {

    public static void initSchem(DataSource dataSource, String file) throws IOException, SQLException {
        InputStream stream = null;
        if (file.startsWith("classpath:")){
            stream = DbUtil.class.getClassLoader().getResourceAsStream(file.substring("classpath:".length()));
        }else {
            stream = new FileInputStream(file);
        }

        try( InputStreamReader reader = new InputStreamReader(stream);
             BufferedReader bufferedReader = new BufferedReader(reader);
             ) {

            StringBuilder sql = new StringBuilder();
            String line = null;
            while ((line = bufferedReader.readLine())!=null){
                sql.append(line.strip());
            }
            try (   Connection connection = dataSource.getConnection();){
                connection.setAutoCommit(true);
                Statement statement =  connection.createStatement();
                for (String s : sql.toString().split(";")) {
                    System.out.println(s);
                    statement.addBatch(s);
                }
                statement.executeBatch();
            }
        }

    }

    private final static String INSERT_TG_MESSAGE_SQL = "INSERT INTO tg_messages VALUES (?,?,?,?,?,setweight(to_tsvector('simple',?),'A'))";
    private final static String UPDATE_TG_MESSAGE_OTHER_SQL = "UPDATE tg_messages SET other = other || setweight(to_tsvector('simple',?),'B') WHERE id = ? AND chat_id = ?";

    public static int insertTgMessage(DataSource dataSource, List<TgMessage> messages) throws IOException, SQLException {
        int success = 0;
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            for (TgMessage tgMessage : messages) {
                try {
                    PreparedStatement preparedStatement = connection.prepareStatement(INSERT_TG_MESSAGE_SQL);
                    preparedStatement.setLong(1, tgMessage.getId());
                    preparedStatement.setLong(2, tgMessage.getChatId());
                    preparedStatement.setLong(3,tgMessage.getAblum());
                    preparedStatement.setString(4, tgMessage.getMessage());
                    preparedStatement.setString(5,tgMessage.getLink());
                    preparedStatement.setString(6,
                            SegUtil.concat(SegUtil.seqByHanLP(tgMessage.getMessage())," "));
                    preparedStatement.execute();
                    preparedStatement.close();

                    preparedStatement = connection.prepareStatement(UPDATE_TG_MESSAGE_OTHER_SQL);
                    preparedStatement.setString(1,SegUtil.concat(SegUtil.seqByAll(tgMessage.getMessage())," "));
                    preparedStatement.setLong(2, tgMessage.getId());
                    preparedStatement.setLong(3,tgMessage.getChatId());
                    preparedStatement.execute();
                    connection.commit();
                    success++;
                }catch (Exception e){
                    log.error(e.getMessage());
                    e.printStackTrace();
                    connection.rollback();
                }
            }
            connection.setAutoCommit(true);
        }
        return success;
    }


    private final static String SELECT_QUERY_TG_MESSAGE_SQL = "SELECT id,chat_id,album,message,link,ts_rank(other,query) as rank FROM tg_messages,(select dd as query from to_tsquery('simple',?) as dd) WHERE query  @@ tg_messages.other ORDER BY rank";
    public static List<TgMessage> selectTgMessageByAll(DataSource dataSource, String query) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement statement = connection.prepareStatement(SELECT_QUERY_TG_MESSAGE_SQL);
            statement.setString(1,SegUtil.concat(SegUtil.seqByAll(query),"<1>"));
            ResultSet resultSet =  statement.executeQuery();
            return TgMessage.getTgMessages(resultSet);
        }

    }

    private final static String SELECT_QUERY_PAGE_TG_MESSAGE_SQL = "SELECT id,chat_id,album,message,link,ts_rank(other,query) as rank FROM tg_messages,(select dd as query from to_tsquery('simple',?) as dd) WHERE query  @@ tg_messages.other ORDER BY rank LIMIT ? OFFSET ?";
    public static MessageContentHandler.TgMessageHelper selectTgMessagePageByAll(DataSource dataSource, MessageContentHandler.TgMessageHelper helper) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            String query = SegUtil.concat(SegUtil.seqByAll(helper.getQuery()),"<1>");
            PreparedStatement statement = connection.prepareStatement(SELECT_QUERY_PAGE_TG_MESSAGE_SQL);
            statement.setString(1,query);
            statement.setInt(2,helper.getOffset());
            statement.setInt(3,helper.getPage());
            ResultSet resultSet =  statement.executeQuery();
            helper.setMessages(TgMessage.getTgMessages(resultSet));
            helper.setTotal(selectTgMessageCountByAll(dataSource, query));
            return helper;
        }

    }


    private final static String SELECT_QUERY_COUNT_TG_MESSAGE_SQL = "SELECT count(*) as num FROM tg_messages,to_tsquery('simple',?) as query WHERE query  @@ tg_messages.other ";
    public static int selectTgMessageCountByAll(DataSource dataSource,String query) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {

            PreparedStatement statement = connection.prepareStatement(SELECT_QUERY_COUNT_TG_MESSAGE_SQL);
            statement.setString(1,query);
            ResultSet resultSet =  statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt("num");
            }
            return 0;
        }

    }

    public static MessageContentHandler.TgMessageHelper selectTgMessageByHanlp(DataSource dataSource, MessageContentHandler.TgMessageHelper helper) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            String query = SegUtil.concat(SegUtil.seqByHanLP(helper.getQuery()),"<1>");
            PreparedStatement statement = connection.prepareStatement(SELECT_QUERY_PAGE_TG_MESSAGE_SQL);
            statement.setString(1,query);
            statement.setInt(2,helper.getOffset());
            statement.setInt(3,helper.getPage());
            ResultSet resultSet =  statement.executeQuery();
            helper.setMessages(TgMessage.getTgMessages(resultSet));
            helper.setTotal(selectTgMessageCountByAll(dataSource, query));
            return helper;
        }

    }

    public static List<TgMessage> selectTgMessageByHanlp(DataSource dataSource, String query) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement statement = connection.prepareStatement(SELECT_QUERY_TG_MESSAGE_SQL);
            statement.setString(1,SegUtil.concat(SegUtil.seqByHanLP(query),"<1>"));
            ResultSet resultSet =  statement.executeQuery();
            return TgMessage.getTgMessages(resultSet);
        }

    }

    private final static String SELECT_ID_TG_MESSAGE_SQL = "SELECT id,chat_id,album,message,link,type  FROM tg_messages WHERE id = ? AND chat_id = ?";

    public TgMessage selectTgMessageById(DataSource dataSource, Long id,Long chatId) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement statement = connection.prepareStatement(SELECT_ID_TG_MESSAGE_SQL);
            statement.setLong(1,id);
            statement.setLong(2,chatId);
            ResultSet resultSet =  statement.executeQuery();
            return TgMessage.getTgMessagesOne(resultSet);
        }
    }
    public boolean exitsTgMessage(DataSource dataSource, Long id,Long chatId) throws SQLException {
        return selectTgMessageById(dataSource, id, chatId) != null;
    }





    /**
     * tg_spider 表
     */
    private final static String INSERT_TG_SPIDER_SQL=  "INSERT INTO tg_spider VALUES (?,?,?)";
    public final static void insertTgSpider(DataSource dataSource, TgSpider spider) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(INSERT_TG_SPIDER_SQL);
            preparedStatement.setLong(1,spider.getChatId());
            preparedStatement.setString(2,spider.getChatName());
            preparedStatement.setLong(3,spider.getLastSpiderId());
            preparedStatement.execute();
        }
    }

    private final static String SELECT_ALL_TG_SPIDER_SQL="SELECT * FROM tg_spider";
    public final static List<TgSpider>  selectTgSpiderAll(DataSource dataSource) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement statement = connection.prepareStatement(SELECT_ALL_TG_SPIDER_SQL);
            ResultSet resultSet =  statement.executeQuery();
            return TgSpider.getTgSpider(resultSet);
        }
    }

    private final static String SELECT_PAGE_ALL_TG_SPIDER_SQL="SELECT * FROM tg_spider LIMIT ? OFFSET ?";
    public final static PageHelper<TgSpider>  selectTgSpiderAll(DataSource dataSource, PageHelper<TgSpider> helper) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement statement = connection.prepareStatement(SELECT_PAGE_ALL_TG_SPIDER_SQL);
            statement.setInt(1,helper.getPage());
            statement.setInt(2,helper.getOffset());
            ResultSet resultSet =  statement.executeQuery();
            helper.setMessages(TgSpider.getTgSpider(resultSet));
            helper.setTotal(selectTgSpiderCountAll(dataSource));
            return helper;
        }
    }

    private final static String SELECT_COUNT_ALL_TG_SPIDER_SQL="SELECT count(*) as num FROM tg_spider";
    public final static int  selectTgSpiderCountAll(DataSource dataSource) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement statement = connection.prepareStatement(SELECT_COUNT_ALL_TG_SPIDER_SQL);

            ResultSet resultSet =  statement.executeQuery();
            if (resultSet.next()) {
                return  resultSet.getInt("num");
            }
            return 0;
        }
    }


    private final static String SELECT_LAST_SPIDER_TG_MESSAGE_SQL="SELECT * FROM tg_spider WHERE chat_id = ?";
    public final static  TgSpider  selectLastTgSpiderById(DataSource dataSource,Long chat_id) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement statement = connection.prepareStatement(SELECT_LAST_SPIDER_TG_MESSAGE_SQL);
            statement.setLong(1,chat_id);
            ResultSet resultSet =  statement.executeQuery();
            return TgSpider.getTgSpiderOne(resultSet);
        }
    }
    public final static boolean exitsTgSpider(DataSource dataSource,Long chat_id) throws SQLException {
        return selectLastTgSpiderById(dataSource,chat_id) != null;
    }

//    private final static String UPDATE_LAST_SPIDER_TG_MESSAGE_SQL="UPDATE tg_spider SET last_spider_id = ? WHERE chat_id = ?";
    public  final static void   updateTgSpider(DataSource dataSource,TgSpider tgSpider) throws SQLException {
        if (tgSpider.getChatId() == null) return;
        if (tgSpider.getLastSpiderId() == null && tgSpider.getChatName() == null) return;
        try (Connection connection = dataSource.getConnection()) {
            StringBuilder sqlBuilder = new StringBuilder();
            sqlBuilder.append("UPDATE tg_spider SET ");
            boolean lastSplit = false;
            if (tgSpider.getLastSpiderId() != null) {
                sqlBuilder.append("last_spider_id=? ");
                lastSplit = true;
            }
            if (tgSpider.getChatName() != null) {
                if (lastSplit) sqlBuilder.append(", ");
                sqlBuilder.append("chat_name=? ");
            }
            sqlBuilder.append("WHERE chat_id=? ");
            PreparedStatement preparedStatement = connection.prepareStatement(sqlBuilder.toString());
            int count = 1;
            if (tgSpider.getLastSpiderId() != null) {
               preparedStatement.setLong(count++,tgSpider.getLastSpiderId());
            }
            if (tgSpider.getChatName() != null) {
                preparedStatement.setString(count++,tgSpider.getChatName());
            }
            preparedStatement.setLong(count,tgSpider.getChatId());
            preparedStatement.execute();
        }
    }

    public final static void insertOrUpdateTgSpider(DataSource dataSource,TgSpider tgSpider) throws SQLException {
        if (tgSpider.getChatId() == null) return;
        if (tgSpider.getLastSpiderId() == null && tgSpider.getChatName() == null) return;
        if (!exitsTgSpider(dataSource,tgSpider.getChatId())) {
            insertTgSpider(dataSource,tgSpider);
        }else {
            updateTgSpider(dataSource,tgSpider);
        }
    }

    private final static String DEL_BY_ID_TG_SPIDER_SQL="DELETE FROM tg_spider WHERE chat_id = ?";
    public final static  void   delTgSpiderById(DataSource dataSource,Long chat_id) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement statement = connection.prepareStatement(DEL_BY_ID_TG_SPIDER_SQL);
            statement.setLong(1,chat_id);
             statement.execute();
        }
    }


    /**
     * tg_group表
     */
    private final static String INSERT_TG_GROUP_SQL = "INSERT INTO tg_group VALUES (?,?,?)";
    public final static void insertTgGroup(DataSource dataSource, TgGroup group) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(INSERT_TG_GROUP_SQL);
            preparedStatement.setLong(1,group.getChatId());
            preparedStatement.setLong(2,group.getInviteUserId());
            preparedStatement.setString(3,group.getChatName());
            preparedStatement.execute();
        }
    }

    private final static  String SELECT_ALL_TG_GROUP_SQL="SELECT * FROM tg_group";
    public final static List<TgGroup>  selectTgGroupAll(DataSource dataSource) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement statement = connection.prepareStatement(SELECT_ALL_TG_GROUP_SQL);
            ResultSet resultSet =  statement.executeQuery();
            return TgGroup.getTgGroupList(resultSet);
        }
    }

    private final static  String SELECT_BY_CHAT_ID_TG_GROUP_SQL ="SELECT * FROM tg_group WHERE chat_id = ?";
    public final static List<TgGroup> selectTgGroupByChatId(DataSource dataSource,Long chat_id) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement statement = connection.prepareStatement(SELECT_BY_CHAT_ID_TG_GROUP_SQL);
            statement.setLong(1, chat_id);
            ResultSet resultSet =  statement.executeQuery();
            return TgGroup.getTgGroupList(resultSet);
        }
    }

    private final static  String SELECT_BY_USER_ID_TG_GROUP_SQL ="SELECT * FROM tg_group WHERE invite_user_id = ?";
    public final static List<TgGroup> selectTgGroupByUserId(DataSource dataSource,Long inviteUserId) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement statement = connection.prepareStatement(SELECT_BY_USER_ID_TG_GROUP_SQL);
            statement.setLong(1, inviteUserId);
            ResultSet resultSet =  statement.executeQuery();
            return TgGroup.getTgGroupList(resultSet);
        }
    }

    private final static  String EXITS_BY_CHAT_ID_TG_GROUP_SQL ="SELECT * FROM tg_group WHERE chat_id = ? limit 1";
    public final  static boolean exitsTgGroupByChatIdAndUserId(DataSource dataSource, Long chat_id) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement statement = connection.prepareStatement(EXITS_BY_CHAT_ID_TG_GROUP_SQL);
            statement.setLong(1, chat_id);
            ResultSet resultSet =  statement.executeQuery();
            return TgGroup.getTgGroupOne(resultSet) != null;
        }
    }

    private final static  String SELECT_BY_CAHT_AND_USER_ID__TG_GROUP_SQL ="SELECT * FROM tg_group WHERE chat_id = ? AND  invite_user_id = ?";
    public final static TgGroup selectTgGroupByChatIdAndUserId(DataSource dataSource,TgGroup tgGroup) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement statement = connection.prepareStatement(SELECT_BY_CAHT_AND_USER_ID__TG_GROUP_SQL);
            statement.setLong(1, tgGroup.getChatId());
            statement.setLong(2, tgGroup.getInviteUserId());
            ResultSet resultSet =  statement.executeQuery();
            return TgGroup.getTgGroupOne(resultSet);
        }
    }

    public final  static boolean exitsTgGroupByChatIdAndUserId(DataSource dataSource, TgGroup tgGroup) throws SQLException {
        return selectTgGroupByChatIdAndUserId(dataSource,tgGroup) != null;
    }

    private final static String DEL_BY_USER_AND_CHAT_TG_GROUP_SQL ="DELETE from tg_group where chat_id = ? and invite_user_id = ?";
    public final static void delTgGroupByChatIdAndUserId(DataSource dataSource, TgGroup tgGroup   ) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement statement = connection.prepareStatement(DEL_BY_USER_AND_CHAT_TG_GROUP_SQL);
            statement.setLong(1, tgGroup.getChatId());
            statement.setLong(2, tgGroup.getInviteUserId());
            statement.execute();
        }
    }

    private final static String DEL_BY_USER_TG_GROUP_SQL ="DELETE from tg_group where invite_user_id = ?";
    public final static void delAllTgGroupByUserId(DataSource dataSource,Long inviteUserId) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement statement = connection.prepareStatement(DEL_BY_USER_TG_GROUP_SQL);
            statement.setLong(1,inviteUserId);
            statement.execute();
        }
    }

    private final static String DEL_BY_CHAT_TG_GROUP_SQL ="DELETE from tg_group where chat_id = ?";
    public final static void delAllTgGroupByChatId(DataSource dataSource,Long chatId) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement statement = connection.prepareStatement(DEL_BY_CHAT_TG_GROUP_SQL);
            statement.setLong(1,chatId);
            statement.execute();
        }
    }


    private final static String UPDATE_TG_GROUP_SQL="UPDATE tg_group SET chat_name = ? WHERE chat_id = ? AND invite_user_id = ?";
    public  final static void   updateTgGroup(DataSource dataSource,TgGroup tgGroup) throws SQLException {
        if (tgGroup.getChatId() == null) return;
        if (tgGroup.getChatName() == null) return;
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(UPDATE_TG_GROUP_SQL);
            preparedStatement.setString(1, tgGroup.getChatName());
            preparedStatement.setLong(2,tgGroup.getChatId());
            preparedStatement.setLong(3,tgGroup.getInviteUserId());
            preparedStatement.execute();
        }
    }

    public final static void insertOrUpdateTgGroup(DataSource dataSource,TgGroup tgGroup) throws SQLException {
        if (tgGroup.getChatId() == null) return;
        if (tgGroup.getChatName() == null) return;
        if (tgGroup.getInviteUserId() == null) return;
        if (!exitsTgGroupByChatIdAndUserId(dataSource,tgGroup)) {
            insertTgGroup(dataSource,tgGroup);
        }else {
            updateTgGroup(dataSource,tgGroup);
        }
    }




    /**
     * tg_bot_info
     */
    private final static  String SELECT_ALL_TG_BOTINFO_SQL="SELECT * FROM tg_bot_info";
    public static List<TgBotInfo> selectTgBotInfoAll(DataSource dataSource) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement statement = connection.prepareStatement(SELECT_ALL_TG_BOTINFO_SQL);
            ResultSet resultSet =  statement.executeQuery();
            return TgBotInfo.getTgBotInfoList(resultSet);
        }
    }

    private final static  String INSERT_TG_BOTINFO_SQL="INSERT INTO tg_bot_info VALUES (?,?)";
    public static void insertTgBotInfo(DataSource dataSource,TgBotInfo tgBotInfo) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement statement = connection.prepareStatement(INSERT_TG_BOTINFO_SQL);
            statement.setString(1,tgBotInfo.getAttrName());
            statement.setString(2,tgBotInfo.getAttrValue());
            statement.execute();

        }
    }

    private final static  String SELECT_NAME_TG_BOTINFO_SQL="SELECT * FROM tg_bot_info WHERE attr_name  = ?";
    public static TgBotInfo selectTgBotInfoByName(DataSource dataSource,String name) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement statement = connection.prepareStatement(SELECT_NAME_TG_BOTINFO_SQL);
            statement.setString(1,name);
            ResultSet resultSet =  statement.executeQuery();
            return TgBotInfo.getTgBotInfoListOne(resultSet);
        }
    }

    public static boolean exitsTgBotInfoByName(DataSource dataSource,String name) throws SQLException {
        return  selectTgBotInfoByName(dataSource,name) != null;
    }

    public static void insertOrUpdateTgBotInfo(DataSource dataSource,TgBotInfo tgBotInfo) throws SQLException {
        if (tgBotInfo.getAttrValue() == null ||  tgBotInfo.getAttrValue() ==null) {
            return;
        }
        if (!exitsTgBotInfoByName(dataSource,tgBotInfo.getAttrName())){
            insertTgBotInfo(dataSource,tgBotInfo);
        }else {
            updateTgBotInfo(dataSource,tgBotInfo);
        }
    }

    private final static String UPDATE_TG_BOTINFO_SQL= "UPDATE tg_bot_info SET attr_value = ? WHERE attr_name = ?";
    public   static void updateTgBotInfo(DataSource dataSource,TgBotInfo tgBotInfo) throws SQLException {
        if (tgBotInfo.getAttrName() == null || tgBotInfo.getAttrValue() == null) return;
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(UPDATE_TG_BOTINFO_SQL);
            preparedStatement.setString(1, tgBotInfo.getAttrValue());
            preparedStatement.setString(2, tgBotInfo.getAttrName());
            preparedStatement.execute();
        }
    }





}
