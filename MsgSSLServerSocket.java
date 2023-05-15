import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Base64;
import java.util.Calendar;
import java.security.InvalidKeyException;
import java.security.KeyFactory;

public class MsgSSLServerSocket {

    /**
     * @param args
     * @throws IOException
     * @throws InterruptedException
     * @throws NoSuchAlgorithmException
     * 
     */

    public static void main(String[] args) throws IOException {

        try {
            Thread t = new ServerThread(7070);
            t.start();
        } catch (Exception e) {
            System.out.println(e);
        }

    }
}

class ServerThread extends Thread {

    private ServerSocket serverSocket;
    private Socket clientSocket;

    String jdbcURL = "jdbc:h2:~/test";
    String username = "sa";
    String password = "1234";

    public ServerThread(int port) throws IOException {
        serverSocket = new ServerSocket(port);
    }

    @Override
    public void run() {
        try {
            System.out.println("Waiting for client on port " + serverSocket.getLocalPort() + "...");
            clientSocket = serverSocket.accept();
            System.out.println("Just connected to " + clientSocket.getRemoteSocketAddress());

            // SQL
            String url = "jdbc:sqlite:mydatabase.db";

            String clientPublicKey = "";

            try {
                // Se inicia una conexion a la DDBB para obtener la clave publica del cliente
                Connection connection = DriverManager.getConnection(url);
                Statement statement = connection.createStatement();
                String query = "SELECT * FROM publickeys";
                ResultSet resultSet = statement.executeQuery(query);
                 
                if (resultSet.next()) {
                   clientPublicKey = resultSet.getString("pkey");
                }
                
                resultSet.close();
                statement.close();
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            // Se recibe del servidor el usuario, el pedido y la firma del pedido
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            System.out.println("Esperando recibir los datos");
            String clientMsg = in.readLine();
            String[] clientMsgSplits = clientMsg.split("@");

            // Se dividen los datos recibidos y se validan
            String clientUsername = clientMsgSplits[0];
            clientMsg = clientMsgSplits[1];
            String clientFirm = clientMsgSplits[2];

            if (!clientUsername.matches("^[a-zA-Z0-9 ]+$")) {
                System.out.println("El usuario recibido no es correcto");
                return;
            }

            String[] materialValues = clientMsg.split(" ");
            boolean inputValidationMaterial = false;

            for (int i = 0; i < materialValues.length; i++) {
                if (Integer.parseInt(materialValues[i].split(":")[1]) > 300) {
                    inputValidationMaterial = true;
                }
            }

            if (inputValidationMaterial) {
                System.out.println("Las cantidades recibidas no son correctas");
                return;
            }

            byte[] firma = Base64.getDecoder().decode(clientFirm);

            // Generación de clave publica a partir de la recibida de la BBDD
            Signature sig = Signature.getInstance("SHA256withRSA");

            byte[] publicBytes = Base64.getDecoder().decode((clientPublicKey));
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PublicKey pubKey = keyFactory.generatePublic(keySpec);
            
            // Verificación de la firma y escritura en el fichero de log el resultado de esta
            sig.initVerify(pubKey);
            sig.update(clientMsg.getBytes());

            BufferedWriter writer = new BufferedWriter(new FileWriter("log.txt", true));
            Calendar dateNow = Calendar.getInstance();

            if (sig.verify(firma)) {
                System.out.println("Firma verificada correctamente");
                writer.write(dateNow.get(Calendar.YEAR) + "," + dateNow.get(Calendar.MONTH) + ",true" + "\n");
            } else {
                System.out.println("Se ha dectectado un ataque");
                writer.write(dateNow.get(Calendar.YEAR) + "," + dateNow.get(Calendar.MONTH) + ",false" + "\n");
            }

            writer.close();

        } catch (IOException | NoSuchAlgorithmException | InvalidKeyException | InvalidKeySpecException | SignatureException e) {
            e.printStackTrace();
        }  finally {
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}