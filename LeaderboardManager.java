import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class LeaderboardManager {

    private Connection getConnection() throws SQLException {
        return DBConnection.getConnection();
    }

    public int createGame(String mode, int startLevel) {
        int gameId = -1;

        String sql = "INSERT INTO game_stats (mode, score, level) VALUES (?, ?, ?)";

        try (Connection con = getConnection();
                PreparedStatement pst = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pst.setString(1, mode); 
            pst.setInt(2, 0);
            pst.setInt(3, startLevel);

            pst.executeUpdate();

            try (ResultSet rs = pst.getGeneratedKeys()) {
                if (rs.next())
                    gameId = rs.getInt(1);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return gameId;
    }

    public void updateGame(int gameId, int score, int level) {
        if (gameId <= 0)
            return;

        String sql = "UPDATE game_stats SET score = ?, level = ? WHERE id = ?";

        try (Connection con = getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, score);
            ps.setInt(2, level);
            ps.setInt(3, gameId);
            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public int getMaxLevel(String mode) {
        int maxLevel = 1;

        String sql = "SELECT COALESCE(MAX(level), 1) AS max_level " +
                "FROM game_stats WHERE mode = ? AND score > 0";

        try (Connection con = getConnection();
                PreparedStatement pst = con.prepareStatement(sql)) {

            pst.setString(1, mode);

            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next())
                    maxLevel = rs.getInt("max_level");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return maxLevel;
    }

    public MyMap<Integer, Integer> getLeaderboard() {
        MyMap<Integer, Integer> leaderboard = new MyMap<>();

        String sql = "SELECT id, score FROM game_stats " +
                "WHERE score > 0 ORDER BY score DESC, updated_at DESC LIMIT 10";

        try (Connection con = getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("id");
                int score = rs.getInt("score");
                leaderboard.putSortedDescending(id, score);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return leaderboard;
    }
}
