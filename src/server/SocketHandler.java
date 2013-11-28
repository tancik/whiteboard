package server;

import java.io.EOFException;
import java.io.IOException;
import java.io.Serializable;
import java.net.SocketException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import javax.imageio.ImageIO;

import name.Identifiable;
import name.Identifier;
import packet.Packet;
import packet.PacketHandler;

public abstract class SocketHandler extends PacketHandler implements Runnable, Identifiable, Serializable {
	private static final long serialVersionUID = -2411978741777099054L;

	private transient final Socket socket;

    protected transient final ObjectOutputStream out;
    protected transient final ObjectInputStream in;
    
    protected Identifier identifier;
    
    protected SocketHandler(Socket socket) throws IOException {
        this.socket = socket;
        
        out = new ObjectOutputStream(socket.getOutputStream());
        in = new ObjectInputStream(socket.getInputStream());
    }
    
    @Override
    public final void run() {
        try {
            handleConnection();
        } catch (IOException e) {
            e.printStackTrace(); // but don't terminate the serve()
        } finally {
            // close the socket at the end
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    protected final void handleConnection() throws IOException {
        try {
            for (Object obj = in.readObject(); obj != null; obj = in.readObject()) {
                receivedPacket((Packet)obj);
            }
        } 
        catch (Exception e) {
            connectionClosed();
        }
        finally {
            connectionClosed();
            out.close();
            in.close();
        }   
    }

    protected void connectionClosed() { }

    public void sendPacket(Packet packet) {
        try {
            out.writeObject(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    @Override
    public Identifier identifier() {
        return identifier;
    }
}
