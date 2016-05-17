package jdd.so;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UnsupportedLookAndFeelException;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import jdd.so.api.CherryPickResult;
import jdd.so.bot.actions.CommandException;
import jdd.so.rest.RESTApiHandler;

/**
 * Very Simple Swing app, to search for close votes
 * 
 * @author Petter Friberg
 *
 */
public class CVAppSwing extends JFrame implements NotifyMe {
	/**
	 * Logger for this class
	 */
	private static final Logger logger = Logger.getLogger(CVAppSwing.class);

	private static final long serialVersionUID = 5735990211677121020L;

	public static final int CHERRY_TYPE_CV = 0;
	public static final int CHERRY_TYPE_DUP = 1;

	public static final int OUTPUT_TYPE_HTML = 0;
	public static final int OUTPUT_TYPE_JSON = 1;
	public static final int OUTPUT_TYPE_REST_API = 2;

	private JTextField textTag;
	private JRadioButton radioCV;
	private JRadioButton radioDupes;
	private JRadioButton radioHMTL;
	private JRadioButton radioJSON;
	private JRadioButton radioRestApi;
	private JTextField textApiCalls;

	private JButton btnSearch;
	private JLabel labelStatus;

	public CVAppSwing() {
		super("CloseVoteFinder");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		jbInit();
	}

