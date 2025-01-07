package com.szadowsz.rotom4j.component;

import com.szadowsz.gui.RotomGui;
import com.szadowsz.gui.component.group.RGroup;
import com.szadowsz.gui.component.group.drawable.tab.RTab;
import com.szadowsz.gui.component.group.drawable.tab.RTabFunction;
import com.szadowsz.gui.component.group.drawable.tab.RTabManager;
import com.szadowsz.gui.component.group.folder.RFolder;
import com.szadowsz.rotom4j.file.RotomFile;

public abstract class R4JFolder<R extends RotomFile> extends RFolder {

    protected static final String TABS = "tabs";

    protected static final String SELECT_NCLR_FILE = "Select NClR";

    protected R data;
    protected String selectName;

    protected /*final*/ RTabManager tabs;
    protected R4JComponent<R> display;

    /**
     * Default Constructor
     * <p>
     * We generally assume that width and height are determined elsewhere: the length of text, the size of an image, etc.
     *
     * @param gui    the gui for the window that the component is drawn under
     * @param path   the path in the component tree
     * @param parent the parent component reference
     */
    public R4JFolder(RotomGui gui, String path, RGroup parent, R data, String selectName) {
        super(gui, path, parent);
        this.data = data;
        this.selectName = selectName;
//        tabs = new RTabManager(gui,path + "/" + TABS, this);
//        children.add(tabs);
//        if (this.data != null) {
//            createTabs();
//        }
    }

    /**
     * Check to see if node should display regular name, or selection name
     *
     * @return true if regular, false otherwise
     */
    protected boolean shouldDisplayName() {
        return data != null;
    }

    protected abstract RTabFunction<R4JComponent<R>> createDisplay();

    protected RTabFunction<R4JEditor> createEditor(String name, boolean isCompressed) {
        return (RTab tab) -> new R4JEditor(gui, tab.getPath() + "/" + name, tab, data, isCompressed);
    }

    protected void createTabs() {
//        if (data.isCompressed()) {
//            tabs.addTab(createEditor("Compressed", true));
//        }
      //  tabs.addTab(createEditor( "Raw", false));
        tabs.addTab(createDisplay());
    }


    /**
     * Change the Nitro Obj currently displayed
     *
     * @return the current obj
     */
    public R getObj() {
        return data;
    }

    /**
     * Change the Nitro Obj currently displayed
     *
     * @param obj the obj to now use
     */
    public void setObj(R obj) {
        this.data = obj;
    }
}
