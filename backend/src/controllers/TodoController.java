package controllers;

import com.sun.net.httpserver.HttpExchange;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

public class TodoController {

    private static List<Todo> todos = new ArrayList<>();
    private static int currentId = 1;

    private static final String FILE_PATH = "src/data/todos.json";

    // Todo class
    static class Todo {
        int id;
        String title;
        boolean completed;

        Todo(int id, String title, boolean completed) {
            this.id = id;
            this.title = title;
            this.completed = completed;
        }
    }

    // Load todos on class load
    static {
        loadTodosFromFile();
    }

    // Load from JSON file
    private static void loadTodosFromFile() {
        try {
            Path filePath = Paths.get(FILE_PATH);
            if (!Files.exists(filePath)) return;

            String content = new String(Files.readAllBytes(filePath)).trim();
            if (!content.startsWith("[") || !content.endsWith("]")) return;
            content = content.substring(1, content.length() - 1);
            if (content.isEmpty()) return;

            String[] items = content.split("\\},\\{");
            for (String item : items) {
                item = item.replace("{", "").replace("}", "");
                Map<String, String> map = Arrays.stream(item.split(","))
                        .map(s -> s.split(":"))
                        .collect(Collectors.toMap(
                                a -> a[0].trim().replace("\"", ""),
                                a -> a[1].trim().replace("\"", "")
                        ));

                int id = Integer.parseInt(map.get("id"));
                String title = map.get("title");
                boolean completed = Boolean.parseBoolean(map.get("completed"));

                todos.add(new Todo(id, title, completed));
                currentId = Math.max(currentId, id + 1);
            }

        } catch (IOException e) {
            System.out.println("No todos.json found, starting with empty list.");
        }
    }

    // Save todos to JSON file
    private static void saveTodosToFile() {
        try {
            Path filePath = Paths.get(FILE_PATH);
            if (!Files.exists(filePath.getParent())) {
                Files.createDirectories(filePath.getParent()); // create src/data folder
            }

            StringBuilder sb = new StringBuilder("[");
            for (Todo t : todos) {
                sb.append("{")
                        .append("\"id\":").append(t.id).append(",")
                        .append("\"title\":\"").append(t.title).append("\",")
                        .append("\"completed\":").append(t.completed)
                        .append("},");
            }
            if (sb.length() > 1) sb.deleteCharAt(sb.length() - 1);
            sb.append("]");

            Files.write(filePath, sb.toString().getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Add CORS headers for all responses
    private static void addCORS(HttpExchange req) {
        req.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        req.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        req.getResponseHeaders().add("Access-Control-Allow-Headers", "*");
    }

    // GET /get-todos
    public static void fetchTodos(HttpExchange req) throws IOException {
        if ("OPTIONS".equals(req.getRequestMethod())) {
            addCORS(req);
            req.sendResponseHeaders(204, -1);
            req.close();
            return;
        }

        addCORS(req);

        StringBuilder json = new StringBuilder("[");
        for (Todo t : todos) {
            json.append("{")
                    .append("\"id\":").append(t.id).append(",")
                    .append("\"title\":\"").append(t.title).append("\",")
                    .append("\"completed\":").append(t.completed)
                    .append("},");
        }
        if (json.charAt(json.length() - 1) == ',') json.deleteCharAt(json.length() - 1);
        json.append("]");

        byte[] response = json.toString().getBytes();
        req.getResponseHeaders().add("Content-Type", "application/json");
        req.sendResponseHeaders(200, response.length);
        req.getResponseBody().write(response);
        req.close();
    }

    // POST /add-todo
    public static void addTodo(HttpExchange req) throws IOException {
        if ("OPTIONS".equals(req.getRequestMethod())) {
            addCORS(req);
            req.sendResponseHeaders(204, -1);
            req.close();
            return;
        }

        addCORS(req);

        String title;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(req.getRequestBody()))) {
            title = reader.lines().collect(Collectors.joining());
        }
        todos.add(new Todo(currentId++, title, false));
        saveTodosToFile();

        req.sendResponseHeaders(201, -1);
        req.close();
    }

    // POST /update-todo?id=ID
    public static void updateTodo(HttpExchange req) throws IOException {
        if ("OPTIONS".equals(req.getRequestMethod())) {
            addCORS(req);
            req.sendResponseHeaders(204, -1);
            req.close();
            return;
        }

        addCORS(req);

        int id = Integer.parseInt(req.getRequestURI().getQuery().split("=")[1]);
        for (Todo t : todos) {
            if (t.id == id) {
                t.completed = !t.completed;
                break;
            }
        }
        saveTodosToFile();

        req.sendResponseHeaders(200, -1);
        req.close();
    }

    // POST /delete-todo?id=ID
    public static void deleteTodo(HttpExchange req) throws IOException {
        if ("OPTIONS".equals(req.getRequestMethod())) {
            addCORS(req);
            req.sendResponseHeaders(204, -1);
            req.close();
            return;
        }

        addCORS(req);

        int id = Integer.parseInt(req.getRequestURI().getQuery().split("=")[1]);
        todos.removeIf(t -> t.id == id);
        saveTodosToFile();

        req.sendResponseHeaders(204, -1);
        req.close();
    }
}
