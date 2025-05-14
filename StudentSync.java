import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;

public class StudentSync {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            StudentSyncFrame frame = new StudentSyncFrame();
            frame.setVisible(true);
        });
    }
}

class StudentSyncFrame extends JFrame {
    private Connection conn;
    private JLabel statusLabel;

    public StudentSyncFrame() {
        initializeDatabase();
        setupUI();
    }

    private void initializeDatabase() {
        try {
            conn = DriverManager.getConnection("jdbc:sqlite:studentsync.db");
            Statement stmt = conn.createStatement();
            stmt.execute("CREATE TABLE IF NOT EXISTS announcements (id INTEGER PRIMARY KEY AUTOINCREMENT, title TEXT, content TEXT)");
            stmt.execute("CREATE TABLE IF NOT EXISTS lost_and_found (id INTEGER PRIMARY KEY AUTOINCREMENT, item TEXT, status TEXT)");
            stmt.execute("CREATE TABLE IF NOT EXISTS results (id INTEGER PRIMARY KEY AUTOINCREMENT, student_id TEXT, department TEXT, semester TEXT, course TEXT, grade TEXT)");
            stmt.execute("CREATE TABLE IF NOT EXISTS timetable (id INTEGER PRIMARY KEY AUTOINCREMENT, course TEXT, day TEXT, time TEXT)");
            stmt.execute("CREATE TABLE IF NOT EXISTS campus_map (id INTEGER PRIMARY KEY AUTOINCREMENT, building TEXT, location TEXT)");
            stmt.execute("CREATE TABLE IF NOT Exists faq (id INTEGER PRIMARY KEY AUTOINCREMENT, question TEXT, answer TEXT)");
            stmt.execute("CREATE TABLE IF NOT EXISTS transport_tracker (id INTEGER PRIMARY KEY AUTOINCREMENT, route TEXT, schedule TEXT, live_location TEXT, student_id TEXT)");
            // Insert sample data if tables are empty
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM announcements");
            if (rs.getInt(1) == 0) {
                stmt.execute("INSERT INTO announcements (title, content) VALUES ('Exam Schedule', 'Midterms: Dec 10-15')");
                stmt.execute("INSERT INTO lost_and_found (item, status) VALUES ('Laptop', 'Reported, Awaiting Claim')");
                stmt.execute("INSERT INTO results (student_id, department, semester, course, grade) VALUES ('S001', 'CS', 'Fall 2025', 'OOP', 'A')");
                stmt.execute("INSERT INTO timetable (course, day, time) VALUES ('OOP', 'Monday', '10 AM')");
                stmt.execute("INSERT INTO campus_map (building, location) VALUES ('Library', 'Building A, North Wing')");
                stmt.execute("INSERT INTO faq (question, answer) VALUES ('How to enroll?', 'Visit registrar’s office or online portal')");
                stmt.execute("INSERT INTO transport_tracker (route, schedule, live_location, student_id) VALUES ('Campus-City', '8 AM - 6 PM', 'Near Library', 'S001')");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            statusLabel.setText("Database error: " + e.getMessage());
        }
    }

    private void setupUI() {
        setTitle("Student Sync - Muhammad Hassan");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(null);

        // Menu Bar
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        JMenuItem saveItem = new JMenuItem("Save");
        JMenuItem exitItem = new JMenuItem("Exit");
        JMenu helpMenu = new JMenu("Help");
        JMenuItem aboutItem = new JMenuItem("About");

        saveItem.addActionListener(e -> statusLabel.setText("Data saved"));
        exitItem.addActionListener(e -> System.exit(0));
        aboutItem.addActionListener(e -> JOptionPane.showMessageDialog(this,
            "Student Sync v1.0\nDeveloped by Muhammad Hassan", "About", JOptionPane.INFORMATION_MESSAGE));

        fileMenu.add(saveItem);
        fileMenu.add(exitItem);
        helpMenu.add(aboutItem);
        menuBar.add(fileMenu);
        menuBar.add(helpMenu);
        setJMenuBar(menuBar);

        // Welcome Label
        JLabel welcomeLabel = new JLabel("Welcome, Muhammad Hassan");
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 20));
        welcomeLabel.setBounds(250, 10, 300, 30);
        add(welcomeLabel);

