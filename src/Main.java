import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;


public class Main {

    public static void main(String[] args) {
        //Item smartphone = new Item("China", "Xiaomi", "Mi 13", 10);
        //Item earphones = new Item("China", "Oneplus", "Buds Pro 2", 40);
        //Item cable = new Item("China", "Ugreen", "Cable Type-C", 100);
        //Item powerbank = new Item("Korea", "Samsung", "Powerbank 10000", 10);
        //Item notebook = new Item("China", "Asus", "Vivobook S15", 5);
        ArrayList<Item> items = new ArrayList<>(5);
        items.add(new Item("China", "Xiaomi", "Mi 13", "10"));
        items.add(new Item("China", "Oneplus", "Buds Pro 2", "40"));
        items.add(new Item("China", "Ugreen", "Cable Type-C", "100"));
        items.add(new Item("Korea", "Samsung", "Powerbank 10000", "10"));
        items.add(new Item("China", "Asus", "Vivobook S15", "5"));
        ServerSocket server_socket;
        try {
            server_socket = new ServerSocket(6000);
            while (true) {
                try {
                    Socket client_socket = server_socket.accept();
                    System.out.println("Подключен новый клиент: " + client_socket.getInetAddress() + ", "
                            + client_socket.getLocalPort());
                    ClientHandler clientHandler = new ClientHandler(client_socket, items);
                    new Thread(clientHandler).start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static class ClientHandler implements Runnable {
        private Socket client_socket;
        private ArrayList<Item> items;

        public ClientHandler(Socket client_socket, ArrayList<Item> items) {
            this.client_socket = client_socket;
            this.items = items;
        }

        public void run() {
            String quantity = "Нет такого товара!";
            String product_name;
            try {
                BufferedWriter out = new BufferedWriter(new OutputStreamWriter(client_socket.getOutputStream()));
                BufferedReader input = new BufferedReader(new InputStreamReader(client_socket.getInputStream()));
                while (!client_socket.isClosed()) {
                    product_name = input.readLine();
                    if (product_name == null) {
                        System.out.println("Нет связи с клиентом!");
                        break;
                    }
                    System.out.println("Получено сообщение: " + product_name);
                    for (Item item : items) {
                        if (item.getProduct_name().equals(product_name)) {
                            quantity = item.getQuantity();
                            break;
                        }
                    }
                    sendMessage(out, quantity);
                    System.out.println("Сервер отправил сообщение: " + quantity);
                }
                client_socket.close();
                input.close();
                out.close();
                System.out.println("Каналы и сокет закрыты");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void sendMessage(BufferedWriter out, String message) {
        try {
            out.write(message + "\n");
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class Item {
    private String manufacturer_country;
    private String manufacturer;
    private String product_name;
    private String quantity;

    public Item(String manufacturer_country, String manufacturer, String product_name, String quantity) {
        this.manufacturer_country = manufacturer_country;
        this.manufacturer = manufacturer;
        this.product_name = product_name;
        this.quantity = quantity;
    }

    public String getManufacturer_country() {
        return manufacturer_country;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public String getProduct_name() {
        return product_name;
    }

    public String getQuantity() {
        return quantity;
    }
}