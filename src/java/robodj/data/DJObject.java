//
// $Id$

package robodj.data;

import com.samskivert.util.ArrayUtil;
import com.samskivert.util.ListUtil;

import com.threerings.presents.dobj.DObject;
import com.threerings.presents.dobj.DSet;

import robodj.Log;

/**
 * Contains the shared state of RoboDJ.
 */
public class DJObject extends DObject
{
    /** The field name of the <code>playlist</code> field. */
    public static final String PLAYLIST = "playlist";

    /** The field name of the <code>playing</code> field. */
    public static final String PLAYING = "playing";

    /** The field name of the <code>paused</code> field. */
    public static final String PAUSED = "paused";

    /** The current playlist. */
    public PlaylistEntry[] playlist;

    /** The index of the currently playing track. */
    public int playing = -1;

    /** Whether or not we're paused. */
    public boolean paused;

    /**
     * Returns the currently playing track.
     */
    public PlaylistEntry getPlaying ()
    {
        return (playing < 0) ? null : getEntry(playing);
    }

    /**
     * Returns the playlist entry for the next song in the playlist.
     */
    public PlaylistEntry getNext ()
    {
        return (playing < 0) ? null : getEntry(playing + 1);
    }

    /**
     * Returns the playlist entry with the current index or null if the
     * index is not valid.
     */
    public PlaylistEntry getEntry (int index)
    {
        return havePlaylist() ? playlist[index % playlist.length] : null;
    }

    /**
     * Plays the specified track in the current playlist.
     */
    public void play (int index)
    {
        if (havePlaylist()) {
            setPlaying(index % playlist.length);
        }
    }

    public void play ()
    {
        if (havePlaylist()) {
            if (playing == -1) {
                setPlaying(0);
            }
            if (paused) {
                setPaused(false);
            }
        }
    }

    public void skip ()
    {
        if (havePlaylist()) {
            play(playing + 1);
        }
    }

    public void back ()
    {
        if (havePlaylist()) {
            play(playing + playlist.length - 1);
        }
    }

    public void stop ()
    {
        setPlaying(-1);
    }

    public void pause ()
    {
        setPaused(!paused);
    }

    public void remove (int start, int length)
    {
        if (havePlaylist()) {
            setPlaylist((PlaylistEntry[])ArrayUtil.splice(
                            playlist, start, length));
            if (playing >= (start+length)) {
                setPlaying(playing-length);
            } else if (playing > start) {
                setPlaying(start);
            }
        }
    }

    public void shuffle ()
    {
        if (havePlaylist()) {
            PlaylistEntry entry = getPlaying();
            ArrayUtil.shuffle(playlist);
            setPlaylist(playlist);
            setPlaying(ListUtil.indexOf(playlist, entry));
        }
    }

    public void clear ()
    {
        setPlaying(-1);
        setPlaylist(null);
    }

    /** Helper function. */
    protected boolean havePlaylist ()
    {
        return (playlist != null && playlist.length > 0);
    }

    /**
     * Requests that the <code>playlist</code> field be set to the specified
     * value. The local value will be updated immediately and an event
     * will be propagated through the system to notify all listeners that
     * the attribute did change. Proxied copies of this object (on
     * clients) will apply the value change when they received the
     * attribute changed notification.
     */
    public void setPlaylist (PlaylistEntry[] playlist)
    {
        requestAttributeChange(PLAYLIST, playlist);
        this.playlist = playlist;
    }

    /**
     * Requests that the <code>index</code>th element of
     * <code>playlist</code> field be set to the specified value. The local
     * value will be updated immediately and an event will be propagated
     * through the system to notify all listeners that the attribute did
     * change. Proxied copies of this object (on clients) will apply the
     * value change when they received the attribute changed notification.
     */
    public void setPlaylistAt (PlaylistEntry value, int index)
    {
        requestElementUpdate(PLAYLIST, value, index);
        this.playlist[index] = value;
    }

    /**
     * Requests that the <code>playing</code> field be set to the specified
     * value. The local value will be updated immediately and an event
     * will be propagated through the system to notify all listeners that
     * the attribute did change. Proxied copies of this object (on
     * clients) will apply the value change when they received the
     * attribute changed notification.
     */
    public void setPlaying (int playing)
    {
        requestAttributeChange(PLAYING, new Integer(playing));
        this.playing = playing;
    }

    /**
     * Requests that the <code>paused</code> field be set to the specified
     * value. The local value will be updated immediately and an event
     * will be propagated through the system to notify all listeners that
     * the attribute did change. Proxied copies of this object (on
     * clients) will apply the value change when they received the
     * attribute changed notification.
     */
    public void setPaused (boolean paused)
    {
        requestAttributeChange(PAUSED, new Boolean(paused));
        this.paused = paused;
    }
}
