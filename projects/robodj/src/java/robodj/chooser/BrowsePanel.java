//
// $Id: BrowsePanel.java,v 1.5 2004/01/26 16:10:55 mdb Exp $

package robodj.chooser;

import java.awt.BorderLayout;
import java.util.ArrayList;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.samskivert.util.CollectionUtil;

import robodj.repository.*;

public class BrowsePanel extends JPanel
{
    public BrowsePanel ()
    {
        setLayout(new BorderLayout(5, 5));

        // obtain our categories
        ArrayList cats = new ArrayList();
        CollectionUtil.addAll(cats, Chooser.model.getCategories());
        // add an "Uncategorized" category
        cats.add(new Category(-1, "Uncategorized"));

        final JList clist = new JList(cats.toArray());
        clist.getSelectionModel().setSelectionMode(
            ListSelectionModel.SINGLE_SELECTION);
        ListSelectionListener lsl = new ListSelectionListener() {
            public void valueChanged (ListSelectionEvent lse) {
                Category cat = (Category)clist.getSelectedValue();
                if (cat != null) {
                    _category.setCategory(cat.categoryid);
                }
            }
        };
        clist.getSelectionModel().addListSelectionListener(lsl);
        add(new JScrollPane(clist), BorderLayout.WEST);

        add(_category = new CategoryEntryList(-1), BorderLayout.CENTER);
        clist.setSelectedIndex(cats.size()-1);
    }

    protected CategoryEntryList _category;
}
