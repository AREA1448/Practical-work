import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Random;
import javax.sound.sampled.*;
import java.io.File;
import javax.swing.text.*;

class BullsAndCowsGUI extends JFrame {

    private JTextField guessField;
    private JTextPane outputArea;
    private String secret;
    private int attempts = 0;
    private JButton guessButton;
    private Clip backgroundMusic;
    private JLabel attemptsLabel;
    private JProgressBar progress;
    private static Color skyBlue = new Color(135, 206, 235);
    private static Color grassGreen = new Color(34, 139, 34);
    private static Color barnRed = new Color(139, 0, 0);

    public BullsAndCowsGUI() {
        System.setProperty("swing.aatext", "true");
        secret = generateSecret();
        setTitle("Bulls and Cows Game");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(550, 450);
        setMinimumSize(new Dimension(400, 300));
        setResizable(true);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                GradientPaint gp = new GradientPaint(0, 0, skyBlue, 0, getHeight(), skyBlue.darker());
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        mainPanel.setLayout(new BorderLayout());
        add(mainPanel);

        outputArea = new JTextPane();
        outputArea.setEditable(false);
        outputArea.setFont(new Font("Consolas", Font.PLAIN, 15));
        outputArea.setBackground(skyBlue.brighter());
        outputArea.setForeground(Color.DARK_GRAY);
        JScrollPane scrollPane = new JScrollPane(outputArea);

        JPanel inputPanel = new JPanel();
        inputPanel.setBackground(skyBlue);
        inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        guessField = new JTextField(8);
        guessField.setFont(new Font("Consolas", Font.BOLD, 18));
        guessField.setHorizontalAlignment(JTextField.CENTER);
        guessField.setBackground(Color.WHITE);

        guessButton = new JButton("Guess");
        guessButton.setFont(new Font("Arial", Font.BOLD, 14));
        guessButton.setBackground(grassGreen);
        guessButton.setForeground(Color.WHITE);
        addHoverEffect(guessButton, grassGreen);

        JButton restartButton = new JButton("Restart 🔄");
        restartButton.setBackground(grassGreen);
        restartButton.setForeground(Color.WHITE);
        addHoverEffect(restartButton, grassGreen);

        JButton quitButton = new JButton("Quit ❌");
        quitButton.setBackground(barnRed);
        quitButton.setForeground(Color.WHITE);
        addHoverEffect(quitButton, barnRed);

        inputPanel.add(new JLabel("Your guess:"));
        inputPanel.add(guessField);
        inputPanel.add(guessButton);
        inputPanel.add(restartButton);
        inputPanel.add(quitButton);

        JLabel header = new JLabel(" Guess the 4-digit number (unique digits)", SwingConstants.CENTER);
        header.setBackground(skyBlue.darker());
        header.setForeground(Color.WHITE);
        header.setOpaque(true);

        JPanel sidebar = new JPanel(new GridLayout(3, 1, 5, 5));
        sidebar.setBackground(skyBlue);
        sidebar.setBorder(BorderFactory.createTitledBorder("Game Stats"));
        attemptsLabel = new JLabel("Attempts: 0");
        attemptsLabel.setFont(new Font("Arial", Font.BOLD, 14));
        JLabel hintLabel = new JLabel("Hint: Unique digits!");
        hintLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        progress = new JProgressBar(0, 10);
        progress.setStringPainted(true);
        sidebar.add(attemptsLabel);
        sidebar.add(hintLabel);
        sidebar.add(progress);

        mainPanel.add(header, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(inputPanel, BorderLayout.SOUTH);
        mainPanel.add(sidebar, BorderLayout.EAST);

        guessButton.addActionListener(e -> processGuess());
        restartButton.addActionListener(e -> restartGame());
        quitButton.addActionListener(e -> {
            stopMusic();
            System.exit(0);
        });
        guessField.addActionListener(e -> processGuess());

        appendCentered("Game started! Secret number generated.\n");
        appendCentered("Enter 4 unique digits (no leading zero)\n\n");
        playBackgroundMusic("background_music.wav");
        showTutorial();
    }

    private void appendCentered(String text) {
        StyledDocument doc = outputArea.getStyledDocument();
        SimpleAttributeSet center = new SimpleAttributeSet();
        StyleConstants.setAlignment(center, StyleConstants.ALIGN_CENTER);
        try {
            int length = doc.getLength();
            doc.insertString(length, text, null);
            doc.setParagraphAttributes(length, text.length(), center, false);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    private void addHoverEffect(JButton button, Color baseColor) {
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(baseColor.darker());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(baseColor);
            }
        });
    }

