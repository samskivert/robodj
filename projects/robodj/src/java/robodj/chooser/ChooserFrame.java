//
// $Id: ChooserFrame.java,v 1.1 2001/06/05 16:42:38 mdb Exp $

package robodj.chooser;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;

import com.samskivert.swing.*;

public class ChooserFrame
    extends JFrame
    implements ActionListener
{
    public ChooserFrame ()
    {
	super("RoboDJ Chooser");

        // we create a top-level panel to manage everything
	JPanel top = new JPanel();
	GroupLayout gl = new VGroupLayout(GroupLayout.STRETCH);
	gl.setOffAxisPolicy(GroupLayout.STRETCH);
	top.setLayout(gl);

	// give ourselves a wee bit of a border
	top.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // the top of the UI is the playlist manager section
        JTabbedPane tpane = new JTabbedPane();
        Component elist = new EntryList();
        tpane.addTab("Browse", null, elist, "Browse music database.");
        tpane.setSelectedIndex(0);
        top.add(tpane);

        // the bottom is the control bar
        GroupLayout bgl = new HGroupLayout(GroupLayout.NONE);
        bgl.setJustification(GroupLayout.RIGHT);
        JPanel cbar = new JPanel(bgl);

        // add some fake control buttons for now
        cbar.add(createControlButton("Play", "play"));
        cbar.add(createControlButton("Pause", "pause"));
        cbar.add(createControlButton("Stop", "stop"));
        cbar.add(createControlButton("Skip", "skip"));
        cbar.add(createControlButton("Clear", "clear"));
        cbar.add(createControlButton("Exit", "exit"));

	// stick it into the frame
	top.add(cbar, GroupLayout.FIXED);

	// now add our top-level panel (we'd not use this if we could set
	// a border on the content pane returned by the frame... alas)
	getContentPane().add(top, BorderLayout.CENTER);
    }

    protected JButton createControlButton (String label, String action)
    {
        JButton cbut = new JButton(label);
        cbut.setActionCommand(action);
        cbut.addActionListener(this);
        return cbut;
    }

    public void actionPerformed (ActionEvent e)
    {
	String cmd = e.getActionCommand();
	if (cmd.equals("exit")) {
	    System.exit(0);

        } else if (cmd.equals("pause")) {
            Chooser.scontrol.pause();

        } else if (cmd.equals("play")) {
            Chooser.scontrol.play();

        } else if (cmd.equals("clear")) {
            Chooser.scontrol.clear();

        } else if (cmd.equals("stop")) {
            Chooser.scontrol.stop();

        } else if (cmd.equals("skip")) {
            Chooser.scontrol.skip();

	} else {
	    System.out.println("Unknown action event: " + cmd);
	}
    }

    protected Component makeTextPanel (String text)
    {
        JPanel panel = new JPanel(false);
        JLabel filler = new JLabel(text);
        filler.setHorizontalAlignment(JLabel.CENTER);
        panel.setLayout(new GridLayout(1, 1));
        panel.add(filler);
        return panel;
    }
}