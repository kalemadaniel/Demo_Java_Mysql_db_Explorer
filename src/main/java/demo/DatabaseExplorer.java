package demo;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

/** Explorateur JDBC : les acces reseau s'executent hors du thread graphique. */
public class DatabaseExplorer extends JFrame {
    private final JDesktopPane desktop = new JDesktopPane();
    private final JTree tree = new JTree(new DefaultMutableTreeNode("Non connecté"));
    private final JLabel status = new JLabel(" Prêt — connectez-vous à votre serveur XAMPP");
    private final JButton connect = new JButton("Connexion"), disconnect = new JButton("Déconnexion"), refresh = new JButton("Actualiser"), sql = new JButton("SQL");
    private final Map<String, JInternalFrame> windows = new HashMap<>();
    private Settings settings;
    private boolean busy;
    private int position;

    public DatabaseExplorer() {
        super("Demo_Java_Mysql_db_Explorer — Database Explorer");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(1100, 720); setMinimumSize(new Dimension(800, 520)); setLocationRelativeTo(null);
        JToolBar bar = new JToolBar(); bar.setFloatable(false);
        for (JButton b : new JButton[]{connect, disconnect, refresh, sql}) bar.add(b);
        JButton quit = new JButton("Quitter"); bar.addSeparator(); bar.add(quit);
        add(bar, BorderLayout.NORTH);
        desktop.setBackground(new Color(48, 77, 96));
        tree.setRowHeight(25);
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(tree), desktop);
        split.setDividerLocation(260); add(split);
        status.setBorder(BorderFactory.createEmptyBorder(7, 5, 7, 5)); add(status, BorderLayout.SOUTH);
        connect.addActionListener(e -> connectionDialog());
        disconnect.addActionListener(e -> { settings = null; clearWindows(); tree.setModel(new DefaultTreeModel(new DefaultMutableTreeNode("Non connecté"))); status.setText(" Déconnecté"); controls(); });
        refresh.addActionListener(e -> loadSchema(settings));
        sql.addActionListener(e -> sqlWindow()); quit.addActionListener(e -> dispose());
        tree.addMouseListener(new MouseAdapter() { public void mouseClicked(MouseEvent e) { if (e.getClickCount() == 2) openSelected(); }});
        tree.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "openTable");
        tree.getActionMap().put("openTable", new AbstractAction() { public void actionPerformed(ActionEvent e) { openSelected(); }});
        controls();
    }

    private void controls() {
        connect.setEnabled(!busy); disconnect.setEnabled(!busy && settings != null);
        refresh.setEnabled(!busy && settings != null); sql.setEnabled(!busy && settings != null); tree.setEnabled(!busy);
    }
    private <T> void task(String message, Callable<T> work, Consumer<T> success) {
        if (busy) { JOptionPane.showMessageDialog(this, "Une opération est déjà en cours."); return; }
        busy = true; controls(); status.setText(" " + message);
        new SwingWorker<T, Void>() {
            protected T doInBackground() throws Exception { return work.call(); }
            protected void done() {
                busy = false; controls();
                try { T result = get(); status.setText(" Prêt"); success.accept(result); }
                catch (Exception ex) {
                    Throwable cause = ex.getCause() == null ? ex : ex.getCause();
                    status.setText(" Échec de l'opération");
                    JOptionPane.showMessageDialog(DatabaseExplorer.this, cause.getMessage(), "Erreur de connexion ou SQL", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }
    private void connectionDialog() {
        JTextField host = new JTextField(settings == null ? "127.0.0.1" : settings.host);
        JTextField port = new JTextField(settings == null ? "3306" : String.valueOf(settings.port));
        JTextField database = new JTextField(settings == null ? "demo_java_mysql_db_explorer" : settings.database);
        JTextField user = new JTextField(settings == null ? "root" : settings.user);
        JPasswordField password = new JPasswordField();
        JPanel form = new JPanel(new GridLayout(0, 2, 10, 10));
        String[] labels = {"Serveur", "Port", "Base de données", "Utilisateur", "Mot de passe"};
        JComponent[] fields = {host, port, database, user, password};
        for (int i = 0; i < labels.length; i++) { form.add(new JLabel(labels[i])); form.add(fields[i]); }
        if (JOptionPane.showConfirmDialog(this, form, "Connexion MySQL / MariaDB (XAMPP)", JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) return;
        try {
            int p = Integer.parseInt(port.getText().trim());
            if (p < 1 || p > 65535 || !host.getText().trim().matches("[a-zA-Z0-9._-]+") || !database.getText().trim().matches("[a-zA-Z0-9_-]+") || user.getText().trim().isEmpty()) throw new IllegalArgumentException();
            loadSchema(new Settings(host.getText().trim(), p, database.getText().trim(), user.getText().trim(), new String(password.getPassword())));
        } catch (IllegalArgumentException ex) { JOptionPane.showMessageDialog(this, "Vérifiez le serveur, le port (1–65535), la base et l'utilisateur."); }
    }
    private void loadSchema(Settings candidate) {
        task("Connexion et lecture des tables…", () -> {
            List<String> names = new ArrayList<>();
            try (Connection c = candidate.open(); ResultSet rs = c.getMetaData().getTables(candidate.database, null, "%", new String[]{"TABLE", "VIEW"})) {
                while (rs.next()) names.add(rs.getString("TABLE_NAME"));
            }
            Collections.sort(names); return names;
        }, names -> {
            settings = candidate; clearWindows();
            DefaultMutableTreeNode root = new DefaultMutableTreeNode(candidate.database + " — database");
            DefaultMutableTreeNode tables = new DefaultMutableTreeNode("Tables (" + names.size() + ")"); root.add(tables);
            for (String name : names) tables.add(new DefaultMutableTreeNode(name));
            tree.setModel(new DefaultTreeModel(root)); tree.expandRow(0); tree.expandRow(1); controls();
            status.setText(" Connecté à " + candidate.host + ":" + candidate.port + " / " + candidate.database + " — double-cliquez sur une table");
        });
    }
    private void clearWindows() { for (JInternalFrame f : desktop.getAllFrames()) f.dispose(); windows.clear(); position = 0; }
    private void openSelected() {
        TreePath path = tree.getSelectionPath();
        if (busy || settings == null || path == null || path.getPathCount() != 3) return;
        String name = path.getLastPathComponent().toString();
        JInternalFrame old = windows.get(name);
        if (old != null && !old.isClosed()) { old.toFront(); try { old.setIcon(false); old.setSelected(true); } catch (Exception ignored) {} return; }
        Settings active = settings;
        String query = "SELECT * FROM " + quote(name) + " LIMIT 500";
        task("Lecture de " + name + "…", () -> query(active, query), result -> {
            JInternalFrame f = frame("Table " + name); windows.put(name, f);
            JTable table = table(result.model);
            JToolBar tools = new JToolBar(); tools.setFloatable(false);
            JButton reload = new JButton("Actualiser"), showSql = new JButton("Voir SQL"); tools.add(reload); tools.add(showSql);
            JLabel count = new JLabel(result.message); f.add(tools, BorderLayout.NORTH); f.add(new JScrollPane(table)); f.add(count, BorderLayout.SOUTH);
            reload.addActionListener(e -> task("Actualisation…", () -> query(active, query), r -> { table.setModel(r.model); count.setText(r.message); }));
            showSql.addActionListener(e -> JOptionPane.showMessageDialog(this, query, "Requête de la table", JOptionPane.INFORMATION_MESSAGE));
            f.setVisible(true);
        });
    }
    static String quote(String identifier) { return "`" + identifier.replace("`", "``") + "`"; }
    private JInternalFrame frame(String title) {
        JInternalFrame f = new JInternalFrame(title, true, true, true, true);
        f.setSize(700, 400); int offset = (position++ % 7) * 27; f.setLocation(offset, offset); desktop.add(f); return f;
    }
    private JTable table(DefaultTableModel model) {
        JTable table = new JTable(model); table.setAutoCreateRowSorter(true); table.setRowHeight(25);
        table.setFillsViewportHeight(true); table.setSelectionBackground(new Color(180, 213, 232)); return table;
    }
    private void sqlWindow() {
        final Settings active = settings;
        JInternalFrame f = frame("Éditeur SQL — " + active.database);
        JTextArea editor = new JTextArea("SELECT * FROM T_Articles LIMIT 500;", 5, 50);
        editor.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
        JPanel top = new JPanel(new BorderLayout()); top.add(new JScrollPane(editor));
        JButton run = new JButton("Exécuter (une instruction)"); top.add(run, BorderLayout.SOUTH);
        JTable results = table(new DefaultTableModel()); JLabel info = new JLabel(" Maximum 500 lignes affichées. Les modifications sont appliquées immédiatement.");
        f.add(new JSplitPane(JSplitPane.VERTICAL_SPLIT, top, new JScrollPane(results))); f.add(info, BorderLayout.SOUTH);
        run.addActionListener(e -> {
            if (busy) return;
            String statement = editor.getText().trim(); if (statement.isEmpty()) return;
            if (!statement.matches("(?is)^SELECT\\b.*") && JOptionPane.showConfirmDialog(this, "Cette instruction peut modifier la base. L'exécuter ?", "Exécution SQL", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
            task("Exécution SQL…", () -> query(active, statement), r -> { results.setModel(r.model); info.setText(r.message); });
        }); f.setVisible(true);
    }
    static Result query(Settings settings, String sql) throws SQLException {
        try (Connection c = settings.open(); Statement s = c.createStatement()) {
            s.setMaxRows(500); s.setQueryTimeout(20);
            if (!s.execute(sql)) return new Result(new DefaultTableModel(), " " + s.getUpdateCount() + " ligne(s) affectée(s). Actualisez l'arbre si la structure a changé.");
            try (ResultSet rs = s.getResultSet()) {
                ResultSetMetaData meta = rs.getMetaData(); Vector<String> columns = new Vector<>();
                for (int i = 1; i <= meta.getColumnCount(); i++) columns.add(meta.getColumnLabel(i));
                Vector<Vector<Object>> rows = new Vector<>();
                while (rs.next()) {
                    Vector<Object> row = new Vector<>();
                    for (int i = 1; i <= meta.getColumnCount(); i++) {
                        Object value = rs.getObject(i);
                        row.add(value instanceof byte[] ? "[binaire : " + ((byte[]) value).length + " octets]" : value);
                    } rows.add(row);
                }
                DefaultTableModel model = new DefaultTableModel(rows, columns) { public boolean isCellEditable(int r, int col) { return false; } };
                return new Result(model, " " + rows.size() + " ligne(s) affichée(s) — limite : 500. Grille en lecture seule.");
            }
        }
    }
    static class Result { final DefaultTableModel model; final String message; Result(DefaultTableModel m, String s) { model = m; message = s; } }
    static class Settings {
        final String host, database, user, password; final int port;
        Settings(String h, int p, String d, String u, String pass) { host = h; port = p; database = d; user = u; password = pass; }
        Connection open() throws SQLException {
            Properties props = new Properties(); props.setProperty("user", user); props.setProperty("password", password);
            props.setProperty("connectTimeout", "5000"); props.setProperty("socketTimeout", "30000"); props.setProperty("allowMultiQueries", "false");
            return DriverManager.getConnection("jdbc:mariadb://" + host + ":" + port + "/" + database, props);
        }
    }
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try { UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel"); } catch (Exception ignored) {}
            new DatabaseExplorer().setVisible(true);
        });
    }
}