    private void showTutorial() {
        String tutorialText = "Welcome to Bulls and Cows!\n\n" +
                "The goal is to guess a secret 4-digit number with unique digits (no leading zero).\n" +
                "- A 'Bull' means a digit is correct and in the right position.\n" +
                "- A 'Cow' means a digit is correct but in the wrong position.\n\n" +
                "Enter your guess and press 'Guess' or Enter. Good luck!";
        JOptionPane.showMessageDialog(this, tutorialText, "Tutorial", JOptionPane.INFORMATION_MESSAGE);
    }

    private String generateSecret() {
        Random r = new Random();
        StringBuilder s = new StringBuilder();
        boolean[] used = new boolean[10];
        s.append(r.nextInt(9) + 1);
        used[s.charAt(0) - '0'] = true;
        while (s.length() < 4) {
            int d = r.nextInt(10);
            if (!used[d]) {
                s.append(d);
                used[d] = true;
            }
        }
        return s.toString();
    }

    private void processGuess() {
        String guess = guessField.getText().trim();
        guessField.setText("");
        if (guess.length() != 4 || !guess.matches("\\d{4}") || guess.charAt(0) == '0') {
            appendCentered("❌ Invalid! 4 unique digits, no leading zero.\n");
            return;
        }
        attempts++;
        attemptsLabel.setText("Attempts: " + attempts);
        progress.setValue(attempts);
        int[] result = calculateBullsCows(secret, guess);
        appendCentered(attempts + ". " + guess + " → " + result[0] + " Bulls, " + result[1] + " Cows\n");

        if (result[0] == 4) {
            appendCentered("\n🎉 CONGRATULATIONS! You won in " + attempts + " attempts!\n");
            appendCentered("The number was: " + secret);
            guessButton.setEnabled(false);
            guessField.setEnabled(false);
            stopMusic();
        } else if (attempts >= 10) {
            appendCentered("\nGame over! Out of attempts. Secret was: " + secret);
            guessButton.setEnabled(false);
            guessField.setEnabled(false);
            stopMusic();
        }
    }

    private int[] calculateBullsCows(String secret, String guess) {
        int bulls = 0, cows = 0;
        for (int i = 0; i < 4; i++) {
            if (secret.charAt(i) == guess.charAt(i)) bulls++;
            else if (secret.indexOf(guess.charAt(i)) != -1) cows++;
        }
        return new int[]{bulls, cows};
    }

    private void playBackgroundMusic(String filePath) {
        try {
            AudioInputStream audioInput = AudioSystem.getAudioInputStream(new File(filePath));
            backgroundMusic = AudioSystem.getClip();
            backgroundMusic.open(audioInput);
            backgroundMusic.loop(Clip.LOOP_CONTINUOUSLY);
            backgroundMusic.start();
        } catch (Exception e) {
            System.out.println("Error playing music: " + e.getMessage());
        }
    }

    private void stopMusic() {
        if (backgroundMusic != null && backgroundMusic.isRunning()) {
            backgroundMusic.stop();
            backgroundMusic.close();
        }
    }

    private void restartGame() {
        secret = generateSecret();
        attempts = 0;
        outputArea.setText("");
        appendCentered("Game restarted! New secret generated.\n");
        appendCentered("Enter 4 unique digits (no leading zero)\n\n");
        guessButton.setEnabled(true);
        guessField.setEnabled(true);
        attemptsLabel.setText("Attempts: 0");
        progress.setValue(0);
        stopMusic();
        playBackgroundMusic("background_music.wav");
    }
}

public class MainMenu extends JFrame {
    private static int width = 550;
    private static int height = 450;
    private static boolean fullScreen = false;
    private static Color skyBlue = new Color(135, 206, 235);
    private static Color grassGreen = new Color(34, 139, 34);
    private static Color barnRed = new Color(139, 0, 0);
    private Clip menuMusic;

