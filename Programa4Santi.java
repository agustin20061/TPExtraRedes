import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Properties;

public class Programa4Santi {

        public Programa4Santi() {
        }

        public void startProgram() throws IOException, NombreExc {
            Properties prop = new Properties();
            String fileName = "app.config";  // nombre del archivo de configuracion
            try (FileInputStream fis = new FileInputStream(fileName)) {
                prop.load(fis);
                System.out.println("El archivo de configuracion existe");
            }
            catch (FileNotFoundException ex) {
                System.out.println("El archivo de configuracion no existe");
            }
            catch (IOException ex) {
                System.out.println("Error de acceso en el archivo de configuracion");
            }
            BufferedReader lectorConsola = new BufferedReader(new InputStreamReader(System.in));
            String nombreIngresado;
            System.out.println("ingresa tu nombre: ");
            nombreIngresado=lectorConsola.readLine();
            HashSet<String> listaNombres=new HashSet<>();
            String nombresAgregados= prop.getProperty("Nombre");
            String[] separacion=nombresAgregados.split("<>");


            for (int i=0;i<= separacion.length-1;i++){
                String nombrePerArchivo=separacion[i];

                listaNombres.add(nombrePerArchivo);//agrego los nombres del app.config
            }
            Boolean verofal=true;
            for (String nom:listaNombres) {

                if (nombreIngresado.equals(nom)){//verifico si esta el nombre
                    System.out.println("el nombre esta en el archivo de configuracion");
                    verofal=true;
                    break;
                }else {
                    verofal=false;
                }
            }
            if (!verofal){
                throw new NombreExc("el nombre no esta en el archivo de configuracion");
            }
            DatagramSocket socket=new DatagramSocket(saberPuerto(nombreIngresado,prop));
            // Hilo para escuchar mensajes
            Thread hiloReceptor = new Thread(() -> {
                while (true) {
                    try {
                        leerMensaje(nombreIngresado,prop,socket);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            });

            // Hilo para enviar mensajes
            Thread hiloEmisor = new Thread(() -> {
                try {
                    while (true) {
                        mandarMensaje(prop,socket,listaNombres,nombreIngresado);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (NombreExc e) {
                    throw new RuntimeException(e);
                }
            });

            hiloReceptor.start();
            hiloEmisor.start();

            //leerMensaje(nombreIngresado,prop,socket);

            //mandarMensaje(mensaje,prop,nombreIngresado,socket);
        }
    public int saberPuerto(String nombreIngresado,Properties prop){
        //esta dividido los nombres en posiciones sabemos en la posicion que esta el nombre a mandar el msj
        String topologia = prop.getProperty("Topologia");
        String[] aTopo = topologia.split("<->");
        int posicionNombreIngresado=0;
        if (nombreIngresado.equals("Felipe")){
            posicionNombreIngresado=1;
            System.out.println(posicionNombreIngresado);
        }else {
            posicionNombreIngresado= Arrays.binarySearch(aTopo, nombreIngresado);
            System.out.println(posicionNombreIngresado);//posicion del nombre ingresado
        }
        String contacto = prop.getProperty(aTopo[posicionNombreIngresado]);
        int puertoNombreIngresado= Integer.parseInt(contacto.split(":")[1]);
        return puertoNombreIngresado;
    }
    public void mandarMensaje(Properties prop,DatagramSocket socket,HashSet<String>listaNombres,String nombreIngresado) throws IOException, NombreExc {
        BufferedReader lectorConsola = new BufferedReader(new InputStreamReader(System.in));

        System.out.println("escriba un mensaje: ");//escribirlo asi: @nombre mensaje
        String mensaje=lectorConsola.readLine();

        String[] partes = mensaje.split(" ", 2);
        String nombreClienteRecibio = partes[0].substring(1);
        String mensajePrivado = partes[1];

        Boolean verofal1=true;
        for (String nom:listaNombres) {

            if (nombreClienteRecibio.equals(nom)){//verifico si esta el nombre
                System.out.println("el nombre esta en el archivo de configuracion");
                verofal1=true;
                break;
            }else {
                verofal1=false;
            }
        }
        if (!verofal1){
            throw new NombreExc("el nombre no esta en el archivo de configuracion");
        }




        //esta dividido los nombres en posiciones sabemos en la posicion que esta el nombre a mandar el msj
        String topologia = prop.getProperty("Topologia");
        String[] aTopo = topologia.split("<->");


        int posicionNombreRecibido=0;
        if (nombreClienteRecibio.equals("Felipe")){
            posicionNombreRecibido=1;
            System.out.println(posicionNombreRecibido);
        }else {
            posicionNombreRecibido= Arrays.binarySearch(aTopo, nombreClienteRecibio);
            System.out.println(posicionNombreRecibido);//posicion del nombre ingresado
        }
        //int posicionNombreRecibido = Arrays.binarySearch(aTopo, nombreClienteRecibio);
        //System.out.println(posicionNombreRecibido);//posicion del nombre a mandar el msj

        int posicionNombreIngresado=0;
        if (nombreIngresado.equals("Felipe")){
            posicionNombreIngresado=1;
            System.out.println(posicionNombreIngresado);
        }else {
            posicionNombreIngresado= Arrays.binarySearch(aTopo, nombreIngresado);
            System.out.println(posicionNombreIngresado);//posicion del nombre ingresado
        }

        //int posicionNombreIngresado= Arrays.binarySearch(aTopo, nombreIngresado);
        //System.out.println(posicionNombreIngresado);//posicion del nombre ingresado

        int difPosiciones=posicionNombreRecibido-posicionNombreIngresado;
        System.out.println(difPosiciones);

        if (difPosiciones==1||difPosiciones==-1){
            // Construir mensaje con el formato "origen:destino:mensaje"
            String mensajeCompleto = nombreIngresado + ":" + nombreClienteRecibio+":"+mensajePrivado;
            byte[] buffer = mensajeCompleto.getBytes();
            String contacto = prop.getProperty(aTopo[posicionNombreRecibido]);
            String ipDestino=contacto.split(":")[0];
            int puertoDestino= Integer.parseInt(contacto.split(":")[1]);
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, InetAddress.getByName(ipDestino), puertoDestino);
            socket.send(packet);
        } else if (difPosiciones>1) {
            // Construir mensaje con el formato "origen:destino:mensaje"
            String mensajeCompleto = nombreIngresado + ":" + nombreClienteRecibio+":"+mensajePrivado;
            byte[] buffer = mensajeCompleto.getBytes();
            String contacto = prop.getProperty(aTopo[posicionNombreIngresado+1]);
            String ipDestino=contacto.split(":")[0];
            int puertoDestino= Integer.parseInt(contacto.split(":")[1]);
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, InetAddress.getByName(ipDestino), puertoDestino);
            socket.send(packet);
        } else if (difPosiciones<-1) {
            // Construir mensaje con el formato "origen:destino:mensaje"
            String mensajeCompleto = nombreIngresado + ":" + nombreClienteRecibio+":"+mensajePrivado;
            byte[] buffer = mensajeCompleto.getBytes();
            String contacto = prop.getProperty(aTopo[posicionNombreIngresado-1]);
            String ipDestino=contacto.split(":")[0];
            int puertoDestino= Integer.parseInt(contacto.split(":")[1]);
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, InetAddress.getByName(ipDestino), puertoDestino);
            socket.send(packet);
        }
    }
    public void leerMensaje(String nombreIngresado,Properties prop,DatagramSocket socket) throws IOException {
        //try {
        //DatagramSocket serverSocket = new DatagramSocket(5000);

        //  while (true) {
        byte[] receiveData = new byte[1024];
        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
        socket.receive(receivePacket);
        System.out.println("recivido");
        String mensajeRecibido = new String(receivePacket.getData(), 0, receivePacket.getLength());
        String nombreOrigen=mensajeRecibido.split(":")[0].trim();
        String nombreDestino= mensajeRecibido.split(":")[1].trim();
        String mensaje=mensajeRecibido.split(":")[2].trim();

        //esta dividido los nombres en posiciones sabemos en la posicion que esta el nombre a mandar el msj
        String topologia = prop.getProperty("Topologia");
        String[] aTopo = topologia.split("<->");
        int posicionNombreRecibido=0;
        if (nombreDestino.equals("Felipe")){
            posicionNombreRecibido=1;
            System.out.println(posicionNombreRecibido);
        }else {
            posicionNombreRecibido = Arrays.binarySearch(aTopo, nombreDestino);
            System.out.println(posicionNombreRecibido);//posicion del nombre a mandar el msj
        }

        int posicionNombreIngresado=0;
        if (nombreIngresado.equals("Felipe")){
            posicionNombreIngresado=1;
            System.out.println(posicionNombreIngresado);
        }else {
            posicionNombreIngresado= Arrays.binarySearch(aTopo, nombreIngresado);
            System.out.println(posicionNombreIngresado);//posicion del nombre ingresado
        }
        //int posicionNombreIngresado= Arrays.binarySearch(aTopo, nombreIngresado);
        //System.out.println(posicionNombreIngresado);//posicion del nombre ingresado

        int difPosiciones=posicionNombreRecibido-posicionNombreIngresado;
        System.out.println(difPosiciones);
        if (nombreDestino.equals(nombreIngresado)){
            System.out.println("Mensaje de: "+ nombreOrigen+ " Dijo: "+mensaje);
        }  else if (difPosiciones==1||difPosiciones==-1){
            // Construir mensaje con el formato "origen:destino:mensaje"
            String mensajeCompleto = nombreOrigen + ":" + nombreDestino+":"+mensaje;
            byte[] buffer = mensajeCompleto.getBytes();
            String contacto = prop.getProperty(aTopo[posicionNombreRecibido]);
            String ipDestino=contacto.split(":")[0];
            int puertoDestino= Integer.parseInt(contacto.split(":")[1]);
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, InetAddress.getByName(ipDestino), puertoDestino);
            socket.send(packet);
        } else if (difPosiciones>1) {
            // Construir mensaje con el formato "origen:destino:mensaje"
            String mensajeCompleto = nombreOrigen + ":" + nombreDestino+":"+mensaje;
            byte[] buffer = mensajeCompleto.getBytes();
            String contacto = prop.getProperty(aTopo[posicionNombreIngresado+1]);
            String ipDestino=contacto.split(":")[0];
            int puertoDestino= Integer.parseInt(contacto.split(":")[1]);
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, InetAddress.getByName(ipDestino), puertoDestino);
            socket.send(packet);
        } else if (difPosiciones<-1) {
            // Construir mensaje con el formato "origen:destino:mensaje"
            String mensajeCompleto = nombreOrigen + ":" + nombreDestino+":"+mensaje;
            byte[] buffer = mensajeCompleto.getBytes();
            String contacto = prop.getProperty(aTopo[posicionNombreIngresado-1]);
            String ipDestino=contacto.split(":")[0];
            int puertoDestino= Integer.parseInt(contacto.split(":")[1]);
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, InetAddress.getByName(ipDestino), puertoDestino);
            socket.send(packet);
        }


    }

        public static void main(String[] args) throws IOException, NoSuchAlgorithmException, NombreExc {
            Programa4Santi programa=new Programa4Santi();
            programa.startProgram();
        }



}
