package rs.milosnikolic.todoapp;

import java.io.*;
import java.net.*;
import java.util.Scanner;

public class ApiTest {

    private static final String API_URL = "http://localhost:8080/api/tasks";

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("=== ToDo API Tester ===");
        System.out.println("1. Dodaj zadatak");
        System.out.println("2. Prikaži sve zadatke");
        System.out.println("3. Promeni naziv zadatka");
        System.out.println("4. Promeni status zadatka");
        System.out.println("5. Obriši zadatak");
        System.out.print("Izaberi opciju (1-5): ");
        int izbor = scanner.nextInt();
        scanner.nextLine(); // čisti newline

        switch (izbor) {
            case 1 -> dodajZadatak(scanner);
            case 2 -> prikaziSveZadatke();
            case 3 -> izmeniNaziv(scanner);
            case 4 -> promeniStatus(scanner);
            case 5 -> obrisiZadatak(scanner);
            default -> System.out.println("Nepoznata opcija.");
        }
    }

    private static void dodajZadatak(Scanner scanner) {
        try {
            System.out.print("Unesi naziv zadatka: ");
            String title = scanner.nextLine();

            HttpURLConnection con = (HttpURLConnection) new URL(API_URL).openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json");
            con.setDoOutput(true);

            String json = "{\"title\":\"" + title + "\",\"completed\":false}";
            try (OutputStream os = con.getOutputStream()) {
                os.write(json.getBytes());
            }

            System.out.println("Status: " + con.getResponseCode());
            prikaziOdgovor(con);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void prikaziSveZadatke() {
        try {
            HttpURLConnection con = (HttpURLConnection) new URL(API_URL).openConnection();
            con.setRequestMethod("GET");

            System.out.println("Status: " + con.getResponseCode());
            prikaziOdgovor(con);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void izmeniNaziv(Scanner scanner) {
        try {
            System.out.print("ID zadatka za izmenu: ");
            long id = scanner.nextLong();
            scanner.nextLine();
            System.out.print("Novi naziv: ");
            String noviNaziv = scanner.nextLine();

            HttpURLConnection con = (HttpURLConnection) new URL(API_URL + "/" + id).openConnection();
            con.setRequestMethod("PUT");
            con.setRequestProperty("Content-Type", "application/json");
            con.setDoOutput(true);

            String json = "{\"title\":\"" + noviNaziv + "\"}";
            try (OutputStream os = con.getOutputStream()) {
                os.write(json.getBytes());
            }

            System.out.println("Status: " + con.getResponseCode());
            prikaziOdgovor(con);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void promeniStatus(Scanner scanner) {
        try {
            System.out.print("ID zadatka za promenu statusa: ");
            long id = scanner.nextLong();

            HttpURLConnection con = (HttpURLConnection) new URL(API_URL + "/" + id + "/toggle").openConnection();
            con.setRequestMethod("PUT");

            System.out.println("Status: " + con.getResponseCode());
            prikaziOdgovor(con);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void obrisiZadatak(Scanner scanner) {
        try {
            System.out.print("ID zadatka za brisanje: ");
            long id = scanner.nextLong();

            HttpURLConnection con = (HttpURLConnection) new URL(API_URL + "/" + id).openConnection();
            con.setRequestMethod("DELETE");

            System.out.println("Status: " + con.getResponseCode());
            if (con.getResponseCode() == 204) {
                System.out.println("Zadatak uspešno obrisan.");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void prikaziOdgovor(HttpURLConnection con) {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
            String linija;
            while ((linija = br.readLine()) != null) {
                System.out.println(linija);
            }
        } catch (IOException e) {
            System.out.println("Greška prilikom čitanja odgovora.");
        }
    }
}