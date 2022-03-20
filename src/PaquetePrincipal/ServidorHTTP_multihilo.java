package PaquetePrincipal;

import java.io.BufferedReader;
import java.net.Socket;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * *****************************************************************************
 * Servidor HTTP que atiende peticiones de tipo 'GET' recibidas por el puerto
 * 8066
 *
 * NOTA: para probar este código, comprueba primero de que no tienes ningún otro
 * servicio por el puerto 8066 (por ejemplo, con el comando 'netstat' si estás
 * utilizando Windows)
 *
 * @author Mario Bello García
 */
class ServidorHTTP_multihilo {

    /**
     * **************************************************************************
     * procedimiento principal que asigna a cada petición un nuevo hilo, por donde se enviará la peticion
     * el hilo principal queda libre para nuevas peticiones.
     *
     */
    public static void main(String[] args) throws IOException, Exception {

        try {
            //Asociamos al servidor el puerto 8066
            ServerSocket socServidor = new ServerSocket(8066);
            imprimeDisponible();
            Socket socCliente;
            int cliente = 0;
           //bucle while que gestiona las peticiones entrantes, las gestiona y las envia a un nuevo hilo,
           //queda libre pendiente de nuevas peticdiones.           
            while (true) {
                
                socCliente = socServidor.accept();
                //enumeramos las peticiones que entran con una variable creciente.
                System.out.println("____________________________________Nuevo Cliente " + cliente);
                System.out.println("Atendiendo al cliente ");
                //nuevo hilo que es el encargado de ejecutar la peticion
                HiloDespachador hilo = new HiloDespachador(socCliente);
                hilo.star();
                cliente++;//suma +1 a la variable.
            }
        } catch (IOException ex) {
            ex.getMessage();
      }
    }

    /**
     * **************************************************************************
     * muestra un mensaje en la Salida que confirma el arranque, y da algunas
     * indicaciones posteriores
     */
    private static void imprimeDisponible() {

        System.out.println("El Servidor WEB *****multihilo*****se está ejecutando y permanece a la "
                + "escucha por el puerto 8066.\nEscribe en la barra de direcciones "
                + "de tu explorador preferido:\n\nhttp://localhost:8066\npara "
                + "solicitar la página de bienvenida\n\nhttp://localhost:8066/"
                + "quijote\n para solicitar una página del Quijote,\n\nhttp://"
                + "localhost:8066/q\n para simular un error");
    }

    //Metodo getDate para la cabecera DATE
    public static String getDate() {
        DateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss ");
        return dateFormat.format(new Date());

    }

    //Hilo ejecuta las peticiones, extension de la clase Thread.
    private static class HiloDespachador extends Thread {

        private final Socket socketCliente;
        //Constructor que almacena el socketCliente que ejecuta el metodo run().
        public HiloDespachador(Socket socketCliente) {
            this.socketCliente = socketCliente;
        }

        private void star() throws IOException {

            //variables locales
            String peticion;
            String html;

            //Flujo de entrada
            InputStreamReader inSR = new InputStreamReader(
                    socketCliente.getInputStream());
            //espacio en memoria para la entrada de peticiones
            BufferedReader bufLeer = new BufferedReader(inSR);

            //objeto de java.io que entre otras características, permite escribir 
            //'línea a línea' en un flujo de salida
            PrintWriter printWriter = new PrintWriter(
                    socketCliente.getOutputStream(), true);

            //mensaje petición cliente
            peticion = bufLeer.readLine();

            //para compactar la petición y facilitar así su análisis, suprimimos todos 
            //los espacios en blanco que contenga
            peticion = peticion.replaceAll(" ", "");

            //si realmente se trata de una petición 'GET' (que es la única que vamos a
            //implementar en nuestro Servidor)
            if (peticion.startsWith("GET")) {
                //extrae la subcadena entre 'GET' y 'HTTP/1.1'
                peticion = peticion.substring(3, peticion.lastIndexOf("HTTP"));

                //si corresponde a la página de inicio
                if (peticion.length() == 0 || peticion.equals("/")) {
                    //sirve la página
                    html = Paginas.html_index;
                    printWriter.println(Mensajes.lineaInicial_OK);
                    printWriter.println(Paginas.primeraCabecera);
                    printWriter.println(Paginas.segundaCabecera);
                    // printWriter.println(getDate());
                    printWriter.println("Content-Length: " + html.length() + 1);
                    printWriter.println("\n");
                    printWriter.println(html);
                } //si corresponde a la página del Quijote
                else if (peticion.equals("/quijote")) {
                    //sirve la página
                    html = Paginas.html_quijote;
                    printWriter.println(Mensajes.lineaInicial_OK);
                    printWriter.println(Paginas.primeraCabecera);
                    printWriter.println(Paginas.segundaCabecera);
                    // printWriter.println(getDate());
                    printWriter.println("Content-Length: " + html.length() + 1);
                    printWriter.println("\n");
                    printWriter.println(html);
                } //en cualquier otro caso
                else {
                    //sirve la página
                    html = Paginas.html_noEncontrado;
                    printWriter.println(Mensajes.lineaInicial_NotFound);
                    printWriter.println(Paginas.primeraCabecera);
                    printWriter.println(Paginas.segundaCabecera);
                    //printWriter.println(getDate());
                    printWriter.println("Content-Length: " + html.length() + 1);
                    printWriter.println("\n");
                    printWriter.println(html);
                }
            }
        }
    }
}
