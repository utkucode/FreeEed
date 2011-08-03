/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * HistoryUI.java
 *
 * Created on Jul 22, 2011, 11:58:10 AM
 */
package org.freeeed.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JOptionPane;
import org.freeeed.main.Version;
import org.freeeed.util.History;
import javax.swing.Timer;

/**
 *
 * @author mark
 */
public class HistoryUI extends javax.swing.JFrame implements ActionListener {

	private Timer timer = null;
	private static int refreshInterval = 5000;

	/** Creates new form HistoryUI */
	public HistoryUI() {
		initComponents();
		timer = new Timer(refreshInterval, this);
	}

	/** This method is called from within the constructor to
	 * initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is
	 * always regenerated by the Form Editor.
	 */
	@SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        closeButton = new javax.swing.JButton();
        historyScrollPane = new javax.swing.JScrollPane();
        historyTextArea = new javax.swing.JTextArea();
        eraseHistoryButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Processing history");

        closeButton.setText("Close");
        closeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeButtonActionPerformed(evt);
            }
        });

        historyTextArea.setColumns(20);
        historyTextArea.setRows(5);
        historyScrollPane.setViewportView(historyTextArea);

        eraseHistoryButton.setText("Erase history");
        eraseHistoryButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                eraseHistoryButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(eraseHistoryButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 657, Short.MAX_VALUE)
                .addComponent(closeButton)
                .addContainerGap())
            .addComponent(historyScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 811, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(historyScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 370, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(closeButton)
                    .addComponent(eraseHistoryButton))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

	private void eraseHistoryButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_eraseHistoryButtonActionPerformed
		eraseHistory();
	}//GEN-LAST:event_eraseHistoryButtonActionPerformed

	private void closeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeButtonActionPerformed
		closeHistory();
	}//GEN-LAST:event_closeButtonActionPerformed

	/**
	 * @param args the command line arguments
	 */
	public static void main(String args[]) {
		java.awt.EventQueue.invokeLater(new Runnable() {

			@Override
			public void run() {
				new HistoryUI().setVisible(true);
			}
		});
	}
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton closeButton;
    private javax.swing.JButton eraseHistoryButton;
    private javax.swing.JScrollPane historyScrollPane;
    private javax.swing.JTextArea historyTextArea;
    // End of variables declaration//GEN-END:variables

	@Override
	public void setVisible(boolean b) {
		if (b) {
			try {
				String history = History.getInstance().getHistory();
				historyTextArea.setText(history);
				timer.start();
			} catch (Exception e) {
				e.printStackTrace(System.out);
				JOptionPane.showMessageDialog(this, historyErrorMessage());
			}
		}
		super.setVisible(b);
	}

	private void eraseHistory() {
		try {
			History.getInstance().eraseHistory();
			String history = History.getInstance().getHistory();
			historyTextArea.setText(history);
		} catch (Exception e) {
			e.printStackTrace(System.out);
			JOptionPane.showMessageDialog(this, historyErrorMessage());
		}
	}

	private String historyErrorMessage() {
		return "Something is wrong with history.\n"
				+ "Please send the content of the terminal window to support at "
				+ Version.getSupportEmail();
	}

	private void closeHistory() {
		timer.stop();
		setVisible(false);
		dispose();
	}

	private void refreshHistory() {
		try {
			historyTextArea.setText(History.getInstance().getHistory());
		} catch (Exception e) {
			e.printStackTrace(System.out);
		}
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		refreshHistory();
	}

	@Override
	public void dispose() {
		timer.stop();
		super.dispose();
	}
}
