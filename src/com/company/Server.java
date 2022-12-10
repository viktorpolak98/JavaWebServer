package com.company;

import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;

public class Server extends HttpServer {
    private ServerSocket serverSocket;
    private Map<String, String> routes = new HashMap<>();

    public Server(){

        try{
            serverSocket = new ServerSocket(8080);
        } catch (IOException e) {
            e.printStackTrace();
        }
        connectionAcceptor();
    }

    @Override
    public void bind(InetSocketAddress addr, int backlog) throws IOException {

    }

    @Override
    public void start() {

    }

    @Override
    public void setExecutor(Executor executor) {

    }

    @Override
    public Executor getExecutor() {
        return null;
    }

    @Override
    public void stop(int delay) {

    }

    @Override
    public HttpContext createContext(String path, HttpHandler handler) {
        return null;
    }

    @Override
    public HttpContext createContext(String path) {
        return null;
    }

    @Override
    public void removeContext(String path) throws IllegalArgumentException {

    }

    @Override
    public void removeContext(HttpContext context) {

    }

    @Override
    public InetSocketAddress getAddress() {
        return null;
    }

    private void populateRoutes(){

    }

    private void connectionAcceptor(){
        new Runnable(){

            @Override
            public void run(){
                while(!serverSocket.isClosed()){
                    try{
                        serverSocket.accept();
                    } catch (IOException e){
                        e.printStackTrace();
                    }
                }
            }
        };
    }
}
