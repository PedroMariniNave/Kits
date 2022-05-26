package com.zpedroo.kits.mysql;

import com.zpedroo.kits.managers.DataManager;
import com.zpedroo.kits.objects.Kit;
import com.zpedroo.kits.objects.PlayerData;
import org.bukkit.entity.Player;

import java.sql.*;
import java.util.*;

public class DBManager extends DataManager {

    public void saveData(PlayerData data) {
        executeUpdate("REPLACE INTO `" + DBConnection.TABLE + "` (`uuid`, `cooldowns`) VALUES " +
                "('" + data.getUUID().toString() + "', " +
                "'" + serializeCooldowns(data.getCooldowns()) + "');");
    }

    public PlayerData loadData(Player player) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet result = null;
        String query = "SELECT * FROM `" + DBConnection.TABLE + "` WHERE `uuid`='" + player.getUniqueId().toString() + "';";

        try {
            connection = getConnection();
            preparedStatement = connection.prepareStatement(query);
            result = preparedStatement.executeQuery();

            if (result.next()) {
                UUID uuid = UUID.fromString(result.getString(1));
                Map<Kit, Long> cooldowns = deserializeCooldowns(result.getString(2));

                return new PlayerData(uuid, cooldowns);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            closeConnection(connection, result, preparedStatement, null);
        }

        return new PlayerData(player.getUniqueId(), new HashMap<>());
    }

    private String serializeCooldowns(Map<Kit, Long> cooldowns) {
        StringBuilder builder = new StringBuilder(cooldowns.size()*2);

        for (Map.Entry<Kit, Long> entry : cooldowns.entrySet()) {
            long expiration = entry.getValue();
            if (System.currentTimeMillis() >= expiration) continue;

            Kit kit = entry.getKey();
            if (kit == null) continue;

            builder.append(kit.getName()).append(",");
            builder.append(expiration).append("#");
        }

        return builder.toString();
    }

    private Map<Kit, Long> deserializeCooldowns(String serialized) {
        Map<Kit, Long> ret = new HashMap<>(4);

        String[] split = serialized.split("#");

        for (String str : split) {
            String[] infoSplit = str.split(",");

            String kitName = infoSplit[0];
            Kit kit = getCache().getKits().get(kitName);
            if (kit == null) continue;

            long expiration = Long.parseLong(infoSplit[1]);
            if (System.currentTimeMillis() >= expiration) continue;

            ret.put(kit, expiration);
        }

        return ret;
    }

    private void executeUpdate(String query) {
        Connection connection = null;
        Statement statement = null;
        try {
            connection = getConnection();
            statement = connection.createStatement();
            statement.executeUpdate(query);
        } catch (SQLException ex) {
            ex.printStackTrace();
        } finally {
            closeConnection(connection, null, null, statement);
        }
    }

    private void closeConnection(Connection connection, ResultSet resultSet, PreparedStatement preparedStatement, Statement statement) {
        try {
            if (connection != null) connection.close();
            if (resultSet != null) resultSet.close();
            if (preparedStatement != null) preparedStatement.close();
            if (statement != null) statement.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    protected void createTable() {
        String query = "CREATE TABLE IF NOT EXISTS `" + DBConnection.TABLE + "` (`uuid` VARCHAR(255), `cooldowns` LONGTEXT, PRIMARY KEY(`uuid`));";
        executeUpdate(query);
    }

    private Connection getConnection() throws SQLException {
        return DBConnection.getInstance().getConnection();
    }
}