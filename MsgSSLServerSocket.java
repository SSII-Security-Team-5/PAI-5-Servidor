import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;

public class MsgSSLServerSocket {

	/**
	 * @param args
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws NoSuchAlgorithmException
	 * 
	 */

	public static void main(String[] args) throws IOException {

		// SE GENERA EL SOCKET DEL SERVIDOR
		ServerSocket socketServidor = new ServerSocket(7070);

		while (true) {
			try{
				Socket socket = socketServidor.accept();
				BufferedReader bufferEntrada = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				Thread t = new HiloMultiCliente(socket, bufferEntrada);
				t.start();
			} catch (Exception e) {
				socketServidor.close();
				System.out.println(e);
			}
		}
	}
}

class HiloMultiCliente extends Thread {
	final BufferedReader bufferEntrada;
	final Socket socket;

	public HiloMultiCliente(Socket socket, BufferedReader bufferEntrada) {
		this.socket = socket;
		this.bufferEntrada = bufferEntrada;
	}

	@Override
	public void run() {
		try {
			System.out.println(bufferEntrada.readLine());
		} catch (Exception e) {
			System.out.println("Error");
		}

		while (true) {
			break;
		}
	}
}