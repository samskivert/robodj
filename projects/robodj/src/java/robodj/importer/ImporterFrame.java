//
// $Id: ImporterFrame.java,v 1.7 2002/03/03 06:32:12 mdb Exp $

package robodj.importer;

import java.awt.*;
import java.awt.event.ActionListener;
import javax.swing.*;

import com.samskivert.swing.*;

import robodj.Version;

public class ImporterFrame extends JFrame
{
    public ImporterFrame ()
    {
	super("RoboDJ CD Importer " + Version.RELEASE_VERSION);

        // quit if we're closed
        setDefaultCloseOperation(EXIT_ON_CLOSE);

	_top = new JPanel();
	GroupLayout gl = new VGroupLayout(GroupLayout.STRETCH);
	gl.setOffAxisPolicy(GroupLayout.STRETCH);
	_top.setLayout(gl);

	// give ourselves a wee bit of a border
	_top.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

	// create a container for our control buttons
        GroupLayout bgl = new HGroupLayout(GroupLayout.NONE);
        bgl.setJustification(GroupLayout.RIGHT);
	_buttonPanel = new JPanel(bgl);

	// stick it into the frame
	_top.add(_buttonPanel, GroupLayout.FIXED);

	// now add our top-level panel (we'd not use this if we could set
	// a border on the content pane returned by the frame... alas)
	getContentPane().add(_top, BorderLayout.CENTER);
    }

    public void setPanel (ImporterPanel panel)
    {
	// clear out any old panel
	if (_panel != null) {
	    _top.remove(_panel);
	}

	// clear out old control buttons
	clearControlButtons();

	// then stick the new panel in there
	if (panel != null) {
	    _panel = panel;
	    _top.add(_panel, 0);
	    // let the panel know that it was added
	    _panel.wasAddedToFrame(this);
	}

	// and lay out the new panel
        validate();
    }

    public JButton addControlButton (String label, String command,
				     ActionListener target)
    {
	JButton abutton = new JButton(label);
	abutton.setActionCommand(command);
	abutton.addActionListener(target);
	_buttonPanel.add(abutton);
        // swing doesn't automatically validate after adding/removing
        // children
        _buttonPanel.revalidate();
	return abutton;
    }

    public void clearControlButtons ()
    {
	_buttonPanel.removeAll();
        // swing doesn't automatically validate after adding/removing
        // children
        _buttonPanel.revalidate();
    }

    protected ImporterPanel _panel;
    protected JPanel _top;
    protected JPanel _buttonPanel;
}
