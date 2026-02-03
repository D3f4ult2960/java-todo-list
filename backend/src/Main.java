import com.sun.net.httpserver.HttpServer;
import controllers.TodoController;

import java.net.InetSocketAddress;

public class Main {

    public static void main(String[] args) throws Exception {

        // Start HTTP server
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        // Routes
        server.createContext("/get-todos", TodoController::fetchTodos);
        server.createContext("/add-todo", TodoController::addTodo);
        server.createContext("/update-todo", TodoController::updateTodo);
        server.createContext("/delete-todo", TodoController::deleteTodo);

        server.start();
        System.out.println("Server running on port 8080");
    }
}