        // Tabbed Pane
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setBounds(50, 50, 700, 400);

        // Dashboard Tab
        JPanel dashboardPanel = new JPanel(null);
        JTextArea dashboardArea = new JTextArea();
        dashboardArea.setEditable(false);
        dashboardArea.setBounds(10, 10, 400, 200);
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT title, content FROM announcements LIMIT 1");
            if (rs.next()) {
                dashboardArea.append("Latest Announcement: " + rs.getString("title") + "\n" + rs.getString("content") + "\n\n");
            }
            rs = stmt.executeQuery("SELECT route, live_location FROM transport_tracker LIMIT 1");
            if (rs.next()) {
                dashboardArea.append("Transport Update: " + rs.getString("route") + " is at " + rs.getString("live_location"));
            }
        } catch (SQLException e) {
            dashboardArea.setText("Error loading dashboard: " + e.getMessage());
        }
        dashboardPanel.add(dashboardArea);
        tabbedPane.addTab("Dashboard", dashboardPanel);

        // Announcement Tab
        JPanel announcementPanel = new JPanel(null);
        JTextArea announcementArea = new JTextArea();
        announcementArea.setEditable(false);
        announcementArea.setBounds(10, 10, 400, 180);
        updateAnnouncementArea(announcementArea);
        JComboBox<String> announcementCombo = new JComboBox<>();
        announcementCombo.setBounds(10, 200, 100, 25);
        updateComboBox(announcementCombo, "announcements");
        JTextField titleField = new JTextField();
        titleField.setBounds(120, 200, 150, 25);
        JTextField contentField = new JTextField();
        contentField.setBounds(280, 200, 150, 25);
        JButton addAnnouncement = new JButton("Add");
        addAnnouncement.setBounds(440, 200, 80, 25);
        JButton editAnnouncement = new JButton("Edit");
        editAnnouncement.setBounds(530, 200, 80, 25);
        addAnnouncement.addActionListener(e -> {
            try {
                PreparedStatement pstmt = conn.prepareStatement("INSERT INTO announcements (title, content) VALUES (?, ?)");
                pstmt.setString(1, titleField.getText());
                pstmt.setString(2, contentField.getText());
                pstmt.executeUpdate();
                updateAnnouncementArea(announcementArea);
                updateComboBox(announcementCombo, "announcements");
                statusLabel.setText("Announcement added");
                titleField.setText("");
                contentField.setText("");
            } catch (SQLException ex) {
                statusLabel.setText("Error: " + ex.getMessage());
            }
        });
        editAnnouncement.addActionListener(e -> {
            try {
                int index = Integer.parseInt((String) announcementCombo.getSelectedItem());
                PreparedStatement pstmt = conn.prepareStatement("UPDATE announcements SET title = ?, content = ? WHERE id = ?");
                pstmt.setString(1, titleField.getText());
                pstmt.setString(2, contentField.getText());
                pstmt.setInt(3, index + 1);
                pstmt.executeUpdate();
                updateAnnouncementArea(announcementArea);
                statusLabel.setText("Announcement edited");
                titleField.setText("");
                contentField.setText("");
            } catch (SQLException ex) {
                statusLabel.setText("Error: " + ex.getMessage());
            }
        });
        announcementCombo.addActionListener(e -> {
            try {
                int index = announcementCombo.getSelectedIndex();
                PreparedStatement pstmt = conn.prepareStatement("SELECT title, content FROM announcements WHERE id = ?");
                pstmt.setInt(1, index + 1);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    titleField.setText(rs.getString("title"));
                    contentField.setText(rs.getString("content"));
                }
            } catch (SQLException ex) {
                statusLabel.setText("Error: " + ex.getMessage());
            }
        });
        announcementPanel.add(announcementArea);
        announcementPanel.add(new JLabel("Select:").setBounds(10, 180, 50, 20));
        announcementPanel.add(announcementCombo);
        announcementPanel.add(new JLabel("Title:").setBounds(120, 180, 50, 20));
        announcementPanel.add(titleField);
        announcementPanel.add(new JLabel("Content:").setBounds(280, 180, 60, 20));
        announcementPanel.add(contentField);
        announcementPanel.add(addAnnouncement);
        announcementPanel.add(editAnnouncement);
        tabbedPane.addTab("Announcements", announcementPanel);

        // Lost & Found Tab
        JPanel lostFoundPanel = new JPanel(null);
        JTextArea lostFoundArea = new JTextArea();
        lostFoundArea.setEditable(false);
        lostFoundArea.setBounds(10, 10, 400, 200);
        updateLostFoundArea(lostFoundArea);
        JTextField itemField = new JTextField();
        itemField.setBounds(10, 220, 400, 25);
        JButton reportLost = new JButton("Report Lost");
        reportLost.setBounds(420, 220, 100, 25);
        reportLost.addActionListener(e -> {
            try {
                PreparedStatement pstmt = conn.prepareStatement("INSERT INTO lost_and_found (item, status) VALUES (?, ?)");
                pstmt.setString(1, itemField.getText());
                pstmt.setString(2, "Reported");
                pstmt.executeUpdate();
                updateLostFoundArea(lostFoundArea);
                statusLabel.setText("Item reported");
                itemField.setText("");
            } catch (SQLException ex) {
                statusLabel.setText("Error: " + ex.getMessage());
            }
        });
        lostFoundPanel.add(lostFoundArea);
        lostFoundPanel.add(new JLabel("Item:").setBounds(10, 200, 50, 20));
        lostFoundPanel.add(itemField);
        lostFoundPanel.add(reportLost);
        tabbedPane.addTab("Lost & Found", lostFoundPanel);

        // Results Tab
        JPanel resultPanel = new JPanel(null);
        JTextArea resultArea = new JTextArea();
        resultArea.setEditable(false);
        resultArea.setBounds(10, 10, 400, 150);
        JComboBox<String> departmentCombo = new JComboBox<>(new String[]{"CS", "EE", "ME"});
        departmentCombo.setBounds(10, 170, 100, 25);
        JComboBox<String> semesterCombo = new JComboBox<>(new String[]{"Fall 2025", "Spring 2026"});
        semesterCombo.setBounds(120, 170, 100, 25);
        updateResultArea(resultArea, (String) departmentCombo.getSelectedItem(), (String) semesterCombo.getSelectedItem());
        JComboBox<String> resultCombo = new JComboBox<>();
        resultCombo.setBounds(10, 200, 100, 25);
        updateComboBox(resultCombo, "results", (String) departmentCombo.getSelectedItem(), (String) semesterCombo.getSelectedItem());
        JTextField studentIdField = new JTextField();
        studentIdField.setBounds(120, 200, 100, 25);
        JTextField courseField = new JTextField();
        courseField.setBounds(230, 200, 100, 25);
        JTextField gradeField = new JTextField();
        gradeField.setBounds(340, 200, 100, 25);
        JButton addResult = new JButton("Add");
        addResult.setBounds(450, 200, 80, 25);
        JButton editResult = new JButton("Edit");
        editResult.setBounds(540, 200, 80, 25);
        departmentCombo.addActionListener(e -> updateResultArea(resultArea, (String) departmentCombo.getSelectedItem(), (String) semesterCombo.getSelectedItem()));
        semesterCombo.addActionListener(e -> updateResultArea(resultArea, (String) departmentCombo.getSelectedItem(), (String) semesterCombo.getSelectedItem()));
        addResult.addActionListener(e -> {
            try {
                PreparedStatement pstmt = conn.prepareStatement("INSERT INTO results (student_id, department, semester, course, grade) VALUES (?, ?, ?, ?, ?)");
                pstmt.setString(1, studentIdField.getText());
                pstmt.setString(2, (String) departmentCombo.getSelectedItem());
                pstmt.setString(3, (String) semesterCombo.getSelectedItem());
                pstmt.setString(4, courseField.getText());
                pstmt.setString(5, gradeField.getText());
                pstmt.executeUpdate();
                updateResultArea(resultArea, (String) departmentCombo.getSelectedItem(), (String) semesterCombo.getSelectedItem());
                updateComboBox(resultCombo, "results", (String) departmentCombo.getSelectedItem(), (String) semesterCombo.getSelectedItem());
                statusLabel.setText("Result added");
                studentIdField.setText("");
                courseField.setText("");
                gradeField.setText("");
            } catch (SQLException ex) {
                statusLabel.setText("Error: " + ex.getMessage());
            }
        });
        editResult.addActionListener(e -> {
            try {
                int index = Integer.parseInt((String) resultCombo.getSelectedItem());
                PreparedStatement pstmt = conn.prepareStatement("UPDATE results SET student_id = ?, department = ?, semester = ?, course = ?, grade = ? WHERE id = ?");
                pstmt.setString(1, studentIdField.getText());
                pstmt.setString(2, (String) departmentCombo.getSelectedItem());
                pstmt.setString(3, (String) semesterCombo.getSelectedItem());
                pstmt.setString(4, courseField.getText());
                pstmt.setString(5, gradeField.getText());
                pstmt.setInt(6, index + 1);
                pstmt.executeUpdate();
                updateResultArea(resultArea, (String) departmentCombo.getSelectedItem(), (String) semesterCombo.getSelectedItem());
                statusLabel.setText("Result edited");
                studentIdField.setText("");
                courseField.setText("");
                gradeField.setText("");
            } catch (SQLException ex) {
                statusLabel.setText("Error: " + ex.getMessage());
            }
        });
        resultCombo.addActionListener(e -> {
            try {
                int index = resultCombo.getSelectedIndex();
                PreparedStatement pstmt = conn.prepareStatement("SELECT student_id, course, grade FROM results WHERE id = ?");
                pstmt.setInt(1, index + 1);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    studentIdField.setText(rs.getString("student_id"));
                    courseField.setText(rs.getString("course"));
                    gradeField.setText(rs.getString("grade"));
                }
            } catch (SQLException ex) {
                statusLabel.setText("Error: " + ex.getMessage());
            }
        });
        resultPanel.add(resultArea);
        resultPanel.add(new JLabel("Dept:").setBounds(10, 150, 50, 20));
        resultPanel.add(departmentCombo);
        resultPanel.add(new JLabel("Sem:").setBounds(120, 150, 50, 20));
        resultPanel.add(semesterCombo);
        resultPanel.add(new JLabel("Select:").setBounds(10, 180, 50, 20));
        resultPanel.add(resultCombo);
        resultPanel.add(new JLabel("ID:").setBounds(120, 180, 30, 20));
        resultPanel.add(studentIdField);
        resultPanel.add(new JLabel("Course:").setBounds(230, 180, 50, 20));
        resultPanel.add(courseField);
        resultPanel.add(new JLabel("Grade:").setBounds(340, 180, 50, 20));
        resultPanel.add(gradeField);
        resultPanel.add(addResult);
        resultPanel.add(editResult);
        tabbedPane.addTab("Results", resultPanel);

        // Timetable Tab
        JPanel timetablePanel = new JPanel(null);
        JTextArea timetableArea = new JTextArea();
        timetableArea.setEditable(false);
        timetableArea.setBounds(10, 10, 400, 180);
        updateTimetableArea(timetableArea);
        JComboBox<String> timetableCombo = new JComboBox<>();
        timetableCombo.setBounds(10, 200, 100, 25);
        updateComboBox(timetableCombo, "timetable");
        JTextField courseField2 = new JTextField();
        courseField2.setBounds(120, 200, 100, 25);
        JTextField dayField = new JTextField();
        dayField.setBounds(230, 200, 100, 25);
        JTextField timeField = new JTextField();
        timeField.setBounds(340, 200, 100, 25);
        JButton addTimetable = new JButton("Add");
        addTimetable.setBounds(450, 200, 80, 25);
        JButton editTimetable = new JButton("Edit");
        editTimetable.setBounds(540, 200, 80, 25);
        addTimetable.addActionListener(e -> {
            try {
                PreparedStatement pstmt = conn.prepareStatement("INSERT INTO timetable (course, day, time) VALUES (?, ?, ?)");
                pstmt.setString(1, courseField2.getText());
                pstmt.setString(2, dayField.getText());
                pstmt.setString(3, timeField.getText());
                pstmt.executeUpdate();
                updateTimetableArea(timetableArea);
                updateComboBox(timetableCombo, "timetable");
                statusLabel.setText("Timetable entry added");
                courseField2.setText("");
                dayField.setText("");
                timeField.setText("");
            } catch (SQLException ex) {
                statusLabel.setText("Error: " + ex.getMessage());
            }
        });
        editTimetable.addActionListener(e -> {
            try {
                int index = Integer.parseInt((String) timetableCombo.getSelectedItem());
                PreparedStatement pstmt = conn.prepareStatement("UPDATE timetable SET course = ?, day = ?, time = ? WHERE id = ?");
                pstmt.setString(1, courseField2.getText());
                pstmt.setString(2, dayField.getText());
                pstmt.setString(3, timeField.getText());
                pstmt.setInt(4, index + 1);
                pstmt.executeUpdate();
                updateTimetableArea(timetableArea);
                statusLabel.setText("Timetable entry edited");
                courseField2.setText("");
                dayField.setText("");
                timeField.setText("");
            } catch (SQLException ex) {
                statusLabel.setText("Error: " + ex.getMessage());
            }
        });
        timetableCombo.addActionListener(e -> {
            try {
                int index = timetableCombo.getSelectedIndex();
                PreparedStatement pstmt = conn.prepareStatement("SELECT course, day, time FROM timetable WHERE id = ?");
                pstmt.setInt(1, index + 1);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    courseField2.setText(rs.getString("course"));
                    dayField.setText(rs.getString("day"));
                    timeField.setText(rs.getString("time"));
                }
            } catch (SQLException ex) {
                statusLabel.setText("Error: " + ex.getMessage());
            }
        });
        timetablePanel.add(timetableArea);
        timetablePanel.add(new JLabel("Select:").setBounds(10, 180, 50, 20));
        timetablePanel.add(timetableCombo);
        timetablePanel.add(new JLabel("Course:").setBounds(120, 180, 50, 20));
        timetablePanel.add(courseField2);
        timetablePanel.add(new JLabel("Day:").setBounds(230, 180, 50, 20));
        timetablePanel.add(dayField);
        timetablePanel.add(new JLabel("Time:").setBounds(340, 180, 50, 20));
        timetablePanel.add(timeField);
        timetablePanel.add(addTimetable);
        timetablePanel.add(editTimetable);
        tabbedPane.addTab("Timetable", timetablePanel);

        // Campus Map Tab
        JPanel campusMapPanel = new JPanel(null);
        JTextArea campusMapArea = new JTextArea();
        campusMapArea.setEditable(false);
        campusMapArea.setBounds(10, 10, 400, 200);
        updateCampusMapArea(campusMapArea);
        JTextField buildingField = new JTextField();
        buildingField.setBounds(10, 220, 200, 25);
        JTextField locationField = new JTextField();
        locationField.setBounds(220, 220, 200, 25);
        JButton addMap = new JButton("Add");
        addMap.setBounds(430, 220, 80, 25);
        addMap.addActionListener(e -> {
            try {
                PreparedStatement pstmt = conn.prepareStatement("INSERT INTO campus_map (building, location) VALUES (?, ?)");
                pstmt.setString(1, buildingField.getText());
                pstmt.setString(2, locationField.getText());
                pstmt.executeUpdate();
                updateCampusMapArea(campusMapArea);
                statusLabel.setText("Map entry added");
                buildingField.setText("");
                locationField.setText("");
            } catch (SQLException ex) {
                statusLabel.setText("Error: " + ex.getMessage());
            }
        });
        campusMapPanel.add(campusMapArea);
        campusMapPanel.add(new JLabel("Building:").setBounds(10, 200, 60, 20));
        campusMapPanel.add(buildingField);
        campusMapPanel.add(new JLabel("Location:").setBounds(220, 200, 60, 20));
        campusMapPanel.add(locationField);
        campusMapPanel.add(addMap);
        tabbedPane.addTab("Campus Map", campusMapPanel);

        // FAQ Tab
        JPanel faqPanel = new JPanel(null);
        JTextArea faqArea = new JTextArea();
        faqArea.setEditable(false);
        faqArea.setBounds(10, 10, 400, 200);
        updateFAQArea(faqArea);
        JTextField questionField = new JTextField();
        questionField.setBounds(10, 220, 200, 25);
        JTextField answerField = new JTextField();
        answerField.setBounds(220, 220, 200, 25);
        JButton addFAQ = new JButton("Add");
        addFAQ.setBounds(430, 220, 80, 25);
        addFAQ.addActionListener(e -> {
            try {
                PreparedStatement pstmt = conn.prepareStatement("INSERT INTO faq (question, answer) VALUES (?, ?)");
                pstmt.setString(1, questionField.getText());
                pstmt.setString(2, answerField.getText());
                pstmt.executeUpdate();
                updateFAQArea(faqArea);
                statusLabel.setText("FAQ added");
                questionField.setText("");
                answerField.setText("");
            } catch (SQLException ex) {
                statusLabel.setText("Error: " + ex.getMessage());
            }
        });
        faqPanel.add(faqArea);
        faqPanel.add(new JLabel("Question:").setBounds(10, 200, 60, 20));
        faqPanel.add(questionField);
        faqPanel.add(new JLabel("Answer:").setBounds(220, 200, 60, 20));
        faqPanel.add(answerField);
        faqPanel.add(addFAQ);
        tabbedPane.addTab("FAQ", faqPanel);

        // Transport Tracker Tab
        JPanel transportPanel = new JPanel(null);
        JTextArea transportArea = new JTextArea();
        transportArea.setEditable(false);
        transportArea.setBounds(10, 10, 400, 180);
        updateTransportArea(transportArea);
        JTextField routeField = new JTextField();
        routeField.setBounds(10, 200, 100, 25);
        JTextField scheduleField = new JTextField();
        scheduleField.setBounds(120, 200, 100, 25);
        JTextField locationField = new JTextField();
        locationField.setBounds(230, 200, 100, 25);
        JTextField transportIdField = new JTextField();
        transportIdField.setBounds(340, 200, 100, 25);
        JButton addTransport = new JButton("Add");
        addTransport.setBounds(450, 200, 80, 25);
        addTransport.addActionListener(e -> {
            try {
                PreparedStatement pstmt = conn.prepareStatement("INSERT INTO transport_tracker (route, schedule, live_location, student_id) VALUES (?, ?, ?, ?)");
                pstmt.setString(1, routeField.getText());
                pstmt.setString(2, scheduleField.getText());
                pstmt.setString(3, locationField.getText());
                pstmt.setString(4, transportIdField.getText());
                pstmt.executeUpdate();
                updateTransportArea(transportArea);
                statusLabel.setText("Transport entry added");
                routeField.setText("");
                scheduleField.setText("");
                locationField.setText("");
                transportIdField.setText("");
            } catch (SQLException ex) {
                statusLabel.setText("Error: " + ex.getMessage());
            }
        });
        transportPanel.add(transportArea);
        transportPanel.add(new JLabel("Route:").setBounds(10, 180, 50, 20));
        transportPanel.add(routeField);
        transportPanel.add(new JLabel("Schedule:").setBounds(120, 180, 60, 20));
        transportPanel.add(scheduleField);
        transportPanel.add(new JLabel("Location:").setBounds(230, 180, 60, 20));
        transportPanel.add(locationField);
        transportPanel.add(new JLabel("ID:").setBounds(340, 180, 30, 20));
        transportPanel.add(transportIdField);
        transportPanel.add(addTransport);
        tabbedPane.addTab("Transport Tracker", transportPanel);

        add(tabbedPane);

        // Status Bar
        statusLabel = new JLabel("Ready");
        statusLabel.setBounds(50, 460, 700, 20);
        add(statusLabel);
    }

    private void updateAnnouncementArea(JTextArea area) {
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT id, title, content FROM announcements");
            StringBuilder sb = new StringBuilder();
            while (rs.next()) {
                sb.append("[").append(rs.getInt("id") - 1).append("] Title: ").append(rs.getString("title"))
                  .append("\nContent: ").append(rs.getString("content")).append("\n\n");
            }
            area.setText(sb.toString());
        } catch (SQLException e) {
            area.setText("Error: " + e.getMessage());
        }
    }

    private void updateLostFoundArea(JTextArea area) {
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT item, status FROM lost_and_found");
            StringBuilder sb = new StringBuilder();
            while (rs.next()) {
                sb.append("Item: ").append(rs.getString("item")).append(", Status: ")
                  .append(rs.getString("status")).append("\n");
            }
            area.setText(sb.toString());
        } catch (SQLException e) {
            area.setText("Error: " + e.getMessage());
        }
    }

    private void updateResultArea(JTextArea area, String department, String semester) {
        try {
            PreparedStatement pstmt = conn.prepareStatement("SELECT id, student_id, course, grade FROM results WHERE department = ? AND semester = ?");
            pstmt.setString(1, department);
            pstmt.setString(2, semester);
            ResultSet rs = pstmt.executeQuery();
            StringBuilder sb = new StringBuilder();
            while (rs.next()) {
                sb.append("[").append(rs.getInt("id") - 1).append("] Student ID: ").append(rs.getString("student_id"))
                  .append(", Course: ").append(rs.getString("course")).append(", Grade: ")
                  .append(rs.getString("grade")).append("\n");
            }
            area.setText(sb.toString());
        } catch (SQLException e) {
            area.setText("Error: " + e.getMessage());
        }
    }

    private void updateTimetableArea(JTextArea area) {
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT id, course, day, time FROM timetable");
            StringBuilder sb = new StringBuilder();
            while (rs.next()) {
                sb.append("[").append(rs.getInt("id") - 1).append("] Course: ").append(rs.getString("course"))
                  .append(", Day: ").append(rs.getString("day")).append(", Time: ")
                  .append(rs.getString("time")).append("\n");
            }
            area.setText(sb.toString());
        } catch (SQLException e) {
            area.setText("Error: " + e.getMessage());
        }
    }

    private void updateCampusMapArea(JTextArea area) {
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT building, location FROM campus_map");
            StringBuilder sb = new StringBuilder();
            while (rs.next()) {
                sb.append("Building: ").append(rs.getString("building")).append(", Location: ")
                  .append(rs.getString("location")).append("\n");
            }
            area.setText(sb.toString());
        } catch (SQLException e) {
            area.setText("Error: " + e.getMessage());
        }
    }

    private void updateFAQArea(JTextArea area) {
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT question, answer FROM faq");
            StringBuilder sb = new StringBuilder();
            while (rs.next()) {
                sb.append("Q: ").append(rs.getString("question")).append("\nA: ")
                  .append(rs.getString("answer")).append("\n");
            }
            area.setText(sb.toString());
        } catch (SQLException e) {
            area.setText("Error: " + e.getMessage());
        }
    }

    private void updateTransportArea(JTextArea area) {
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT route, schedule, live_location, student_id FROM transport_tracker");
            StringBuilder sb = new StringBuilder();
            while (rs.next()) {
                sb.append("Route: ").append(rs.getString("route")).append(", Schedule: ")
                  .append(rs.getString("schedule")).append(", Live: ").append(rs.getString("live_location"))
                  .append(", Student ID: ").append(rs.getString("student_id")).append("\n");
            }
            area.setText(sb.toString());
        } catch (SQLException e) {
            area.setText("Error: " + e.getMessage());
        }
    }

    private void updateComboBox(JComboBox<String> combo, String table) {
        combo.removeAllItems();
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM " + table);
            if (rs.next()) {
                int count = rs.getInt(1);
                for (int i = 0; i < count; i++) {
                    combo.addItem(String.valueOf(i));
                }
            }
        } catch (SQLException e) {
            statusLabel.setText("Error: " + e.getMessage());
        }
    }

    private void updateComboBox(JComboBox<String> combo, String table, String department, String semester) {
        combo.removeAllItems();
        try {
            PreparedStatement pstmt = conn.prepareStatement("SELECT COUNT(*) FROM " + table + " WHERE department = ? AND semester = ?");
            pstmt.setString(1, department);
            pstmt.setString(2, semester);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                int count = rs.getInt(1);
                for (int i = 0; i < count; i++) {
                    combo.addItem(String.valueOf(i));
                }
            }
        } catch (SQLException e) {
            statusLabel.setText("Error: " + e.getMessage());
        }
    }
}