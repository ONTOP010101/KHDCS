import java.sql.*;

public class CreateIndex {
    public static void main(String[] args) throws Exception {
        String url = "jdbc:mysql://localhost:3306/photo_management?useUnicode=true&characterEncoding=UTF-8";
        String user = "root";
        String pass = "123456";

        try (Connection conn = DriverManager.getConnection(url, user, pass);
             Statement stmt = conn.createStatement()) {

            // Check existing FULLTEXT indexes
            ResultSet rs = stmt.executeQuery("SHOW INDEX FROM samples WHERE Index_type='FULLTEXT'");
            boolean hasIndex = false;
            while (rs.next()) {
                hasIndex = true;
                System.out.println("Existing FULLTEXT: " + rs.getString("Key_name") + " columns: " + rs.getString("Column_name"));
            }
            rs.close();

            if (!hasIndex) {
                System.out.println("No FULLTEXT index found. Creating...");
                try {
                    stmt.execute("ALTER TABLE samples ADD FULLTEXT INDEX ft_ngram (sample_name, sample_code, factory_code, manufacturer_code) WITH PARSER ngram");
                    System.out.println("Index ft_ngram created successfully.");
                } catch (SQLException e) {
                    System.out.println("Failed to create ft_ngram: " + e.getMessage());
                    // Try without ngram parser (fallback)
                    try {
                        stmt.execute("ALTER TABLE samples ADD FULLTEXT INDEX ft_basic (sample_name, sample_code, factory_code, manufacturer_code)");
                        System.out.println("Index ft_basic created (no ngram parser).");
                    } catch (SQLException e2) {
                        System.out.println("Failed to create ft_basic: " + e2.getMessage());
                    }
                }
            }

            // Also ensure sample_name has separate FULLTEXT index for single-column MATCH
            try {
                stmt.execute("ALTER TABLE samples ADD FULLTEXT INDEX ft_sample_name (sample_name) WITH PARSER ngram");
                System.out.println("Index ft_sample_name created successfully.");
            } catch (SQLException e) {
                System.out.println("ft_sample_name already exists or failed: " + e.getMessage());
            }

            System.out.println("Done.");
        }
    }
}
