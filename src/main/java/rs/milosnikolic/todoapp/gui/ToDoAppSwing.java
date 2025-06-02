package rs.milosnikolic.todoapp.gui;

import javax.swing.*;
import com.google.gson.Gson;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.List;

public class ToDoAppSwing extends JFrame {

    private DefaultListModel<String> taskModel = new DefaultListModel<>();
    private JList<String> taskList = new JList<>(taskModel);
    private JTextField inputField = new JTextField(20);
    private JComboBox<String> filterBox;
    private final String apiUrl = "http://localhost:8080/api/tasks";

    public ToDoAppSwing() {
        setTitle("To-Do Lista");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(600, 450);

        // Gornji panel
        JPanel topPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; topPanel.add(new JLabel("Unesi zadatak:"), gbc);
        gbc.gridx = 1; topPanel.add(inputField, gbc);

        JButton popupInputBtn = new JButton("...");
        gbc.gridx = 2; topPanel.add(popupInputBtn, gbc);

        JButton addBtn = new JButton("Dodaj");
        gbc.gridx = 3; topPanel.add(addBtn, gbc);

        JButton refreshBtn = new JButton("Osveži");
        gbc.gridx = 4; topPanel.add(refreshBtn, gbc);

        filterBox = new JComboBox<>(new String[]{"Svi", "Završeni", "Nezavršeni"});
        gbc.gridx = 5; topPanel.add(filterBox, gbc);

        // Donji panel
        JPanel bottomPanel = new JPanel(new FlowLayout());
        JButton deleteBtn = new JButton("Obriši");
        JButton toggleBtn = new JButton("Promeni status");
        JButton editBtn = new JButton("Izmeni naziv");
        JButton sortBtn = new JButton("Sortiraj A-Z");
        JButton exportBtn = new JButton("Sačuvaj u fajl");
        JButton importBtn = new JButton("Učitaj iz fajla");
        JButton csvBtn = new JButton("Izvezi u CSV");

        bottomPanel.add(deleteBtn);
        bottomPanel.add(toggleBtn);
        bottomPanel.add(editBtn);
        bottomPanel.add(sortBtn);
        bottomPanel.add(exportBtn);
        bottomPanel.add(importBtn);
        bottomPanel.add(csvBtn);

        // Glavni panel
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(new JScrollPane(taskList), BorderLayout.CENTER);
        panel.add(bottomPanel, BorderLayout.SOUTH);
        add(panel);

        // Akcije
        popupInputBtn.addActionListener(e -> otvoriPopupZaUnos());
        addBtn.addActionListener(e -> dodajZadatak());
        deleteBtn.addActionListener(e -> obrisiZadatak());
        toggleBtn.addActionListener(e -> promeniStatus());
        editBtn.addActionListener(e -> izmeniZadatak());
        refreshBtn.addActionListener(e -> osveziListu());
        sortBtn.addActionListener(e -> sortirajListu());
        exportBtn.addActionListener(e -> sacuvajUFajl());
        importBtn.addActionListener(e -> ucitajIzFajla());
        csvBtn.addActionListener(e -> izveziCSV());
        filterBox.addActionListener(e -> osveziListu());

        osveziListu();
        setVisible(true);
    }

    private void otvoriPopupZaUnos() {
        JTextArea area = new JTextArea(5, 30);
        JTextField dateField = new JTextField(20);

        JPanel panel = new JPanel(new GridLayout(0, 1));
        panel.add(new JLabel("Unesi zadatak:"));
        panel.add(new JScrollPane(area));
        panel.add(new JLabel("Rok (npr. 2025-06-01):"));
        panel.add(dateField);

        int result = JOptionPane.showConfirmDialog(this, panel, "Novi zadatak", JOptionPane.OK_CANCEL_OPTION);

        if (result == JOptionPane.OK_OPTION) {
            inputField.setText(area.getText().trim());
            inputField.putClientProperty("dueDate", dateField.getText().trim());
        }
    }