	private void jbInit() {
		JPanel formPanel = new JPanel(new GridBagLayout());
		formPanel.setBorder(BorderFactory.createEtchedBorder());
		textTag = new JTextField();
		textTag.setPreferredSize(new Dimension(200, 21));

		radioCV = new JRadioButton("CV");
		radioDupes = new JRadioButton("Dupes");

		ButtonGroup groupCherry = new ButtonGroup();
		groupCherry.add(radioCV);
		groupCherry.add(radioDupes);
		radioCV.setSelected(true);

		radioHMTL = new JRadioButton("HTML");
		radioJSON = new JRadioButton("JSON");
		radioRestApi = new JRadioButton("RESTApi");

		ButtonGroup groupOutput = new ButtonGroup();
		groupOutput.add(radioHMTL);
		groupOutput.add(radioJSON);
		groupOutput.add(radioRestApi);
		radioHMTL.setSelected(true);

		textApiCalls = new JTextField();
		textApiCalls.setPreferredSize(new Dimension(200, 21));
		textApiCalls.setText("10");

		btnSearch = new JButton("Search");
		btnSearch.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				btnSearch_actionPerformed(e);
			}
		});

		int runNr = 0;
		formPanel.add(new JLabel("Tag:"),
				new GridBagConstraints(0, runNr, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(2, 2, 2, 2), 0, 0));
		formPanel.add(textTag,
				new GridBagConstraints(1, runNr, 3, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 2), 0, 0));
		runNr++;
		formPanel.add(new JLabel("Cherry pick:"),
				new GridBagConstraints(0, runNr, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(2, 2, 2, 2), 0, 0));
		formPanel.add(radioCV,
				new GridBagConstraints(1, runNr, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(2, 2, 2, 2), 0, 0));
		formPanel.add(radioDupes,
				new GridBagConstraints(2, runNr, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(2, 2, 2, 2), 0, 0));
		runNr++;
		formPanel.add(new JLabel("Output:"),
				new GridBagConstraints(0, runNr, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(2, 2, 2, 2), 0, 0));
		formPanel.add(radioHMTL,
				new GridBagConstraints(1, runNr, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(2, 2, 2, 2), 0, 0));
		formPanel.add(radioJSON,
				new GridBagConstraints(2, runNr, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(2, 2, 2, 2), 0, 0));
		formPanel.add(radioRestApi,
				new GridBagConstraints(3, runNr, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(2, 2, 2, 2), 0, 0));
		runNr++;

		formPanel.add(new JLabel("API Calls:"),
				new GridBagConstraints(0, runNr, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(2, 2, 2, 2), 0, 0));
		formPanel.add(textApiCalls,
				new GridBagConstraints(1, runNr, 3, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 2), 0, 0));
		runNr++;

		formPanel.add(btnSearch,
				new GridBagConstraints(0, runNr, 4, 1, 0.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(2, 2, 2, 2), 0, 0));

		labelStatus = new JLabel("Ready to search");

		this.getContentPane().setLayout(new BorderLayout());
		this.getContentPane().add(formPanel, BorderLayout.CENTER);
		this.getContentPane().add(labelStatus, BorderLayout.SOUTH);
		this.pack();

	}

	protected void btnSearch_actionPerformed(ActionEvent e) {
		String tag = textTag.getText();
		if (tag == null || tag.trim().isEmpty()) {
			tag = null;
		}

		int type = CHERRY_TYPE_CV;
		if (radioDupes.isSelected()) {
			type = CHERRY_TYPE_DUP;
		}

		int output = OUTPUT_TYPE_HTML;
		if (radioJSON.isSelected()) {
			output = OUTPUT_TYPE_JSON;
		}
		if (radioRestApi.isSelected()) {
			output = OUTPUT_TYPE_REST_API;
		}

		int apiCalls = 10;

		try {
			apiCalls = Integer.parseInt(textApiCalls.getText());
		} catch (NumberFormatException e1) {
			textApiCalls.setText(String.valueOf(apiCalls));
			logger.error("btnSearch_actionPerformed(ActionEvent)", e1);
		}

		btnSearch.setEnabled(false);

		ExcuteRequest er = new ExcuteRequest(this, tag, type, output, apiCalls);
		er.start();
	}

	@Override
	public void message(final String text) {
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				labelStatus.setText(text);
			}
		});

	}

	@Override
	public void done(final File exportFile) {
		if (exportFile != null) {
			if (Desktop.isDesktopSupported()) {
				try {
					Desktop.getDesktop().open(exportFile);
				} catch (IOException ex) {
					logger.error("done(File)", ex);
				}
			}
		} else {
			JOptionPane.showMessageDialog(this, "Troubles when searching see log");
		}
		done();
	}

	@Override
	public void done(String remoteUrl) {

		if (remoteUrl != null) {
			Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
			if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
				try {
					desktop.browse(new URI(remoteUrl));
				} catch (Exception e) {
					logger.error("done(String)", e);
				}
			}
		}

		done();
	}

	@Override
	public void done() {
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				btnSearch.setEnabled(true);
			}
		});
	}

	/**
	 * This should actually be a swing worker, but who cares, lets do something
	 * quick and dirty
	 *
	 */
	private class ExcuteRequest extends Thread {
		/**
		 * Logger for this class
		 */
		private final Logger logger = Logger.getLogger(ExcuteRequest.class);

		private NotifyMe notifyMe;
		private String tag;
		private int cherryPickType;

		private int outputType;

		private int apiCalls;

		public ExcuteRequest(NotifyMe notifyMe, String tag, int cherryPickType, int outputType, int apiCalls) {
			this.notifyMe = notifyMe;
			this.tag = tag;
			this.cherryPickType = cherryPickType;
			this.outputType = outputType;
			this.apiCalls = apiCalls;

		}

		@Override
		public void run() {

			SwingAppCommands bot = new SwingAppCommands(apiCalls,false);
			bot.setNotifyMe(notifyMe);
			CherryPickResult result;
			try {
				if (cherryPickType == CHERRY_TYPE_DUP) {
					result = bot.getPossibileDuplicatesBatch(0L, 0L, tag, 40);
				} else {
					result = bot.getCherryPickBatch(0L, 0L, tag, 40, null, null, null, null);
				}
			} catch (CommandException e1) {
				logger.error("run()", e1);
				notifyMe.message("Error: " + e1.getMessage());
				notifyMe.done();
				return;
			}

			notifyMe.message("Finished calling api, quota=" + result.getApiResult().getQuotaRemaining());

			String ext;
			String output;
			switch (outputType) {
			case OUTPUT_TYPE_JSON:
				ext = "json";
				output = result.getJSONObject().toString(1);
				break;
			case OUTPUT_TYPE_REST_API:
				RESTApiHandler rest = new RESTApiHandler();
				try {
					String retVal = rest.getRemoteURL(result);
					System.out.println(retVal);
					done(retVal);
				} catch (IOException e1) {
					logger.error("run()", e1);
					notifyMe.message(e1.getMessage());
					notifyMe.done();
				}
				return;
			default:
				ext = "html";
				output = result.getHTML();
			}

			String dateName = new SimpleDateFormat("yyyyMMdd_HHmm").format(new Date());
			File f = new File("output/" + tag + "_" + dateName + "." + ext);
			if (!f.getParentFile().exists()) {
				f.mkdir();
			}
			// lets just overwrite
			PrintWriter out = null;
			try {
				out = new PrintWriter(f, "UTF-8");
				out.write(output);
			} catch (IOException e) {
				logger.error("run()", e);
			} finally {
				if (out != null) {
					out.close();
				}
			}

			notifyMe.done(f);

		}
	}

	public static void main(String[] args)
			throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException, FileNotFoundException, IOException {

		/**
		 * Logger for this class
		 */

		PropertyConfigurator.configure("ini/log4j.properties");

		Properties properties = new Properties();
		properties.load(new FileInputStream("ini/SOCVService.properties"));
		CloseVoteFinder.initInstance(properties);

		javax.swing.UIManager.setLookAndFeel(javax.swing.UIManager.getSystemLookAndFeelClassName());

		CVAppSwing app = new CVAppSwing();
		app.setLocationRelativeTo(null);
		app.setVisible(true);
	}

}
