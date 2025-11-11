import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public class TagExtractorGUI extends JFrame {

    private final TagProcessor processor = new TagProcessor();

    private final JLabel txtFileLabel = new JLabel("No text file selected");
    private final JLabel stopFileLabel = new JLabel("No stop-words file selected");
    private final JTextArea outputArea = new JTextArea(20, 60);

    private Path textFilePath = null;
    private Path stopWordsPath = null;
    private List<Map.Entry<String,Integer>> lastSorted = null;

    public TagExtractorGUI() {
        setTitle("Tag / Keyword Extractor");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout(8,8));
        outputArea.setEditable(false);
        outputArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));

        JPanel topPanel = new JPanel(new GridLayout(3,1,4,4));
        JLabel title = new JLabel("Tag / Keyword Extractor", SwingConstants.CENTER);
        title.setFont(new Font("Serif", Font.BOLD, 24));
        topPanel.add(title);

        JPanel filePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        JButton chooseTxt = new JButton("Choose Text File...");
        JButton chooseStop = new JButton("Choose Stop Words...");
        filePanel.add(chooseTxt);
        filePanel.add(txtFileLabel);
        filePanel.add(chooseStop);
        filePanel.add(stopFileLabel);
        topPanel.add(filePanel);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        JButton processBtn = new JButton("Process File");
        JButton saveBtn = new JButton("Save Tags...");
        JButton clearBtn = new JButton("Clear");
        actionPanel.add(processBtn);
        actionPanel.add(saveBtn);
        actionPanel.add(clearBtn);
        topPanel.add(actionPanel);

        add(topPanel, BorderLayout.NORTH);
        add(new JScrollPane(outputArea), BorderLayout.CENTER);

        // Button actions
        chooseTxt.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            fc.setFileFilter(new FileNameExtensionFilter("Text Files", "txt", "text"));
            int res = fc.showOpenDialog(this);
            if (res == JFileChooser.APPROVE_OPTION) {
                textFilePath = fc.getSelectedFile().toPath();
                txtFileLabel.setText(textFilePath.getFileName().toString());
            }
        });

        chooseStop.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            fc.setFileFilter(new FileNameExtensionFilter("Text Files", "txt", "text"));
            int res = fc.showOpenDialog(this);
            if (res == JFileChooser.APPROVE_OPTION) {
                stopWordsPath = fc.getSelectedFile().toPath();
                stopFileLabel.setText(stopWordsPath.getFileName().toString());
                try {
                    processor.loadStopWords(stopWordsPath);
                    JOptionPane.showMessageDialog(this, "Loaded " + processor.hasStopWords() + " stop words.");
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(this, "Failed loading stop words: " + ex.getMessage());
                }
            }
        });

        processBtn.addActionListener(e -> {
            if (textFilePath == null) {
                JOptionPane.showMessageDialog(this, "Please choose a text file first.");
                return;
            }
            if (stopWordsPath == null) {
                int opt = JOptionPane.showConfirmDialog(this, "No stop-words loaded. Continue?", "Warning", JOptionPane.YES_NO_OPTION);
                if (opt != JOptionPane.YES_OPTION) return;
            }
            try {
                Map<String,Integer> map = processor.extractTags(textFilePath);
                List<Map.Entry<String,Integer>> sorted = processor.sortByFrequency(map);
                lastSorted = sorted;
                outputArea.setText("");
                outputArea.append("File: " + textFilePath.getFileName().toString() + "\n\n");
                outputArea.append(String.format("%6s  %s%n", "Count", "Tag"));
                outputArea.append("-----------------------------\n");
                int shown = 0;
                for (Map.Entry<String,Integer> entry : sorted) {
                    outputArea.append(String.format("%6d  %s%n", entry.getValue(), entry.getKey()));
                    shown++;
                }
                if (shown == 0) outputArea.append("\nNo tags found (check stop words file).");
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error processing file: " + ex.getMessage());
            }
        });

        saveBtn.addActionListener(e -> {
            if (lastSorted == null) {
                JOptionPane.showMessageDialog(this, "No results to save. Run Process File first.");
                return;
            }
            JFileChooser fc = new JFileChooser();
            fc.setSelectedFile(new java.io.File("tags_output.txt"));
            int res = fc.showSaveDialog(this);
            if (res == JFileChooser.APPROVE_OPTION) {
                Path out = fc.getSelectedFile().toPath();
                try {
                    processor.saveTags(out, lastSorted);
                    JOptionPane.showMessageDialog(this, "Saved tags to " + out.toString());
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(this, "Failed to save: " + ex.getMessage());
                }
            }
        });

        clearBtn.addActionListener(e -> {
            outputArea.setText("");
            txtFileLabel.setText("No text file selected");
            stopFileLabel.setText("No stop-words file selected");
            textFilePath = null;
            stopWordsPath = null;
            lastSorted = null;
        });

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(TagExtractorGUI::new);
    }
}
