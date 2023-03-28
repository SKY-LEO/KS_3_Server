import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;


public class Main {
    static int num_active_clients = 0;

    public static void main(String[] args) {
        ArrayList<Item> items = new ArrayList<>(5);
        items.add(new Item("China", "Xiaomi", "Mi_13", "10"));
        items.add(new Item("China", "Oneplus", "Buds_Pro_2", "40"));
        items.add(new Item("China", "Ugreen", "Cable_Type-C", "100"));
        items.add(new Item("Korea", "Samsung", "Powerbank_10000", "10"));
        items.add(new Item("China", "Asus", "Vivobook_S15", "5"));
        ServerSocket server_socket;
        int client_number = 1;
        try {
            server_socket = new ServerSocket(6000);
            while (true) {
                try {
                    Socket client_socket = server_socket.accept();
                    num_active_clients++;
                    System.out.println("Подключен новый клиент " + client_number + " " + client_socket.getInetAddress()
                            + " " + client_socket.getLocalPort());
                    System.out.println("Число активных клиентов: " + num_active_clients + "\n");
                    ClientHandler clientHandler = new ClientHandler(client_socket, items, client_number);
                    new Thread(clientHandler).start();
                    client_number++;
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
        private int client_number;

        public ClientHandler(Socket client_socket, ArrayList<Item> items, int client_number) {
            this.client_socket = client_socket;
            this.items = items;
            this.client_number = client_number;
        }

        public void run() {
            String message = "";
            String received_message;
            try {
                BufferedWriter out = new BufferedWriter(new OutputStreamWriter(client_socket.getOutputStream()));
                BufferedReader input = new BufferedReader(new InputStreamReader(client_socket.getInputStream()));
                while (!client_socket.isClosed()) {
                    received_message = input.readLine();
                    if (received_message == null) {
                        System.out.println("Нет связи с клиентом " + client_number + "!");
                        break;
                    }
                    System.out.println("\nПолучено сообщение от клиента " + client_number + ": " + received_message);
                    String[] split_message = received_message.split(" ");
                    if (received_message.equals("view")) {
                        for (Item item : items) {
                            message += item.getManufacturer_country() + " " + item.getManufacturer() + " "
                                    + item.getProduct_name() + " " + item.getQuantity() + "; ";

                        }
                    } else if (split_message[0].equals("add")) {
                        items.add(new Item(split_message[1], split_message[2], split_message[3],
                                split_message[4]));
                        message = "Добавлен новый товар";
                    } else if (split_message[0].equals("edit")) {
                        for (Item item : items) {
                            if (item.getProduct_name().equals(split_message[3])) {
                                item.setManufacturer_country(split_message[1]);
                                item.setManufacturer(split_message[2]);
                                item.setQuantity(split_message[4]);
                                message = "Товар успешно изменен";
                                break;
                            }
                        }
                    } else if (split_message[0].equals("delete")) {
                        for (Item item : items) {
                            if (item.getManufacturer_country().equals(split_message[1])
                                    && item.getProduct_name().equals(split_message[3])) {
                                items.remove(item);
                                message = "Товар успешно удален";
                                break;
                            }
                        }
                    } else {
                        for (Item item : items) {
                            if (item.getProduct_name().equals(received_message)) {
                                message = item.getQuantity();
                                break;
                            }
                        }
                    }
                    if (message.isEmpty()) {
                        message = "Ошибка!";
                    }
                    sendMessage(out, message);
                    System.out.println("Сервер отправил сообщение клиенту " + client_number + ": " + message);
                    message = "";
                }
                client_socket.close();
                input.close();
                out.close();
                System.out.println("Каналы и сокет закрыты с клиентом " + client_number);
                num_active_clients--;
                System.out.println("Число активных клиентов: " + num_active_clients);
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

    public void setManufacturer_country(String manufacturer_country) {
        this.manufacturer_country = manufacturer_country;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public void setProduct_name(String product_name) {
        this.product_name = product_name;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }
}