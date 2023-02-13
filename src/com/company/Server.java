package com.company;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class Server {
    private ServerSocket serverSocket;
    //Map to handle different html pages
    private final Map<String, File> routes = new HashMap<>();
    //Executor to handle clients
    private final Executor clientHandlerService;

    private final int Port = 8080;
    //Index page
    private final String index = "index";
    //error page
    private final String error = "error";

    public Server(){
        populateRoutes();

        clientHandlerService = Executors.newCachedThreadPool();
        try{
            serverSocket = new ServerSocket(Port);
        } catch (IOException e) {
            e.printStackTrace();
        }
        acceptConnections();
    }

    /**
     * Returns a requested file.
     * If the requested file is equals to "" then the index file is returned
     * If the file does not exist the error file is returned
     * @param requestedFile input from client
     * @return returns a html file from routes map
     */
    private File getFile(String requestedFile){

        return requestedFile.equals("") ?
                routes.get(index) :
                routes.containsKey(requestedFile) ?
                        routes.get(requestedFile) :
                        routes.get(error);
    }

    private boolean fileExists(String requestedFile){
        return routes.containsKey(requestedFile) || requestedFile.equals("");
    }

    /**
     * Returns a file as an array of bytes
     * @param file file to return as bytes
     * @return input file as array of bytes
     * @throws IOException
     */
    private byte[] getFileData(File file) throws IOException{
        FileInputStream fileIn = null;

        byte[] data = new byte[(int) file.length()];

        try{
            fileIn = new FileInputStream(file);
            fileIn.read(data);
        }finally {
            if(fileIn != null) fileIn.close();
        }

        return data;
    }

    /**
     * Gets each file from the Pages directory and adds them to the routes map
     */
    private void populateRoutes(){
        Path directory = Paths.get("Pages");
        try {
            Files.walk(directory).forEach(path -> addFileToRoutes(path.toFile()));
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    /**
     * Adds a file to the routes map if the file is not already added
     * @param file
     */
    private void addFileToRoutes(File file){
        if(file.isDirectory()){
            return;
        }

        String name = file.getName().substring(0, file.getName().indexOf(".html"));

        routes.putIfAbsent(name, file);
    }

    /**
     * Creates a new client via Executor
     */
    private void acceptConnections(){
        while(!serverSocket.isClosed()){
            try{
                clientHandlerService.execute(new Client(serverSocket.accept()));
            } catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    /**
     * Private class to handle each client separately
     */
    private class Client implements Runnable{
        Socket socket;
        public Client(Socket socket){
            this.socket = socket;
        }

        /**
         * Using readers and writers to communicate with server on what pages are to be displayed
         */
        @Override
        public void run() {
            BufferedReader in = null;
            BufferedOutputStream out = null;
            PrintWriter pw = null;
            String fileRequested;

            try{
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new BufferedOutputStream(socket.getOutputStream());
                pw = new PrintWriter(socket.getOutputStream());

                String input = in.readLine(); //input from client
                StringTokenizer parse = new StringTokenizer(input); //separate input into tokens

                String method = parse.nextToken().toUpperCase(); //method token
                fileRequested = parse.nextToken().toLowerCase().substring(1); //requested file. begin from index 1 to get rid of "/"

                if (method.equals("GET")){

                    File file = getFile(fileRequested);
                    byte[] data = getFileData(file);

                    if (fileExists(fileRequested)){
                        pw.println("HTTP/1.1 200 OK");
                    } else {
                        pw.println("HTTP/1.1 404 File not found");
                    }

                    pw.println(); // blank line between headers and content, very important !
                    pw.flush(); // flush character output stream buffer

                    out.write(data);
                    out.flush();
                }

            } catch (IOException e){
                e.printStackTrace();
            } finally {
                try{
                    in.close();
                    out.close();
                    pw.close();
                    socket.close();
                } catch (IOException e){
                    e.printStackTrace();
                }
            }
        }

    }
}
