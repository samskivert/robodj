//
// $Id: PlaylistPanel.java,v 1.7 2001/09/20 20:42:48 mdb Exp $

package robodj.chooser;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import javax.swing.*;

import com.samskivert.jdbc.PersistenceException;
import com.samskivert.swing.*;
import com.samskivert.swing.util.*;
import com.samskivert.util.StringUtil;

import robodj.Log;
import robodj.repository.*;

public class PlaylistPanel
    extends JPanel
    implements TaskObserver, ActionListener
{
    public PlaylistPanel ()
    {
	GroupLayout gl = new VGroupLayout(GroupLayout.STRETCH);
        gl.setOffAxisPolicy(GroupLayout.STRETCH);
	setLayout(gl);

        // create the pane that will hold the buttons
        gl = new VGroupLayout(GroupLayout.NONE);
        gl.setJustification(GroupLayout.TOP);
        _bpanel = new SmartPanel();
        _bpanel.setLayout(gl);

	// give ourselves a wee bit of a border
	_bpanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // put it into a scrolling pane
	JScrollPane bscroll = new JScrollPane(_bpanel);
        add(bscroll);

        GroupLayout bgl = new HGroupLayout(GroupLayout.NONE);
        bgl.setJustification(GroupLayout.RIGHT);
        JPanel cbar = new JPanel(bgl);

        // add our control buttons
        cbar.add(_clearbut = createControlButton("Clear", "clear"));
        cbar.add(createControlButton("Skip", "skip"));
        cbar.add(createControlButton("Refresh", "refresh"));
        add(cbar, GroupLayout.FIXED);

        // use a special font for our name buttons
        _nameFont = new Font("Helvetica", Font.PLAIN, 10);

        // load up the playlist
        refreshPlaylist();
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
	if (cmd.equals("clear")) {
            Chooser.scontrol.clear();
            refreshPlaylist();

        } else if (cmd.equals("skip")) {
            Chooser.scontrol.skip();
            refreshPlaying();

        } else if (cmd.equals("refresh")) {
            refreshPlaylist();

        } else if (cmd.equals("skipto")) {
            JButton src = (JButton)e.getSource();
            PlaylistEntry entry =
                (PlaylistEntry)src.getClientProperty("entry");
            Chooser.scontrol.skipto(entry.song.songid);
            refreshPlaying();

        } else if (cmd.equals("remove")) {
            JButton src = (JButton)e.getSource();
            PlaylistEntry entry =
                (PlaylistEntry)src.getClientProperty("entry");
            Chooser.scontrol.remove(entry.song.songid);

            // remove the entry UI elements
            JPanel epanel = (JPanel)entry.label.getParent();
            epanel.getParent().remove(epanel);

            // update the playing indicator because we may have removed
            // the playing entry
            refreshPlaying();
        }
    }

    protected void refreshPlaylist ()
    {
        // stick a "loading" label in the list to let the user know
        // what's up
        _bpanel.removeAll();
        _bpanel.add(new JLabel("Loading..."));
        // we've removed and added components and swing won't properly
        // repaint automatically
        _bpanel.repaint();

        // start up the task that reads the CD info from the database
        TaskMaster.invokeMethodTask("readPlaylist", this, this);
    }

    protected void refreshPlaying ()
    {
        // unhighlight whoever is playing now
        PlaylistEntry pentry = getPlayingEntry();
        if (pentry != null) {
            pentry.label.setForeground(Color.black);
        }

        // figure out who's playing
        TaskMaster.invokeMethodTask("readPlaying", this, this);
    }

    public void readPlaylist ()
        throws PersistenceException
    {
        // clear out any previous playlist
        _plist.clear();

        // find out what's currently playing
        readPlaying();

        // get the playlist from the music daemon
        String[] plist = Chooser.scontrol.getPlaylist();

        // parse it into playlist entries
        for (int i = 0; i < plist.length; i++) {
            String[] toks = StringUtil.split(plist[i], "\t");
            int eid, sid;
            try {
                eid = Integer.parseInt(toks[0]);
                sid = Integer.parseInt(toks[1]);
            } catch (NumberFormatException nfe) {
                Log.warning("Bogus playlist record: '" + plist[i] + "'.");
                continue;
            }
            Entry entry = Chooser.model.getEntry(eid);
            if (entry != null) {
                Song song = entry.getSong(sid);
                _plist.add(new PlaylistEntry(entry, song));
            } else {
                Log.warning("Unable to load entry [eid=" + eid + "].");
            }
        }
    }

    public void readPlaying ()
    {
        String playing = Chooser.scontrol.getPlaying();
        playing = StringUtil.split(playing, ":")[1].trim();
        _playid = -1;
        if (!playing.equals("<none>")) {
            try {
                _playid = Integer.parseInt(playing);
            } catch (NumberFormatException nfe) {
                Log.warning("Unable to parse currently playing id '" +
                            playing + "'.");
            }
        }
    }

    public void taskCompleted (String name, Object result)
    {
	if (name.equals("readPlaylist")) {
            populatePlaylist();

        } else if (name.equals("readPlaying")) {
            highlightPlaying();
        }
    }

    public void taskFailed (String name, Throwable exception)
    {
        String msg;
        if (Exception.class.equals(exception.getClass())) {
            msg = exception.getMessage();
        } else {
            msg = exception.toString();
        }
        JOptionPane.showMessageDialog(this, msg, "Error",
                                      JOptionPane.ERROR_MESSAGE); 
        Log.logStackTrace(exception);
    }

    protected void populatePlaylist ()
    {
        // clear out any existing children
        _bpanel.removeAll();

        // adjust our layout policy
        GroupLayout gl = (GroupLayout)_bpanel.getLayout();
        gl.setOffAxisPolicy(GroupLayout.EQUALIZE);
        gl.setOffAxisJustification(GroupLayout.LEFT);

        // add buttons for every entry
        String title = null;
        for (int i = 0; i < _plist.size(); i++) {
            PlaylistEntry entry = (PlaylistEntry)_plist.get(i);

            // add record/artist indicators when the record and artist
            // changes
            if (!entry.entry.title.equals(title)) {
                title = entry.entry.title;
                JLabel label = new JLabel(entry.entry.title + " - " +
                                          entry.entry.artist);
                _bpanel.add(label);
            }

            // create a browse and a play button
            JPanel hpanel = new JPanel();
            gl = new HGroupLayout(GroupLayout.NONE);
            gl.setOffAxisPolicy(GroupLayout.STRETCH);
            gl.setJustification(GroupLayout.LEFT);
            hpanel.setLayout(gl);

            entry.label = new JLabel(entry.song.title);
            entry.label.setForeground((entry.song.songid == _playid) ?
                                      Color.red : Color.black);
            entry.label.setFont(_nameFont);
            hpanel.add(entry.label);

            JButton button;
            button = new JButton("Skip to");
            button.setFont(_nameFont);
            button.setActionCommand("skipto");
            button.addActionListener(this);
            button.putClientProperty("entry", entry);
            hpanel.add(button);

            button = new JButton("Remove");
            button.setActionCommand("remove");
            button.setFont(_nameFont);
            button.addActionListener(this);
            button.putClientProperty("entry", entry);
            hpanel.add(button);

            // let the bpanel know that we want to scroll the active track
            // label into place once we're all laid out
            if (entry.song.songid == _playid) {
                _bpanel.setScrollTarget(hpanel);
            }

            _bpanel.add(hpanel);
        }

        // if there were no entries, stick a label in to that effect
        if (_plist.size() == 0) {
            _bpanel.add(new JLabel("Nothing playing."));
        }

        // we've removed and added components and swing won't properly
        // repaint automatically
        _bpanel.repaint();
    }

    protected void highlightPlaying ()
    {
        for (int i = 0; i < _plist.size(); i++) {
            PlaylistEntry entry = (PlaylistEntry)_plist.get(i);
            if (entry.song.songid == _playid) {
                entry.label.setForeground(Color.red);
            }
        }
    }

    protected PlaylistEntry getPlayingEntry ()
    {
        for (int i = 0; i < _plist.size(); i++) {
            PlaylistEntry entry = (PlaylistEntry)_plist.get(i);
            if (entry.song.songid == _playid) {
                return entry;
            }
        }
        return null;
    }

    protected static class PlaylistEntry
    {
        public Entry entry;

        public Song song;

        public JLabel label;

        public PlaylistEntry (Entry entry, Song song)
        {
            this.entry = entry;
            this.song = song;
        }
    }

    /**
     * A panel that can be made to scroll a particular child into view
     * when it becomes visible.
     */
    protected static class SmartPanel extends JPanel
    {
        public void setScrollTarget (JComponent comp)
        {
            _target = comp;
        }

        public void doLayout ()
        {
            super.doLayout();

            if (_target != null) {
                Rectangle bounds = _target.getBounds();
                _target = null;
                // this seems to sometimes call layout(), so we need to
                // prevent recursion
                scrollRectToVisible(bounds);
            }
        }

        protected JComponent _target;
    }

    protected SmartPanel _bpanel;
    protected JButton _clearbut;

    protected ArrayList _plist = new ArrayList();
    protected int _playid;
    protected int _oldid = -1;

    protected Font _nameFont;
}
