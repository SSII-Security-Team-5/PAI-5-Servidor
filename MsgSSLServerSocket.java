import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;

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

    public ServerThread(int port) throws IOException {
        serverSocket = new ServerSocket(port);
    }

    @Override
    public void run() {
        try {
            System.out.println("Waiting for client on port " + serverSocket.getLocalPort() + "...");
            clientSocket = serverSocket.accept();
            System.out.println("Just connected to " + clientSocket.getRemoteSocketAddress());

            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);

            String clientMsg = in.readLine();
            String[] clientMsgSplits = clientMsg.split("@");

            System.out.println(clientMsgSplits[0]);
            System.out.println(clientMsgSplits[1]);
            System.out.println(clientMsgSplits[2]);

            clientMsg = clientMsgSplits[0];
            String clientFirm = clientMsgSplits[1];
            String clientPublicKey = clientMsgSplits[2];

            Signature sig = Signature.getInstance("SHA256withRSA");

            byte[] publicBytes = Base64.getDecoder().decode((clientPublicKey));
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PublicKey pubKey = keyFactory.generatePublic(keySpec);

            sig.initVerify(pubKey);
            sig.update(clientMsg.getBytes());
            
            if(sig.verify(clientFirm.getBytes())) {
                System.out.println("Firma verificada correctamente");
            }else {
                System.out.println("Se ha dectectado un ataque");
            }

            // Do something with the clientSocket
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (SignatureException e) {
            e.printStackTrace();
        } finally {
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}