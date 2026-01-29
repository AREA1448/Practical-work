import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Random;
import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

public class BullsAndCowsGUI extends JFrame {

    private JTextField guessField;
    private JTextArea outputArea;
    private String secret;
    private int attempts = 0;
    private JButton guessButton;
    private Clip backgroundMusic; // For audio

    public BullsAndCowsGUI() {
        secret = generateSecret();

        setTitle("Bulls and Cows Game");
        setSize(550, 450);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Center window
        setResizable(false);
        setLayout(new BorderLayout());

        getContentPane().setBackground(Color.GRAY);

        // Gray theme
        Color gray = new Color(128, 128, 128); // Medium gray color

        // Output area
        outputArea = new RoundedTextArea();
        outputArea.setEditable(false);
        outputArea.setFont(new Font("Consolas", Font.PLAIN, 15));
        outputArea.setBackground(gray.brighter()); // Lighter gray for text area
        outputArea.setForeground(Color.BLACK);
        JScrollPane scrollPane = new JScrollPane(outputArea);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.getViewport().setBackground(new Color(0, 0, 0, 0));

        // Input panel
        JPanel inputPanel = new RoundedPanel();
        inputPanel.setBackground(gray);

        guessField = new JTextField(8);
        guessField.setFont(new Font("Consolas", Font.BOLD, 18));
        guessField.setHorizontalAlignment(JTextField.CENTER);
        guessField.setBackground(Color.WHITE);

        guessButton = new JButton("Guess");
        guessButton.setFont(new Font("Arial", Font.BOLD, 14));
        guessButton.setBackground(new Color(100, 100, 100)); // Darker gray for button
        guessButton.setForeground(Color.WHITE);

        JButton quitButton = new JButton("Quit");
        quitButton.setBackground(Color.RED);
        quitButton.setForeground(Color.WHITE);

        inputPanel.add(new JLabel("Your guess:"));
        inputPanel.add(guessField);
        inputPanel.add(guessButton);
        inputPanel.add(quitButton);

        JLabel header = new RoundedLabel(" Guess the 4-digit number (unique digits)", SwingConstants.CENTER);
        header.setBackground(gray.darker());
        header.setForeground(Color.WHITE);

        add(header, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(inputPanel, BorderLayout.SOUTH);

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

    private static class RoundedPanel extends JPanel {
        private int radius = 15;

        public RoundedPanel() {
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
            super.paintComponent(g2);
            g2.dispose();
        }
    }

    private static class RoundedLabel extends JLabel {
        private int radius = 15;

        public RoundedLabel(String text, int alignment) {
            super(text, alignment);
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
            super.paintComponent(g2);
            g2.dispose();
        }
    }

    private static class RoundedTextArea extends JTextArea {
        private int radius = 15;

        public RoundedTextArea() {
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
            super.paintComponent(g2);
            g2.dispose();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new BullsAndCowsGUI().setVisible(true);
        });
    }
}