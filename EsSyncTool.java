import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.*;

public class EsSyncTool {
    public static void main(String[] args) throws Exception {
        String jdbcUrl = "jdbc:mysql://localhost:3306/photo_management?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true&useSSL=false";
        String user = "root";
        String password = "123456";
        String outPath = "D:/es-sync/es-bulk-all.ndjson";

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        Class.forName("com.mysql.cj.jdbc.Driver");

        try (Connection conn = DriverManager.getConnection(jdbcUrl, user, password);
             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                 new FileOutputStream(outPath), StandardCharsets.UTF_8))) {

            long total;
            try (Statement s = conn.createStatement();
                 ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM samples WHERE deleted=0")) {
                rs.next();
                total = rs.getLong(1);
            }
            System.out.println("Total samples: " + total);

            int batchSize = 1000;
            long offset = 0;
            long written = 0;

            while (offset < total) {
                String sql = "SELECT s.id, s.sample_code, s.manufacturer_code, s.sample_name, s.english_name, " +
                    "s.category, s.category_code, s.factory_code, s.supplier, s.booth_no, s.contact_person, " +
                    "s.contact_phone, s.mobile, s.sms, s.factory_price, s.sample_length, s.sample_width, s.sample_height, " +
                    "s.packaging_cn, s.package_code, s.certification, s.infringement, s.battery_info, s.hide_from_xzx, " +
                    "s.carton_capacity, s.inner_box_count, s.package_length, s.package_width, s.package_height, " +
                    "s.carton_length, s.carton_width, s.carton_height, s.registrant, s.modifier, " +
                    "s.create_time, s.update_time, s.remark, s.deleted, t.image_id AS first_image_id " +
                    "FROM samples s LEFT JOIN sample_thumbnail t ON s.id = t.sample_id " +
                    "WHERE s.deleted=0 ORDER BY s.id LIMIT " + batchSize + " OFFSET " + offset;

                int batchCount = 0;
                try (Statement s = conn.createStatement();
                     ResultSet rs = s.executeQuery(sql)) {
                    while (rs.next()) {
                        Map<String, Object> doc = new LinkedHashMap<>();
                        doc.put("id", rs.getLong("id"));
                        putIfNotNull(doc, "sampleCode", rs.getString("sample_code"));
                        putIfNotNull(doc, "manufacturerCode", rs.getString("manufacturer_code"));
                        putIfNotNull(doc, "sampleName", rs.getString("sample_name"));
                        putIfNotNull(doc, "englishName", rs.getString("english_name"));
                        putIfNotNull(doc, "category", rs.getString("category"));
                        putIfNotNull(doc, "categoryCode", rs.getString("category_code"));
                        putIfNotNull(doc, "factoryCode", rs.getString("factory_code"));
                        putIfNotNull(doc, "supplier", rs.getString("supplier"));
                        putIfNotNull(doc, "boothNo", rs.getString("booth_no"));
                        putIfNotNull(doc, "contactPerson", rs.getString("contact_person"));
                        putIfNotNull(doc, "contactPhone", rs.getString("contact_phone"));
                        putIfNotNull(doc, "mobile", rs.getString("mobile"));
                        putIfNotNull(doc, "sms", rs.getString("sms"));
                        putIfNotNull(doc, "factoryPrice", rs.getBigDecimal("factory_price"));
                        putIfNotNull(doc, "sampleLength", rs.getBigDecimal("sample_length"));
                        putIfNotNull(doc, "sampleWidth", rs.getBigDecimal("sample_width"));
                        putIfNotNull(doc, "sampleHeight", rs.getBigDecimal("sample_height"));
                        putIfNotNull(doc, "packagingCn", rs.getString("packaging_cn"));
                        putIfNotNull(doc, "packageCode", rs.getString("package_code"));
                        putIfNotNull(doc, "certification", rs.getString("certification"));
                        putIfNotNull(doc, "infringement", rs.getString("infringement"));
                        putIfNotNull(doc, "batteryInfo", rs.getString("battery_info"));
                        putIfNotNull(doc, "hideFromXzx", rs.getString("hide_from_xzx"));
                        putIfNotNull(doc, "cartonCapacity", rs.getObject("carton_capacity"));
                        putIfNotNull(doc, "innerBoxCount", rs.getObject("inner_box_count"));
                        putIfNotNull(doc, "packageLength", rs.getBigDecimal("package_length"));
                        putIfNotNull(doc, "packageWidth", rs.getBigDecimal("package_width"));
                        putIfNotNull(doc, "packageHeight", rs.getBigDecimal("package_height"));
                        putIfNotNull(doc, "cartonLength", rs.getBigDecimal("carton_length"));
                        putIfNotNull(doc, "cartonWidth", rs.getBigDecimal("carton_width"));
                        putIfNotNull(doc, "cartonHeight", rs.getBigDecimal("carton_height"));
                        putIfNotNull(doc, "registrant", rs.getString("registrant"));
                        putIfNotNull(doc, "modifier", rs.getString("modifier"));
                        if (rs.getTimestamp("create_time") != null) {
                            String ts = rs.getTimestamp("create_time").toString();
                            doc.put("createTime", ts.replace(" ", "T"));
                        }
                        if (rs.getTimestamp("update_time") != null) {
                            String ts = rs.getTimestamp("update_time").toString();
                            doc.put("updateTime", ts.replace(" ", "T"));
                        }
                        putIfNotNull(doc, "remark", rs.getString("remark"));
                        doc.put("deleted", rs.getInt("deleted"));
                        Object fid = rs.getObject("first_image_id");
                        if (fid != null) doc.put("firstImageId", fid);

                        writer.write("{\"index\":{\"_id\":" + rs.getLong("id") + "}}");
                        writer.newLine();
                        writer.write(mapper.writeValueAsString(doc));
                        writer.newLine();
                        batchCount++;
                    }
                }

                written += batchCount;
                System.out.println("Written: " + batchCount + ", total=" + written + "/" + total);
                offset += batchSize;
            }

            System.out.println("File written: " + outPath + ", records=" + written);
        }
    }

    private static void putIfNotNull(Map<String, Object> map, String key, Object value) {
        if (value != null) {
            map.put(key, value);
        }
    }
}
