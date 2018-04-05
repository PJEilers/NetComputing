//package nc;
//
//import com.rabbitmq.client.*;
//import static Backend.Database.SENSORS;
//
//import java.io.ByteArrayInputStream;
//import java.io.IOException;
//import java.io.ObjectInput;
//import java.io.ObjectInputStream;
//import java.util.concurrent.TimeoutException;
//
//import java.io.IOException;
//import java.net.DatagramPacket;
//import java.net.DatagramSocket;
//import java.net.InetAddress;
//import java.net.SocketException;
//import java.net.UnknownHostException;
//
//
//public class Bu extends Thread {
//    private Sensor[][] sens;
//    private final static String QUEUE_NAME_ONE = "backups";
//
//
//    public Sensor[] getBackup(int index){
//        return sens[index];
//    }
//    public Sensor[][] getWholeBackup(){
//        return sens;
//    }
//    //Backup server, uses rabbit mq to periodically receive backup data, also RAID as it sends it to other servers via UDP
//    public void run() {
//        sens = SENSORS;
//        try {
//            ConnectionFactory factory = new ConnectionFactory();
//            factory.setHost("localhost");
//            Connection connection = factory.newConnection();
//            Channel channel = connection.createChannel();
//
//            channel.queueDeclare(QUEUE_NAME_ONE, false, false, false, null);
//            Consumer consumer = new DefaultConsumer(channel) {
//                @Override
//                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body)
//                        throws IOException {
//                    ByteArrayInputStream bis = new ByteArrayInputStream(body);
//                    ObjectInput in = null;
//                    DatagramSocket aSocket = null;
//                    try {
//                        in = new ObjectInputStream(bis);
//                        Sensor[] o = (Sensor[]) in.readObject();
//                        int check = o[0].getPort();
//                        int i=0;
//                        int backupArea=6999;
//                        //Find port of backup sent, and send it to server keeping backup of that server
//                        for (i = 0; i < sens.length; i++) {
//                            if(sens[i][0].getPort()==check){
//                                sens[i]=o;
//                                backupArea=sens[(i+1)%sens.length][0].getPort();
//                                break;
//                            }
//                        }
//
//                        aSocket = new DatagramSocket();
//                        InetAddress IPAddress = InetAddress.getByName("localhost");
//                        DatagramPacket request = new DatagramPacket(body, body.length, IPAddress,
//                                backupArea);
//                        aSocket.send(request);
//
//                    } catch (ClassNotFoundException e) {
//                        e.printStackTrace();
//                    } finally {
//                        try {
//                            if (in != null) {
//                                in.close();
//                            }
//                        } catch (IOException ex) {
//                            // ignore close exception
//                        }
//                    }
//                }
//            };
//            channel.basicConsume(QUEUE_NAME_ONE, true, consumer);
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (TimeoutException e) {
//            e.printStackTrace();
//        }
//
//    }
//}