    public MainMenu() {
        System.setProperty("swing.aatext", "true");
        setTitle("Bulls and Cows - Main Menu");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 300);
        setMinimumSize(new Dimension(300, 200));
        setResizable(true);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                GradientPaint gp = new GradientPaint(0, 0, skyBlue, 0, getHeight(), skyBlue.darker());
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        mainPanel.setLayout(new BorderLayout());
        add(mainPanel);

        JLabel title = new JLabel("Bulls and Cows", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 24));
        title.setForeground(Color.WHITE);
        mainPanel.add(title, BorderLayout.NORTH);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(skyBlue);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 0, 10, 0);
        gbc.gridx = 0;
        gbc.gridy = GridBagConstraints.RELATIVE;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JButton startButton = new JButton("Start");
        startButton.setFont(new Font("Arial", Font.BOLD, 16));
        startButton.setBackground(grassGreen);
        startButton.setForeground(Color.WHITE);
        startButton.addActionListener(e -> startGame());
        addHoverEffect(startButton, grassGreen);

        JButton settingsButton = new JButton("Settings");
        settingsButton.setFont(new Font("Arial", Font.BOLD, 16));
        settingsButton.setBackground(grassGreen);
        settingsButton.setForeground(Color.WHITE);
        settingsButton.addActionListener(e -> showSettings());
        addHoverEffect(settingsButton, grassGreen);

        JButton exitButton = new JButton("Exit");
        exitButton.setFont(new Font("Arial", Font.BOLD, 16));
        exitButton.setBackground(barnRed);
        exitButton.setForeground(Color.WHITE);
        exitButton.addActionListener(e -> System.exit(0));
        addHoverEffect(exitButton, barnRed);

        panel.add(startButton, gbc);
        panel.add(settingsButton, gbc);
        panel.add(exitButton, gbc);
        mainPanel.add(panel, BorderLayout.CENTER);

        playBackgroundMusic("menu_music.wav");
    }

    private void addHoverEffect(JButton button, Color baseColor) {
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(baseColor.darker());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(baseColor);
            }
        });
    }

    private void startGame() {
        stopMusic();
        dispose();
        new BullsAndCowsGUI().setVisible(true);
    }

    private void showSettings() {
        // Settings dialog remains unchanged
        JDialog dialog = new JDialog(this, "Settings", true);
        dialog.setSize(300, 200);
        dialog.setLocationRelativeTo(this);
        dialog.getContentPane().setBackground(skyBlue);
        dialog.setLayout(new FlowLayout());

        String[] resolutions = {"Small (550x450)", "Medium (800x600)", "Large (1024x768)"};
        JComboBox<String> resolutionCombo = new JComboBox<>(resolutions);
        JCheckBox fullScreenCheck = new JCheckBox("Full Screen Mode", fullScreen);

        JButton okButton = new JButton("OK");
        okButton.addActionListener(e -> {
            String selected = (String) resolutionCombo.getSelectedItem();
            if ("Small (550x450)".equals(selected)) { width = 550; height = 450; }
            else if ("Medium (800x600)".equals(selected)) { width = 800; height = 600; }
            else { width = 1024; height = 768; }
            fullScreen = fullScreenCheck.isSelected();
            dialog.dispose();
        });

        dialog.add(new JLabel("Screen Resolution:"));
        dialog.add(resolutionCombo);
        dialog.add(fullScreenCheck);
        dialog.add(okButton);
        dialog.setVisible(true);
    }

    private void playBackgroundMusic(String filePath) {
        try {
            AudioInputStream audioInput = AudioSystem.getAudioInputStream(new File(filePath));
            menuMusic = AudioSystem.getClip();
            menuMusic.open(audioInput);
            menuMusic.loop(Clip.LOOP_CONTINUOUSLY);
            menuMusic.start();
        } catch (Exception e) {
            System.out.println("Error playing music: " + e.getMessage());
        }
    }

    private void stopMusic() {
        if (menuMusic != null && menuMusic.isRunning()) {
            menuMusic.stop();
            menuMusic.close();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainMenu().setVisible(true));
    }
}