    private void dodajZadatak() {
        String title = inputField.getText();
        if (title.isEmpty()) return;

        try {
            HttpURLConnection con = (HttpURLConnection) new URL(apiUrl).openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json");
            con.setDoOutput(true);

            TaskDTO novi = new TaskDTO();
            novi.title = title;
            novi.completed = false;

            Object dueDate = inputField.getClientProperty("dueDate");
            if (dueDate != null) novi.dueDate = dueDate.toString();

            String json = new Gson().toJson(novi);
            try (OutputStream os = con.getOutputStream()) {
                os.write(json.getBytes());
            }

            if (con.getResponseCode() == 200) {
                inputField.setText("");
                osveziListu();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void obrisiZadatak() {
        int index = taskList.getSelectedIndex();
        if (index == -1) {
            JOptionPane.showMessageDialog(this, "Selektuj zadatak");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this, "Obriši zadatak?", "Potvrda", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                Long id = Long.parseLong(taskModel.get(index).split("\\)")[0]);
                HttpURLConnection con = (HttpURLConnection) new URL(apiUrl + "/" + id).openConnection();
                con.setRequestMethod("DELETE");
                if (con.getResponseCode() == 204 || con.getResponseCode() == 200) osveziListu();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private void promeniStatus() {
        int index = taskList.getSelectedIndex();
        if (index == -1) {
            JOptionPane.showMessageDialog(this, "Selektuj zadatak");
            return;
        }
        try {
            Long id = Long.parseLong(taskModel.get(index).split("\\)")[0]);
            HttpURLConnection con = (HttpURLConnection) new URL(apiUrl + "/" + id + "/toggle").openConnection();
            con.setRequestMethod("PUT");
            if (con.getResponseCode() == 200) osveziListu();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void izmeniZadatak() {
        int index = taskList.getSelectedIndex();
        if (index == -1) {
            JOptionPane.showMessageDialog(this, "Selektuj zadatak");
            return;
        }
        String noviNaslov = JOptionPane.showInputDialog(this, "Unesi novi naziv:");
        if (noviNaslov == null || noviNaslov.trim().isEmpty()) return;
        try {
            Long id = Long.parseLong(taskModel.get(index).split("\\)")[0]);
            HttpURLConnection con = (HttpURLConnection) new URL(apiUrl + "/" + id).openConnection();
            con.setRequestMethod("PUT");
            con.setDoOutput(true);
            con.setRequestProperty("Content-Type", "application/json");

            String json = "{\"title\":\"" + noviNaslov + "\"}";
            try (OutputStream os = con.getOutputStream()) {
                os.write(json.getBytes());
            }

            if (con.getResponseCode() == 200) osveziListu();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void sortirajListu() {
        List<String> elementi = Collections.list(taskModel.elements());
        elementi.sort(Comparator.comparing(s -> s.substring(s.indexOf(")") + 1).trim(), String.CASE_INSENSITIVE_ORDER));
        taskModel.clear();
        for (String e : elementi) taskModel.addElement(e);
    }

    private void osveziListu() {
        taskModel.clear();
        try {
            HttpURLConnection con = (HttpURLConnection) new URL(apiUrl).openConnection();
            con.setRequestMethod("GET");
            try (BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
                Gson gson = new Gson();
                TaskDTO[] tasks = gson.fromJson(br, TaskDTO[].class);
                String filter = (String) filterBox.getSelectedItem();

                for (TaskDTO task : tasks) {
                    if (filter.equals("Završeni") && !task.completed) continue;
                    if (filter.equals("Nezavršeni") && task.completed) continue;

                    String prikaz = task.id + ") " + task.title;
                    if (task.dueDate != null && !task.dueDate.isBlank()) prikaz += " (rok: " + task.dueDate + ")";
                    if (task.completed) prikaz += " ✓";
                    taskModel.addElement(prikaz);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void sacuvajUFajl() {
        try {
            HttpURLConnection con = (HttpURLConnection) new URL(apiUrl).openConnection();
            con.setRequestMethod("GET");

            BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));
            Gson gson = new Gson();
            TaskDTO[] tasks = gson.fromJson(br, TaskDTO[].class);

            FileWriter writer = new FileWriter("todo_backup.json");
            gson.toJson(tasks, writer);
            writer.close();

            JOptionPane.showMessageDialog(this, "Zadaci sačuvani u todo_backup.json");
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Greška prilikom čuvanja u fajl!");
        }
    }

    private void ucitajIzFajla() {
        try {
            Gson gson = new Gson();
            BufferedReader br = new BufferedReader(new FileReader("todo_backup.json"));
            TaskDTO[] tasks = gson.fromJson(br, TaskDTO[].class);
            br.close();

            for (TaskDTO task : tasks) {
                HttpURLConnection con = (HttpURLConnection) new URL(apiUrl).openConnection();
                con.setRequestMethod("POST");
                con.setRequestProperty("Content-Type", "application/json");
                con.setDoOutput(true);

                String json = gson.toJson(task);
                try (OutputStream os = con.getOutputStream()) {
                    os.write(json.getBytes());
                }
            }

            JOptionPane.showMessageDialog(this, "Zadaci uspešno učitani iz backup fajla.");
            osveziListu();
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Greška pri učitavanju iz fajla.");
        }
    }

    private void izveziCSV() {
        try {
            HttpURLConnection con = (HttpURLConnection) new URL(apiUrl).openConnection();
            con.setRequestMethod("GET");

            BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));
            Gson gson = new Gson();
            TaskDTO[] tasks = gson.fromJson(br, TaskDTO[].class);

            PrintWriter writer = new PrintWriter("todo_export.csv");
            writer.println("ID,Naziv,Završen,Rok");

            for (TaskDTO task : tasks) {
                writer.printf("%d,%s,%s,%s%n",
                        task.id,
                        task.title.replace(",", " "),
                        task.completed ? "Da" : "Ne",
                        task.dueDate != null ? task.dueDate : ""
                );
            }

            writer.close();
            JOptionPane.showMessageDialog(this, "Zadaci su eksportovani u todo_export.csv");

            Desktop.getDesktop().open(new File("todo_export.csv")); // automatski otvori fajl

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Greška pri eksportovanju u CSV.");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ToDoAppSwing::new);
    }
}
