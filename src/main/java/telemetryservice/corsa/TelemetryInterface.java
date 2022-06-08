package telemetryservice.corsa;

import telemetryservice.StructWriter;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.PortUnreachableException;
import java.net.SocketException;

public class TelemetryInterface {
    DatagramSocket clientSocket;
    public static final int CORSA_PORT = 9996;
    private Status status = Status.init;
    InetAddress IPAddress;
    RTCarInfo telemetry;

    public void connect() {
        new Thread(new Runnable() {
            public void run() {
                try {
                    startHandshake();
                } catch (IOException e) {
                    System.err.println("Failed to connect to assetto corsa");
                }
            }
        }).start();

    }

    private void startHandshake() throws IOException {
        IPAddress = InetAddress.getByName("localhost");

        clientSocket = new DatagramSocket(9999);
        clientSocket.connect(IPAddress, CORSA_PORT);

        byte[] receiveData = new byte[1024];

        byte[] handShake = getHandshake();
        clientSocket.send(new DatagramPacket(handShake, handShake.length));

        DatagramPacket receivePacket = new DatagramPacket(receiveData, HandshakeResponse.SIZE);
        try{
            clientSocket.receive(receivePacket);
        } catch(PortUnreachableException e){
            throw new IOException();
        }


        HandshakeResponse handshakeResponse = new HandshakeResponse(receiveData);
        System.out.println("Handshake: " + handshakeResponse);

        status = Status.handshake;

        byte[] subscribe = getSubscribeUpdates();
        clientSocket.send(new DatagramPacket(subscribe, subscribe.length));

        System.out.println("Subscribed");
        status = Status.subscribed;

        while (Status.subscribed.equals(status) && !clientSocket.isClosed()) {
            try {
                //System.out.println("Receiving");

                DatagramPacket receivedLapData = new DatagramPacket(receiveData, 328);
                clientSocket.receive(receivedLapData);
                telemetry = new RTCarInfo(receiveData);

                //System.out.println("RECEIVED: " + Arrays.toString(receiveData));
                //System.out.println(telemetry);
            } catch (SocketException e){
                System.err.println("Socket closed");
            }
        }

        byte[] dismiss = getDismiss();
        try {
            clientSocket.send(new DatagramPacket(dismiss, dismiss.length, IPAddress, CORSA_PORT));

        } catch (Exception e) {
            e.printStackTrace();
        }

        clientSocket.close();

        System.out.println("Bye");

    }

    public RTCarInfo getTelemetry() {
        return telemetry;
    }

    private byte[] getHandshake() throws IOException {
        StructWriter structWriter = new StructWriter(12);
        structWriter.writeInt(1);
        structWriter.writeInt(1);
        structWriter.writeInt(OperationId.HANDSHAKE);
        return structWriter.toByteArray();
    }

    private byte[] getSubscribeUpdates() throws IOException {
        StructWriter structWriter = new StructWriter(12);
        structWriter.writeInt(1);
        structWriter.writeInt(1);
        structWriter.writeInt(OperationId.SUBSCRIBE_UPDATE);
        return structWriter.toByteArray();
    }

    private byte[] getSubscribeSpot() throws IOException {
        StructWriter structWriter = new StructWriter(12);
        structWriter.writeInt(1);
        structWriter.writeInt(1);
        structWriter.writeInt(OperationId.SUBSCRIBE_SPOT);
        return structWriter.toByteArray();
    }

    private byte[] getDismiss() throws IOException {
        StructWriter structWriter = new StructWriter(12);
        structWriter.writeInt(1);
        structWriter.writeInt(1);
        structWriter.writeInt(OperationId.DISMISS);
        return structWriter.toByteArray();
    }

    public void stop() {
        System.out.println("Dismissing");

        status = Status.dismissed;

    }
}
