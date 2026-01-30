import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Random;
import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

class BullsAndCowsGUI extends JFrame {

    private JTextField guessField;
    private JTextArea outputArea;
    private String secret;
    private int attempts = 0;
    private JButton guessButton;
    private Clip backgroundMusic; // For audio
    private JPanel emotePanel;

    public BullsAndCowsGUI() {
        secret = generateSecret();
        setTitle("Bulls and Cows Game");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        // Bright blue theme
        Color brightBlue = new Color(98, 172, 90); // Bright blue color
        // Output area
        outputArea = new JTextArea();
        outputArea.setEditable(false);
        outputArea.setFont(new Font("Consolas", Font.PLAIN, 15));
        outputArea.setBackground(brightBlue.brighter()); // Lighter blue for text area
        outputArea.setForeground(Color.BLACK);
        JScrollPane scrollPane = new JScrollPane(outputArea);
        scrollPane.getViewport().setBackground(brightBlue);
        // Input panel
        JPanel inputPanel = new JPanel();
        inputPanel.setBackground(brightBlue);
        guessField = new JTextField(8);
        guessField.setFont(new Font("Consolas", Font.BOLD, 18));
        guessField.setHorizontalAlignment(JTextField.CENTER);
        guessField.setBackground(Color.WHITE);
        guessButton = new JButton("Guess");
        guessButton.setFont(new Font("Arial", Font.BOLD, 14));
        guessButton.setBackground(new Color(255, 255, 255)); // Darker blue for button
        guessButton.setForeground(Color.WHITE);
        JButton quitButton = new JButton("Quit");
        quitButton.setBackground(Color.RED);
        quitButton.setForeground(Color.WHITE);
        inputPanel.add(new JLabel("Your guess:"));
        inputPanel.add(guessField);
        inputPanel.add(guessButton);
        inputPanel.add(quitButton);
        JLabel header = new JLabel(" Guess the 4-digit number (unique digits)", SwingConstants.CENTER);
        header.setBackground(brightBlue.darker());
        header.setForeground(Color.WHITE);
        header.setOpaque(true);
        add(header, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(inputPanel, BorderLayout.SOUTH);
        // Emote panel
        emotePanel = new JPanel();
        emotePanel.setLayout(null);
        emotePanel.setOpaque(false);
        getLayeredPane().add(emotePanel, Integer.valueOf(300)); // On top
        // Listeners
        guessButton.addActionListener(e -> processGuess());
        quitButton.addActionListener(e -> {
            stopMusic();
            System.exit(0);
        });
        guessField.addActionListener(e -> processGuess()); // Enter key also works
        outputArea.append("Game started! Secret number generated.\n");
        outputArea.append("Enter 4 unique digits (no leading zero)\n\n");
        // Start background music (local file - download from YouTube manually)
        playBackgroundMusic("background_music.wav"); // Replace with your audio file path
        // Show tutorial
        showTutorial();
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
            outputArea.append("❌ Invalid! 4 unique digits, no leading zero.\n");
            return;
        }
        attempts++;
        int[] result = calculateBullsCows(secret, guess);
        outputArea.append(attempts + ". " + guess + " → " + result[0] + " Bulls, " + result[1] + " Cows\n");
        showEmotes(result[0], result[1]);
        if (result[0] == 4) {
            outputArea.append("\n🎉 CONGRATULATIONS! You won in " + attempts + " attempts!\n");
            outputArea.append("The number was: " + secret);
            guessButton.setEnabled(false);
            guessField.setEnabled(false);
            stopMusic(); // Optional: Stop music on win
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

    private void showEmotes(int bulls, int cows) {
        Random rand = new Random();
        for (int i = 0; i < bulls; i++) {
            JLabel bullEmote = new JLabel("🐂"); // Bull emoji
            bullEmote.setFont(new Font("Arial", Font.PLAIN, 30));
            int x = rand.nextInt(getWidth() - 50);
            int y = getHeight() - 50;
            bullEmote.setBounds(x, y, 50, 50);
            emotePanel.add(bullEmote);
            animateEmote(bullEmote, x, y, true); // true for dynamic
        }
        for (int i = 0; i < cows; i++) {
            JLabel cowEmote = new JLabel("🐄"); // Cow emoji
            cowEmote.setFont(new Font("Arial", Font.PLAIN, 30));
            int x = rand.nextInt(getWidth() - 50);
            int y = getHeight() - 50;
            cowEmote.setBounds(x, y, 50, 50);
            emotePanel.add(cowEmote);
            animateEmote(cowEmote, x, y, true); // true for dynamic
        }
        // For grass, add if no bulls or cows (miss)
        if (bulls == 0 && cows == 0) {
            for (int i = 0; i < 3; i++) { // Add 3 grass emotes
                JLabel grassEmote = new JLabel("🌿"); // Grass emoji
                grassEmote.setFont(new Font("Arial", Font.PLAIN, 30));
                int x = rand.nextInt(getWidth() - 50);
                int y = getHeight() - 50;
                grassEmote.setBounds(x, y, 50, 50);
                emotePanel.add(grassEmote);
                animateEmote(grassEmote, x, y, true); // true for dynamic
            }
        }
    }

    private void animateEmote(JLabel emote, int startX, int startY, boolean dynamic) {
        Random rand = new Random();
        final int speed = rand.nextInt(3) + 2; // Random speed 2-4
        final int drift = rand.nextBoolean() ? 1 : -1; // Left or right drift
        Timer timer = new Timer(20, new ActionListener() {
            int x = startX;
            int y = startY;
            int frame = 0;
            @Override
            public void actionPerformed(ActionEvent e) {
                y -= speed;
                if (dynamic) {
                    x += drift * (frame % 10 == 0 ? rand.nextInt(3) - 1 : 0); // Occasional drift
                    frame++;
                }
                emote.setLocation(x, y);
                if (y < -50) {
                    ((Timer) e.getSource()).stop();
                    emotePanel.remove(emote);
                    emotePanel.repaint();
                }
            }
        });
        timer.start();
    }

    // Play background music (loops)
    private void playBackgroundMusic(String filePath) {
        try {
            AudioInputStream audioInput = AudioSystem.getAudioInputStream(new File(filePath));
            backgroundMusic = AudioSystem.getClip();
            backgroundMusic.open(audioInput);
            backgroundMusic.loop(Clip.LOOP_CONTINUOUSLY); // Loop forever
            backgroundMusic.start();
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            System.out.println("Error playing music: " + e.getMessage());
            // Fallback: Optionally open YouTube in browser
            try {
                Desktop.getDesktop().browse(new java.net.URI("https://youtu.be/dtpfN_Wrd3U?si=tezVwUqy4ev2B-h7")); // Replace with YouTube URL
            } catch (Exception ex) {
                System.out.println("Failed to open browser: " + ex.getMessage());
            }
        }
    }

    // Stop music
    private void stopMusic() {
        if (backgroundMusic != null && backgroundMusic.isRunning()) {
            backgroundMusic.stop();
            backgroundMusic.close();
        }
    }
}

public class MainMenu extends JFrame {
    private static int width = 550;
    private static int height = 450;
    private static boolean fullScreen = false;
    private JPanel emotePanel;
    private Timer emoteTimer;

    public MainMenu() {
        setTitle("Bulls and Cows Menu");
        setSize(300, 200);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        Color brightBlue = new Color(98, 172, 90);
        getContentPane().setBackground(brightBlue);

        JPanel panel = new JPanel(new GridLayout(3, 1, 10, 10));
        panel.setBackground(brightBlue);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JButton startButton = new JButton("Start Game");
        startButton.setBackground(new Color(0, 150, 255));
        startButton.setForeground(Color.WHITE);
        startButton.addActionListener(e -> startGame());

        JButton settingsButton = new JButton("Settings");
        settingsButton.setBackground(new Color(0, 150, 255));
        settingsButton.setForeground(Color.WHITE);
        settingsButton.addActionListener(e -> showSettings());

        JButton exitButton = new JButton("Exit");
        exitButton.setBackground(Color.RED);
        exitButton.setForeground(Color.WHITE);
        exitButton.addActionListener(e -> System.exit(0));

        panel.add(startButton);
        panel.add(settingsButton);
        panel.add(exitButton);

        add(panel);

        // Emote panel for menu
        emotePanel = new JPanel();
        emotePanel.setLayout(null);
        emotePanel.setOpaque(false);
        getLayeredPane().add(emotePanel, Integer.valueOf(300)); // On top

        // Start emote animation timer
        emoteTimer = new Timer(1500, e -> addRandomEmote());
        emoteTimer.start();
    }

    private void addRandomEmote() {
        Random rand = new Random();
        String[] emoteStrings = {"🐂", "🐄", "🌿"}; // Bull, Cow, Grass
        String emoteStr = emoteStrings[rand.nextInt(emoteStrings.length)];
        JLabel emote = new JLabel(emoteStr);
        emote.setFont(new Font("Arial", Font.PLAIN, 30));
        int startY = rand.nextInt(getHeight() - 50);
        int startX = -50; // Start from left
        emote.setBounds(startX, startY, 50, 50);
        emotePanel.add(emote);
        animateEmote(emote, startX, startY, true); // true for dynamic
    }

    private void animateEmote(JLabel emote, int startX, int startY, boolean dynamic) {
        Random rand = new Random();
        final int speed = rand.nextInt(3) + 2; // Random speed 2-4
        final int wiggle = rand.nextBoolean() ? 1 : -1; // Up or down wiggle
        Timer timer = new Timer(20, new ActionListener() {
            int x = startX;
            int y = startY;
            int frame = 0;
            @Override
            public void actionPerformed(ActionEvent e) {
                x += speed;
                if (dynamic) {
                    y += wiggle * (frame % 10 == 0 ? rand.nextInt(3) - 1 : 0); // Occasional wiggle
                    frame++;
                }
                emote.setLocation(x, y);
                if (x > getWidth()) {
                    ((Timer) e.getSource()).stop();
                    emotePanel.remove(emote);
                    emotePanel.repaint();
                }
            }
        });
        timer.start();
    }

    private void startGame() {
        emoteTimer.stop(); // Stop menu emotes
        dispose();
        BullsAndCowsGUI game = new BullsAndCowsGUI();
        game.setResizable(false);
        if (fullScreen) {
            GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
            if (gd.isFullScreenSupported()) {
                game.setUndecorated(true);
                game.setVisible(true);
                gd.setFullScreenWindow(game);
            } else {
                game.setSize(width, height);
                game.setLocationRelativeTo(null);
                game.setVisible(true);
            }
        } else {
            game.setSize(width, height);
            game.setLocationRelativeTo(null);
            game.setVisible(true);
        }
    }

    private void showSettings() {
        JDialog dialog = new JDialog(this, "Settings", true);
        dialog.setSize(300, 200);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new FlowLayout());

        String[] resolutions = {"Small (550x450)", "Medium (800x600)", "Large (1024x768)"};
        JComboBox<String> resolutionCombo = new JComboBox<>(resolutions);
        if (width == 550 && height == 450) {
            resolutionCombo.setSelectedIndex(0);
        } else if (width == 800 && height == 600) {
            resolutionCombo.setSelectedIndex(1);
        } else {
            resolutionCombo.setSelectedIndex(2);
        }

        JCheckBox fullScreenCheck = new JCheckBox("Full Screen Mode", fullScreen);

        JButton okButton = new JButton("OK");
        okButton.addActionListener(e -> {
            String selected = (String) resolutionCombo.getSelectedItem();
            if ("Small (550x450)".equals(selected)) {
                width = 550;
                height = 450;
            } else if ("Medium (800x600)".equals(selected)) {
                width = 800;
                height = 600;
            } else {
                width = 1024;
                height = 768;
            }
            fullScreen = fullScreenCheck.isSelected();
            dialog.dispose();
        });

        dialog.add(new JLabel("Screen Resolution:"));
        dialog.add(resolutionCombo);
        dialog.add(fullScreenCheck);
        dialog.add(okButton);

        dialog.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new MainMenu().setVisible(true);
        });
    }
}