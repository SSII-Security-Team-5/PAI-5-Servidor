import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Base64;
import java.util.Base64.Decoder;

public class h {
    
    public static void main(String[] args) throws SQLException, NoSuchAlgorithmException, InvalidKeySpecException {
        String jdbcURL = "jdbc:h2:~/test";
        String username = "sa";
        String password = "1234";
 
        Connection connection = DriverManager.getConnection(jdbcURL, username, password);
        System.out.println("Connected to H2 embedded database.");
 
        String sql = "SELECT * FROM PUBLICKEYS";
 
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery(sql);
 
        int count = 0;
 
        while (resultSet.next()) {
            count++;
 
            int ID = resultSet.getInt("ID");
            String name = resultSet.getString("name");
            String publickey = resultSet.getString("pkey");

             
            //byte[] publicBytes = publickey.getBytes();
            byte[] publicBytes = Base64.getDecoder().decode(publickey);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PublicKey pubKey = keyFactory.generatePublic(keySpec);

            System.out.println(pubKey);

            System.out.println("PUBLICKEYS #" + count + ": " + ID + ", " + name);
        }
 
        connection.close();
    }

}
