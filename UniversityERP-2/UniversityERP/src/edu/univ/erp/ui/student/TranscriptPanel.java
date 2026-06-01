package edu.univ.erp.ui.student;

import edu.univ.erp.domain.Student;
import edu.univ.erp.api.StudentAPI;
import edu.univ.erp.util.CSVExporter;
import edu.univ.erp.util.PDFExporter;
import edu.univ.erp.util.MessageHelper;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TranscriptPanel extends JPanel {
    private Student student;
    private StudentAPI studentAPI;
    
    public TranscriptPanel(Student currentStudent) {
        this.student = currentStudent;
        this.studentAPI = new StudentAPI();
        initializeUI();
    }
    
    private void initializeUI() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createTitledBorder("Academic Transcript"));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        JLabel titleLabel = new JLabel("Download Academic Transcript", SwingConstants.CENTER);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        mainPanel.add(titleLabel, gbc);
        
        JTextArea descArea = new JTextArea(
            "Generate and download your official academic transcript in PDF or CSV format. " +
            "The transcript will include all completed courses with grades, credits, and GPA information."
        );
        descArea.setWrapStyleWord(true);
        descArea.setLineWrap(true);
        descArea.setEditable(false);
        descArea.setBackground(getBackground());
        descArea.setFont(new Font("SansSerif", Font.PLAIN, 12));
        gbc.gridy = 1;
        mainPanel.add(descArea, gbc);
        
        JPanel formatPanel = new JPanel(new FlowLayout());
        JLabel formatLabel = new JLabel("Select Format:");
        JComboBox<String> formatCombo = new JComboBox<>(new String[]{"PDF", "CSV"});
        
        formatPanel.add(formatLabel);
        formatPanel.add(formatCombo);
        gbc.gridwidth = 1;
        gbc.gridy = 2; gbc.gridx = 0;
        mainPanel.add(formatPanel, gbc);
        
        JButton downloadBtn = new JButton("📥 Download Transcript");
        downloadBtn.setFont(new Font("SansSerif", Font.BOLD, 14));
        downloadBtn.setBackground(new Color(70, 130, 180));
        downloadBtn.setForeground(Color.WHITE);
        downloadBtn.addActionListener(e -> downloadTranscript(
            formatCombo.getSelectedItem().toString()
        ));
        
        gbc.gridx = 1;
        mainPanel.add(downloadBtn, gbc);
        
        JPanel infoPanel = createInfoPanel();
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        mainPanel.add(infoPanel, gbc);
        
        add(mainPanel, BorderLayout.CENTER);
    }
    
    private JPanel createInfoPanel() {
        JPanel infoPanel = new JPanel(new GridLayout(0, 1, 5, 5));
        infoPanel.setBorder(BorderFactory.createTitledBorder("Transcript Information"));
        
        infoPanel.add(createInfoLabel("• Includes all completed courses with grades"));
        infoPanel.add(createInfoLabel("• Official GPA calculation"));
        infoPanel.add(createInfoLabel("• Course credits and letter grades"));
        infoPanel.add(createInfoLabel("• PDF format recommended for official use"));
        infoPanel.add(createInfoLabel("• CSV format for data analysis"));
        
        return infoPanel;
    }
    
    private JLabel createInfoLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.PLAIN, 11));
        return label;
    }
    
    private void downloadTranscript(String format) {
        try {
            File downloadsDir = new File("downloads");
            if (!downloadsDir.exists()) {
                downloadsDir.mkdir();
            }
            
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String filename = String.format("transcript_%s_%s.%s", 
                                          student.getRollNo(), timestamp, 
                                          format.toLowerCase());
            
            String filePath = new File(downloadsDir, filename).getAbsolutePath();
            
            ProgressMonitor progressMonitor = new ProgressMonitor(
                this, 
                "Generating transcript...", 
                "", 0, 100
            );
            progressMonitor.setMillisToDecideToPopup(0);
            progressMonitor.setMillisToPopup(0);
            
            SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
                @Override
                protected Boolean doInBackground() throws Exception {
                    progressMonitor.setProgress(30);
                    
                    var result = studentAPI.exportTranscript(
                        student.getUserId(), 
                        filePath, 
                        format
                    );
                    
                    progressMonitor.setProgress(100);
                    return result.isSuccess();
                }
                
                @Override
                protected void done() {
                    progressMonitor.close();
                    try {
                        boolean success = get();
                        if (success) {
                            MessageHelper.showSuccess(
                                TranscriptPanel.this, 
                                "Transcript downloaded successfully!\nFile: " + filename
                            );
                            
                            int choice = JOptionPane.showConfirmDialog(
                                TranscriptPanel.this,
                                "Transcript downloaded successfully. Would you like to open it?",
                                "Download Complete",
                                JOptionPane.YES_NO_OPTION
                            );
                            
                            if (choice == JOptionPane.YES_OPTION) {
                                openFile(filePath);
                            }
                            
                        } else {
                            MessageHelper.showError(
                                TranscriptPanel.this, 
                                "Failed to download transcript"
                            );
                        }
                    } catch (Exception e) {
                        MessageHelper.showError(
                            TranscriptPanel.this, 
                            "Error downloading transcript: " + e.getMessage()
                        );
                    }
                }
            };
            
            worker.execute();
            
        } catch (Exception e) {
            MessageHelper.showError(this, "Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void openFile(String filePath) {
        try {
            File file = new File(filePath);
            if (file.exists()) {
                Desktop.getDesktop().open(file);
            }
        } catch (Exception ex) {
            MessageHelper.showWarning(this, "Cannot open file automatically. Please open manually: " + filePath);
        }
    }
}